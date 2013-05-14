/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jan
 */
public class ProcessData implements Runnable {
    public enum timerDo {
        cam
    }
    public enum camDo {
        reset, takeFoto, stopTakePhoto, compressRatio, setJpegSize, readJpegSize, jpegData
    }
    public enum mcuDo {
        reset
    }
    
    private TcpIp conn;
    private BridgeAppCode bridge;
    private boolean process=true;
    private static long delay=50;
    private String message="";
    private int XH=0, XL=0, AH=0, AL=0;
    private boolean jpegReading=false;
    
    private MakeFile makeImgFile=null;
    private Thread fileThread;
    
    private DeathTimer camDeath;
    
    ProcessData(BridgeAppCode bridge) {
        this.bridge = bridge;
        conn = bridge.getTcpIpRef();
    }
    @Override
    public void run() {
        camDeath=new DeathTimer(5000);
        Thread tCamDeath = new Thread(camDeath);
        tCamDeath.setDaemon(true);
        tCamDeath.start();
        
        while (process && conn!=null) {
            int znak=conn.read();
            if (znak!=-1) {
                if (jpegReading) {
                    //jpegData+=(char)znak;
                    
                    if (makeImgFile!=null) {
                        makeImgFile.write((char)znak);
                    }
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
    
    private final String sCamInit = new String(new char[]           {0x49, 0x6E, 0x69, 0x74, 0x20, 0x65, 0x6E, 0x61, 0x64, 0x0D, 0x0A});
    private final String sMcuCommandsStart = new String(new char[]  {0x76, 0x01, 0x01, 0x01});
    private final String sMcuCommand = new String(new char[]        {0x76, 0x75}); //+4 Byte messgae
    private final String sMcuCommandsStop = new String(new char[]   {0x76, 0x02, 0x02, 0x02});
    private final String sTakePicture = new String(new char[]       {0x76, 0x00, 0x36, 0x00, 0x00});
    private final String sOutStopTakePicture = new String(new char[]{0x56, 0x00, 0x36, 0x01, 0x03});
    private final String sJpegSize = new String(new char[]          {0x76, 0x00, 0x34, 0x00, 0x04, 0x00, 0x00});
    private final String sJpegFileContent = new String(new char[]   {0x76, 0x00, 0x32, 0x00, 0x00});
    private final String sImageRatioSize = new String(new char[]    {0x76, 0x00, 0x31, 0x00, 0x00});
    private boolean stopTakingPicture=false;
    private boolean compresRatio=true;
    
    private void checkRecievedData(String message) { //process recieved data
        if (message.length()>10 && message.substring(message.length()-11, message.length()).equals(sCamInit)) {
            System.out.println("initComplete");
            compresRatio=true;
            
            sendToCam(camDo.compressRatio); //change compress ratio
        }
        if (message.equals(sImageRatioSize)) { //compress ratio, change img size
            if (compresRatio) {
                System.out.println("changed compress ratio");
                compresRatio=false;
                
                sendToCam(camDo.setJpegSize); //change img size to 320x240
            }
            else {
                System.out.println("changed image size");
            }
        }
        if (message.equals(sTakePicture)) { //picture taked
            System.out.println("picture taked");
            if (!stopTakingPicture) sendToCam(camDo.readJpegSize); //read JPEG size
            stopTakingPicture=false;
        }
        if (message.length()>2 && message.substring(0, message.length()-2).equals(sJpegSize)) { //recieve JPEG size
            System.out.println("get Jpeg size");
            XH=(int)message.charAt(7);
            XL=(int)message.charAt(8);
            jpegReading=false;
            
            sendToCam(camDo.jpegData);
            //conn.send(new String(new char[] {0x56, 0x00, 0x32, 0x0C, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (char)XH, (char)XL, 0x00, 0x0A})); //read JPEG data
            
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
                
                makeImgFile.close();
                
                stopTakingPicture=true;
                sendToCam(camDo.stopTakePhoto);
                //conn.send(sOutStopTakePicture); //ukončení přijímání .jpg
            }
            else {
                jpegReading=true;
                System.out.println("picture file content start");
                
                makeImgFile = new MakeFile();
                fileThread = new Thread(makeImgFile);
                fileThread.setDaemon(true);
                fileThread.start();
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
    public void sendToCam(camDo cDo) {
        sendToCam(cDo, null);
    }
    public void sendToCam(camDo cDo, char[] adition) {
        if (cDo.equals(camDo.reset)) { 
            conn.send(new String(new char[] {0x56, 0, 0x26, 0})); //reset Cam
        }
        if (cDo.equals(camDo.takeFoto)) { 
            conn.send(new String(new char[] {0x56, 0, 0x36, 0x01, 0})); //take photo
        }
        if (cDo.equals(camDo.stopTakePhoto)) {
            conn.send(sOutStopTakePicture); //change img size to 320x240
        }
        if (cDo.equals(camDo.compressRatio)) {
            conn.send(new String(new char[] {0x56, 0x00, 0x31, 0x05, 0x01, 0x01, 0x12, 0x04, 0xFF})); //change compress ratio
        }
        if (cDo.equals(camDo.setJpegSize)) {
            conn.send(new String(new char[] {0x56, 0x00, 0x54, 0x01, 0x11})); //change img size to 320x240
        }
        if (cDo.equals(camDo.readJpegSize)) {
            conn.send(new String(new char[] {0x56, 0x00, 0x34, 0x01, 0x00})); //read JPEG size
        }
        if (cDo.equals(camDo.jpegData)) {
            conn.send(new String(new char[] {0x56, 0x00, 0x32, 0x0C, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (char)XH, (char)XL, 0x00, 0x0A})); //read JPEG data
        }
    }
    public void sendToMcu(mcuDo mDo, char[] adition) {
        if (mDo.equals(mcuDo.reset)) {
            sendToMcu((char)0, (char)0, (char)0, (char)0);
        }
    }
    public void sendToMcu(char HA, char LA, char HD, char LD) { //send command to MCU
        conn.send(new String(new char[] {0x56, 0x55, HA, LA, HD, LD}));
    }
    
    private class MakeFile implements Runnable {
        private OutputStream output = null;
        private String file="";
        private boolean closed=true;
        private boolean run=false;
        private boolean close=false;
        private Object fifoLock=new Object();
        
        private Queue<Character> fifo = null;
        
        public MakeFile() {
            this.file = "camImg_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".jpg";
            fifo = new LinkedList<>();
        }
        public MakeFile(String file) {
            this();
            this.file=file;
        }
        public void write(char add) {
            synchronized(fifoLock) {
                fifo.add(add);
            }
        }
        public void close() {
            close=true;
        }
        private void close(boolean x) {
            try {                
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                Thread.sleep(150);
                File showImg =new File( file );
                Desktop.getDesktop().open(showImg);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
            }
            closed=true;
        }
        public boolean isClosed() {
            return closed;
        }
        public String getFile() {
            return file;
        }

        @Override
        public void run() {
            try {
                output = new BufferedOutputStream(new FileOutputStream(file));
                closed=false;
                run=true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            while(run) {
                if (!close) {
                    writeByte();
                }
                else if (!closed) {
                    while(!fifo.isEmpty()) {
                        writeByte();
                    }
                    close(true);
                }
            }
        }
        private void writeByte() {
            synchronized(fifoLock) {
                if (!closed && !fifo.isEmpty()) {
                    try {
                        output.write(fifo.remove());
                        //output.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    private class DeathTimer implements Runnable {
        private int delay;
        private boolean tick=false;
        private timerDo toDo;
        
        public DeathTimer(int delay) {
            this.delay=delay;
        }
        public void startTick(timerDo toDo) {
            this.toDo=toDo;
            tick=true;
        }
        public void killTick() {
            tick=false;
            Thread.interrupted();
        }
        @Override
        public void run() {
            while(true) {
                Thread.yield();
                if (tick) {
                    try {
                        Thread.sleep(delay);
                        if (tick) endTick();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        private void endTick() {
            if (toDo.equals(timerDo.cam)) {
                System.out.println("cam not response!");
            }
        }
    }
}
