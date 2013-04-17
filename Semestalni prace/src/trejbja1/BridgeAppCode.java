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
public class BridgeAppCode {
  TcpIp conn= null;
  App app;
  
  public BridgeAppCode(App app) { //init
    System.out.println("create bridge");
    this.app=app;
  }
  public void connButton() {
    if (conn==null) {
      conn=new TcpIp("localhost", 21);
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
    conn.send("a");
  }
}
