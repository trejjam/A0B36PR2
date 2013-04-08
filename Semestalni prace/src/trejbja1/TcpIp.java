/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

/**
 *
 * @author Jan
 */
public class TcpIp {
  private String IP;
  private int port;
  private boolean sendThread=false, recieveThread=false;
  
  public TcpIp() {
    
  }
  
  private class sendThread implements Runnable {
    public sendThread() {
      
    }

    @Override
    public void run() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
}
