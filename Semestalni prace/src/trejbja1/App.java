package trejbja1;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Vytvoření a správa okna
 * @author Jan
 */
public class App extends javax.swing.JFrame {
    private BridgeAppCode bridge;
    private DefaultComboBoxModel IPmodel = new javax.swing.DefaultComboBoxModel(new String[] {});
    private DefaultComboBoxModel LangModel = new javax.swing.DefaultComboBoxModel(new String[] {});
    private ImageIcon iconCompass = new javax.swing.ImageIcon(getClass().getResource("/resources/compass.jpg"));
    private String sLastIp="";
    private String sPort="";
    private String sLang="";
    private Map<String, String> langValues;
    private float angleImg=0;
    private boolean autoPhotos=false;
    private String autoPhotosTime="1500";

    /**
     * Inicializace {@link BridgeAppCode}
     * Inicializace komponent
     * Zobrazení okna
     */
    public App() {
        bridgeInit();
        initComponents();
        autoPhotosCheckStateChanged(null); //zbarvení nepovolených komponent
        setVisible(true);
    }
    /**
     * Nastavení jazyků
     * @param langValues 
     */
    public void setLangValues(Map<String, String> langValues) {
        this.langValues = langValues;
    }
    /**
     * Nastavení poslední IP do GUI
     * @param sLastIp 
     */
    public void setLastIp(String sLastIp) {
        this.sLastIp=sLastIp;
    }
    /**
     * Nastavení portu do GUI
     * @param sPort 
     */
    public void setPort(String sPort) {
        this.sPort=sPort;
    }
    /**
     * Nastavení uloženého jazyku do GUI
     * @param sLang 
     */
    public void setLang(String sLang) {
        this.sLang=sLang;
    }
    /**
     * 
     * @param IPmodel 
     */
    public void setIPmodul(DefaultComboBoxModel IPmodel) {
        this.IPmodel=IPmodel;
    }
    /**
     * Získání reference na combo box model s IP adresami 
     * @return IPComboBoxModel
     */
    public DefaultComboBoxModel getIpModul() {
        return IPmodel;
    }
    /**
     * Získání reference na combo box model s jazyky
     * @return LanguageComboBoxModel
     */
    public DefaultComboBoxModel getLangModul() {
        return LangModel;
    }
    /**
     * Získání vybrané IP adresy
     */
    public String getIP() {
        return ipAddress.getSelectedItem().toString();
    }
    /**
     * Získání vybraného portu
     */
    public int getPort() {
        return Integer.parseInt(port.getText());
    }
    /**
     * Získání vybraného jazyka
     */
    public String getLang() {
        return langBox.getSelectedItem().toString();
    }
    /**
     * Nastavení automatického stahování fotek před zobrazením GUI
     * @param photos 
     */
    public void setAutoPhotos(boolean photos) {
        this.autoPhotos=photos;
    }
    /**
     * Zjištění zda mají být automaticky stahovány fotky
     */
    public boolean getAutoPhotos() {
        return autoPhotosCheck.isSelected();
    }
    /**
     * Nastavení intervalu pro automatické stahování před zobrazením GUI
     * @param photos 
     */
    public void setAutoPhotosTime(String photos) {
        this.autoPhotosTime=photos;
    }
    /**
     * Zjištění intervalu pro automatické stahování
     */
    public String getAutoPhotosTime() {
        return photosTime.getText();
    }
    /**
     * Vytvoření mostu mezi třídami
     */
    private void bridgeInit() {
        if (bridge==null) {
            bridge = new BridgeAppCode(this);
            langValues=bridge.getLangValues();
        }
    }
    private Image setIcon() {
        ImageIcon img = new ImageIcon(getClass().getResource("/resources/ship.png"));

        return img.getImage();
    }
    /**
     * Nastavení textu tlačítka vyvolávajícího změnu spojení
     * @param text 
     */
    public void setTextConn(String text) {
        conn.setText(text);
    }
    /**
     * Znovu načtení komponent GUI
     */
    public void reInitComponents() {
        IPmodel.removeAllElements();
        LangModel.removeAllElements();
        initComponents();
    }
    /**
     * Nastavení úhlu kompasu
     * @param angle 
     */
    public void compasAngle(float angle) {
        angleImg=angle;
        ((Compass)jCompass).setAngle(angle);
    }
    /**
     * Nastavení jména souboru fotky pro zobrazení v GUI
     * @param file 
     */
    public void setPhoto(String file) {
        ((Photo)jImage).setImage(file);
    }
    /**
     * Třída obsluhující natáčení kompasové růžice
     */
    private class Compass extends JPanel {
        private float angle=0;
        
