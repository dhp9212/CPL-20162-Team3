import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;



public class Server extends CoapServer{
	
	static final String REQUEST_CURRENT_TEMP = "2";
	static final String RQUEST_TODAY_TEMP ="3";
	
	static String ip_addr = "";
	
	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	public static void main(String[] args){
		
		
/*
		if(args.length != 1){
			System.out.println("Usage : java -jar FILENAME.jar IP_ADDRESS");
			System.exit(0);
		}
		ip_addr = args[0];
*/
		ip_addr = "192.168.10.100";
		
		try{
			Server server = new Server();
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
	
	public Server() throws SocketException{
		add(new Resource());
	}

	class Resource extends CoapResource{
		
		//String message = "Exchange with Server";
		
		public Resource(){
			super("ServerResource");
			getAttributes().setTitle("CoAP Server Resource");
		}
		
		
		Database db = new Database(ip_addr);
		
		// If server receives GET from client
		// DB select
		@Override
		public void handleGET(CoapExchange exchange){
			byte[] payload = exchange.getRequestPayload();
			
			String ret = "";
			String message;
			try {
				message = new String(payload, "UTF-8");
				if(message.equals(REQUEST_CURRENT_TEMP)){
					ret = db.processMsg(REQUEST_CURRENT_TEMP,  "GET");
				}
				else if(message.equals(RQUEST_TODAY_TEMP)){
					ret = db.processMsg(RQUEST_TODAY_TEMP,  "GET");
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			
			exchange.respond(ret);
		}
		
		// If server receives PUT from client
		// DB modify
		@Override
		public void handlePUT(CoapExchange exchange){
			byte[] payload = exchange.getRequestPayload();
			
			try{
				String message = new String(payload, "UTF-8");
				String ret = db.processMsg(message, "PUT");
				
				//exchange.respond(CHANGED, ret);
				exchange.respond(ret);
				
			}catch(Exception e){
				e.printStackTrace();
				exchange.respond(BAD_REQUEST, "Invalid Payload");
			}
			
			
		}
	}
	
}
