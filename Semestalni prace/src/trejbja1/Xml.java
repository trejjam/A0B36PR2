/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

/**
 *
 * @author Jan
 */
public class Xml {
    private String name;
    private XMLReader parser;
    
    public Xml(String name) {
       this.name=name; 
    } 
    
    public void Load() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            SAXParser saxLevel1 = spf.newSAXParser();
            parser = saxLevel1.getXMLReader();

            //parser.setErrorHandler(new ChybyZjisteneParserem());
            parser.setContentHandler(new DefaultHandler());   
            parser.parse(name);
            
            System.out.println(name + " precten bez chyb");
        }
        catch(ParserConfigurationException | SAXException | IOException e){
            e.printStackTrace();
        }
        
        try {
            System.out.println(parser.getProperty("localizableStrings").toString());
            //ContentHandler contentHandler = parser.getContentHandler();
            
            
            //System.out.println(contentHandler);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            Logger.getLogger(Xml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
