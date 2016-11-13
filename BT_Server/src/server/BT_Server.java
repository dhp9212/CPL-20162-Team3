package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	private StreamConnectionNotifier server = null;
	
	public BT_Server() throws IOException {  
		String connectionString = "btspp://localhost:" + uuid + ";name=Sample SPP Server";
  
        server = (StreamConnectionNotifier) Connector.open(connectionString);  
		System.out.println("\nServer Started. Waiting for clients to connect..."); 
    }  
	
	public Session accept() throws IOException {  
        StreamConnection channel = server.acceptAndOpen();  
        
        //단말 정보 불러옴
        RemoteDevice dev = RemoteDevice.getRemoteDevice(channel);
		System.out.println("Remote device address: " + dev.getBluetoothAddress());
		System.out.println("Remote device name: " + dev.getFriendlyName(true));
		 
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
            	//연결 확인
            	if (!connect) 
            	{
            		btOut.write(RESULT_CONNECTION.getBytes("UTF-8"));
            		btOut.flush();
            		System.out.println("Connected"); 
            		connect = true;
            	}
            	
            	byte[] buff = new byte[1024]; 
            	clearArray(buff);
            	int n;
                while ((n = btIn.read(buff)) > 0) {  
                    String data = new String(buff, 0, n, "UTF-8");  
                    
                    if(data.equals(REQUEST_CURRENT_TEMP))
                    {
                    	System.out.println("mobile device requires current temperature");
                    	
                    	String response = "32.3";//현재 온도
                    	              
	                    System.out.println("Sending response :" + response);
	                    btOut.write(response.getBytes("UTF-8"));  
	                    btOut.flush();
                    }
                    else if(data.equals(RQUEST_TODAY_TEMP))
                    {
                    	System.out.println("mobile device requires today's temperature");
                    	
                    	String response = "32.7/35.0/40.5/18.4/22.3/23.9/20.2/30.2";//오늘 온도
                    	              
	                    System.out.println("Sending response :" + response);
	                    btOut.write(response.getBytes("UTF-8"));  
	                    btOut.flush();
                    }
                }            	
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
		// display local device address and name
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		BT_Server sampleSPPServer = new BT_Server();
		while (true) {
			Session session = sampleSPPServer.accept();  
            new Thread(session).start(); 
		}

	}
}

