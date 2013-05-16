/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Třída pro komunikaci s hardwarem
 * @author Jan
 */
public class TcpIp {
    private BridgeAppCode bridge;
    private static long delay=50;
    private static int conDelay=10000;
    private String IP;
    private int port=0;
    private boolean TcpThread=false;
    
    private boolean echo=false;

    private Object lock = new Object();
    private Object lockSend = new Object();

    private Queue<Integer> fifo = null;
    private Queue<Integer> fifoSend = null;

    private Thread tcpThread = null;
    private TcpThread threadContent = null;

    private Thread tcpThreadSend = null;
    private TcpThreadSend threadSendContent = null;

    private Socket socket = null;
    private BufferedOutputStream streamOut = null;
    private BufferedReader streamIn=null;
    private InputStream inputStream=null;

    /**
     * Konstruktor třídy se zpětnou referencí do BridgeAppCode
     * Vytvoření objektu bez znalosti IP adresy
     * @param bridge 
     */
    public TcpIp(BridgeAppCode bridge) {
        this.bridge=bridge;
        synchronized (lock) {
            fifo = new LinkedList<>();
        }
        synchronized (lockSend) {
            fifoSend = new LinkedList<>();
        }
    }
    /**
     * Konstruktor třídy se zpětnou referencí do BridgeAppCode
     * Vytvoření objektu se znalostí IP adresy a automatickým doplněním portu 5003
     * @param IP
     * @param bridge 
     */
    public TcpIp(String IP, BridgeAppCode bridge) {
      this(IP, 5003, bridge);    
    }
    /**
     * Konstruktor třídy se zpětnou referencí do BridgeAppCode
     * Vytvoření objektu se znalostí IP adresy a portu
     * @param IP
     * @param port
     * @param bridge 
     */
    public TcpIp(String IP, int port, BridgeAppCode bridge) {    
      this(bridge);

      this.IP=IP;
      this.port=port;

      conn();
    }
    /**
     * Vytvoření připojení na uloženou adresu
     * @return 
     */
    public final boolean conn() {
      return conn(IP, port);
    }
    /**
     * Vytvoření připojení na nově IP adresu
     * @param IP
     * @return 
     */
    public boolean conn(String IP) {
      return conn(IP, port);
    }
    /**
     * Vytvoření připojení na nově zadanou IP adresu a port
     * @param IP
     * @param port
     * @return 
     */
    public boolean conn(String IP, int port) {
      if ("".equals(IP) || port==0) {
        return false;
      }

      threadContent= new TcpThread(IP, port);

      tcpThread=new Thread(threadContent);
      tcpThread.setName("RecieveThread");
      tcpThread.setDaemon(true);

      tcpThread.start();

      return true;
    }
    /**
     * Zrušení připojení
     */
    public void close() {
      TcpThread=false;
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
        }
      tcpThread.interrupt();
    }
    /**
     * Přečtšní přijatých hodnot
     * @return 
     */
    public int read() {
      synchronized (lock) {
        if (!fifo.isEmpty()) {
          return fifo.remove();
        }
      }
      return -1;
    }
    /**
     * Přidání dat k odeslání
     * @param data
     * @return 
     */
    public boolean send(String data) {
      if (isConnected()) {
        synchronized (lockSend) {
          for (int i=0; i<data.length(); i++) {
            fifoSend.add(new Integer (data.charAt(i)));
          }
        }

        try {
            tcpThreadSend.interrupt();
        }
        catch (NullPointerException e) {
            System.out.println("tcpThreadSend exception");
        }
      }
      else {
        return false;
      }
      return true;
    }
    /**
     * Ověření navázání socketu na cílové zařízení
     * @return 
     */
    public boolean isConnected() {
      return socket.isConnected();
    }
    /**
     * Zjištění povoleného času pro připojení
     * @return 
     */
    public int getConDelay() {
        return conDelay;
    }
    /**
     * Třída pro vytvoření streamů, odesílacího vlákna
     * Třída starající se o přijímání dat
     */
    private class TcpThread implements Runnable {
        private String IP;
        private int port;

        public TcpThread(String IP, int port) {
          this.IP=IP;
          this.port=port;
        }

        @Override
        public void run() {
            String message;
            byte[] inByte = new byte[ 2048 ];
            int lengthInByte;

            try {
                SocketAddress sAddr = new InetSocketAddress(IP, port);
                socket= new Socket();
                socket.connect(sAddr, conDelay);
                TcpThread=true;
            } catch (SocketTimeoutException ex) {
                System.out.println("conn Timeout");
                bridge.setConnButon(bridge.getLangValues().get("Connect"), 0);
                return;
            } catch (UnknownHostException ex) {
                //nenalezen DNS,...
                Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
                return;
            } catch (IOException ex) {
                //nepodařilo se spojit
                Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

            bridge.stopConnTimer();
            bridge.setConnButon(bridge.getLangValues().get("Disconnect"), 1);
            bridge.initProcessDataThread();

            try {
                streamOut = new BufferedOutputStream(socket.getOutputStream());
                //streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                inputStream = socket.getInputStream();
            }
            catch (IOException ex) {
                //chyba
                Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
            }

            startSendThread();

            //bridge.getProcessData().sendToMcu(ProcessData.mcuDo.stopListen); //reset Cam
            bridge.getProcessData().sendToCam(ProcessData.camDo.reset); //reset Cam

            while(TcpThread) {
            try {
                if (!socket.isConnected()) {
                    System.out.println("Disconnected");
                    break;
                }
                /*if (!streamIn.ready()) {
                  continue;
                }*/
                
                message="";
                try {
                    if ((lengthInByte=inputStream.read(inByte))!=0) {
                        if (lengthInByte==-1) {
                            System.out.println("Disconnected");
                            break; 
                        }

                        for (int i=0; i<lengthInByte; i++) {
                            if (echo) System.out.print((char)inByte[i]);
                            message += (char)inByte[i];
                        }                  
                    }
                }
                catch (SocketTimeoutException e) {
                    System.out.println("Disconnected");
                    break;
                }
if (echo) {
                System.out.print("\nIn: \n" + " - ");
                for (int i=0; i<message.length(); i++) {
                    //System.out.print(message.charAt(i) + ", ");
                    System.out.print(Integer.toHexString(new Integer (message.charAt(i))) + ", ");
                }
                System.out.println("\n");
}
                synchronized (lock) {
                    for (int i=0; i<message.length(); i++) {
                        fifo.add(new Integer (message.charAt(i)));
                    }
                }
              }
              catch (SocketException ex) {
                  //nevypisování chyby při odpojení
                  if (TcpThread) Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
              }
              catch (IOException ex) {
                  TcpThread=false;
                  //chyba
                  Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
              }
              
              try {
                  Thread.yield();
                  Thread.sleep(delay);
              } catch (InterruptedException ex) {
                  //nevypisování chyby při odpojení
                  if (TcpThread) Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
              }
          }

          try {
              // pokud mame skoncit, tak uzavreme otevrene proudy a socket
              //streamIn.close();
              //streamOut.close();
              socket.close();
          } catch (IOException ex) {
              Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
      private void startSendThread() {
        threadSendContent= new TcpThreadSend();

        tcpThreadSend=new Thread(threadSendContent);
        tcpThreadSend.setName("SendThread");
        tcpThreadSend.setDaemon(true);
        //tcpThreadSend.setPriority(1);

        tcpThreadSend.start();
      }
    }
    /**
     * Třída starající se o odesílání dat
     */
    private class TcpThreadSend implements Runnable {
    @Override
        public void run() {
            boolean sendAble=false;
            while(TcpThread) {
                synchronized (lockSend) {
                    while(!fifoSend.isEmpty()) {
                        try {
                            int s = fifoSend.remove();
                            streamOut.write(s);
                            System.out.print(Integer.toHexString(s)+", ");
                            sendAble=true;
                        } catch (IOException ex) {
                            Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                if (sendAble) {
                    sendAble=false;
                    System.out.println("\n");
                    try {
                        streamOut.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {              
                    tcpThreadSend.join();
                } catch (InterruptedException ex) {
                    //Probuzení vlákna
                    System.out.println("\nsend notify");
                }
            }
        }
    }
}
