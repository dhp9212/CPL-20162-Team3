import org.eclipse.californium.core.CoapClient;


public class Client {

	public static void main(String[] args){
		System.out.println("CoAP client test");
		
		CoapClient client = new CoapClient("coap://192.168.10.107:5683");
		
		if(!client.ping()){
			System.out.println("Server is down.");
			return;
		}
		
		System.out.println("Get");
		System.out.println("Response\n" + client.get().getResponseText());
	}
	
}
