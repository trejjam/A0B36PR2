/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.BufferedOutputStream;
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
 * Třída starající se o zpracování přijatých dat
 * @author Jan
 */
public class ProcessData implements Runnable {
    public enum timerDo {
        nothing, camNotResponse
    }
    public enum camDo {
        reset, takeFoto, stopTakePhoto, compressRatio, setJpegSize, readJpegSize, jpegData
    }
    public enum mcuDo {
        reset, startListen, stopListen, servo1, servo2, servo3
    }
    
    private TcpIp conn;
    private BridgeAppCode bridge;
    private boolean process=true;
    private static long delay=50;
    private String message="";
    private int XH=0, XL=0, AH=0, AL=0;
    private boolean jpegReading=false;
    private boolean gettingPhoto=false;
    
    private MakeFile makeImgFile=null;
    private Thread fileThread;
    
    private DeathTimer camDeath;
    private boolean getPhotoTimer=false;
    
    /**
     * Konstruktor se zpětným parametrem do BridgeAppCode
     * @param bridge 
     */
    ProcessData(BridgeAppCode bridge) {
        this.bridge = bridge;
        
        conn = bridge.getTcpIpRef();
        
        camDeath=new DeathTimer(5000);
        Thread tCamDeath = new Thread(camDeath);
        tCamDeath.setName("camDeathThread");
        tCamDeath.setDaemon(true);
        tCamDeath.start();
    }
    @Override
    public void run() {    
        startTimer("onStartPhotoTimer");
        
        char mcuMessage[] = new char[10];
        mcuMessage[0]=0;
        int mcuI=0;
        
        while (process && conn!=null) {
            int znak=conn.read();
            if (znak!=-1) {
                if (jpegReading) {
                    //jpegData+=(char)znak;
                    
                    if (znak==0x76 && !(mcuI==1 && mcuMessage[0]==0x76 && mcuMessage[1]==0x75)) {
                        mcuMessage[0]=(char)znak;
                        mcuMessage[1]=0;
                        mcuI=0;
                    }
                    else if (mcuI==0 && mcuMessage[0]==0x76 && mcuMessage[1]==0) {
                        mcuMessage[++mcuI]=(char)znak;
                        if (mcuMessage[1]!=0x75) {
                            if (makeImgFile!=null) {
                                makeImgFile.write(mcuMessage[0]);
                                makeImgFile.write((char)znak);
                            }
                            mcuMessage[0]=0;
                        }
                        mcuMessage[2]=0;
                    }
                    else if (mcuI==1 && mcuMessage[0]==0x76 && mcuMessage[1]==0x75 && mcuMessage[2]==0) {
                        mcuMessage[++mcuI]=(char)znak;
                        if (mcuMessage[2]!=0x76) {
                            if (makeImgFile!=null) {
                                makeImgFile.write(mcuMessage[0]);
                                makeImgFile.write(mcuMessage[1]);
                                makeImgFile.write((char)znak);
                            }
                            mcuMessage[0]=0;
                        }
                    }
                    else if (mcuMessage[0]==0x76 && mcuMessage[1]==0x75 && mcuMessage[2]==0x76) {
                        mcuMessage[++mcuI]=(char)znak;
                        if (mcuI==5) {
                            System.out.println("MCU comand in Photo!");
                            //command from MCU
                            mcuMessage[0]=0;
                            messageToMcu(new String(new char[] {mcuMessage[3], mcuMessage[4], mcuMessage[5]}));
                        }
                    }
                    else {
                        if (makeImgFile!=null) {
                            makeImgFile.write((char)znak);
                        }
                    }
                }
                if (znak==(int)0x76 && !(message.length()==2 && message.charAt(0)==(char)0x76 && message.charAt(1)==(char)0x75)) {
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
    private final String sTakePicture = new String(new char[]       {0x76, 0x00, 0x36, 0x00, 0x00});
    private final String sOutStopTakePicture = new String(new char[]{0x56, 0x00, 0x36, 0x01, 0x03});
    private final String sJpegSize = new String(new char[]          {0x76, 0x00, 0x34, 0x00, 0x04, 0x00, 0x00});
    private final String sJpegFileContent = new String(new char[]   {0x76, 0x00, 0x32, 0x00, 0x00});
    private final String sImageRatioSize = new String(new char[]    {0x76, 0x00, 0x31, 0x00, 0x00});
    private final String sImageResized = new String(new char[]      {0x76, 0x00, 0x54, 0x00, 0x00});
    private boolean stopTakingPicture=false;
    private boolean compresRatio=true;
    
    private final String sMcuCommandsStart = new String(new char[]  {0x76, 0x75, 0x76, 0x01, 0x01, 0x01});
    private final String sMcuCommand = new String(new char[]        {0x76, 0x75, 0x76}); //+3 Byte message
    private final String sMcuCommandsStop = new String(new char[]   {0x76, 0x75, 0x76, 0x01, 0x01, 0x02});
    private boolean mcuListen = false;
    
    /**
     * Dělení přijatých dat
     * @param message 
     */
    private void checkRecievedData(String message) { //process recieved data
        // ******** CAM block ********
        if (message.length()>10 && message.substring(message.length()-11, message.length()).equals(sCamInit)) {
            System.out.println("initComplete");
            
            camDeath.killTick();
            
            compresRatio=true;
            
            sendToCam(camDo.compressRatio); //change compress ratio
        }
        if (message.equals(sImageRatioSize)) { //compress ratio, change img size
            if (compresRatio) {
                System.out.println("changed compress ratio");
                compresRatio=false;
                
                camDeath.killTick();
                
                sendToCam(camDo.setJpegSize); //change img size to 320x240
            }
            else {
                System.out.println("changed image size");
                camDeath.killTick();
                
                startTimer("imageResizedPhotoTimerA");
            }
        }
        if (message.equals(sImageResized)) { //picture taked
            System.out.println("changed image size");
            camDeath.killTick();

            startTimer("imageResizedPhotoTimerB");
        }
        if (message.equals(sTakePicture)) { //picture taked
            System.out.println("picture taked");
            
            camDeath.killTick();
            
            if (!stopTakingPicture) sendToCam(camDo.readJpegSize); //read JPEG size
            stopTakingPicture=false;
        }
        if (message.length()>2 && message.substring(0, message.length()-2).equals(sJpegSize)) { //recieve JPEG size
            System.out.println("get Jpeg size");
           
            camDeath.killTick();
            
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
            
            camDeath.killTick();
            
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
                fileThread.setPriority(4);
                fileThread.setDaemon(true);
                fileThread.setName("MakeFile");
                fileThread.start();
            }
        }
        // ******** end CAM block ********
        // ******** MCU block ********
        if (message.equals(sMcuCommandsStart)) { //switch MCU to listen mode
            System.out.println("MCU listen");
            mcuListen=true;
        }
        if (message.equals(sMcuCommandsStop)) { //switch off MCU listen mode
            System.out.println("MCU not listen"); 
            mcuListen=false;
        }
        if (message.length()>3 && message.substring(0, message.length()-3).equals(sMcuCommand)) { //recieve MCU message
            System.out.println("MCU command");  
            messageToMcu(message.substring(3, message.length()));
        }
        
        // ******** end MCU block ********
    }
    /**
     * Přijmutí zprávy z MCU
     * @param message 
     */
    private void messageToMcu(String message) {
        System.out.println("message - " + message);
        if (message.charAt(0)==0x03) {
            if (message.charAt(1)==0x01) { //kompas
                float angle=message.charAt(2);
                if (angle>130) angle=0xFF-(65535-angle);
                
                angle*=360;
                angle/=256;
                System.out.println(angle);
                
                System.out.println("Angle: "+angle);
                bridge.getAppRef().compasAngle(360-angle);
            }
        }
        if (message.charAt(0)==0x04) { //battery
            int batteryH=(int)message.charAt(1);
            if (batteryH>130) batteryH=0xFF-(65535-batteryH);
            int batteryL=(int)message.charAt(2);
            if (batteryL>130) batteryL=0xFF-(65535-batteryL);

            System.out.println("Battery: "+batteryH +" "+batteryL);
        }
    }
    /**
     * Odeslání příkazu do Kamery
     * @param cDo 
     */
    public void sendToCam(camDo cDo) {
        sendToCam(cDo, null);
    }
    /**
     * Odeslání příkazu s doplňujícími parametry do kamery
     * (doplňující parametry prozatím nepoužity)
     * @param cDo
     * @param adition 
     */
    public void sendToCam(camDo cDo, char[] adition) {
        if (conn!=null) {
            if (cDo.equals(camDo.reset)) { 
                conn.send(new String(new char[] {0x56, 0, 0x26, 0})); //reset Cam
                gettingPhoto=false;
                jpegReading=false;
                stopTakingPicture=false;
            }
            if (cDo.equals(camDo.takeFoto)) { 
                if (!gettingPhoto) {
                    gettingPhoto=true;
                    conn.send(new String(new char[] {0x56, 0, 0x36, 0x01, 0})); //take photo
                }
            }
            if (cDo.equals(camDo.stopTakePhoto)) {
                conn.send(sOutStopTakePicture);
                gettingPhoto=false;
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

            camDeath.startTick(timerDo.camNotResponse);
        }
        else {
            System.out.println("conn not defined");
        }
    }
    /**
     * Odeslání dat do MCU
     * @param mDo 
     */
    public void sendToMcu(mcuDo mDo) {
        sendToMcu(mDo, null);
    }
    /**
     * Odeslání dat s rozšiřujícími parametry
     * Serva, parametr 0: procentuelní výchylka
     * @param mDo
     * @param adition 
     */
    public void sendToMcu(mcuDo mDo, char[] adition) {
        if (mDo.equals(mcuDo.reset)) {
            sendToMcu((char)0, (char)0, (char)0);
        }
        if (mDo.equals(mcuDo.startListen)) {
            mcuListen=true;
            sendToMcu((char)1, (char)1, (char)1);
        }
        if (mDo.equals(mcuDo.stopListen)) {
            sendToMcu((char)1, (char)1, (char)2);
        }
        if (mDo.equals(mcuDo.servo1)) {
            /*if (!mcuListen) {
                sendToMcu(mcuDo.startListen);
                sendToMcu((char)2, (char)1, adition[0]);
                sendToMcu(mcuDo.stopListen);
            }
            else {*/
                sendToMcu((char)2, (char)1, adition[0]);
            //}
        }
        if (mDo.equals(mcuDo.servo2)) {
            sendToMcu((char)2, (char)2, adition[0]);
        }
        if (mDo.equals(mcuDo.servo3)) {
            sendToMcu((char)2, (char)3, adition[0]);
        }
    }
    /**
     * Odeslání příkazu do MCU bez režie
     * @param A
     * @param HD
     * @param LD 
     */
    public void sendToMcu(char A, char HD, char LD) { //send command to MCU
        if (conn!=null) {
            System.out.println(A+"-"+HD+"-"+LD);
            conn.send(new String(new char[] {0x76, 0x75, A, HD, LD}));
        }
    }
    /**
     * Spuštění časovače pro automatizovaný příjem fotografie (vždy běží pouze jednou)
     * @param name 
     */
    private synchronized void startTimer(String name) {
        if (!getPhotoTimer && bridge.getAppRef().getAutoPhotos()) {
            getPhotoTimer=true;
            Thread connTimer = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Integer.parseInt(bridge.getAppRef().getAutoPhotosTime()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    System.out.println("photo");
                    sendToCam(ProcessData.camDo.takeFoto);

                    getPhotoTimer=false;
                }
            };
            connTimer.setDaemon(true);
            connTimer.setName(name);
            connTimer.start();
        }
    }
    
    /**
     * Třída vytvářející soubor s fotografií
     * po uzavření souboru fotografií zobrazí v GUI
     */
    private class MakeFile implements Runnable {
        private OutputStream output = null;
        private String file="";
        private boolean closed=true;
        private boolean run=false;
        private boolean close=false;
        private Object fifoLock=new Object();
        private String imgName="";
        
        private Queue<Character> fifo = null;
        
        /**
         * Konstruktor s generovaným názvem souboru
         */
        public MakeFile() {
            imgName="camImg_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".jpg";
            this.file = "photos/"+imgName;
            fifo = new LinkedList<>();
        }
        /**
         * Konstruktor se zadaným názvem názvem souboru
         * @param file 
         */
        public MakeFile(String file) {
            this();
            this.file=file;
        }
        /**
         * Zápis dat do souboru
         * @param add 
         */
        public void write(char add) {
            synchronized(fifoLock) {
                fifo.add(add);
            }
        }
        /**
         * Požadavek na zavření souboru
         */
        public void close() {
            close=true;
        }
        /**
         * Skutečné zavření souboru po zapsání všech dat z bufferu
         * @param x 
         */
        private void close(boolean x) {
            try {                
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //try {
                bridge.getAppRef().setPhoto(imgName);
                //Thread.sleep(150);
                //File showImg =new File( file );
                //Desktop.getDesktop().open(showImg);
            //} catch (InterruptedException | IOException ex) {
            //    Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
            //}
            closed=true;
            run=false;
            
            startTimer("getNewImage");
        }
        /**
         * Ověření zavřeného souboru
         * @return 
         */
        public boolean isClosed() {
            return closed;
        }
        /**
         * Získání jména souboru
         * @return 
         */
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
        /**
         * Zápis dat z bufferu do souboru
         */
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
    /**
     * Třída hlídající čas pro odpověd od kamery,
     * podle zadaného vstupního příkazu provede příslušnou akci
     */
    private class DeathTimer implements Runnable {
        private int delay;
        private boolean tick=false;
        private timerDo toDo=timerDo.nothing;
        
        /**
         * Vytvoření objektu se zadaným časem pro spuštní automatizované akce
         * @param delay 
         */
        public DeathTimer(int delay) {
            this.delay=delay;
        }
        /*
         * Spuštění časovače s nastavením akce po vypršení limitu
         */
        public void startTick(timerDo toDo) {
            this.toDo=toDo;
            tick=true;
        }
        /**
         * Zastavení časovače
         */
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
                        tick=false;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        private void endTick() {
            if (toDo.equals(timerDo.camNotResponse)) {
                System.out.println("cam not response!");
                if (mcuListen) {
                    sendToMcu(mcuDo.stopListen);
                    sendToCam(camDo.reset);
                    sendToMcu(mcuDo.startListen);
                }
                else {
                    sendToCam(camDo.reset);
                }
            }
        }
    }
}
