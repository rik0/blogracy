package net.blogracy.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonWebSignature {

	private static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
	private static String DEFAULT_KEYFACTORY_ALGORITHM = "RSA";

	/***
	 * Sign the content using the given keypair
	 * 
	 * @param content
	 *            content to be signed
	 * @param keyPair
	 *            keyPair to be used
	 * @return
	 */
	public static String sign(String content, KeyPair keyPair) {
		String result = null;
		try {
			String payload = Base64.encodeBase64URLSafeString(content
					.getBytes("UTF-8"));

			byte[] encodedKey = keyPair.getPublic().getEncoded();
	            String kid = Base64.encodeBase64URLSafeString(encodedKey);
			
			JSONObject headerObj = new JSONObject().put("typ", "JWT").put(
					"alg", "RS256").put("kid", kid);
			String header = Base64.encodeBase64URLSafeString(headerObj
					.toString().getBytes("UTF-8"));

			byte[] bytesToSign = (header + "." + payload).getBytes("UTF-8");
			Signature signer = Signature
					.getInstance(DEFAULT_SIGNATURE_ALGORITHM);
			signer.initSign(keyPair.getPrivate());
			signer.update(bytesToSign);
			String signature = Base64.encodeBase64URLSafeString(signer.sign());

			result = header + "." + payload + "." + signature;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}


	public static PublicKey getSignerKey(String signed) {
		PublicKey signerKey = null;
		try {
			String header = signed.split("\\.")[0];
			String plainHeader = new String(Base64.decodeBase64(header),
					"UTF-8");
			JSONObject headerObj = new JSONObject(plainHeader);
			String kid = headerObj.getString("kid");

			byte[] encodedKey = Base64.decodeBase64(kid);

			signerKey = KeyFactory.getInstance(DEFAULT_KEYFACTORY_ALGORITHM)
					.generatePublic(new X509EncodedKeySpec(encodedKey));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return signerKey;
	}

	public static String getPublicKeyString(PublicKey pKey) {
		if(pKey == null)
			return null;
		
		byte[] encodedKey = pKey.getEncoded();
		String keyString = Base64.encodeBase64URLSafeString(encodedKey);
		return keyString;
	}

	public static String verify(String signed, String signerKeyString)
			throws SignatureException {
		String content = null;
		try {
			byte[] encodedKey = Base64.decodeBase64(signerKeyString);

			PublicKey signerKey = KeyFactory.getInstance(
					DEFAULT_KEYFACTORY_ALGORITHM).generatePublic(
					new X509EncodedKeySpec(encodedKey));
			content = verify(signed, signerKey);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static String verify(String signed, PublicKey signerKey)
			throws SignatureException {
		String[] split = signed.split("\\.");
		String header = split[0];
		String payload = split[1];
		String signature = split[2];
		String content = null;
		try {
			Signature verifier = Signature
					.getInstance(DEFAULT_SIGNATURE_ALGORITHM);
			verifier.initVerify(signerKey);
			verifier.update((header + "." + payload).getBytes("UTF-8"));
			if (!verifier.verify(Base64.decodeBase64(signature)))
				throw new SignatureException(signed);
			content = new String(Base64.decodeBase64(payload), "UTF-8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}
}
