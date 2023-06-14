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
                {"abd", CipherAlgorithm.AES, encryptValue("abd", CipherAlgorithm.AES), true},
                {" ", CipherAlgorithm.BCRYPT, encryptValue(" ", CipherAlgorithm.BCRYPT), true},
                {"", CipherAlgorithm.BCRYPT, encryptValue("", CipherAlgorithm.BCRYPT), true},
                {"abcd", CipherAlgorithm.BCRYPT, encryptValue("abcd", CipherAlgorithm.BCRYPT), true},
                // cipherAlgorithm != encrypted cipherAlgorithm
                {"abcd", CipherAlgorithm.AES, encryptValue("abcd", CipherAlgorithm.BCRYPT), false},
                {null, CipherAlgorithm.BCRYPT, encryptValue("abcd", CipherAlgorithm.AES), false},
                // line coverage 126 PIT
                {"abcd", CipherAlgorithm.SHA, encryptValue("abcd", CipherAlgorithm.SHA), true},
                // line coverage 121 JACOCO
                {"abd", null, encryptValue("abd", CipherAlgorithm.AES), true},

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
            encoded = null;
        }
        return encoded;
    }

}

