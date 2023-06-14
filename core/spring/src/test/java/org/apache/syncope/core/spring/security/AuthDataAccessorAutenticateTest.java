package org.apache.syncope.core.spring.security;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.common.keymaster.client.api.ConfParamOps;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.spring.security.entity.MyUser;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorEnum.AuthenticationType;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorEnum.ConfParamType;
import org.apache.syncope.core.spring.security.util.AuthDataAccessorUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AuthDataAccessorAutenticateTest extends AuthDataAccessorUtil {
    private final String domain; // {null, empty, notEmpty}
    private final Object expected;

    private AuthDataAccessor authDataAccessor;
    private Authentication authentication;
    private User user;

    public AuthDataAccessorAutenticateTest(String domain, AuthenticationType authenticationType, ConfParamType confParamType, Object expected) {
        this.domain = domain;
        this.expected = expected;
        config(domain, authenticationType, confParamType);
    }

    private void config(String domain, AuthenticationType authenticationType, ConfParamType confParamType) {
        String username = "myUsername";
        String password = "myPassword";

        user = new MyUser();
        switch (authenticationType) {
            case NULL -> {
                user = null;
                authentication = null;
            }
            case ACTIVE -> {
                user = getUser(username, password);
                authentication = authentication(domain, username, password);
            }
            case ACTIVE_PASSWORD_WRONG -> {
                user = getUser(username, password);
                authentication = authentication(domain, username, "wrongPassword");
            }
            case ACTIVE_USERNAME_WRONG -> {
                user = getUser(username, password);
                authentication = authentication(domain, "wrongUsername", password);
            }
            case NO_USER -> {
                user = null;
                authentication = authentication(domain, username, password);
            }
            case NO_AUTHENTICATION -> {
                user = getUser(username, password);
                authentication = null;
            }
            case IS_SUSPENDED -> {
                user = getUser(username, password);
                user.setSuspended(true);
                authentication = authentication(domain, username, password);
            }
            case IS_FAILED_LOGINS -> {
                user = getUser(username, password);
                user.setFailedLogins(1);
                authentication = authentication(domain, username, password);
            }
            case IS_USER_MODIFIED -> {
                user = getUser(username, password);
                user.setLastModifier("newUser");
                authentication = authentication(domain, username, password);
            }

        }

        ConfParamOps confParamOps = null;
        switch (confParamType) {
            case USERNAME -> confParamOps = confParam("username");
            case NOT_USERNAME -> confParamOps = confParam("different-username");
        }
        try {
            this.authDataAccessor = new AuthDataAccessor(new SecurityProperties(), mockRealDAO(), mockUserDAO(user), null, null, null, confParamOps, null, null, null, null, null, null);
        } catch (Error | Exception e) {
            Assert.fail();
        }
    }


    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {null, AuthenticationType.ACTIVE, ConfParamType.USERNAME, NullPointerException.class},
                {"ABD", AuthenticationType.ACTIVE, ConfParamType.USERNAME, DisabledException.class},
                {"ABD", AuthenticationType.NULL, ConfParamType.USERNAME, NullPointerException.class},
                {"ABD", AuthenticationType.NO_USER, ConfParamType.USERNAME, null},
                {"ABD", AuthenticationType.NO_AUTHENTICATION, ConfParamType.USERNAME, NullPointerException.class},
                {" ", AuthenticationType.ACTIVE, ConfParamType.USERNAME, DisabledException.class},
                {"", AuthenticationType.ACTIVE, ConfParamType.USERNAME, DisabledException.class},

                {"ABD", AuthenticationType.ACTIVE, ConfParamType.NOT_USERNAME, NullPointerException.class},
                {"ABD", AuthenticationType.IS_SUSPENDED, ConfParamType.USERNAME, DisabledException.class},
                {"ABD", AuthenticationType.ACTIVE_PASSWORD_WRONG, ConfParamType.USERNAME, DisabledException.class},
                {"ABD", AuthenticationType.ACTIVE_USERNAME_WRONG, ConfParamType.USERNAME, DisabledException.class},
        });
    }

    @Test
    public void authenticate() {
        Object actual;
        try {
            Triple<User, Boolean, String> result = this.authDataAccessor.authenticate(this.domain, this.authentication);
            actual = result.getMiddle();
            Assert.assertEquals(user, result.getLeft());
        } catch (Exception e) {
            actual = e.getClass();
        }

        Assert.assertEquals(expected, actual);
    }


}