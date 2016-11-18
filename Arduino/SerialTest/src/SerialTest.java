import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
 
public class SerialTest implements SerialPortEventListener {
 
    SerialPort serialPort;
 
    private static final String PORT_NAMES[] = {   
    		"/dev/tty.usbserial-A9007UX1",//MAX OS X
    		"/dev/ttyUSB0" ,//Linux
            "COM3", // Windows 
            };
    
    private InputStream input;//포트에서 데이터를 읽음 
    private OutputStream output;//포트를 통해 아두이노에 데이터 전송
    private static final int TIME_OUT = 2000; //포트 오픈 대기 시간(2초)
    private static final int DATA_RATE = 9600;//아두이노 통신속도와 맞춰야됨
 
    public void initialize() { 
        CommPortIdentifier portId = null; 
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
     //내 컴퓨터에서 지원하는 시리얼 포트들 중 아두이노와 연결된 포트에 대한 식별자를 찾음
        while (portEnum.hasMoreElements()) { 
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            
            for (String portName : PORT_NAMES) { 
                if (currPortId.getName().equals(portName)) { 
                    portId = currPortId; 
                    break; 
                } 
            } 
        }
        // 식별자를 찾지 못했을 경우 종료
        if (portId == null) { 
            System.out.println("Could not find COM port."); 
            return; 
        }
 
        try { // 시리얼 포트 오픈, 클래스 이름을 애플리케이션을 위한 포트 식별 이름으로 사용
            serialPort = (SerialPort) portId.open(this.getClass().getName(), 
                    TIME_OUT);
         // 속도등 포트의 파라메터 설정
            serialPort.setSerialPortParams(DATA_RATE, 
                    SerialPort.DATABITS_8, 
                    SerialPort.STOPBITS_1, 
                    SerialPort.PARITY_NONE);
         // 포트를 통해 읽고 쓰기 위한 스트림 오픈
            input = serialPort.getInputStream(); 
            output = serialPort.getOutputStream(); 
         // 아두이노로 부터 전송된 데이터를 수신하는 리스너를 등록
            serialPort.addEventListener(this); 
            serialPort.notifyOnDataAvailable(true);
 
        } catch (Exception e) { 
            System.err.println(e.toString()); 
        } 
    }
    //이 메서드는 포트 사용을 중지할 때 반드시 호출해야 한다.리눅스와 같은 플랫폼에서는 포트 잠금을 방지한다.
    public synchronized void close() { 
        if (serialPort != null) { 
            serialPort.removeEventListener(); 
            serialPort.close(); 
        } 
    }
    // 시리얼 통신에 대한 이벤트를 처리, 데이터를 읽고 출력
    public synchronized void serialEvent(SerialPortEvent oEvent) { 
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) { 
            try { 
                int available = input.available(); 
                byte chunk[] = new byte[available]; 
                input.read(chunk, 0, available); 
                System.out.print(new String(chunk)); 
            } catch (Exception e) { 
                System.err.println(e.toString()); 
            } 
        }
    }
    
    public static void main(String[] args) throws Exception { 
        SerialTest main = new SerialTest(); 
        main.initialize(); 
        System.out.println("Started"); 
    } 
}