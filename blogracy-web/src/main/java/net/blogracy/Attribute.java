package net.blogracy;

import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import de.odysseus.el.tree.impl.Parser;
import net.blogracy.config.Configurations;
import net.blogracy.controller.FileSharing;
import net.blogracy.util.FileUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class Attribute {
	
	static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();
	
	private static final Attribute theInstance = new Attribute();
    private static final FileSharing sharing = FileSharing.getSingleton();

	public static Attribute getSingleton() {
        return theInstance;
    }
	
	Attribute() {
		
	}
	
	/**
     * Set the attribute for friends of the user
     * NOTE: That function create a file with the name: "id.attribute"
     *
     * @param id to identify user that set the attribute
     * @param attr a List that contains a String in the form "ID attribute"
     * 
     */
	public void setAttribute(String uri, String id, String attribute)
			throws JSONException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		try {
			
			PublicKey pk = null;
			if( id.equalsIgnoreCase(Configurations.getUserConfig().getUser().getHash().toString()) ) {
				pk = Configurations.getUserConfig().getUserKeyPair().getPublic();
			} else {
				pk = Configurations.getUserConfig().getFriendPublicKey(
						Configurations.getUserConfig().getFriend(id).getLocalNick());
			}
			
			final Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			byte[] cipherText = cipher.doFinal(attribute.getBytes());
			String cipherText64 = Base64.encodeBase64String(cipherText);
		
			File cipherInfoFile = new File(CACHE_FOLDER + File.separator + sharing.getHashFromMagnetURI(uri) + ".json" );
			
			JSONObject jsonObject = FileUtils.getJSONFromFile(cipherInfoFile);
			
			if( jsonObject.has("attributes") ) {
				JSONArray listAttr = (JSONArray) jsonObject.get("attributes");
				JSONObject idAttribute = new JSONObject();
				
				boolean flag = false;
				for(int i=0; i<listAttr.length(); i++) {
					JSONObject j = (JSONObject) listAttr.get(i);
					if( j.get("id").toString().equalsIgnoreCase(id) ) {
						j.put("attribute", cipherText64);
						flag = true;
					}
				}
				
				if( !flag ) {
					JSONObject jObj = new JSONObject();
					jObj.put("id", id);
					jObj.put("attribute", cipherText64);
					listAttr.put(jObj);
				}
			} else {
				JSONArray listAttr = new JSONArray();
				JSONObject idAttribute = new JSONObject();
				idAttribute.put("id", id);
				idAttribute.put("attribute", cipherText64);
				listAttr.put(idAttribute);
				jsonObject.put("attributes", listAttr);
			}
			
			FileWriter w = new FileWriter(cipherInfoFile);
			w.write(jsonObject.toString());
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Returns a string that contains the attributes of a given user
     *
     * @param file to identify the file that contains the attributes
     * @param id to identify the user
     * @return the attributes
	 * @throws JSONException 
     */
	public String getAttribute(File file, String id) 
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
					BadPaddingException, JSONException {
		String attributes = "";
		
		JSONObject jsonObj = FileUtils.getJSONFromFile(file);
		JSONArray jsonArr = (JSONArray) jsonObj.get("attributes");
				
		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, Configurations.getUserConfig().getUserKeyPair().getPrivate());
		
		for(int i=0; i<jsonArr.length(); i++){
			JSONObject j = (JSONObject) jsonArr.get(i);
			if( j.get("id").toString().equalsIgnoreCase(id) ) {
				byte[] cipherText = Base64.decodeBase64(j.getString("attribute"));
				byte[] decText = cipher.doFinal(cipherText);
				
				attributes += new String(decText) + " ";
			}
		}

		if (attributes.length() > 0)
			attributes = attributes.substring(0, attributes.length()-1);

		return attributes;
	}
	
}
