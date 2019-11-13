package net.blogracy.messaging.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MessagingUtils {

	
	  
	  public static void copyFile(File srcFile, File dstFile) {
			if (!dstFile.exists()) {
		        try {
					dstFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    FileChannel source = null;
		    FileChannel destination = null;
		    try {
		        source = new FileInputStream(srcFile).getChannel();
		        destination = new FileOutputStream(dstFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
		    finally {
		        if (source != null) {
		            try {
						source.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		        if (destination != null) {
		            try {
						destination.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
		    }
	    }
	
}
