
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;


public class Client {
	
	
	// exit codes for runtime errors
	private static final int ERR_MISSING_METHOD  = 1;
	private static final int ERR_UNKNOWN_METHOD  = 2;
	private static final int ERR_MISSING_URI     = 3;
	private static final int ERR_BAD_URI         = 4;
	private static final int ERR_REQUEST_FAILED  = 5;
	private static final int ERR_RESPONSE_FAILED = 6;
	
	static String method = null;
	static URI uri = null;
	static String payload = "";
	static boolean loop = false;

	/*
	 * Application entry point.
	 * 
	 */	
	public static void main(String args[]) {
		
		
		
		loop = true;
		method = "GET";
		try {
			uri = new URI("coap://localhost:5683");
		} catch (URISyntaxException e) {
			System.err.println("Failed to parse URI: " + e.getMessage());
			System.exit(ERR_BAD_URI);
		}
		
		
		if (method == null) {
			System.err.println("Method not specified");
			System.exit(ERR_MISSING_METHOD);
		}
		if (uri == null) {
			System.err.println("URI not specified");
			System.exit(ERR_MISSING_URI);
		}
		
		Request request = newRequest(method);
		
		request.setURI(uri);
		request.setPayload(payload);
		//request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		
		try{
			request.send();
			
			do{
				Response response = null;
				try {
					response = request.waitForResponse();
				} catch (InterruptedException e) {
					System.err.println("Failed to receive response: " + e.getMessage());
					System.exit(ERR_RESPONSE_FAILED);
				}
				
				
				if (response != null) {
					
					System.out.println(Utils.prettyPrint(response));
					System.out.println("Time elapsed (ms): " + response.getRTT());
	
					// check of response contains resources
					if (response.getOptions().isContentFormat(MediaTypeRegistry.APPLICATION_LINK_FORMAT)) {
	
						String linkFormat = response.getPayloadString();
	
						// output discovered resources
						System.out.println("\nDiscovered resources:");
						System.out.println(linkFormat);
	
					} else {
						// check if link format was expected by client
						if (method.equals("DISCOVER")) {
							System.out.println("Server error: Link format not specified");
						}
					}
	
				} else {
					// no response received	
					System.err.println("Request timed out");
					break;
				}
				
			}while(loop);
			
		} catch (Exception e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(ERR_REQUEST_FAILED);
		}
		
		/*
		URI uri = null;
		
		try{
			uri = new URI("coap://localhost:5683");
		}
		catch(URISyntaxException e){
			System.err.println("Invalid URI: " + e.getMessage());
			System.exit(-1);
		}
		
		CoapClient client = new CoapClient(uri);
		CoapResponse response = client.get();
		
		if (response!=null) {
			
			System.out.println(response.getCode());
			System.out.println(response.getOptions());
			System.out.println(response.getResponseText());
			
			System.out.println("\nADVANCED\n");
			// access advanced API with access to more details through .advanced()
			System.out.println(Utils.prettyPrint(response));
			
		} else {
			System.out.println("No response received.");
		}
		
		*/
	}
	
	
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
