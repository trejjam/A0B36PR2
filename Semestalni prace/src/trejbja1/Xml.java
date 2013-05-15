/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Čtení a zápis do XML souborů
 * @author Jan
 */
public class Xml {
    private String name;
    
    private XPath xpath;
    private Document xmlDoc;
    private String language;
    
    //make XML
    private Document xmlCreate;
    private Element rootElementCreate;
    
    /**
     * Konstruktor pro čtení konfiguračního XML
     * Konstruktor pro čtení jazykového XML - při pozdějším doplnění jazyku
     * @param name 
     */
    public Xml(String name) {
       this.name=name; 
    }
    /**
     * Konstruktor pro čtení jazykového XML
     * @param name
     * @param language 
     */
    public Xml(String name, String language) {
        this(name);
        this.language=language;
    }
    /**
     * Nastavení jazyku
     * @param language 
     */
    public void setLanguage(String language) {
        this.language=language;
    }
    /**
     * Načtení souboru
     * @return 
     */
    public boolean Load() {
        try {
            privateLoad();
            return true;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            //v budoucnu createXml();
            Logger.getLogger(Xml.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    /**
     * Vlastní načtení souboru
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    private void privateLoad() throws ParserConfigurationException, SAXException, IOException {
        File is = new File(name);// getClass().getResourceAsStream(name);
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = xmlFactory.newDocumentBuilder();
        //docBuilder.
        xmlDoc = docBuilder.parse(is);
        //System.out.println();
        XPathFactory xpathFact = XPathFactory.newInstance();
        xpath = xpathFact.newXPath();
    }
    /**
     * Získání hodnoty podle XML řetězce
     * @param key
     * @return
     * @throws XPathExpressionException 
     */
    public String getByKey(String key) throws XPathExpressionException {
        return (String) getByKey(key, XPathConstants.STRING);
    }
    /**
     * Získání hodnoty podle XML řetězce
     * Volba návratového typu
     * @param key
     * @param retType
     * @return
     * @throws XPathExpressionException 
     */
    public Object getByKey(String key, QName retType) throws XPathExpressionException {
        return xpath.evaluate(key, xmlDoc, retType);
    }
    /**
     * Získání reference na xpath
     * @return 
     */
    public XPath getXpath() {
        return xpath;
    }
    /**
     * Získání reference na xmlDoc
     * @return 
     */
    public Document getDocument() {
        return xmlDoc;
    }
    /**
     * Získání hodnoty podle skupiny a klíče
     * bez ošetření vyjímek
     * @param group
     * @param text
     * @return
     * @throws XPathExpressionException 
     */
    public String getValueThrows(String group, String text) throws XPathExpressionException {
        return (String) getByKey("/localizableStrings/group[@id='"+group+"']/text[@id='"+text+"']");
    }
    /**
     * Získání hodnoty podle skupiny a klíče
     * s ošetření vyjímek
     * @param group
     * @param text
     * @return 
     */
    public String getValue(String group, String text) {
        try {
            return getValueThrows(group, text);
        } catch (XPathExpressionException ex) {
            System.out.println("error load XML settings");
            return "";
        }
    }
    /**
     * Získání jazykových hodnot podle skupiny a klíče
     * @param group
     * @param string
     * @return 
     */
    public String getLanguageValue(String group, String string) {
        if (language.equals("")) {
            System.out.println("not defined language");
            try {
                return (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='"+"en"+"']");
            } catch (XPathExpressionException ex) {
                System.out.println("cannot load default lang value");
                return "";
            }
        }
        try {
            String value=  (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='"+language+"']");
            if (value.equals("")) {
                value=  (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='en']");
            }
            return value;
        } catch (XPathExpressionException ex) {
            System.out.println("error load XML settings");
            try {
                return (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='"+"en"+"']");
            } catch (XPathExpressionException ex1) {
                return "";
            }
        }
    }
    
    /**
     * Vytvoření XML elementu
     * @param element
     * @param value
     * @return 
     */
    private Element createElement(String element, String value) {
        return createElement(element, "", "", value);
    }
    /**
     * Vytvoření XML elementu s atributem, hodnotou atributu bez hodnoty obsahu - skupina
     * @param element
     * @param attribute
     * @param attValue
     * @return 
     */
    private Element createElement(String element, String attribute, String attValue) {
        return createElement(element, attribute, attValue, "");
    }
    /**
     * Vytvoření XML elementu s atributem, hodnotou atributu a hodnotou obsahu
     * @param element
     * @param attribute
     * @param attValue
     * @param value
     * @return 
     */
    private Element createElement(String element, String attribute, String attValue, String value) {
        Element elem = xmlCreate.createElement(element);
        if (!attribute.equals("")) {
            Attr attr = xmlCreate.createAttribute(attribute);
            attr.setValue(attValue);
            elem.setAttributeNode(attr);
        }
        if (!value.equals("")) {
            elem.appendChild(xmlCreate.createTextNode(value));
        }
        return elem;
    }
    
    /**
     * Vytvoření XML objektu
     */
    private void createXml() {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try { 
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            xmlCreate = docBuilder.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Xml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Přidání hlavičky
     */
    private void addRoot() {
        rootElementCreate = xmlCreate.createElement("localizableStrings");
	xmlCreate.appendChild(rootElementCreate);
        
    }
    /**
     * Přidání meta hodnot
     */
    private void addMeta() {
        Element meta = xmlCreate.createElement("meta");
        rootElementCreate.appendChild(meta);
        
        meta.appendChild(createElement("appName", "Lboat"));
        meta.appendChild(createElement("version", "1.0"));
        meta.appendChild(createElement("author", "TREMI s.r.o. - Jan Trejbal"));
        meta.appendChild(createElement("language", "multilingual"));
        meta.appendChild(createElement("grouping", "multi"));    
    }
    /**
     * Přidání konfiguračních hodnot
     * @param xmlLang
     * @param bridge 
     */
    private void addConfig(Xml xmlLang, BridgeAppCode bridge) {
        if (this==xmlLang) return;
        
        Element group = createElement("group" ,"id" , "Config");
        rootElementCreate.appendChild(group);
        
        group.appendChild(createElement("text", "id", "LanguageFile", getValue("Config", "LanguageFile")));
        try {
            group.appendChild(createElement("text", "id", "Language", ((Node)xmlLang.getXpath().evaluate("/localizableStrings/group[@id='Lang']/string/*[contains(text(), '"+bridge.getAppRef().getLang()+"')]", xmlLang.getDocument(), XPathConstants.NODE)).getAttributes().item(0).getNodeValue()));
        } catch (XPathExpressionException ex) {
            group.appendChild(createElement("text", "id", "Language", "en"));
        }
        group.appendChild(createElement("text", "id", "SetNetwork", getValue("Config", "SetNetwork")));
        group.appendChild(createElement("text", "id", "Network", getValue("Config", "Network")));
        group.appendChild(createElement("text", "id", "NetworkPassword", getValue("Config", "NetworkPassword")));
        group.appendChild(createElement("text", "id", "PasswordHashCode", getValue("Config", "PasswordHashCode")));
        
        if (bridge.getAppRef().getAutoPhotos()) {
            group.appendChild(createElement("text", "id", "AutoPhotos", "True"));
        }
        else {
            group.appendChild(createElement("text", "id", "AutoPhotos", "False"));
        }
        group.appendChild(createElement("text", "id", "AutoPhotosTime", bridge.getAppRef().getAutoPhotosTime()));
        
        group.appendChild(createElement("text", "id", "Port", ""+bridge.getAppRef().getPort()));
        
        group.appendChild(createElement("text", "id", "LastIp", bridge.getAppRef().getIP()));
        
        try {
            int IPsCount = Integer.parseInt(getValueThrows("Config", "IpCount"));
            for (int i=1; i<=IPsCount; i++) {
                try {
                    String IP = getValueThrows("Config", "IP_"+i);
                    group.appendChild(createElement("text", "id", "IP_"+i, IP));
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(BridgeAppCode.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (XPathExpressionException ex) {
            System.out.println("IPscount not loaded");
        }
        group.appendChild(createElement("text", "id", "IpCount", getValue("Config", "IpCount")));
    }
    /**
     * Uložení XML do souboru
     */
    private void saveXml() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xmlCreate);
            
            StreamResult result = new StreamResult(new File(name));
            //StreamResult result =  new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Xml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Xml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Uložení konfigurace
     * @param bridge 
     */
    public void saveConfigXml(BridgeAppCode bridge) {
        createXml();
        addRoot();
        addMeta();
        addConfig(bridge.getXmlLang(), bridge);
        saveXml();
    }
}
