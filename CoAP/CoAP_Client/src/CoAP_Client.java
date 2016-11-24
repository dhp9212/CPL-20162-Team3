/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others. 
 *  
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *  
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html 
 * and the Eclipse Distribution License is available at 
 *    http://www.eclipse.org/org/documents/edl-v10.html. 
 *  
 * Contributors: 
 *    Matthias Kovatsch - creator and main architect 
 ******************************************************************************/

 
import java.io.FileInputStream; 
import java.io.FileNotFoundException;
import java.io.IOException; 
import java.io.InputStream; 
import java.net.InetSocketAddress; 
import java.net.URI; 
import java.net.URISyntaxException; 
import java.security.GeneralSecurityException; 
import java.security.KeyStore; 
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey; 
import java.security.cert.Certificate; 
import java.security.cert.CertificateException;
import java.util.logging.Level; 
 



import org.eclipse.californium.core.CaliforniumLogger; 
import org.eclipse.californium.core.Utils; 
import org.eclipse.californium.core.coap.CoAP; 
import org.eclipse.californium.core.coap.MediaTypeRegistry; 
import org.eclipse.californium.core.coap.Request; 
import org.eclipse.californium.core.coap.Response; 
import org.eclipse.californium.core.network.CoapEndpoint; 
import org.eclipse.californium.core.network.Endpoint; 
import org.eclipse.californium.core.network.EndpointManager; 
import org.eclipse.californium.core.network.config.NetworkConfig; 



 
/**
 * This class implements a simple CoAP client for testing purposes. Usage: 
 * <p> 
 * {@code java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]} 
 * <ul> 
 * <li>METHOD: {GET, POST, PUT, DELETE, DISCOVER, OBSERVE} 
 * <li>URI: The URI to the remote endpoint or resource} 
 * <li>PAYLOAD: The data to send with the request} 
 * </ul> 
 * Options: 
 * <ul> 
 * <li>-l: Loop for multiple responses} 
 * </ul> 
 * Examples: 
 * <ul> 
 * <li>{@code SampleClient DISCOVER coap://localhost} 
 * <li>{@code SampleClient POST coap://someServer.org:5683 my data} 
 * </ul> 
 */ 
public class CoAP_Client { 
 
 static { 
  CaliforniumLogger.initialize(); 
  CaliforniumLogger.setLevel(Level.WARNING); 

 } 
  
 // the trust store file used for DTLS server authentication 
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks"; 
 private static final String TRUST_STORE_PASSWORD = "rootPass"; 
 
 private static final String KEY_STORE_LOCATION = "certs/keyStore.jks"; 
 private static final String KEY_STORE_PASSWORD = "endPass"; 
  
 // resource URI path used for discovery 
 private static final String DISCOVERY_RESOURCE = "/.well-known/core"; 
 
 // indices of command line parameters 
 private static final int IDX_METHOD          = 0; 
 private static final int IDX_URI             = 1; 
 private static final int IDX_PAYLOAD         = 2; 
 
 // exit codes for runtime errors 
 private static final int ERR_MISSING_METHOD  = 1; 
 private static final int ERR_UNKNOWN_METHOD  = 2; 
 private static final int ERR_MISSING_URI     = 3; 
 private static final int ERR_BAD_URI         = 4; 
 private static final int ERR_REQUEST_FAILED  = 5; 
 private static final int ERR_RESPONSE_FAILED = 6; 
 
 
 // initialize parameters 
 static String method = null; 
 static URI uri = null; 
 static String payload = ""; 
 static boolean loop = false; 
 
 static boolean usePSK = false; 
 static boolean useRaw = true; 
 
 private String message = null;
 
 // for coaps 
 private static Endpoint dtlsEndpoint; 
 
 
  
 /*
  * Main method of this client. 
  */ 
 public CoAP_Client(){
	 
 }
 
 public CoAP_Client(String method, String uri, String payload) throws IOException, GeneralSecurityException{
	 
	 // create request according to specified method 
	  Request request = newRequest(method); 
	   
	  request.setURI(uri); 
	  request.setPayload(payload); 
	  request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN); 
	  
	  
	  // for security
	  if (request.getScheme().equals(CoAP.COAP_SECURE_URI_SCHEME)) { 
		   // load trust store 
		   KeyStore trustStore = KeyStore.getInstance("JKS"); 
		   InputStream inTrust = new FileInputStream(TRUST_STORE_LOCATION); 
		   trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray()); 
		   // load multiple certificates if needed 
		   Certificate[] trustedCertificates = new Certificate[1]; 
		   trustedCertificates[0] = trustStore.getCertificate("root"); 

		   dtlsEndpoint.start(); 
		   EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint); 
	  } 
	  
	// execute request 
	try { 
		request.send(); 
	 
		// loop for receiving multiple responses 
		do { 
	  
	    // receive response 
	    Response response = null; 
	    	
	    try { 
	    	response = request.waitForResponse(); 
	    } catch (InterruptedException e) { 
	    	System.err.println("Failed to receive response: " + e.getMessage()); 
	    	System.exit(ERR_RESPONSE_FAILED); 
	    } 
	  
	    // output response 
	    if (response != null) { 
	  
	    	System.out.println(Utils.prettyPrint(response));
	    	System.out.println("Time elapsed (ms): " + response.getRTT()); 
	    	
	    	byte[] resp_payload = response.getPayload();
	    	message = new String(resp_payload, "UTF-8");
	    	System.out.println("message :" + message);
	    	
	  
	     // check of response contains resources 
	    	if (response.getOptions().isContentFormat(MediaTypeRegistry.APPLICATION_LINK_FORMAT)) { 
	  
	    		String linkFormat = response.getPayloadString(); 
	  
	    		// output discovered resources 
	    		System.out.println("\nDiscovered resources:"); 
	    		System.out.println(linkFormat); 
	  
	    	} 
	    	else { 
	    	 	// check if link format was expected by client 
	    	 	if (method.equals("DISCOVER")) { 
	    		System.out.println("Server error: Link format not specified"); 
	    	 	} 
	    	} 
	    }
	    else { 
	    	// no response received  
	    	System.err.println("Request timed out"); 
	    	break; 
	    } 
	   } while (loop); 
	  } catch (Exception e) { 
		  System.err.println("Failed to execute request: " + e.getMessage()); 
		  System.exit(ERR_REQUEST_FAILED); 
	  } 
 }
 

 	public String getMessage(){
 		return message;
 	}

 
 /*
  * Instantiates a new request based on a string describing a method. 
  *  
  * @return A new request object, or null if method not recognized 
  */ 
 private static Request newRequest(String method) { 
  if (method.equals("GET")) { 
   return Request.newGet(); 
  } else if (method.equals("POST")) { 
   return Request.newPost(); 
  } else if (method.equals("PUT")) { 
   return Request.newPut(); 
  } else if (method.equals("DELETE")) { 
   return Request.newDelete(); 
  } else if (method.equals("DISCOVER")) { 
   return Request.newGet(); 
  } else if (method.equals("OBSERVE")) { 
   Request request = Request.newGet(); 
   request.setObserve(); 
   loop = true; 
   return request; 
  } else { 
   System.err.println("Unknown method: " + method); 
   System.exit(ERR_UNKNOWN_METHOD); 
   return null; 
  } 
 } 
 
}