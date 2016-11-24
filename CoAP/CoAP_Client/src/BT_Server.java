

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import java.net.InetAddress;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


public class BT_Server {
	static boolean connect = false;
	
	static final String RESULT_CONNECTION = "1";
	static final String REQUEST_CURRENT_TEMP = "2";
	static final String RQUEST_TODAY_TEMP ="3";
	
	static final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
	
	private static String method;
	private static String url;
	private static String payload;
	
	// for bluetooth local device info
	private static String ld_addr;
	private static String ld_name;

	private static String ep_addr;
	private static String ep_name;
	

	
	private StreamConnectionNotifier server = null;
	
	public BT_Server() throws IOException {  
		String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
  
        server = (StreamConnectionNotifier) Connector.open(connectionString);  
		System.out.println("\nServer Started. Waiting for clients to connect..."); 
    }  
	
	public Session accept() throws IOException {  
        StreamConnection channel = server.acceptAndOpen();  
        
        
        RemoteDevice dev = RemoteDevice.getRemoteDevice(channel);
		System.out.println("Remote device address: " + dev.getBluetoothAddress());
		System.out.println("Remote device name: " + dev.getFriendlyName(true));
		
		ep_addr = dev.getBluetoothAddress();
		ep_name = dev.getFriendlyName(true);
		 
        return new Session(channel);  
    }  
    public void dispose() {  
    	System.out.println("Dispose");  
        if (server  != null) try {server.close();} catch (Exception e) {/*ignore*/}  
    }  
    
    static class Session implements Runnable {  
        private StreamConnection channel = null;  
        private InputStream btIn = null;  
        private OutputStream btOut = null;  
        
        private String response = null;
  
        public Session(StreamConnection channel) throws IOException {  
            this.channel = channel;  
            this.btIn = channel.openInputStream();  
            this.btOut = channel.openOutputStream();  
        }  
  
        public void run() {  
            try {  
            	
            	if (!connect) 
            	{
            		btOut.write(RESULT_CONNECTION.getBytes("UTF-8"));
            		btOut.flush();
            		System.out.println("Connected"); 
            		connect = true;
            	}
            	
            	
            	method = "PUT";
        		//url = "coap://192.168.10.107/ServerResource";
        		payload = "BT#" + ld_addr + "/" + ld_name + "/" + ep_addr + "/" + ep_name;
        		
        		try {
        			CoAP_Client clnt = new CoAP_Client(method, url, payload);
        		} catch (GeneralSecurityException e) {
        			e.printStackTrace();
        		}
            	
            	
            	byte[] buff = new byte[1024]; 
            	clearArray(buff);
            	int n;
                while ((n = btIn.read(buff)) > 0) {  
                    String data = new String(buff, 0, n, "UTF-8");  
                    
                    if(data.equals(REQUEST_CURRENT_TEMP))
                    {
                    	System.out.println("mobile device requires current temperature");
                    	
                    	method = "GET";
                    	payload = REQUEST_CURRENT_TEMP;
                    	
                    	
	                	try {
	            			CoAP_Client clnt = new CoAP_Client(method, url, payload);
	            			response = clnt.getMessage();
	            			
	            			System.out.println("Sending response :" + response);
	                	
	                    } catch (GeneralSecurityException e) {
	            			e.printStackTrace();
	            		}
	                	finally{
	                		btOut.write(response.getBytes("UTF-8"));  
		                    btOut.flush();
	                	}

                    }
                    else if(data.equals(RQUEST_TODAY_TEMP))
                    {
                    	System.out.println("mobile device requires today's temperature");
                    	
                    	method = "GET";
                    	payload = RQUEST_TODAY_TEMP;
                    	
                    	try {
                			CoAP_Client clnt = new CoAP_Client(method, url, payload);
                			response = clnt.getMessage();
                			System.out.println(response);
                			
                			System.out.println("Sending response :" + response);
    	                    
                		} catch (GeneralSecurityException e) {
                			e.printStackTrace();
                		}
                    	finally{
	                    	btOut.write(response.getBytes("UTF-8"));
		                    btOut.flush();
                    	}
                    }
                }            	
            } catch (Throwable t) {  
                t.printStackTrace();  
            } finally {  
                close();  
            }  
        }
        
        public void close() { 
        	
        	method = "PUT";
        	payload = "BTCL#" + ld_addr + "/" + ep_addr;
        	try {
        		
				CoAP_Client clnt = new CoAP_Client(method, url, payload);
				response = clnt.getMessage();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (GeneralSecurityException e1) {
				e1.printStackTrace();
			}
        	
        	System.out.println("Session Close");  
            if (btIn    != null) try {btIn.close();} catch (Exception e) {/*ignore*/}  
            if (btOut   != null) try {btOut.close();} catch (Exception e) {/*ignore*/}  
            if (channel != null) try {channel.close();} catch (Exception e) {/*ignore*/} 
            connect = false;
        }  
        
        public void clearArray(byte[] buff) {
            for (int i = 0; i < buff.length; i++)
            {
                buff[i] = 0;
            }
        }
    }  

	public static void main(String[] args) throws IOException {
		
		if(args.length != 1){
			System.out.println("Usage : java -jar FILENAME.jar IP_ADDRESS");
			System.exit(0);
		}
		System.out.println("Connect to " + args[0]);
		
		url = "coap://" + args[0] + "/RelayServerResource";
		
		// display local device address and name
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		//System.out.println("Address: " + localDevice.getBluetoothAddress());
		//System.out.println("Name: " + localDevice.getFriendlyName());
			
		

		//InetAddress host = InetAddress.getLocalHost();
		//host.getHostAddress();
		
		
		ld_addr = localDevice.getBluetoothAddress();
		ld_name = localDevice.getFriendlyName();
		

		BT_Server sampleSPPServer = new BT_Server();
		while (true) {
			BT_Server.Session session = sampleSPPServer.accept();  
	        new Thread(session).start(); 
		}

	}

}

