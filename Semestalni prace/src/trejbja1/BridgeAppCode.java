/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

/**
 *
 * @author Jan
 */
public class BridgeAppCode {
  private TcpIp conn= null;
  private App app;
  
  public BridgeAppCode(App app) { //init
    System.out.println("create bridge");
    this.app=app;
  }
  public void connButton() {
    if (conn==null) {
      conn=new TcpIp("192.168.1.3", 5003);
      app.conn.setText("Odpojit");
    }
    else {
      conn.close();
      conn=null;
      app.conn.setText("Připojit");  
    }
    System.out.println("connButton");
  }
  
  public void saveSettings() {
    System.out.println("save");
    //conn.send("a");
  }
  
  public void getPhoto() {
    System.out.println("photo");
    conn.send(new String(new char[] {0x56, 0, 0x36, 0x01, 0}));
    //conn.send("a");
  }
}
