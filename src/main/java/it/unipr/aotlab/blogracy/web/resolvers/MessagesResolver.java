/*
 * Copyright (c)  2011 Enrico Franchi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.Blogracy;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.web.misc.HttpResponseCode;
import it.unipr.aotlab.blogracy.web.post.PostQuery;
import it.unipr.aotlab.blogracy.web.post.PostQueryParser;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

public class MessagesResolver extends AbstractRequestResolver {
    @Override
    protected String getViewType() {
        return "text/json";
    }

    @Override
    protected void post(final TrackerWebPageRequest request,
                        final TrackerWebPageResponse response)
            throws URLMappingError {
        final InputStream inputStream = request.getInputStream();
        @SuppressWarnings("unchecked")
        final Map<String, String> headers =
                (Map<String,String>) request.getHeaders();

        try {
        	String message = null;

        	String contentType = headers.get("content-type");
        	System.out.println("ct!" + contentType);
        	if (contentType.startsWith("multipart/form-data")) {
        		ParameterParser parser = new ParameterParser();
        		Map<String, String> typeInfo =
        				(Map<String, String>) parser.parse(contentType, ';');
        		byte[] boundary = typeInfo.get("boundary").trim().getBytes();
        	
        		MultipartStream multipartStream = new MultipartStream(inputStream, boundary);
			    boolean nextPart = multipartStream.skipPreamble();
			    while (nextPart) {
				    String partHeaders = multipartStream.readHeaders();
				    System.out.println("h!" + partHeaders);
				    
			        final Map<String, String> partInfo =
			                (Map<String,String>) parser.parse(partHeaders, ';');

		            java.util.Iterator iterator = partInfo.keySet().iterator();  
		            while (iterator.hasNext()) {  
		            	String key = iterator.next().toString();
		            	String value = partInfo.get(key);
		            	
		            	System.out.println(key + " " + value);  
		            }
			        
			        if ("message".equals(partInfo.get("name"))) {
			        	ByteArrayOutputStream out = new ByteArrayOutputStream();
			        	message = out.toString();
			        } else if ("file".equals(partInfo.get("name"))) {
			        	System.out.print("d!");
			        	multipartStream.readBodyData(System.out);
			        }
				    nextPart = multipartStream.readBoundary();
			    }
        	} else {            
                final PostQueryParser parser = new PostQueryParser();
	            final PostQuery query = parser.parse(inputStream, headers);
	            message = query.getStringValue("message");
        	}
	
            if (message != null) {
                Logger.info(message);
                Blogracy.getSingleton().shareMessage(message);
            } else {
                throw new URLMappingError(
                        HttpResponseCode.HTTP_BAD_REQUEST,
                        "No message field found!");
            }
        } catch (MultipartStream.MalformedStreamException e) {
            throw new URLMappingError(HttpResponseCode.HTTP_INTERNAL_ERROR, e);
        } catch (IOException e) {
            throw new URLMappingError(HttpResponseCode.HTTP_INTERNAL_ERROR, e);
        } catch (URISyntaxException e) {
            throw new URLMappingError(HttpResponseCode.HTTP_INTERNAL_ERROR, e);
        }

    }
}
