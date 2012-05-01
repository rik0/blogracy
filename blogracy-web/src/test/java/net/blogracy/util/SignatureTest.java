package net.blogracy.util;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SignatureTest {

    private static String content;
    private static KeyPair keyPair;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        keyPair = keyPairGenerator.genKeyPair();
        content = "Guybrush Treepwood";
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBasicSignature() {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(content.getBytes("UTF-8"));
            String signature = Base64.encodeBase64URLSafeString(signer.sign());

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(keyPair.getPublic());
            verifier.update(content.getBytes("UTF-8"));
            assert (verifier.verify(Base64.decodeBase64(signature)));

        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJwtSignature() {
        try {
            String signed = JsonWebSignature.sign(content, keyPair);

            PublicKey publicKey = JsonWebSignature.getSignerKey(signed);
            assertEquals(publicKey, keyPair.getPublic());

            String verified = JsonWebSignature.verify(signed, publicKey);
            assertEquals(verified, content);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

}
