package org.apache.syncope.core.spring.security.util;

import org.apache.syncope.common.keymaster.client.api.ConfParamOps;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.apache.syncope.core.persistence.api.dao.AnySearchDAO;
import org.apache.syncope.core.persistence.api.dao.DelegationDAO;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.dao.search.SearchCond;
import org.apache.syncope.core.persistence.api.entity.Any;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.persistence.api.entity.Role;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.Encryptor;
import org.apache.syncope.core.spring.security.SecurityProperties;
import org.apache.syncope.core.spring.security.SyncopeAuthenticationDetails;
import org.apache.syncope.core.spring.security.entity.MyDelegation;
import org.apache.syncope.core.spring.security.entity.MyUser;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

public class AuthDataAccessorMock {
    @Mock
    private RealmDAO realmDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private AnySearchDAO anySearchDAO;
    @Mock
    private DelegationDAO delegationDAO;

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
        Mockito.when(confParamOps.get(any(), eq("authentication.statuses"), any(), any())).thenReturn(new String[]{"ACTIVE", "SUSPENDED", null});
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
     * Mock UserDAO
     *
     * @param user:
     * @return UserDAO
     */
    protected UserDAO mockUserDAO(User user, boolean isFoundUser) {
        userDAO = Mockito.mock(UserDAO.class);
        if (isFoundUser) {
            Mockito.when(userDAO.findByUsername(any())).thenReturn(user);
        } else {
            Mockito.when(userDAO.findByUsername(any())).thenThrow(new UsernameNotFoundException("Could not find any user with username " + user.getUsername()));
        }
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
        try {
            user.setPassword(Encryptor.getInstance().encode(password, CipherAlgorithm.AES));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Mock AnySearchDAO
     *
     * @param user:
     * @param numUsers:
     * @return AnySearchDAO
     */
    protected AnySearchDAO mockAnySearchDAO(User user, int numUsers) {
        List<Any<?>> userList = new ArrayList<>();

        if (numUsers == 1) {
            userList.add(user);
        } else {
            userList.add(user);
            for (int i = 1; i < numUsers; i++) {
                User newUser = new MyUser();
                newUser.setUsername(user.getUsername() + i);
                newUser.setPassword(user.getPassword() + i);
                userList.add(newUser);
            }
        }

        anySearchDAO = mock(AnySearchDAO.class);
        Mockito.when(anySearchDAO.search(any(SearchCond.class), any(AnyTypeKind.class))).thenReturn(userList);
        return anySearchDAO;
    }

    /**
     * Mock DelegationDAO
     *
     * @param user:
     * @param delegationKey:
     * @param findDelegation:
     * @return DelegationDAO
     */
    public DelegationDAO mockDelegationDAO(User user, String delegationKey, boolean findDelegation, boolean isEmptyRole) {
        MyDelegation myDelegation = new MyDelegation(user);
        delegationDAO = mock(DelegationDAO.class);
        if (findDelegation) {
            Mockito.when(delegationDAO.find(delegationKey)).thenReturn(myDelegation);
        } else {
            Mockito.when(delegationDAO.find(delegationKey)).thenThrow(new UsernameNotFoundException("Could not find delegation " + delegationKey));
        }

        MyDelegation delegationMock = Mockito.spy(myDelegation);
        Set<Role> roleSet = new HashSet<>();
        Role role = mock(Role.class);
        if (!isEmptyRole) {
            roleSet.add(role);
        }
        Mockito.doReturn(roleSet).when(delegationMock).getRoles();
        Mockito.when(delegationDAO.find(delegationKey)).thenReturn(delegationMock);
        return delegationDAO;
    }

}
