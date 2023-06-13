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

@RunWith(Parameterized.class)
public class EncryptorEncodeTest {

    private final String value; // {null, empty, notEmpty}
    private final CipherAlgorithm cipherAlgorithm; // {null, CipherAlgorithm}
    private final Object expected;

    private static final Encryptor encryptor = Encryptor.getInstance();

    public EncryptorEncodeTest(String value, CipherAlgorithm cipherAlgorithm, Object expected) {
        this.value = value;
        this.cipherAlgorithm = cipherAlgorithm;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {null, null, false},
                {null, CipherAlgorithm.AES, false},
                {null, CipherAlgorithm.BCRYPT, false},
                {"", CipherAlgorithm.BCRYPT, true},
                {" ", CipherAlgorithm.BCRYPT, true},
                {"", null, true},
                {"abcd", CipherAlgorithm.BCRYPT, true},
                {"abcd", null, true},
                // line coverage 109
                {"abcd", CipherAlgorithm.SHA, true},
        });
    }

    @Test
    public void testEncode() {
        String encode;
        Object result = false;
        try {
            encode = encryptor.encode(this.value, this.cipherAlgorithm);

            if (encode != null) {
                Assert.assertNotNull(encode);
                result = true;
            }
        } catch (NullPointerException e) {
            result = e.getClass();
            e.printStackTrace();
        } catch (UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(this.expected, result);
    }
}