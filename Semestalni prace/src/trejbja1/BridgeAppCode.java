/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Jan
 */
public class BridgeAppCode {
    private TcpIp conn= null;
    private App app;
    private String configFile="config.sys";
    
    Xml xml=new Xml(configFile);

    public BridgeAppCode(App app) { //init
        System.out.println("create bridge");
        this.app=app;
        
        loadConfig();
    }

    public void connButton() {
        if (conn==null) {
            conn=new TcpIp(app.getIP(), app.getPort());
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
    }

    public void getPhoto() {
        System.out.println("photo");
        conn.send(new String(new char[] {0x56, 0, 0x36, 0x01, 0}));
    }
  
    private void loadConfig() {
        if (!xml.Load()) return;
        
        //load IP
        int IPsCount;
        
        try {
            IPsCount = Integer.parseInt(xml.getKey("/localizableStrings/group[@id='Config']/text[@id='IpCount']"));
            ComboBoxModel model = app.ipAddress.getModel();
            
            for (int i=1; i<=IPsCount; i++) {
                app.getIpModul().addElement(xml.getKey("/localizableStrings/group[@id='Config']/text[@id='IP_"+i+"']"));
            }
            
            app.setLastIp(xml.getKey("/localizableStrings/group[@id='Config']/text[@id='LastIp']"));
        } catch (XPathExpressionException ex) {
            Logger.getLogger(SemestalniPrace.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //load port
        try {            
            app.setPort(xml.getKey("/localizableStrings/group[@id='Config']/text[@id='Port']"));
            //app.ipAddress.addItem(IPs);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(SemestalniPrace.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
