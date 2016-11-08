package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


public class BTApp_server {
	private static final String RESPONSE = "Greetings from serverland";
	
	static final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
	
	private StreamConnectionNotifier server = null;
	
	public BTApp_server() throws IOException {  
		String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
  
        server = (StreamConnectionNotifier) Connector.open(connectionString);  
		System.out.println("\nServer Started. Waiting for clients to connect..."); 
    }  
	
	public Session accept() throws IOException {  
        StreamConnection channel = server.acceptAndOpen();  
        
        RemoteDevice dev = RemoteDevice.getRemoteDevice(channel);
		System.out.println("Remote device address: " + dev.getBluetoothAddress());
		System.out.println("Remote device name: " + dev.getFriendlyName(true));
		
        System.out.println("Connected");  
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
  
        public Session(StreamConnection channel) throws IOException {  
            this.channel = channel;  
            this.btIn = channel.openInputStream();  
            this.btOut = channel.openOutputStream();  
        }  
  
        public void run() {  
            try {  
            	byte[] buff = new byte[1024]; 
            	int n;
                while ((n = btIn.read(buff)) != -1) {  
                    String data = new String(buff, 0, n, "UTF-8");  
                    System.out.println("Message from mobile device: " + data);
                    
                    System.out.println("Sending response :" + data);
                    btOut.write(data.getBytes("UTF-8"));  
                    btOut.flush(); 
                }
            	/*BufferedReader bReader = new BufferedReader(new InputStreamReader(btIn, "UTF8"));
        		String data = bReader.readLine();
        		System.out.println("Message from mobile device: " + data);
        		
        		PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(btOut));
                System.out.println("Sending response (" + data + ")");
                pWriter.write(data);  
                pWriter.flush();*/
            	
            	
            } catch (Throwable t) {  
                t.printStackTrace();  
            } finally {  
                close();  
            }  
        }  
        public void close() {  
        	System.out.println("Session Close");  
            if (btIn    != null) try {btIn.close();} catch (Exception e) {/*ignore*/}  
            if (btOut   != null) try {btOut.close();} catch (Exception e) {/*ignore*/}  
            if (channel != null) try {channel.close();} catch (Exception e) {/*ignore*/}  
        }  
    }  

	public static void main(String[] args) throws IOException {
		// display local device address and name
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		BTApp_server sampleSPPServer = new BTApp_server();
		while (true) {
			Session session = sampleSPPServer.accept();  
            new Thread(session).start(); 
		}

	}
}
