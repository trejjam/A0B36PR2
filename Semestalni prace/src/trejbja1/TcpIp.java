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
  
  Queue<Integer> fifo = null;
  
  Thread tcpThread = null;
  TcpThread threadContent = null;
  
  public TcpIp() {
    fifo = new LinkedList<>();
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
  
  public boolean conn() {
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
  
  public int read() {
    if (!fifo.isEmpty()) {
      return fifo.remove();
    }
    return -1;
  }
  
  public void send(String data) {
    threadContent.sendData(data);
  }
  
  private class TcpThread implements Runnable {
    private PrintWriter streamOut=null;
    private BufferedReader streamIn=null;

    private Socket socket = null;
  
    public TcpThread(String IP, int port) {
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
    }

    @Override
    public void run() {
      String message;
      
      while(TcpThread)
      {
        try {
          // jinak precteme, co server odpovedel a vypiseme 
          message = streamIn.readLine();
          System.out.println("Message: " + message);
          
          for (int i=0; i<message.length(); i++) {
            fifo.add(new Integer (message.charAt(i)));
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
    
    public void sendData(String data) {
      // takhle se posilaji data serveru
      streamOut.println(data);
    }
    
    public void close() {
      TcpThread=false;
    }
  }
}
