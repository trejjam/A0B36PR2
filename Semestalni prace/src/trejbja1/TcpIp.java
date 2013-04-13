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
 *
 * @author Jan
 */
public class TcpIp {
  private static long delay=50;
  private String IP;
  private int port=0;
  private boolean TcpThread=false;
  
  private Object lock = new Object();
  private Object lockSend = new Object();
  
  Queue<Integer> fifo = null;
  Queue<Integer> fifoSend = null;
  
  Thread tcpThread = null;
  TcpThread threadContent = null;
  
  public TcpIp() {
    synchronized (lock) {
      fifo = new LinkedList<>();
    }
    synchronized (lockSend) {
      fifoSend = new LinkedList<>();
    }
  }
  
  public TcpIp(String IP) {
    this(IP, 5003);    
  }
  
  public TcpIp(String IP, int port) {    
    this();
    
    this.IP=IP;
    this.port=port;
    
    conn();
  }
  
  public final boolean conn() {
    return conn(IP, port);
  }
  
  public boolean conn(String IP) {
    return conn(IP, port);
  }
  
  public boolean conn(String IP, int port) {
    if ("".equals(IP) || port==0) {
      return false;
    }
    
    threadContent= new TcpThread(IP, port);
    
    tcpThread=new Thread(threadContent);
    tcpThread.setDaemon(true);
    
    tcpThread.start();
    
    return true;
  }
  
  public void close() {
    TcpThread=false;
  }
  
  public int read() {
    synchronized (lock) {
      if (!fifo.isEmpty()) {
        return fifo.remove();
      }
    }
    return -1;
  }
  
  public boolean send(String data) {
    if (isConnected()) {
      threadContent.sendData(data);
    }
    else {
      return false;
    }
    return true;
  }
  
  public boolean isConnected() {
      return threadContent.isConnected();
    }
  
  private class TcpThread implements Runnable {
    private PrintWriter streamOut=null;
    private BufferedReader streamIn=null;

    private Socket socket = null;
    private String IP;
    private int port;
    
    Thread tcpThreadSend = null;
    TcpThreadSend threadSendContent = null;
  
    public TcpThread(String IP, int port) {
      this.IP=IP;
      this.port=port;
    }

    @Override
    public void run() {
      String message;
      
      try {
        socket= new Socket(IP, port);
        TcpThread=true;
      } catch (UnknownHostException ex) {
        //nenalezen DNS,...
        Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        //nepodařilo se spojit
        Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      try {
        streamOut = new PrintWriter(socket.getOutputStream(), true);
        streamIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }
      catch (IOException ex) {
        //chyba
        Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      startSendThread();
      
      while(TcpThread)
      {
        try {
          if (!socket.isConnected()) {
            TcpThread=false;
            break;
          }
          if (!streamIn.ready()) {
            continue;
          }
          // jinak precteme, co server odpovedel a vypiseme 
          message = streamIn.readLine();
          System.out.println("Message: " + message);
          
          synchronized (lock) {
            for (int i=0; i<message.length(); i++) {
              fifo.add(new Integer (message.charAt(i)));
            }
          }
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
          Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      
      try {
        // pokud mame skoncit, tak uzavreme otevrene proudy a socket
        streamIn.close();
        streamOut.close();
        socket.close();
      } catch (IOException ex) {
        Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    private void startSendThread() {
      threadSendContent= new TcpThreadSend();
    
      tcpThreadSend=new Thread(threadSendContent);
      tcpThreadSend.setDaemon(true);

      tcpThreadSend.start();
    }
    
    public void sendData(String data) {
      synchronized (lockSend) {
        for (int i=0; i<data.length(); i++) {
          fifoSend.add(new Integer (data.charAt(i)));
        }
      }
      tcpThreadSend.interrupt();
    }
    
    public void close() {
      TcpThread=false;
    }
    
    public boolean isConnected() {
      return socket.isConnected();
    }
    
      protected class TcpThreadSend implements Runnable {
        @Override
        public void run() {
          while(TcpThread)
          {
            synchronized (lockSend) {
              while(!fifoSend.isEmpty()) {
                // odesílání dat serveru
                streamOut.println(fifoSend.remove().toString());
              }
            }
            try {
              tcpThreadSend.wait();
            } catch (InterruptedException ex) {
              Logger.getLogger(TcpIp.class.getName()).log(Level.SEVERE, null, ex);
            }
          }
        }
      }
  }
}
