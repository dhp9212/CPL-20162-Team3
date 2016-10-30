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
	
	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	public static void main(String[] args){
		
		CoapServer server = new CoapServer();
		
		server.add(new Resource());
		
		
		CoapResource path = new CoapResource("path");
		path.add(new Resource1());
		server.add(path);
		
		server.add(new Resource2(), new TimeResource(), new WritableResource());
	
		server.start();
	}
	
	
	public static class Resource extends CoapResource{
		
		public Resource(){
			super("HI");
		}
		
		@Override
		public void handleGET(CoapExchange ex){
			ex.respond("asdf");
		}
	}
	
	public static class Resource1 extends CoapResource{
		public Resource1(){
			super("Another");
		}
		
		@Override
		public void handleGET(CoapExchange ex){
			ex.respond("good?");
		}
	}
	
	public static class Resource2 extends CoapResource{
		public Resource2(){
			super("rmv");
		}
		
		@Override
		public void handleDELETE(CoapExchange ex){
			delete();
			ex.respond(DELETED);
		}
	}
	
	public static class TimeResource extends CoapResource{
		public TimeResource(){
			super("TIME");
		}
		
		@Override
		public void handleGET(CoapExchange ex){
			ex.respond(String.valueOf(System.currentTimeMillis()));
		}
	}
	
	public static class WritableResource extends CoapResource{
		public String str = "test";
		
		public WritableResource(){
			super("write");
		}
		
		@Override
		public void handleGET(CoapExchange ex){
			ex.respond(str);
		}
		
		@Override
		public void handlePUT(CoapExchange ex){
			byte[] payload = ex.getRequestPayload();
			
			try{
				str = new String(payload, "UTF-8");
				ex.respond(CHANGED, str);
			}
			catch(Exception e){
				e.printStackTrace();
                ex.respond(BAD_REQUEST, "Invalid String");
			}
		}
	}
}

/*
public class Server extends CoapServer{
	
	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	public static void main(String[] args){
		
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
		public Resource(){
			super("Server Resource");
			getAttributes().setTitle("CoAP Server Resource");
		}
		
		@Override
		public void handleGET(CoapExchange ex){
			ex.respond("Exchange with Server");
		}
	}
}
	*/