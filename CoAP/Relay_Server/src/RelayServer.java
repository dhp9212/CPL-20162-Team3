import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;





public class RelayServer extends CoapServer{
	
	static final String REQUEST_CURRENT_TEMP = "2";
	static final String RQUEST_TODAY_TEMP ="3";
	
	 // exit codes for runtime errors 
	 private static final int ERR_MISSING_METHOD  = 1; 
	 private static final int ERR_UNKNOWN_METHOD  = 2; 
	 private static final int ERR_MISSING_URI     = 3; 
	 private static final int ERR_BAD_URI         = 4; 
	 private static final int ERR_REQUEST_FAILED  = 5; 
	 private static final int ERR_RESPONSE_FAILED = 6; 
	 
	
	 static boolean loop = false; 
	 static String uri = "";
	

	
	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	public static void main(String[] args){
		
		/*
		if(args.length != 1){
			System.out.println("Usage : java -jar FILENAME.jar IP_ADDRESS");
			System.exit(0);
		}
		uri = "coap://" + args[0] + "/ServerResource";
		*/
		uri = "coap://192.168.10.100/ServerResource";
		
		try{
			RelayServer server = new RelayServer();
			server.addEndpoints();
			server.start();
			
		}
		catch(SocketException e){
			System.err.println("Failed to initialize server : " + e.getMessage());
		}

	}
	
	private void addEndpoints(){
		for(InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()){
			if(addr instanceof Inet4Address || addr.isLoopbackAddress()){
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
				addEndpoint(new CoapEndpoint(bindToAddress));
			}
			else if(addr instanceof Inet6Address){
				InetSocketAddress bintToAddress = new InetSocketAddress(addr, COAP_PORT);
				addEndpoint(new CoapEndpoint(bintToAddress));
			}
		}
	}
	
	public RelayServer() throws SocketException{
		add(new Resource());
	}

	class Resource extends CoapResource{
		
		//String message = "Exchange with Server";
		
		
		public Resource(){
			super("RelayServerResource");
			getAttributes().setTitle("Relay Server Resource");
		}
		
		// If server receives GET from client
		// DB select
		@Override
		public void handleGET(CoapExchange exchange){
			
			byte[] payload = exchange.getRequestPayload();
			
			String method = "GET";
			String message = "";
			
			Request request = newRequest(method);
			
			try {
				message = new String(payload, "UTF-8");
				
				request.setURI(uri); 
				request.setPayload(message); 
				request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN); 

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				exchange.respond(BAD_REQUEST, "Invalid Payload");
			}
			
			System.out.println(message);
			
			String ret = processRequest(request, method);
			
			exchange.respond(ret);
		}
		
		// If server receives PUT from client
		// DB modify
		@Override
		public void handlePUT(CoapExchange exchange){
			byte[] payload = exchange.getRequestPayload();
			
			String method = "PUT";
			String message = "";
			
			Request request = newRequest(method);
			
			try{
				message = new String(payload, "UTF-8");
				
				request.setURI(uri); 
				request.setPayload(message); 
				request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN); 
				
			}catch(UnsupportedEncodingException e){
				e.printStackTrace();
				exchange.respond(BAD_REQUEST, "Invalid Payload");
			}
			
			
			System.out.println(message);
			
			processRequest(request, method);
			exchange.respond("SUCCESS");
		}
	}
	
	public String processRequest(Request request, String method){
		
		String ret = "";
		
		try{
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
		  
		    	ret = response.getPayloadString();
		    	System.out.println(Utils.prettyPrint(response)); 
		    	System.out.println("Time elapsed (ms): " + response.getRTT()); 
		  
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
		

		return ret;
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
