/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;

/**
 * Třída umožňující komunikaci mezi jednotlivými třídami
 * @author Jan
 */
public class BridgeAppCode {
    private TcpIp conn = null;
    private App app;
    private ProcessData processData=null;
    private String configFile="config.sys";
    private int connDelay;
    private Timer connTimer=null;
    
    //language map
    private Map<String, String> langValues;
    //element status map
    private Map<String, Integer> elementStat;
    
    private Xml xml;
    private Xml xmlLang;

    /**
     * Vytvoření mostu s referencí na GUI
     * @param app 
     */
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
    /**
     * Reference na GUI
     * @return 
     */
    public App getAppRef() {
        return app;
    }
    /**
     * Reference na komunikační třídu
     * @return 
     */
    public TcpIp getTcpIpRef() {
        return conn;
    }
    /**
     * Reference na jazykový XML soubor
     * @return 
     */
    public Xml getXmlLang() {
        return xmlLang;
    }
    /**
     * Metoda pro obsluhu tlačítka obsluhujícího připojení
     */
    public void connButton() {
        if (conn==null || elementStat.get("conn")==0) {
            conn=new TcpIp(app.getIP(), app.getPort(), this);
            
            connDelay=conn.getConDelay()/1000;
            setConnButon(langValues.get("Connecting")+" ("+ (connDelay+1) +")", -1);
            
            connTimer = new Timer("connectTimer");
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
    /**
     * Zastavení časovače deinkremetujícího zbývající čas pro připojení
     */
    public void stopConnTimer() {
        if (connTimer==null) return;
        connTimer.cancel();
    }
    /**
     * Nastavení textu tlačítka obsluhujícího připojení
     * změna jeho číselného stavů, z důvodu multijazykové podpory
     * @param text
     * @param statutConn 
     */
    synchronized public void setConnButon(String text, int statutConn) {
        app.setTextConn(text);
        elementStat.put("conn", statutConn);
    }
    /**
     * Uložení configurace do XML souboru
     */
    public void saveSettings() {
        System.out.println("save");
        xml.saveConfigXml(this);
        
        if (!app.getLang().equals(xmlLang.getLanguageValue("Lang", "LanguageName"))) {
            System.out.println("reInit");
         
            app.dispose();
            app=new App();
        }
    }
    /**
     * Požadavek na stáhnutí fotografie
     */
    public void getPhoto() {
        System.out.println("photo");
        processData.sendToCam(ProcessData.camDo.takeFoto);
    }
    /**
     * Reset kamery
     */
    public void rCam() {
        System.out.println("rCam");
        processData.sendToCam(ProcessData.camDo.reset); //reset cam
    }
    /**
     * Načtení konfigurace z XML souboru
     */
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
        
        String photos=xml.getValue("Config", "AutoPhotos");
        if (photos.equals("True")) {
            app.setAutoPhotos(true);
        }
        else {
            app.setAutoPhotos(false);
        }
        app.setAutoPhotosTime(xml.getValue("Config", "AutoPhotosTime"));
    }
    /**
     * Inicializace statutu složitých elementů
     */
    private void initElementStat() {
        elementStat.put("conn", 0);
    }
    /**
     * Načtení jazykových hodnot
     */
    private void loadLanguageValues() {
        if (xmlLang==null) return;
        
        langValues.put("LanguageName", xmlLang.getLanguageValue("Lang", "LanguageName"));
        
        langValues.put("mainLabel", xmlLang.getLanguageValue("main", "CardName"));
        langValues.put("Connect", xmlLang.getLanguageValue("main", "Connect"));
        langValues.put("Connecting", xmlLang.getLanguageValue("main", "Connecting"));
        langValues.put("Disconnect", xmlLang.getLanguageValue("main", "Disconnect"));
        
        langValues.put("TakePhoto", xmlLang.getLanguageValue("main", "TakePhoto"));
        
        langValues.put("Rudder", xmlLang.getLanguageValue("main", "Rudder"));
        langValues.put("Sail1", xmlLang.getLanguageValue("main", "Sail1"));
        langValues.put("Sail2", xmlLang.getLanguageValue("main", "Sail2"));
        
        langValues.put("ConnectSettings", xmlLang.getLanguageValue("settings", "ConnectSettings"));
        langValues.put("IpAddress", xmlLang.getLanguageValue("settings", "IpAddress"));
        langValues.put("Port", xmlLang.getLanguageValue("settings", "Port"));
        langValues.put("WifiNetwork", xmlLang.getLanguageValue("settings", "WifiNetwork"));
        langValues.put("WifiPassword", xmlLang.getLanguageValue("settings", "WifiPassword"));
        langValues.put("Language", xmlLang.getLanguageValue("settings", "Language"));
        langValues.put("Save", xmlLang.getLanguageValue("settings", "Save"));
        langValues.put("AutoPhotos", xmlLang.getLanguageValue("settings", "AutoPhotos"));
        langValues.put("AutoPhotosTime", xmlLang.getLanguageValue("settings", "AutoPhotosTime"));
        
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
    /**
     * Získání reference na mapu jazykových hodnot
     * @return 
     */
    public Map<String, String> getLangValues() {
        return langValues;
    }
    /**
     * Inicializace objektu zpracovávajícího přijatá data
     * řídícího automatické reakce na ně
     */
    public void initProcessDataThread() {
        Thread processDataThread=new Thread(processData= new ProcessData(this));
        processDataThread.setName("ProcessData");
        processDataThread.setDaemon(true);

        processDataThread.start();
    }
    /**
     * Získání reference na objekt zpracovávající data
     * @return 
     */
    public ProcessData getProcessData() {
        return processData;
    }
}
