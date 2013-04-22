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
    
    ProcessData(BridgeAppCode bridge) {
        this.bridge = bridge;
        conn = bridge.getTcpIpRef();
    }
    @Override
    public void run() {
        while (process && conn!=null) {
            int znak=conn.read();
            if (znak!=-1) {
                if (znak==(int)0x76) {
                    message=Character.toString((char)znak);
                }
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
    private final String sTakePicture = new String(new char[] {0x76, 0x00, 0x36, 0x00, 0x00});
    private final String sJpegSize = new String(new char[] {0x76, 0x00, 0x34, 0x00, 0x04, 0x00, 0x00});
    private void checkRecievedData(String message) {
        if (message.equals(sTakePicture)) {
            System.out.println("picture taked");
            conn.send(new String(new char[] {0x56, 0x00, 0x34, 0x01, 0x00}));
        }
        if (message.length()>2 && message.substring(0, message.length()-2).equals(sJpegSize)) {
            System.out.println("get Jpeg size");
            //conn.send(new String(new char[] {0x56, 0x00, 0x34, 0x01, 0x00}));
        }
    }
}
