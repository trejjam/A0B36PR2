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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;



/**
 *
 * @author Jan
 */
public class Xml {
    private String name;
    
    private XPath xpath;
    private Document xmlDoc;
    private String language;
    
    public Xml(String name) {
       this.name=name; 
    }
    
    public Xml(String name, String language) {
        this(name);
        this.language=language;
    }
    
    public void setLanguage(String language) {
        this.language=language;
    }
    
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
    
    public String getByKey(String key) throws XPathExpressionException {
        return (String) getByKey(key, XPathConstants.STRING);
    }
    
    public Object getByKey(String key, QName retType) throws XPathExpressionException {
        return xpath.evaluate(key, xmlDoc, retType);
    }
    
    public String getValueThrows(String group, String text) throws XPathExpressionException {
        return (String) getByKey("/localizableStrings/group[@id='"+group+"']/text[@id='"+text+"']");
    }
    
    public String getValue(String group, String text) {
        try {
            return getValueThrows(group, text);
        } catch (XPathExpressionException ex) {
            System.out.println("error load XML settings");
            return "";
        }
    }
    
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
            return (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='"+language+"']");
            //return value;
        } catch (XPathExpressionException ex) {
            System.out.println("error load XML settings");
            try {
                return (String)getByKey("/localizableStrings/group[@id='"+group+"']/string[@id='"+string+"']/text[@lang='"+"en"+"']");
            } catch (XPathExpressionException ex1) {
                return "";
            }
        }
    }
}
