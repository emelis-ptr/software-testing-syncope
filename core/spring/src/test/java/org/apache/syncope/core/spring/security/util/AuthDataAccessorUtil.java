package org.apache.syncope.core.spring.security.util;

import org.apache.syncope.common.keymaster.client.api.ConfParamOps;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.SyncopeAuthenticationDetails;
import org.apache.syncope.core.spring.security.entity.MyUser;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;

public class AuthDataAccessorUtil {
    @Mock
    private RealmDAO realmDAO;
    @Mock
    private UserDAO userDAO;

    protected Authentication authentication(String domain, String username, String password) {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn(username);
        Mockito.when(auth.getCredentials()).thenReturn(password);
        Mockito.when(auth.getDetails()).thenReturn(new SyncopeAuthenticationDetails(domain, null));
        return auth;
    }

    protected ConfParamOps confParam(String username) {
        ConfParamOps confParamOps = Mockito.mock(ConfParamOps.class);
        Mockito.when(confParamOps.get(anyString(), eq("authentication.attributes"), any(), any())).thenReturn(new String[]{username});
        Mockito.when(confParamOps.get(any(), eq("authentication.statuses"), any(), any())).thenReturn(new String[]{"ACTIVE", "SUSPENDED"});
        Mockito.when(confParamOps.get(any(), eq("log.lastlogindate"), any(), any())).thenReturn(true);
        return confParamOps;
    }

    protected RealmDAO mockRealDAO() {
        realmDAO = Mockito.mock(RealmDAO.class);
        Realm mockRealm = Mockito.mock(Realm.class);
        Mockito.when(realmDAO.findAncestors(any())).thenReturn(Collections.singletonList(mockRealm));
        return realmDAO;
    }

    protected UserDAO mockUserDAO(String username, String password, User user) {
        userDAO = Mockito.mock(UserDAO.class);
        Mockito.when(userDAO.findByUsername(any())).thenReturn(user);
        return userDAO;
    }

    protected User getUser(String username, String password) {
        User user = new MyUser();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    public enum AuthenticationType {

        NULL,
        ACTIVE,
        NO_ACTIVE_USER,
        NO_ACTIVE_AUTHENTICATION
    }

    public enum ConfParamType {
        USERNAME,
        NOT_USERNAME
    }
}
