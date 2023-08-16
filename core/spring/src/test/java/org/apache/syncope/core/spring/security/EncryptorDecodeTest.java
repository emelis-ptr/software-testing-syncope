package org.apache.syncope.core.spring.security;

import org.apache.syncope.common.lib.types.CipherAlgorithm;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@RunWith(Parameterized.class)
public class EncryptorDecodeTest {
    private final String encoded; // {null, empty, notEmpty}
    private final CipherAlgorithm cipherAlgorithm; // {null, CipherAlgorithm}
    private final Object expected;

    private static final Encryptor encryptor = Encryptor.getInstance();
    private static final String PASSWORD_VALUE = "password";

    public EncryptorDecodeTest(String encoded, CipherAlgorithm cipherAlgorithm, Object expected) {
        this.encoded = encoded;
        this.cipherAlgorithm = cipherAlgorithm;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {encryptValue(null, CipherAlgorithm.AES), CipherAlgorithm.AES, false},
                {encryptValue(" ", CipherAlgorithm.AES), CipherAlgorithm.AES, false},
                {encryptValue("password", CipherAlgorithm.AES), CipherAlgorithm.AES, true},
                {encryptValue("9Pav+xl+UyHt02H9ZBytiA==", CipherAlgorithm.AES), CipherAlgorithm.AES, false},
                {encryptValue("password", CipherAlgorithm.AES), CipherAlgorithm.BCRYPT, false},
                {encryptValue("password", CipherAlgorithm.AES), null, false},
                {encryptValue("password", CipherAlgorithm.BCRYPT), CipherAlgorithm.BCRYPT, false},
                {encryptValue("passwordSbagliata", CipherAlgorithm.BCRYPT), CipherAlgorithm.BCRYPT, false},
                {null, CipherAlgorithm.BCRYPT, false},
                // Other CipherAlgorithm
                {encryptValue("password", CipherAlgorithm.SHA), CipherAlgorithm.SHA, false},
                {encryptValue("password", CipherAlgorithm.SHA1), CipherAlgorithm.SHA1, false},
                {encryptValue("password", CipherAlgorithm.SHA256), CipherAlgorithm.SHA256, false},
                {encryptValue("password", CipherAlgorithm.SHA512), CipherAlgorithm.SHA512, false},
                {encryptValue("password", CipherAlgorithm.SSHA), CipherAlgorithm.SSHA, false},
                {encryptValue("password", CipherAlgorithm.SSHA1), CipherAlgorithm.SSHA1, false},
                {encryptValue("password", CipherAlgorithm.SSHA256), CipherAlgorithm.SSHA256, false},
                {encryptValue("password", CipherAlgorithm.SSHA512), CipherAlgorithm.SSHA512, false},
                {encryptValue("password", CipherAlgorithm.SMD5), CipherAlgorithm.SMD5, false},
        });
    }

    @Test
    public void testDecode() {
        String encode;
        Object result = false;
        try {
            encode = encryptor.decode(this.encoded, this.cipherAlgorithm);

            if (encode != null) {
                Assert.assertNotNull(encode);
            }

            if (Objects.equals(encode, PASSWORD_VALUE)) {
                result = true;
            }

        } catch (NullPointerException | UnsupportedEncodingException | NoSuchAlgorithmException |
                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            result = e.getClass();
            e.printStackTrace();
        }

        Assert.assertEquals(this.expected, result);
    }


    private static String encryptValue(String value, CipherAlgorithm cipherAlgorithm) {
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
