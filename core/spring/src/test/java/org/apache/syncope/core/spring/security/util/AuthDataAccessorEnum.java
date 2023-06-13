package org.apache.syncope.core.spring.security.util;

public class AuthDataAccessorEnum {
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
