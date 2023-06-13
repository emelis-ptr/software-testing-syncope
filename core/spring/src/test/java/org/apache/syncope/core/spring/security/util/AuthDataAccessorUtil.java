package org.apache.syncope.core.spring.security.util;

import org.apache.syncope.common.keymaster.client.api.ConfParamOps;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.SecurityProperties;
import org.apache.syncope.core.spring.security.SyncopeAuthenticationDetails;
import org.apache.syncope.core.spring.security.entity.MyUser;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

public class AuthDataAccessorUtil {
    @Mock
    private RealmDAO realmDAO;
    @Mock
    private UserDAO userDAO;

    /**
     * Mock Authentication
     *
     * @param domain:
     * @param username:
     * @param password:
     * @return Authentication
     */
    protected Authentication authentication(String domain, String username, String password) {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn(username);
        Mockito.when(auth.getCredentials()).thenReturn(password);
        Mockito.when(auth.getDetails()).thenReturn(new SyncopeAuthenticationDetails(domain, null));
        return auth;
    }

    /**
     * Mock ConfParamOps
     *
     * @param username:
     * @return ConfParamOps
     */
    protected ConfParamOps confParam(String username) {
        ConfParamOps confParamOps = Mockito.mock(ConfParamOps.class);
        Mockito.when(confParamOps.get(anyString(), eq("authentication.attributes"), any(), any())).thenReturn(new String[]{username});
        Mockito.when(confParamOps.get(any(), eq("authentication.statuses"), any(), any())).thenReturn(new String[]{"ACTIVE", "SUSPENDED"});
        Mockito.when(confParamOps.get(any(), eq("log.lastlogindate"), any(), any())).thenReturn(true);
        return confParamOps;
    }

    /**
     * Mock RealmDAO
     *
     * @return RealmDAO
     */
    protected RealmDAO mockRealDAO() {
        realmDAO = Mockito.mock(RealmDAO.class);
        Realm mockRealm = Mockito.mock(Realm.class);
        Mockito.when(realmDAO.findAncestors(any())).thenReturn(Collections.singletonList(mockRealm));
        return realmDAO;
    }

    /**
     * Mock UserDAO
     *
     * @param user:
     * @return UserDAO
     */
    protected UserDAO mockUserDAO(User user) {
        userDAO = Mockito.mock(UserDAO.class);
        Mockito.when(userDAO.findByUsername(any())).thenReturn(user);
        return userDAO;
    }

    /**
     * User
     *
     * @param username:
     * @param password:
     * @return User
     */
    protected User getUser(String username, String password) {
        User user = new MyUser();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    /**
     * Mock SecurityProperties
     *
     * @param anonymousUser:
     * @param adminUser:
     * @return SecurityProperties
     */
    protected static SecurityProperties mockSecurityProperties(String anonymousUser, String adminUser) {
        SecurityProperties securityProperties = mock(SecurityProperties.class);
        Mockito.when(securityProperties.getAnonymousUser()).thenReturn(anonymousUser);
        Mockito.when(securityProperties.getAdminUser()).thenReturn(adminUser);
        return securityProperties;
    }

}
