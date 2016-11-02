package btserver;

import java.io.IOException;  
import java.util.Date;  
import javax.bluetooth.LocalDevice;  
import javax.bluetooth.ServiceRecord;  
import javax.microedition.io.Connector;  
import javax.microedition.io.StreamConnection;  
import javax.microedition.io.StreamConnectionNotifier;  
import java.io.InputStream;  
import java.io.OutputStream;  

public class BTServer {
	 
    //Standard SerialPortService ID  
    static final String serverUUID = "0000110100001000800000805f9b34fb";  
    //static final String serverUUID = "11111111111111111111111111111123";  
  
    private StreamConnectionNotifier server = null;  
  
    public BTServer() throws IOException {  
  
        server = (StreamConnectionNotifier) Connector.open(  
                "btspp://localhost:" + serverUUID,  
                Connector.READ_WRITE, true  
        );  
          
        ServiceRecord record = LocalDevice.getLocalDevice().getRecord(server);  
        LocalDevice.getLocalDevice().updateRecord(record);  
    }  
  
   
    public Session accept() throws IOException {  
        log("Accept");  
        StreamConnection channel = server.acceptAndOpen();  
        log("Connected");  
        return new Session(channel);  
    }  
    public void dispose() {  
        log("Dispose");  
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
                byte[] buff = new byte[512];  
                int n = 0;  
                while ((n = btIn.read(buff)) > 0) {  
                    String data = new String(buff, 0, n);  
                    log("Receive:"+data);  
                    btOut.write(data.toUpperCase().getBytes());  
                    btOut.flush();  
                }  
            } catch (Throwable t) {  
                t.printStackTrace();  
            } finally {  
                close();  
            }  
        }  
        public void close() {  
            log("Session Close");  
            if (btIn    != null) try {btIn.close();} catch (Exception e) {/*ignore*/}  
            if (btOut   != null) try {btOut.close();} catch (Exception e) {/*ignore*/}  
            if (channel != null) try {channel.close();} catch (Exception e) {/*ignore*/}  
        }  
    }  
  
  
    public static void main(String[] args) throws Exception {  
          
    	BTServer server = new BTServer();  
          
        while (true) {  
            Session session = server.accept();  
            new Thread(session).start();  
        }  
         
    }  
      
    private static void log(String msg) {  
        System.out.println("["+(new Date()) + "] " + msg);  
    }  
}
