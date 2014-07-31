package net.blogracy.web;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import net.blogracy.config.Configurations;
import net.blogracy.controller.CpAbeController;

public class AttributeUpload extends HttpServlet {

	private static final CpAbeController cpabe = CpAbeController.getSingleton();
	
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

    	try {
    		// CP-ABE: Set friends' private key
        	for(int i=0; i<Configurations.getUserConfig().getFriends().size(); i++) {
	        	String friendNick = Configurations.getUserConfig().getFriends().get(i).getLocalNick();
	        	String friendAttr = req.getParameter(friendNick);
	        	if( !friendAttr.equals("") )
	        		cpabe.setFriendPrivateKey(Configurations.getUserConfig().getFriends()
										  								 .get(i).getHash().toString(),
										  	  friendAttr);
	        }
        } catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
    }
}
