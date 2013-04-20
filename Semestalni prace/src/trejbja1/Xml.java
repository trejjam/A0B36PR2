/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/**
 *
 * @author Jan
 */
public class Xml {
    private String name;
    
    XPath xpath;
    Document xmlDoc;
    
    public Xml(String name) {
       this.name=name; 
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
    
    public String getKey(String key) throws XPathExpressionException {
        return (String) xpath.evaluate(key, xmlDoc, XPathConstants.STRING);
    }
}
