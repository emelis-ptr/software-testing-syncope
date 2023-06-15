package org.apache.syncope.core.spring.security;

import org.apache.syncope.core.persistence.api.dao.DelegationDAO;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@RunWith(Parameterized.class)
public class AuthDataAccessorGetAuthoritiesTest extends AuthDataAccessorMock {

    private final String username;
    private final String delegationKey;
    private final SecurityProperties securityProperties;
    private final boolean isDelegationFound;
    private final boolean isFoundUser;
    private final boolean isEmptyRole;
    private final Object isExceptionExpected;
    private User user;

    public AuthDataAccessorGetAuthoritiesTest(String username, String delegationKey, SecurityProperties securityProperties, boolean isDelegationFound, boolean isFoundUser, boolean isEmptyRole, Object isExceptionExpected) {
        this.username = username;
        this.delegationKey = delegationKey;
        this.securityProperties = securityProperties;
        this.isDelegationFound = isDelegationFound;
        this.isFoundUser = isFoundUser;
        this.isEmptyRole = isEmptyRole;
        this.isExceptionExpected = isExceptionExpected;
        config();
    }

    private void config() {
        String username = "myUsername";
        String password = "myPassword";

        user = getUser(username, password);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {null, null, mockSecurityProperties("abd", "efg"), true, true, false, NullPointerException.class},
                {"abd", null, mockSecurityProperties("abd", "efg"), true, true, false, false},
                {null, "efg", mockSecurityProperties("abd", "efg"), true, true, false, false},
                // anonymousUser = username && adminUser == username
                {"abd", "efg", mockSecurityProperties("abd", "abd"), false, true, false, UsernameNotFoundException.class},
                {"abd", "efg", mockSecurityProperties("abd", "abd"), true, true, false, false},
                // anonymousUser != username && adminUser == username
                {"abd", "efg", mockSecurityProperties("def", "abd"), true, true, false, false},
                // anonymousUser != username && adminUser != username && delegationKey != null
                {"abd", "efg", mockSecurityProperties("def", "abdd"), true, true, false, false},
                {"abd", "efg", mockSecurityProperties("def", "abdd"), false, true, false, UsernameNotFoundException.class},
                {"abd", "efg", mockSecurityProperties("def", "abdd"), true, true, true, NullPointerException.class},
                {"abd", "efg", mockSecurityProperties("def", "abdd"), true, true, false, false},
                // anonymousUser != username && adminUser != username && delegationKey == null
                {"abd", null, mockSecurityProperties("def", "abdd"), true, false, true, UsernameNotFoundException.class},
                {"abd", null, mockSecurityProperties("def", "abdd"), true, true, false, NullPointerException.class},

        });
    }

    @Test
    public void getAuthorities() {
        Object error = false;
        try {
            DelegationDAO delegationDAO = mockDelegationDAO(user, this.delegationKey, this.isDelegationFound, this.isEmptyRole);
            AuthDataAccessor authDataAccessor = new AuthDataAccessor(this.securityProperties, mockRealDAO(), mockUserDAO(user, this.isFoundUser), null, null, null, confParam("username"), null, delegationDAO, null, null, null, null);
            Set<SyncopeGrantedAuthority> result = authDataAccessor.getAuthorities(this.username, this.delegationKey);

            Assert.assertNotNull(result);

        } catch (NullPointerException | UsernameNotFoundException e) {
            error = e.getClass();
        }

        Assert.assertEquals(error, isExceptionExpected);
    }
}