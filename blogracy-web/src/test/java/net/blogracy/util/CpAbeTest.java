package net.blogracy.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CpAbeController;
import net.blogracy.controller.FileSharing;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public class CpAbeTest {
	
	private static final CpAbeController cpabe = CpAbeController.getSingleton();
    private static final FileSharing sharing = FileSharing.getSingleton();

	private static String CPABE = "CpAbeTest-";
	private static String ATTRIBUTE = "role:student class:B";
	private static String MESSAGE = CPABE + "message";
	private static String POLICY = "role:student class:A 1of2";
	private static String ENCFILE = CPABE + "encfile";
	private static String DECFILE = CPABE + "decfile";
	private static String PRVFILE = CPABE + "prvfile";

	@Rule
	public static TemporaryFolder testFolder = new TemporaryFolder();
	
	private static File message, publicKeyFile, privateKeyFile, decFile;
	private static JSONObject cipherInfoJSON;
	private static String helloWorld = "Hello World\n";
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		cpabe.setup();
		cpabe.setFriendPrivateKey(Configurations.getUserConfig().getUser().getHash().toString(), ATTRIBUTE);
		
		message = testFolder.newFile("cache" + File.separator + MESSAGE);
		org.apache.commons.io.FileUtils.writeStringToFile(message, helloWorld);
		
		String uriCipherInfo = cpabe.getUriCipherInfo();
		cipherInfoJSON = FileUtils.getJSONFromFile(new File("cache" + File.separator
				+ sharing.getHashFromMagnetURI(uriCipherInfo) + ".json"));
		
		publicKeyFile = testFolder.newFile(cpabe.getPubPath());
		org.apache.commons.io.FileUtils.writeStringToFile(publicKeyFile, cipherInfoJSON.get("pubkey").toString() );

		privateKeyFile = testFolder.newFile("cache" + File.separator + PRVFILE );
		//org.apache.commons.io.FileUtils.writeStringToFile(privatekeyFile, );
		
		decFile = testFolder.newFile("cache" + File.separator + DECFILE);
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
    public void testEncDecrypt() throws Exception {
    	System.out.println("Cifro...");
    	cpabe.encryptMessage(publicKeyFile.getPath().toString(), 
    			POLICY, 
    			message.getPath().toString(),
    			"cache" + File.separator + ENCFILE);
    	System.out.println("Decifro...");
    	cpabe.decryptMessage(publicKeyFile.getPath().toString(),
    			             cpabe.getPrivateKeyFile(cipherInfoJSON).getPath().toString(),
    						 "cache" + File.separator + ENCFILE,
    						 decFile.getPath().toString());
    	System.out.println("Confronto...");
    	assertEquals(helloWorld, FileUtils.getContentFromFile(decFile).toString());
    }
    
}