        /**
         * Nastavení úhlu natočení kompasové růžice
         * @param angle 
         */
        public void setAngle(float angle) {
            this.angle=angle;
            this.repaint();
        }
        @Override
        public void paint(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, 140, 140);
            
            Image source = iconCompass.getImage();
            int w = source.getWidth(null);
            int h = source.getHeight(null);
            Graphics2D g2a = (Graphics2D)g;
            AffineTransform at = g2a.getTransform();
            at.rotate(Math.toRadians(angle), w / 2, h / 2);
            g2a.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2a.setTransform(at);
            g2a.drawImage(source, 0, 0, null);
            g2a.dispose();
        }
    }
    /**
     * Třída obsluhující zobrazování Fotografií
     */
    class Photo extends JPanel {
        private ImageIcon FNF = new javax.swing.ImageIcon(getClass().getResource("/resources/FNF.png"));
        private ImageIcon FNT = new javax.swing.ImageIcon(getClass().getResource("/resources/FNT.png"));
        private Image img = FNT.getImage();
        
        /**
         * Nastavení jména fotografie pro zobrazení
         * @param image 
         */
        public void setImage(String image) {
            try {
                img = ImageIO.read(new File("photos/"+image));
            } catch (IOException e) {
                img = FNF.getImage();
                System.out.println("Photos not found!");
            }
            this.repaint();
        }
        @Override
        public void paint(Graphics g) {
            Graphics2D g2a = (Graphics2D)g;
            g2a.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2a.drawImage(img, 0, 0, null);
            g2a.setColor(Color.gray);
            g2a.drawLine(0, 0, 319, 0);
            g2a.drawLine(319, 0, 319, 239);
            g2a.drawLine(0, 239, 319, 239);
            g2a.drawLine(0, 0, 0, 239);
            g2a.dispose();
        }
    }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        conn = new javax.swing.JButton();
        jCompass = new Compass();
        jLabel4 = new javax.swing.JLabel();
        jRudder = new javax.swing.JSlider();
        jSail1 = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSail2 = new javax.swing.JSlider();
        jImage = new Photo();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        ipAddress = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        port = new javax.swing.JFormattedTextField();
        save = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        langBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        autoPhotosCheck = new javax.swing.JCheckBox();
        photosTime = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setIconImage(setIcon());

        jTabbedPane1.setName("LBoat"); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(0, 496));

        conn.setText(langValues.get("Connect"));
        conn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jCompassLayout = new javax.swing.GroupLayout(jCompass);
        jCompass.setLayout(jCompassLayout);
        jCompassLayout.setHorizontalGroup(
            jCompassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );
        jCompassLayout.setVerticalGroup(
            jCompassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
        );

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(langValues.get("Rudder"));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jRudder.setMajorTickSpacing(10);
        jRudder.setMinorTickSpacing(10);
        jRudder.setPaintTicks(true);
        jRudder.setPaintTrack(false);
        jRudder.setToolTipText("");
        jRudder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRudderStateChanged(evt);
            }
        });

        jSail1.setMajorTickSpacing(10);
        jSail1.setMinorTickSpacing(10);
        jSail1.setPaintTicks(true);
        jSail1.setPaintTrack(false);
        jSail1.setToolTipText("");
        jSail1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSail1StateChanged(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(langValues.get("Sail1"));
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(langValues.get("Sail2"));
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jSail2.setMajorTickSpacing(10);
        jSail2.setMinorTickSpacing(10);
        jSail2.setPaintTicks(true);
        jSail2.setPaintTrack(false);
        jSail2.setToolTipText("");
        jSail2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSail2StateChanged(evt);
            }
        });

        jImage.setMaximumSize(new java.awt.Dimension(320, 240));
        jImage.setMinimumSize(new java.awt.Dimension(320, 240));
        jImage.setName(""); // NOI18N
        jImage.setPreferredSize(new java.awt.Dimension(320, 240));

        javax.swing.GroupLayout jImageLayout = new javax.swing.GroupLayout(jImage);
        jImage.setLayout(jImageLayout);
        jImageLayout.setHorizontalGroup(
            jImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );
        jImageLayout.setVerticalGroup(
            jImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
        );

        jButton1.setText(langValues.get("TakePhoto"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(conn)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCompass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jRudder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSail1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSail2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(500, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(conn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCompass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRudder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSail2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        conn.getAccessibleContext().setAccessibleName("conn");

        jTabbedPane1.addTab(langValues.get("mainLabel"), jPanel1);
        jPanel1.getAccessibleContext().setAccessibleName("control");

        ipAddress.setEditable(true);
        ipAddress.setModel(IPmodel);
        ipAddress.setSelectedItem(sLastIp);
        ipAddress.setToolTipText("");

        jLabel1.setText(langValues.get("IpAddress"));

        jLabel2.setText(langValues.get("Port"));

        port.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        port.setText(sPort);

        save.setText(langValues.get("Save"));
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        jLabel3.setText(langValues.get("Language"));

        langBox.setEditable(true);
        langBox.setModel(LangModel);
        langBox.setSelectedItem(sLang);
        langBox.setToolTipText("");

        jLabel7.setText(langValues.get("AutoPhotosTime"));

        autoPhotosCheck.setSelected(autoPhotos);
        autoPhotosCheck.setText(langValues.get("AutoPhotos"));
        autoPhotosCheck.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoPhotosCheckStateChanged(evt);
            }
        });

        photosTime.setText(autoPhotosTime);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autoPhotosCheck)
                    .addComponent(save)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(langBox, 0, 1, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(ipAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(photosTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(480, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ipAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(langBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(save)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autoPhotosCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(photosTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(323, Short.MAX_VALUE))
        );

        ipAddress.getAccessibleContext().setAccessibleName("ipBox");
        save.getAccessibleContext().setAccessibleName("save");
        langBox.getAccessibleContext().setAccessibleName("LangBox");
        autoPhotosCheck.getAccessibleContext().setAccessibleName("");
        autoPhotosCheck.getAccessibleContext().setAccessibleDescription("");

        jTabbedPane1.addTab(langValues.get("ConnectSettings"), jPanel2);
        jPanel2.getAccessibleContext().setAccessibleName("settings");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
  
  private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
    // TODO add your handling code here:
    bridge.saveSettings();
  }//GEN-LAST:event_saveActionPerformed

    private void autoPhotosCheckStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoPhotosCheckStateChanged
        if (autoPhotosCheck.isSelected()) {
            jLabel7.setEnabled(true);
            photosTime.setEnabled(true);
        }
        else {
            jLabel7.setEnabled(false);
            photosTime.setEnabled(false);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_autoPhotosCheckStateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        bridge.getPhoto();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void connActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connActionPerformed
        // TODO add your handling code here:
        //bridgeInit();
        bridge.connButton();
    }//GEN-LAST:event_connActionPerformed

    private boolean sail2=false, sail1=false, rudder=false;
    private void jSail2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSail2StateChanged
        // TODO add your handling code here:
        if (!sail2) {
            sail2=true;
            Timer connTimer = new Timer("sail2Timer");
            connTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    sail2=false;
                    if (bridge.getProcessData()!=null) {
                        char value[];
                        value=new char[] {(char)jSail2.getValue()};
                        bridge.getProcessData().sendToMcu(ProcessData.mcuDo.servo3, value);
                    }
                    this.cancel();
                }
            }, 0, 200);
        }
    }//GEN-LAST:event_jSail2StateChanged

    private void jSail1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSail1StateChanged
        // TODO add your handling code here:
        if (!sail1) {
            sail1=true;
            Timer connTimer = new Timer("sail1Timer");
            connTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    sail1=false;
                    if (bridge.getProcessData()!=null) {
                        char value[];
                        value=new char[] {(char)jSail1.getValue()};
                        bridge.getProcessData().sendToMcu(ProcessData.mcuDo.servo2, value);
                    }
                    this.cancel();
                }
            }, 0, 200);
        }
    }//GEN-LAST:event_jSail1StateChanged

    private void jRudderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRudderStateChanged
        // TODO add your handling code here:
        if (!rudder) {
            rudder=true;
            Timer connTimer = new Timer("rudderTimer");
            connTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    rudder=false;
                    if (bridge.getProcessData()!=null) {
                        char value[];
                        value=new char[] {(char)jRudder.getValue()};
                        bridge.getProcessData().sendToMcu(ProcessData.mcuDo.servo1, value);
                    }
                    this.cancel();
                }
            }, 0, 200);
        }
    }//GEN-LAST:event_jRudderStateChanged
  
/*  public void redesign() {
    try {
      //UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");
      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    } 
    
    catch (ClassNotFoundException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnsupportedLookAndFeelException ex) {
      Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
    }
  }*/
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoPhotosCheck;
    private javax.swing.JButton conn;
    private javax.swing.JComboBox ipAddress;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jCompass;
    private javax.swing.JPanel jImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSlider jRudder;
    private javax.swing.JSlider jSail1;
    private javax.swing.JSlider jSail2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox langBox;
    private javax.swing.JTextField photosTime;
    private javax.swing.JFormattedTextField port;
    private javax.swing.JButton save;
    // End of variables declaration//GEN-END:variables
}
