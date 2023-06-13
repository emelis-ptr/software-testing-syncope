package org.apache.syncope.core.spring.security;

import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@RunWith(Parameterized.class)
public class AuthDataAccessorGetAuthoritiesTest extends AuthDataAccessorUtil {

    private final String username;
    private final String delegationKey;
    private final SecurityProperties securityProperties;
    private final Object expected;
    private User user;

    public AuthDataAccessorGetAuthoritiesTest(String username, String delegationKey, SecurityProperties securityProperties, Object expected) {
        this.username = username;
        this.delegationKey = delegationKey;
        this.securityProperties = securityProperties;
        this.expected = expected;
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
                {null, null, mockSecurityProperties("abd", "efg"), NullPointerException.class},
                {"abd", null, mockSecurityProperties("abd", "efg"), true},
                {null, "efg", mockSecurityProperties("abd", "efg"), NullPointerException.class},
                {"abd", "efg", mockSecurityProperties("abd", "efg"), true},
                {"abd", "efg", mockSecurityProperties("abd", "abd"), true},
                {"abd", "efg", mockSecurityProperties("def", "efeg"), NullPointerException.class}
        });
    }

    @Test
    public void getAuthorities() {
        Object actual;
        try {
            AuthDataAccessor authDataAccessor = new AuthDataAccessor(this.securityProperties, mockRealDAO(), mockUserDAO(user), null, null, null, confParam("username"), null, null, null, null, null, null);
            Set<SyncopeGrantedAuthority> result = authDataAccessor.getAuthorities(this.username, this.delegationKey);

            Assert.assertNotNull(result);
            actual = true;
        } catch (NullPointerException e) {
            actual = e.getClass();
        }

        Assert.assertEquals(expected, actual);
    }
}