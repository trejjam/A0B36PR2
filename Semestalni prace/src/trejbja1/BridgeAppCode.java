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
  
  public BridgeAppCode() { //init
    System.out.println("create bridge");
  }
  public void connButton() {
    if (conn==null) {
      conn=new TcpIp("localhost", 21);
    }
    System.out.println("connButton");
  }
  public void saveSettings() {
    System.out.println("save");
    conn.send("a");
  }
}
