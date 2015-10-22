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
package org.apache.syncope.core.provisioning.java.sync;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.patch.AnyPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.PropagationStatus;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.AnyUtils;
import org.apache.syncope.core.provisioning.api.ProvisioningManager;
import org.apache.syncope.core.provisioning.api.WorkflowResult;
import org.apache.syncope.core.provisioning.api.sync.ProvisioningResult;
import org.apache.syncope.core.provisioning.api.sync.UserSyncResultHandler;
import org.identityconnectors.framework.common.objects.SyncDelta;

public class UserSyncResultHandlerImpl extends AbstractSyncResultHandler implements UserSyncResultHandler {

    @Override
    protected AnyUtils getAnyUtils() {
        return anyUtilsFactory.getInstance(AnyTypeKind.USER);
    }

    @Override
    protected String getName(final AnyTO anyTO) {
        return UserTO.class.cast(anyTO).getUsername();
    }

    @Override
    protected ProvisioningManager<?, ?> getProvisioningManager() {
        return userProvisioningManager;
    }

    @Override
    protected Any<?, ?> getAny(final long key) {
        try {
            return userDAO.authFind(key);
        } catch (Exception e) {
            LOG.warn("Error retrieving user {}", key, e);
            return null;
        }
    }

    @Override
    protected AnyTO getAnyTO(final long key) {
        return userDataBinder.getUserTO(key);
    }

    @Override
    protected AnyPatch newPatch(final long key) {
        UserPatch patch = new UserPatch();
        patch.setKey(key);
        return patch;
    }

    @Override
    protected WorkflowResult<Long> update(final AnyPatch patch) {
        WorkflowResult<Pair<UserPatch, Boolean>> update = uwfAdapter.update((UserPatch) patch);
        return new WorkflowResult<>(
                update.getResult().getLeft().getKey(), update.getPropByRes(), update.getPerformedTasks());
    }

    @Override
    protected AnyTO doCreate(final AnyTO anyTO, final SyncDelta delta, final ProvisioningResult result) {
        UserTO userTO = UserTO.class.cast(anyTO);

        Boolean enabled = syncUtilities.readEnabled(delta.getObject(), profile.getTask());
        Map.Entry<Long, List<PropagationStatus>> created =
                userProvisioningManager.create(userTO, true, true, enabled,
                        Collections.singleton(profile.getTask().getResource().getKey()));

        result.setKey(created.getKey());
        result.setName(getName(anyTO));

        return getAnyTO(created.getKey());
    }

    @Override
    protected AnyTO doUpdate(
            final AnyTO before,
            final AnyPatch anyPatch,
            final SyncDelta delta,
            final ProvisioningResult result) {

        UserPatch userPatch = UserPatch.class.cast(anyPatch);
        Boolean enabled = syncUtilities.readEnabled(delta.getObject(), profile.getTask());

        Map.Entry<Long, List<PropagationStatus>> updated = userProvisioningManager.update(
                userPatch, before.getKey(),
                result,
                enabled,
                Collections.singleton(profile.getTask().getResource().getKey()));

        return getAnyTO(updated.getKey());
    }

    @Override
    protected void doDelete(final AnyTypeKind kind, final Long key) {
        try {
            userProvisioningManager.
                    delete(key, Collections.<String>singleton(profile.getTask().getResource().getKey()));
        } catch (Exception e) {
            // A propagation failure doesn't imply a synchronization failure.
            // The propagation exception status will be reported into the propagation task execution.
            LOG.error("Could not propagate user " + key, e);
        }

        uwfAdapter.delete(key);
    }
}
