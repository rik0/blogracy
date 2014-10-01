package net.blogracy.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.blogracy.config.Configurations;
import net.blogracy.util.FileUtils;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cpabe.Cpabe;

public class CpAbeController {
	
    static final String CACHE_FOLDER = Configurations.getPathConfig().getCachedFilesDirectoryPath();
	
    // CP-ABE Files
    private final String PUBFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath() + File.separator + 
    											Configurations.getUserConfig().getUser().getHash().toString() + ".pub";
    private final String MSKFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath() + File.separator + 
    											Configurations.getUserConfig().getUser().getHash().toString() + ".msk";
    private final String PVRFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath() + 
    											Configurations.getUserConfig().getUser().getHash().toString() + ".pvr";
    private final String DECFILE = Configurations.getPathConfig().getCachedFilesDirectoryPath() + 
    											Configurations.getUserConfig().getUser().getHash().toString() + ".dec";
    
	private static final CpAbeController theInstance = new CpAbeController();
	private static final FileSharing sharing = FileSharing.getSingleton();
	
	private Cpabe cpabe = new Cpabe();
	private String uriCipherInfo = null;
	private int RSAKeySize = 512;
	// blockRSASize: 53 is the maximum size of input block for RSA (with 512bit key)
	private int blockRSASize = (RSAKeySize / 8) - 11;
	private int outputBlockSizeRSA = (RSAKeySize / 8);
	
	public static CpAbeController getSingleton() {
	    return theInstance;
	}
	
	public void setup() throws IOException, ClassNotFoundException, JSONException {
		if( !new File(MSKFILE).exists() ) {
			// CP-ABE: Initial Setup
	        System.out.println(" CP-ABE Setup | Start the initial setup");
			
	        cpabe.setup(PUBFILE, MSKFILE);
			
	    	File pubFile = new File(PUBFILE);
			String pubContent = FileUtils.getContentFromFile(pubFile);
			pubFile.delete();
			
	    	JSONObject JSONObj = new JSONObject();
	    	JSONObj.put("pubkey", pubContent);
	    	
	    	File cipherInfo = new File(Configurations.getPathConfig().getCachedFilesDirectoryPath() 
										+ File.separator + "chiperInfo.json");
			
	    	FileWriter w = new FileWriter(cipherInfo);
	    	try {
	    		w.write( JSONObj.toString() );
	    		w.flush();
	    	} finally {
	    		if(w != null) w.close();
	    	}
	    	
			String uri = sharing.seed(cipherInfo);
			cipherInfo.delete();
			
			uriCipherInfo = uri;
			System.out.println(" CP-ABE Setup | End the intial setup");
		} else {
			System.out.println(" CP-ABE Setup | Initial setup not required, already done");
		}
	}
	
	public String getUriCipherInfo() {
		return this.uriCipherInfo;
	}
	
	public String getPubPath() {
		return PUBFILE;
	}
	
	public String getPvrPath() {
		return PVRFILE;
	}
	
	public String getDecPath() {
		return DECFILE;
	}
	
	public String getMskPath() {
		return MSKFILE;
	}
	
