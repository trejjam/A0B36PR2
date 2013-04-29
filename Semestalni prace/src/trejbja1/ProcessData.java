/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jan
 */
public class ProcessData implements Runnable {
    private TcpIp conn;
    private BridgeAppCode bridge;
    private boolean process=true;
    private static long delay=50;
    private String message="";
    private int XH=0, XL=0, AH=0, AL=0;
    private boolean jpegReading=false;
    private String jpegData="";
    
    ProcessData(BridgeAppCode bridge) {
        this.bridge = bridge;
        conn = bridge.getTcpIpRef();
    }
    @Override
    public void run() {
        while (process && conn!=null) {
            int znak=conn.read();
            if (znak!=-1) {
                if (jpegReading) {
                    jpegData+=znak;
                }
                if (znak==(int)0x76) {
                    message="";
                }
                message+=Character.toString((char)znak);
                checkRecievedData(message);
            }
            else {
                try {
                    Thread.yield();
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    //nevypisování chyby při odpojení
                    if (process) Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private final String sMcuCommandsStart = new String(new char[] {0x76, 0x01, 0x01, 0x01});
    private final String sMcuCommand = new String(new char[] {0x76, 0x75}); //+4 Byte messgae
    private final String sMcuCommandsStop = new String(new char[] {0x76, 0x02, 0x02, 0x02});
    private final String sTakePicture = new String(new char[] {0x76, 0x00, 0x36, 0x00, 0x00});
    private final String sJpegSize = new String(new char[] {0x76, 0x00, 0x34, 0x00, 0x04, 0x00, 0x00});
    private final String sJpegFileContent = new String(new char[] {0x76, 0x00, 0x32, 0x00, 0x00});
    private void checkRecievedData(String message) { //process recieved data
        if (message.equals(sTakePicture)) { //picture taked
            System.out.println("picture taked");
            conn.send(new String(new char[] {0x56, 0x00, 0x34, 0x01, 0x00})); //read JPEG size
        }
        if (message.length()>2 && message.substring(0, message.length()-2).equals(sJpegSize)) { //recieve JPEG size
            System.out.println("get Jpeg size");
            XH=(int)message.charAt(7);
            XL=(int)message.charAt(8);
            jpegReading=false;
            
            conn.send(new String(new char[] {0x56, 0x00, 0x32, 0x0C, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (char)XH, (char)XL, 0x00, 0x0A})); //read JPEG data
            
            /*  56 00 32 0C 00 0A 00 00 MM MM 00 00 KK KK XX XX 
                Return  ： 76 00 32 00 00  （Spacing Interval） FF D8  。。。 ……。。 。（Spacing Interval）
                76 00 32 00 00 
                （spacing interval）= XX  XX*0.01ms 
                00  00  MM  MM    Init address 
                00  00  KK  KK  data length
            */
            
            //76 00 34 00 04 00 00 XH XL 
        }
        if (message.equals(sJpegFileContent)) { //picture taked
            if (jpegReading) {
                jpegReading=false;
                System.out.println("picture file content stop");
            }
            else {
                jpegReading=true;
                System.out.println("picture file content start");
                jpegData="";
            }
        }
        if (message.equals(sMcuCommandsStart)) { //switch MCU to listen mode
            System.out.println("MCU listen");            
        }
        if (message.length()>4 && message.substring(0, message.length()-4).equals(sMcuCommand)) { //recieve MCU message
            System.out.println("MCU command");  
        }
        if (message.equals(sMcuCommandsStop)) { //switch off MCU listen mode
            System.out.println("MCU not listen");            
        }
    }
}
