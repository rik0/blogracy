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
import org.json.JSONException;
import org.json.JSONObject;
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

	private static String myBlogracyContent = "[{\"id\":\"7PAEA2BZXQYZHXXHXWMUSCOVGCT46SLA\",\"uri\":\"magnet:?xt=urn:btih:QY3WSKVBGDZXMSR6DAS3ES4IIPDS6MCJ\",\"version\":\"2012-10-22T22:56:45\"}]";

	@Test
	public void testSignatureSize() {
		try {
			String payload = Base64.encodeBase64URLSafeString(myBlogracyContent
					.getBytes("UTF-8"));

			byte[] encodedKey = keyPair.getPublic().getEncoded();
			//String kid = Base64.encodeBase64URLSafeString(encodedKey);
			JSONObject headerObj = new JSONObject().put("typ", "JWT")
					.put("alg", "RS256");
			//.put("kid", kid);

			String header = Base64.encodeBase64URLSafeString(headerObj
					.toString().getBytes("UTF-8"));

			byte[] bytesToSign = (header + "." + payload).getBytes("UTF-8");
			Signature signer = Signature.getInstance("SHA256withRSA");
			signer.initSign(keyPair.getPrivate());
			signer.update(bytesToSign);
			String signature = Base64.encodeBase64URLSafeString(signer.sign());

			String result = header + "." + payload + "." + signature;
			
			System.out.println("Original payload size (bytes UTF8):" +  myBlogracyContent.getBytes("UTF-8").length);
			System.out.println("Original payload size (string base64 length):" +  payload.length());
			System.out.println("Public Key size (bytes):" +  encodedKey.length);
			//System.out.println("Public Key size (string base64 length):" +  kid.length());
			System.out.println("Header Total size (string base64 length):" +  header.length());
			System.out.println("Signature Total size (string base64 length):" +  signature.length());
			System.out.println("Total size (string length):" +  result.length());
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*@Test
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
	}*/

}