	public void setFriendPrivateKey(String id, String attribute)
			throws JSONException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		try {
			File cipherInfoFile = new File( CACHE_FOLDER + File.separator +
												sharing.getHashFromMagnetURI(uriCipherInfo) + ".json" );
			
			JSONObject cipherInfoJSON = FileUtils.getJSONFromFile(cipherInfoFile);
			
			File publicKeyFile = new File(PUBFILE);
			FileWriter writer = new FileWriter(publicKeyFile);
	                writer.write( cipherInfoJSON.get("pubkey").toString() );
			writer.close();
			
			System.out.println(" CP-ABE Controller | Start to generate Private Key");
			cpabe.keygen(PUBFILE, PVRFILE, MSKFILE, attribute);
			System.out.println(" CP-ABE Controller | Finish to generate Private Key");
			
			publicKeyFile.delete();
			
			// Start the ciphering
			PublicKey pk = null;
			if( id.equalsIgnoreCase(Configurations.getUserConfig().getUser().getHash().toString()) ) {
				pk = Configurations.getUserConfig().getUserKeyPair().getPublic();
			} else {
				pk = Configurations.getUserConfig().getFriendPublicKey(
						Configurations.getUserConfig().getFriend(id).getLocalNick());
			}
			
			File pvrFile = new File(PVRFILE);
			String pvrContent = FileUtils.getContentFromFile(pvrFile);
			pvrFile.delete();
			
			//New function Chipering
			String cipherText64 = ciphering(pvrContent, pk);
			
			if( cipherInfoJSON.has("private-keys") ) {
				JSONArray listKeys = (JSONArray) cipherInfoJSON.get("private-keys");
				
				boolean flag = false;
				for(int i=0; i<listKeys.length(); i++) {
					JSONObject j = (JSONObject) listKeys.get(i);
					if( j.get("id").toString().equalsIgnoreCase(id) ) {
						j.put("private-key", cipherText64);
						flag = true;
					}
				}
				
				if( !flag ) {
					JSONObject jObj = new JSONObject();
					jObj.put("id", id);
					jObj.put("private-key", cipherText64);
					listKeys.put(jObj);
				}
			} else {
				JSONArray listKeys = new JSONArray();
				JSONObject idKey = new JSONObject();
				idKey.put("id", id);
				idKey.put("private-key", cipherText64);
				listKeys.put(idKey);
				cipherInfoJSON.put("private-keys", listKeys);
			}
			
			FileWriter w = new FileWriter(cipherInfoFile);
			w.write(cipherInfoJSON.toString());
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public void encryptMessage(String pubfile, String policy, String inputfile, String encfile) throws Exception {
		cpabe.enc(pubfile, policy, inputfile, encfile);
	}
	
	public void decryptMessage(String pubfile, String prvfile, String encfile, String decfile) throws Exception {
		cpabe.dec(pubfile, prvfile, encfile, decfile);
	}
	
	public File getPrivateKeyFile(JSONObject jObj)
			throws IOException, JSONException, InvalidKeyException, NoSuchAlgorithmException,
						NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
		String userId = Configurations.getUserConfig().getUser().getHash().toString();
		String privateKey = "";
		
		JSONArray jsonArr = (JSONArray) jObj.get("private-keys");

		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init( Cipher.DECRYPT_MODE, Configurations.getUserConfig().getUserKeyPair().getPrivate() );
		
		for(int i=0; i<jsonArr.length(); i++){
			JSONObject j = (JSONObject) jsonArr.get(i);
			if( j.get("id").toString().equalsIgnoreCase(userId) ) {
				String coded64CipherText = j.getString("private-key");
				byte[] cipherByte = Base64.decodeBase64(coded64CipherText);
				
				int cipherByteLength = cipherByte.length;
				int numberOfBlock = cipherByteLength / outputBlockSizeRSA;
				int blockStart = 0;
				int blockEnd = outputBlockSizeRSA;
				for( int k=0; k<numberOfBlock; k++ ) {
					byte[] tempBlock = new byte[outputBlockSizeRSA];
					for( int jj=blockStart; jj<blockEnd; jj++) {
						tempBlock[jj%outputBlockSizeRSA] = cipherByte[jj];
					}
					
					byte[] decText = cipher.doFinal(tempBlock);
					privateKey += new String(decText);
					
					blockStart += outputBlockSizeRSA;
					if( i+1 == numberOfBlock-1 ){
						blockEnd += cipherByteLength-(outputBlockSizeRSA*(i+1));
					} else {
						blockEnd += outputBlockSizeRSA;
					}
				}
				break;
			}
		}

		File privateKeyFile = new File( PVRFILE );
		FileWriter writer = new FileWriter(privateKeyFile);
        	writer.write(privateKey);
		writer.close();
        
		return privateKeyFile;
	}
	
	private String ciphering(String pvrContent, PublicKey pk) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
					BadPaddingException {
		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pk);
		
		byte[] pvrByte = pvrContent.getBytes();
		int pvrByteLength = pvrByte.length;
		int numberOfBlock = pvrByteLength / blockRSASize;
		if ( pvrByteLength % blockRSASize != 0 )
			numberOfBlock++;
		
		int blockStart = 0;
		int blockEnd = blockRSASize;
		byte[] cipherText = new byte[outputBlockSizeRSA * numberOfBlock];
		
		for( int i=0; i<numberOfBlock; i++ ) {
			byte[] tempBlock = new byte[blockRSASize];
			for( int j=blockStart; j<blockEnd; j++) {
				tempBlock[j%blockRSASize] = pvrByte[j];
			}
			
			byte[] cipherBlock = cipher.doFinal(tempBlock);
			
			for( int k=i*outputBlockSizeRSA; k<(i+1)*outputBlockSizeRSA; k++){
				cipherText[k] = cipherBlock[k%outputBlockSizeRSA];
			}
			
			blockStart += blockRSASize;
			if( i+1 == numberOfBlock-1 ){
				blockEnd += pvrByteLength-(blockRSASize*(i+1));
			} else {
				blockEnd += blockRSASize;
			}
		}
		
		String cipherText64 = Base64.encodeBase64String(cipherText);
		return cipherText64;
	}
}
