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

public class JwtSignature {

    public static String sign(String content, KeyPair keyPair) {
        String result = null;
        try {
            String payload = Base64.encodeBase64URLSafeString(content
                    .getBytes());

            byte[] encodedKey = keyPair.getPublic().getEncoded();
            String kid = Base64.encodeBase64URLSafeString(encodedKey);
            JSONObject headerObj = new JSONObject().put("typ", "JWT")
                    .put("alg", "RS256").put("kid", kid);
            String header = Base64.encodeBase64URLSafeString(headerObj
                    .toString().getBytes());

            byte[] bytesToSign = (header + "." + content).getBytes("UTF-8");
            Signature signer = Signature.getInstance("SHA256withRSA");
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
            JSONObject headerObj = new JSONObject(header);
            byte[] encodedKey = Base64.decodeBase64(headerObj.getString("kid"));
            signerKey = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(encodedKey));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return signerKey;
    }

    public static String verify(String signed, PublicKey signerKey)
            throws SignatureException {
        String[] split = signed.split("\\.");
        String header = split[0];
        String content = split[1];
        String signature = split[2];
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(signerKey);
            verifier.update(Base64.decodeBase64(header + "." + content));
            if (!verifier.verify(Base64.decodeBase64(signature)))
                throw new SignatureException(signed);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return content;
    }
}
