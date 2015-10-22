/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.provisioning.java;

import org.apache.syncope.core.provisioning.api.VirAttrHandler;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.core.misc.MappingUtils;
import org.apache.syncope.core.persistence.api.dao.AnyObjectDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.VirSchema;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.api.entity.group.Group;
import org.apache.syncope.core.persistence.api.entity.resource.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.resource.MappingItem;
import org.apache.syncope.core.persistence.api.entity.resource.Provision;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.provisioning.api.Connector;
import org.apache.syncope.core.provisioning.api.ConnectorFactory;
import org.apache.syncope.core.provisioning.api.cache.VirAttrCache;
import org.apache.syncope.core.provisioning.api.cache.VirAttrCacheValue;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Uid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VirAttrHandlerImpl implements VirAttrHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VirAttrHandler.class);

    @Autowired
    private AnyObjectDAO anyObjectDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ConnectorFactory connFactory;

    /**
     * Virtual attribute cache.
     */
    @Autowired
    private VirAttrCache virAttrCache;

    @Autowired
    private MappingUtils mappingUtils;

    private Map<VirSchema, List<String>> getValues(final Any<?, ?> any, final Set<VirSchema> schemas) {
        Collection<? extends ExternalResource> ownedResources;
        if (any instanceof User) {
            ownedResources = userDAO.findAllResources((User) any);
        } else if (any instanceof AnyObject) {
            ownedResources = anyObjectDAO.findAllResources((AnyObject) any);
        } else {
            ownedResources = ((Group) any).getResources();
        }

        Map<VirSchema, List<String>> result = new HashMap<>();

        Map<Provision, Set<VirSchema>> toRead = new HashMap<>();

        for (VirSchema schema : schemas) {
            if (ownedResources.contains(schema.getProvision().getResource())) {
                VirAttrCacheValue virAttrCacheValue =
                        virAttrCache.get(any.getType().getKey(), any.getKey(), schema.getKey());

                if (virAttrCache.isValidEntry(virAttrCacheValue)) {
                    LOG.debug("Values for {} found in cache: {}", schema, virAttrCacheValue);
                    result.put(schema, virAttrCacheValue.getValues());
                } else {
                    Set<VirSchema> schemasToRead = toRead.get(schema.getProvision());
                    if (schemasToRead == null) {
                        schemasToRead = new HashSet<>();
                        toRead.put(schema.getProvision(), schemasToRead);
                    }
                    schemasToRead.add(schema);
                }
            } else {
                LOG.debug("Not considering {} since {} is not assigned to {}",
                        schema, any, schema.getProvision().getResource());
            }
        }

        for (Map.Entry<Provision, Set<VirSchema>> entry : toRead.entrySet()) {
            LOG.debug("About to read from {}: {}", entry.getKey(), entry.getValue());

            String connObjectKey = MappingUtils.getConnObjectKeyItem(entry.getKey()) == null
                    ? null
                    : mappingUtils.getConnObjectKeyValue(any, entry.getKey());
            if (StringUtils.isBlank(connObjectKey)) {
                LOG.error("No ConnObjectKey found for {}, ignoring...", entry.getKey());
            } else {
                Set<MappingItem> linkingMappingItems = new HashSet<>();
                for (VirSchema schema : entry.getValue()) {
                    linkingMappingItems.add(schema.asLinkingMappingItem());
                }

                Connector connector = connFactory.getConnector(entry.getKey().getResource());
                try {
                    ConnectorObject connectorObject = connector.getObject(
                            entry.getKey().getObjectClass(),
                            new Uid(connObjectKey),
                            connector.getOperationOptions(linkingMappingItems.iterator()));

                    if (connectorObject == null) {
                        LOG.debug("No read from {} about {}", entry.getKey(), connObjectKey);
                    } else {
                        for (VirSchema schema : entry.getValue()) {
                            Attribute attr = connectorObject.getAttributeByName(schema.getExtAttrName());
                            if (attr != null) {
                                VirAttrCacheValue virAttrCacheValue = new VirAttrCacheValue();
                                virAttrCacheValue.setValues(attr.getValue());
                                virAttrCache.put(any.getType().getKey(), any.getKey(), schema.getKey(),
                                        virAttrCacheValue);
                                LOG.debug("Values for {} set in cache: {}", schema, virAttrCacheValue);

                                result.put(schema, virAttrCacheValue.getValues());
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error reading from {}", entry.getKey(), e);
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> getValues(final Any<?, ?> any, final VirSchema schema) {
        if (!any.getAllowedVirSchemas().contains(schema)) {
            LOG.debug("{} not allowed for {}", schema, any);
            return Collections.emptyList();
        }

        return ListUtils.emptyIfNull(getValues(any, Collections.singleton(schema)).get(schema));
    }

    @Transactional(readOnly = true)
    @Override
    public Map<VirSchema, List<String>> getValues(final Any<?, ?> any) {
        return getValues(any, any.getAllowedVirSchemas());
    }
}
