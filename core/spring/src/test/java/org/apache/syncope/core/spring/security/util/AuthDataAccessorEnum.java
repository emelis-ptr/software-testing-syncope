package org.apache.syncope.core.spring.security.util;

public class AuthDataAccessorEnum {
    public enum AuthenticationType {
        NULL,
        ACTIVE,
        ACTIVE_PASSWORD_WRONG,
        ACTIVE_USERNAME_WRONG,
        NO_USER,
        NO_AUTHENTICATION,
        IS_SUSPENDED,
        IS_FAILED_LOGINS,
        IS_USER_MODIFIED,
        STATUS,
    }
}
