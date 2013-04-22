/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.awt.ComponentOrientation;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Jan
 */
public class BridgeAppCode {
    private TcpIp conn = null;
    private App app;
    private String configFile="config.sys";
    private int connDelay;
    private Timer connTimer=null;
    
    //language map
    private Map<String, String> langValues;
    //element status map
    private Map<String, Integer> elementStat;
    
    private Xml xml;
    private Xml xmlLang;

    public BridgeAppCode(App app) { //init
        System.out.println("create bridge");
        
        xml=new Xml(configFile);
        langValues = new HashMap<>();
        
        elementStat = new HashMap<>();
        initElementStat();
         
        this.app=app;
        
        loadConfig();
        loadLanguageValues();
    }
    public App getAppRef() {
        return app;
    }
    public TcpIp getTcpIpRef() {
        return conn;
    }
    public Xml getXmlLang() {
        return xmlLang;
    }
    public void connButton() {
        if (conn==null || elementStat.get("conn")==0) {
            conn=new TcpIp(app.getIP(), app.getPort(), this);
            
            connDelay=conn.getConDelay()/1000;
            setConnButon(langValues.get("Connecting")+" ("+ (connDelay+1) +")", -1);
            
            connTimer = new Timer();
            connTimer.scheduleAtFixedRate(new TimerTask() {
                
                @Override
                public void run() {
                  setConnButon(langValues.get("Connecting")+" ("+ (connDelay--) +")", -1);
                  
                  if (connDelay==0) {
                      this.cancel();
                  }
                }
            }, 0, 1000);
        }
        else if (elementStat.get("conn")==-1) {                
            conn.close();
            conn=null;
            
            setConnButon(langValues.get("Connect"), 0);
        }
        else {
            conn.close();
            conn=null;
            setConnButon(langValues.get("Connect"), 0);  
        }
        System.out.println("connButton");
    }
    public void stopConnTimer() {
        if (connTimer==null) return;
        connTimer.cancel();
    }
    synchronized public void setConnButon(String text, int statutConn) {
        app.conn.setText(text);
        elementStat.put("conn", statutConn);
    }
    public void saveSettings() {
        System.out.println("save");
        xml.saveConfigXml(this);
        
        if (!app.getLang().equals(xmlLang.getLanguageValue("Lang", "LanguageName"))) {
            System.out.println("reInit");
            
            /*
            app.setVisible(false);
            xml=new Xml(configFile);
            langValues = new HashMap<>();
            

            elementStat = new HashMap<>();
            
            app.repaint();
            app.revalidate();
            
            initElementStat();
            
            loadConfig();
            loadLanguageValues();
            app.setLangValues(langValues);
            
            app.reInitComponents();
            
            
            app.setVisible(true);
            */
            app.dispose();
            app=new App();
        }
    }
    public void getPhoto() {
        System.out.println("photo");
        conn.send(new String(new char[] {0x56, 0, 0x36, 0x01, 0}));
    }
    public void rCam() {
        System.out.println("rCam");
        conn.send(new String(new char[] {0x56, 0, 0x26, 0}));
    }
    private void loadConfig() {
        if (!xml.Load()) return;
        
        //load IP
        int IPsCount;
        try {
            langValues.put("Language", xml.getValueThrows("Config", "Language"));
        } catch (XPathExpressionException ex) {
            langValues.put("Language", "en");
        }
        
        try {
            xmlLang = new Xml(xml.getValueThrows("Config", "LanguageFile"), langValues.get("Language"));
        } catch (XPathExpressionException ex) {
            File is = new File("language.xml");
            if (is.exists()) xmlLang = new Xml("language.xml", langValues.get("Language"));
        }
        
        if (xmlLang!=null) xmlLang.Load();
        
        try {
            IPsCount = Integer.parseInt(xml.getValueThrows("Config", "IpCount"));
        } catch (XPathExpressionException ex) {
            System.out.println("IPscount not loaded");
            IPsCount=0;
        }

        for (int i=1; i<=IPsCount; i++) {
            try {
                String IP = xml.getValueThrows("Config", "IP_"+i);
                app.getIpModul().addElement(IP);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(BridgeAppCode.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        app.setLastIp(xml.getValue("Config", "LastIp"));
        
        //load port          
        app.setPort(xml.getValue("Config", "Port"));
    }
    private void initElementStat() {
        elementStat.put("conn", 0);
    }
    private void loadLanguageValues() {
        if (xmlLang==null) return;
        
        langValues.put("LanguageName", xmlLang.getLanguageValue("Lang", "LanguageName"));
        
        langValues.put("mainLabel", xmlLang.getLanguageValue("main", "CardName"));
        langValues.put("Connect", xmlLang.getLanguageValue("main", "Connect"));
        langValues.put("Connecting", xmlLang.getLanguageValue("main", "Connecting"));
        langValues.put("Disconnect", xmlLang.getLanguageValue("main", "Disconnect"));
        
        langValues.put("ConnectSettings", xmlLang.getLanguageValue("settings", "ConnectSettings"));
        langValues.put("IpAddress", xmlLang.getLanguageValue("settings", "IpAddress"));
        langValues.put("Port", xmlLang.getLanguageValue("settings", "Port"));
        langValues.put("WifiNetwork", xmlLang.getLanguageValue("settings", "WifiNetwork"));
        langValues.put("WifiPassword", xmlLang.getLanguageValue("settings", "WifiPassword"));
        langValues.put("Language", xmlLang.getLanguageValue("settings", "Language"));
        langValues.put("Save", xmlLang.getLanguageValue("settings", "Save"));
        
        try {
            String langs = xmlLang.getByKey("/localizableStrings/group[@id='Lang']/string[@id='LanguageName']");
            String[] langsArr = langs.split("\n");
            for (int i = 1; i < langsArr.length-1; i++) {
                String lang = langsArr[i].trim();
                app.getLangModul().addElement(lang);
            }
            app.setLang(langValues.get("LanguageName"));
        } catch (XPathExpressionException ex) {
            Logger.getLogger(BridgeAppCode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public Map<String, String> getLangValues() {
        return langValues;
    }
    public void initProcessDataThread() {
        Thread processDataThred=new Thread(new ProcessData(this));
        processDataThred.setDaemon(true);

        processDataThred.start();
    }
}
