package org.apache.syncope.core.spring.security;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorEnum.AuthenticationType;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AuthDataAccessorAutenticateTest extends AuthDataAccessorMock {
    private final String domain; // {null, empty, notEmpty}
    private final Object isExceptionExpected;
    private AuthDataAccessor authDataAccessor;
    private Authentication authentication;
    private User myUser;
    private Integer failedLogins;
    private Object isAuthenticated;
    private static final String DOMAIN = "Master";

    public AuthDataAccessorAutenticateTest(String domain, AuthenticationType authenticationType, String confParamUsername, int numUsers, Object isExceptionExpected) {
        this.domain = domain;
        this.isExceptionExpected = isExceptionExpected;
        config(domain, authenticationType, confParamUsername, numUsers);
    }

    private void config(String domain, AuthenticationType authenticationType, String confParamUsername, int numUsers) {
        String username = "myUsername";
        String password = "myPassword";

        myUser = returnUser(domain, authenticationType, username, password);

        if (myUser != null)
            failedLogins = myUser.getFailedLogins();

        try {
            this.authDataAccessor = new AuthDataAccessor(new SecurityProperties(), mockRealDAO(), mockUserDAO(myUser), null, mockAnySearchDAO(myUser, numUsers), null, confParam(confParamUsername), null, null, null, null, null, null);
        } catch (Error | Exception e) {
            Assert.fail();
        }
    }


    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {null, AuthenticationType.ACTIVE, "username", 1, NullPointerException.class},
                {DOMAIN, AuthenticationType.ACTIVE, "username", 1, false},
                {DOMAIN, AuthenticationType.NULL, "username", 1, NullPointerException.class},
                {DOMAIN, AuthenticationType.NO_USER, "username", 1, false},
                {DOMAIN, AuthenticationType.NO_AUTHENTICATION, "username", 1, NullPointerException.class},
                {" ", AuthenticationType.ACTIVE, "username", 1, false},
                {"", AuthenticationType.ACTIVE, "username", 1, false},
                {DOMAIN, AuthenticationType.ACTIVE_PASSWORD_WRONG, "username", 1, false},
                //{"ABD", AuthenticationType.ACTIVE_USERNAME_WRONG, "username", 1, false},
                {DOMAIN, AuthenticationType.NO_AUTHENTICATION, "username", 1, NullPointerException.class},
                {DOMAIN, AuthenticationType.IS_SUSPENDED, "username", 1, DisabledException.class},
                {DOMAIN, AuthenticationType.IS_FAILED_LOGINS, "username", 1, false},
                {DOMAIN, AuthenticationType.STATUS, "username", 1, DisabledException.class},
                // line coverage 222 PIT & JACOCO
                {DOMAIN, AuthenticationType.ACTIVE, "different-username", 2, NullPointerException.class},
                // line coverage 217 PIT
                {DOMAIN, AuthenticationType.ACTIVE, "different-username", 1, false},
                {DOMAIN, AuthenticationType.ACTIVE, "username", 2, false},
        });
    }

    @Test
    public void authenticate() {
        Object error = false;
        try {
            Triple<User, Boolean, String> result = this.authDataAccessor.authenticate(this.domain, this.authentication);

            if (result.getLeft() != null) {
                Assert.assertEquals(myUser, result.getLeft());
                Assert.assertEquals(isAuthenticated, result.getMiddle());
            }
            if (isAuthenticated != null && isAuthenticated.equals(true)) {
                Assert.assertEquals(Integer.valueOf(0), result.getLeft().getFailedLogins());
            } else if (isAuthenticated != null) {
                failedLogins++;
                Assert.assertEquals(failedLogins, result.getLeft().getFailedLogins());
                Assert.assertEquals(myUser.isSuspended(), result.getLeft().isSuspended());
            }

        } catch (NullPointerException | DisabledException e) {
            error = e.getClass();
            e.printStackTrace();
        }

        Assert.assertEquals(isExceptionExpected, error);
    }

    public User returnUser(String domain, AuthenticationType authenticationType, String username, String password) {
        User user = getUser(username, password);
        authentication = authentication(domain, username, password);
        switch (authenticationType) {
            case NULL -> {
                user = null;
                authentication = null;
            }
            case ACTIVE -> {
                isAuthenticated = true;
            }
            case ACTIVE_PASSWORD_WRONG -> {
                authentication = authentication(domain, username, "wrongPassword");
                isAuthenticated = false;
            }
            case ACTIVE_USERNAME_WRONG -> {
                authentication = authentication(domain, "wrongUsername", password);
                isAuthenticated = false;
            }
            case NO_USER -> {
                user = null;
                isAuthenticated = null;
            }
            case NO_AUTHENTICATION -> {
                authentication = null;
                isAuthenticated = false;
            }
            case IS_SUSPENDED -> {
                user.setSuspended(true);
                isAuthenticated = false;
            }
            case IS_FAILED_LOGINS -> {
                user.setFailedLogins(1);
                isAuthenticated = true;
            }
            case STATUS -> {
                user.setStatus("UNKNOWN");
                isAuthenticated = false;
            }
        }
        return user;
    }

}