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
		
		Database db = new Database();
		
		// If server receives GET from client
		// DB select
		@Override
		public void handleGET(CoapExchange ex){
			
			String message = db.processMsg("", "", "GET");

			/* TODO: 
			getDB���� SELECT�ϰ� �װ� String���� ���� handleGET���� String���� ����
			�� String�� client���� ������ client���� String�� ó���ϵ��� �ƴϸ� �ۿ��� ó���ϵ��� �ؼ� ȭ�鿡 �Ѹ�
			*/
			ex.respond(message);
		}
		
		// If server receives PUT from client
		// DB modify
		@Override
		public void handlePUT(CoapExchange exchange){
			byte[] payload = exchange.getRequestPayload();
			
			try{
				String message = new String(payload, "UTF-8");
				String ret = db.processMsg(message, "/", "PUT");
				
				//exchange.respond(CHANGED, ret);
				exchange.respond(ret);
				
			}catch(Exception e){
				e.printStackTrace();
				exchange.respond(BAD_REQUEST, "Invalid String");
			}
			
			
		}
	}
	
	
	
}