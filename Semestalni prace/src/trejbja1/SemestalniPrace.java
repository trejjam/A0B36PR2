/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trejbja1;

/**
 *
 * @author Jan
 */
public class SemestalniPrace {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
      TcpIp conn= new TcpIp("");
      
      
      
      App okno = new App();
      okno.init();
        
/*      try {
        //test win cmd
          // Execute command
          String command = "cmd /c start cmd.exe";
          Process child = Runtime.getRuntime().exec(command);

          // Get output stream to write from it
          OutputStream out = child.getOutputStream();

          out.write("cd C:/ /r/n".getBytes());
          out.flush();
          out.write("dir /r/n".getBytes());
          out.close();
      } catch (IOException e) {
      }*/

    }
    
    /*
     * Connect to Wifi Win Vista - 8
     * http://www.hanselman.com/blog/HowToConnectToAWirelessWIFINetworkFromTheCommandLineInWindows7.aspx
     *
     * CMD
     * http://stackoverflow.com/questions/4157303/how-to-execute-cmd-commands-via-java
     * 
     * LookAndFeel
     * http://docs.oracle.com/javase/1.5.0/docs/api/javax/swing/plaf/basic/BasicLookAndFeel.html
     */
}
