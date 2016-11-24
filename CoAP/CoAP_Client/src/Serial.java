import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
 
public class Serial implements SerialPortEventListener {
 
    SerialPort serialPort;
 
    private static final String PORT_NAMES[] = {   
    		"/dev/tty.usbserial-A9007UX1",//MAX OS X
    		"/dev/ttyACM0" ,//Linux
            "COM5", // Windows 
            };
    
    
    private InputStream input; 
    private OutputStream output;
    private static final int TIME_OUT = 2000; 
    private static final int DATA_RATE = 9600;
    
    
    // for chunk control
    private final int max_chunk = 10;
    private int chunk_cnt = 0;
    private String full_chunk = "";
    
    // for CoAP
    private String method = "PUT";
    private static String url = "";
    //private String url = "coap://192.168.10.100/ServerResource";
    private String payload = "";
 
    public void initialize() { 
    	
    	System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
    	
        CommPortIdentifier portId = null; 
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
        while (portEnum.hasMoreElements()) { 
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            System.out.println(currPortId.getName());
            
            for (String portName : PORT_NAMES) { 
                if (currPortId.getName().equals(portName)) { 
                    portId = currPortId; 
                    break; 
                } 
            } 
        }
        
        
        
        
        if (portId == null) { 
            System.out.println("Could not find COM port."); 
            return; 
        }
 
        try { 
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
        
            serialPort.setSerialPortParams(DATA_RATE, 
                    SerialPort.DATABITS_8, 
                    SerialPort.STOPBITS_1, 
                    SerialPort.PARITY_NONE);
         
            input = serialPort.getInputStream(); 
            output = serialPort.getOutputStream(); 
         
            serialPort.addEventListener(this); 
            serialPort.notifyOnDataAvailable(true);
 
            
            
        } catch (Exception e) { 
            System.err.println(e.toString()); 
        } 
    }

    public synchronized void close() { 
        if (serialPort != null) { 
            serialPort.removeEventListener(); 
            serialPort.close(); 
        } 
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) { 
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) { 
            try { 
                int available = input.available();
                byte chunk[] = new byte[available]; 
                input.read(chunk, 0, available); 
                
                chunk_cnt += available;
                full_chunk += new String(chunk);
                
                if(chunk_cnt == max_chunk){

                	// processing data : transmit to CoAP Server
                	System.out.println(full_chunk);
                	CoAP_Client clnt = new CoAP_Client(method, url, full_chunk);
                	String response = clnt.getMessage();
                	
                	// initialize
                	chunk_cnt = 0;
                	full_chunk = "";
                }
                
            } catch (Exception e) { 
                System.err.println(e.toString()); 
            } 
        }
    }
    
    public static void main(String[] args) throws Exception {
    	
    	if(args.length != 1){
    		System.out.println("Usage : java -Djava.library.path/usr/lib/jni -cp /usr/share/java/RXTXcomm.jar -jar FILENAME.jar IP_ADDRESS");
    		System.exit(0);
    	}
    	url = "coap://" + args[0] + "/RelayServerResource";
    	
        Serial main = new Serial(); 
        main.initialize(); 
        System.out.println("Started"); 
    } 
}