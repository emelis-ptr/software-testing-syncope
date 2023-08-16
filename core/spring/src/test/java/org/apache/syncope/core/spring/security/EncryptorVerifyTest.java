package org.apache.syncope.core.spring.security;

import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class EncryptorVerifyTest {
    private String value; // {null, empty, notEmpty}
    private final CipherAlgorithm cipherAlgorithm; // {null, CipherAlgorithm}
    private final String encoded;  // {null, empty, notEmpty}
    private final Object expected;

    private static final Encryptor encryptor = Encryptor.getInstance();

    public EncryptorVerifyTest(String value, CipherAlgorithm cipherAlgorithm, String encoded, Object expected) {
        this.value = value;
        this.cipherAlgorithm = cipherAlgorithm;
        this.encoded = encoded;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {null, CipherAlgorithm.AES, encryptValue(null, CipherAlgorithm.AES), false},
                {"Test", CipherAlgorithm.AES, encryptValue("Test", CipherAlgorithm.AES), true},
                {" ", CipherAlgorithm.BCRYPT, encryptValue(" ", CipherAlgorithm.BCRYPT), true},
                {"", CipherAlgorithm.BCRYPT, encryptValue("", CipherAlgorithm.BCRYPT), true},
                {"Test", CipherAlgorithm.BCRYPT, encryptValue("Test", CipherAlgorithm.BCRYPT), true},
                // cipherAlgorithm != encrypted cipherAlgorithm
                {"Test", CipherAlgorithm.AES, encryptValue("Test", CipherAlgorithm.BCRYPT), false},
                {null, CipherAlgorithm.BCRYPT, encryptValue("Test", CipherAlgorithm.AES), false},
                // JACOCO line coverage 121
                {"Test", null, encryptValue("Test", CipherAlgorithm.AES), true},
                // PIT line coverage 126
                {"Test", CipherAlgorithm.SHA, encryptValue("Test", CipherAlgorithm.SHA), true},
                // Other CipherAlgorithm
                {"Test", CipherAlgorithm.SHA1, encryptValue("Test", CipherAlgorithm.SHA1), true},
                {"Test", CipherAlgorithm.SHA256, encryptValue("Test", CipherAlgorithm.SHA256), true},
                {"Test", CipherAlgorithm.SHA512, encryptValue("Test", CipherAlgorithm.SHA512), true},
                {"Test", CipherAlgorithm.SSHA, encryptValue("Test", CipherAlgorithm.SSHA), false},
                {"Test", CipherAlgorithm.SSHA1, encryptValue("Test", CipherAlgorithm.SSHA1), false},
                {"Test", CipherAlgorithm.SSHA256, encryptValue("Test", CipherAlgorithm.SSHA256), false},
                {"Test", CipherAlgorithm.SSHA512, encryptValue("Test", CipherAlgorithm.SSHA512), false},
                {"Test", CipherAlgorithm.SMD5, encryptValue("Test", CipherAlgorithm.SMD5), false},

        });
    }

    @Test
    public void testVerify() {
        Object result = encryptor.verify(this.value, this.cipherAlgorithm, this.encoded);
        Assert.assertEquals(this.expected, result);
    }

    public static String encryptValue(String value, CipherAlgorithm cipherAlgorithm) {
        String encoded;

        try {
            encoded = encryptor.encode(value, cipherAlgorithm);
        } catch (Exception e) {
            e.printStackTrace();
            encoded = e.getClass().toString();
        }
        return encoded;
    }

}

