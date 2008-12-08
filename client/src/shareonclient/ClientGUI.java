/*
 * ClientGUI.java
 *
 * Created on 2008. november 26., 11:55
 */

package shareonclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import shareonclient.ShareOnClient.ParsedShareOn;

/**
 *
 * @author  Bandita
 */

public class ClientGUI extends javax.swing.JFrame implements ActionListener, WindowListener{
    
    private ShareOnClient ownerClient;              //ShareOn Client who owns the GUI
    private Hashtable<String, File> hSharedFiles;   //hashtable to maintain the shared files
    private Vector<String> vSharedFileNames;        //vector to maintain the names of the shared files
    private Vector<String> vSearchResults;          //vector to maintain the search results
    
    /** Creates new form ClientGUI */
    public ClientGUI(ShareOnClient ownerClientIn) {
        ownerClient = ownerClientIn;
        hSharedFiles = new Hashtable<String, File>();
        vSharedFileNames = new Vector<String>();
        vSearchResults = new Vector<String>();
        initComponents();
        jSharesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //jSearchButton.setEnabled(false);
        this.setLocation(100, 100);
        jLoginButton.addActionListener(this);
        jLogoutButton.addActionListener(this);
        jAddShareOnButton.addActionListener(this);
        jAddShareButton.addActionListener(this);
        jRemoveShareButton.addActionListener(this);
        jSearchButton.addActionListener(this);
    }
    
    //ActionListener
    public void actionPerformed(ActionEvent e)
        {
        
        //login
        if (e.getSource() == jLoginButton)
            {
            ownerClient.connectToServer();
            }
        
        //logout
        if (e.getSource() == jLogoutButton)
            {
            ownerClient.disconnectFromServer();
            }
        
        //search
        if (e.getSource() == jSearchButton)
            {
            if (ownerClient.isConnectedToServer())
                {
                vSearchResults.clear();
                String sResults = ownerClient.search(jSearchField.getText());
                if (sResults != null)
                    {
                    String[] sPeers = sResults.split("@");
                    for (int i = 0; i < sPeers.length; i++)
                        {
                        //System.out.println(sPeers[i]);
                        if (!sPeers[i].equals(ownerClient.getLocalIP()))
                            {
                            String sRTT = ownerClient.pseudoPing(sPeers[i]);
                            if (sRTT != null)
                                vSearchResults.add("RTT(ms): " + sRTT + " Peer: " + sPeers[i]);
                            }
                        }
                    //update result list
                    Comparator cDescending = Collections.reverseOrder();
                    Collections.sort(vSearchResults, cDescending);
                    Collections.reverse(vSearchResults);
                    jResultsList = new javax.swing.JList(vSearchResults);
                    jResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jResultsScrollPane.setViewportView(jResultsList);
                    }
                else
                    {
                    JOptionPane.showMessageDialog(this, "No results found!", "", JOptionPane.WARNING_MESSAGE);
                    //clear search results
                    jResultsList = new javax.swing.JList(vSearchResults);
                    jResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jResultsScrollPane.setViewportView(jResultsList);
                    }
                }
            else
                JOptionPane.showMessageDialog(this, "Please login first!", "Warning!", JOptionPane.WARNING_MESSAGE);
            }
        
        //load .shareon
        if (e.getSource() == jAddShareOnButton)
            {
            File fShareOn = ownerClient.chooseFile(true);
            if (fShareOn != null)
                {
                ParsedShareOn psPeers = ownerClient.parseShareOn(fShareOn);
                if (psPeers == null)
                    JOptionPane.showMessageDialog(this, "Invalid ShareOn syntax!", "Error!", JOptionPane.ERROR_MESSAGE);
                else
                    {
                    //update result list
                    jResultsList = new javax.swing.JList(psPeers.getDisplayableResults());
                    jResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    jResultsScrollPane.setViewportView(jResultsList);
                    }
                }
            }
        
        //add shared file
        if (e.getSource() == jAddShareButton)
            {
            boolean bMustUpdate = false;
            if (ownerClient.isConnectedToServer())
                {
                File fChosen = ownerClient.chooseFile(false);
                if (fChosen != null)
                    {
                    //share maintenance
                    if (!vSharedFileNames.contains(fChosen.getName()))
                        {  
                        boolean bAcknowledged = ownerClient.updateShares("added@" + fChosen.getName());
                        if (!bAcknowledged)
                            JOptionPane.showMessageDialog(this, "Unable to send update to the server!", "Error!", JOptionPane.ERROR_MESSAGE);
                        else
                            {
                            hSharedFiles.put(fChosen.getName(), fChosen);
                            vSharedFileNames.add(fChosen.getName());
                            bMustUpdate = true;
                            }
                        }
                    else
                        JOptionPane.showMessageDialog(this, "File already added!", "Error!", JOptionPane.ERROR_MESSAGE);
                    if (bMustUpdate)
                        {
                        //update share list
                        jSharesList = new javax.swing.JList(vSharedFileNames);
                        jSharesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        jSharesScrollPane.setViewportView(jSharesList);
                        }
                    }
                }
            else
                JOptionPane.showMessageDialog(this, "Please login first!", "Warning!", JOptionPane.WARNING_MESSAGE);
            }
        
        //remove shared file
        if (e.getSource() == jRemoveShareButton)
            {
            boolean bMustUpdate = false;
            int iIndex = jSharesList.getSelectedIndex();
            //share maintenance
            if (iIndex != -1)
                {
                boolean bAcknowledged = ownerClient.updateShares("removed@" + (hSharedFiles.get(vSharedFileNames.elementAt(iIndex))).getName());
                if (!bAcknowledged)
                    JOptionPane.showMessageDialog(this, "Unable to send update to the server!", "Error!", JOptionPane.ERROR_MESSAGE);
                else
                    {
                    hSharedFiles.remove(vSharedFileNames.elementAt(iIndex));
                    vSharedFileNames.remove(iIndex);
                    bMustUpdate = true;
                    }
                }
            if (bMustUpdate)
                {
                //update share list
                jSharesList = new javax.swing.JList(vSharedFileNames);
                jSharesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                jSharesScrollPane.setViewportView(jSharesList);
                }
            }
        }
    
    //set text to the status label
    public void setStatusText(String sStatusToSet)
        {
        jStatusLabel.setText("Status: " + sStatusToSet);
        }
    
    //WindowListener
    public void windowClosed(WindowEvent e)
        {
        ownerClient.exit();
        this.dispose();
        }
    
    public void windowClosing(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jResultsPanel = new javax.swing.JPanel();
        jResultsScrollPane = new javax.swing.JScrollPane();
        jResultsList = new javax.swing.JList();
        jDownloadButton = new javax.swing.JButton();
        jSharePanel = new javax.swing.JPanel();
        jSharesScrollPane = new javax.swing.JScrollPane();
        jSharesList = new javax.swing.JList();
        jShareControlPanel = new javax.swing.JPanel();
        jRemoveShareButton = new javax.swing.JButton();
        jAddShareButton = new javax.swing.JButton();
        jHeadPanel = new javax.swing.JPanel();
        jServerPanel = new javax.swing.JPanel();
        jStatusLabel = new javax.swing.JLabel();
        jLoginButton = new javax.swing.JButton();
        jLogoutButton = new javax.swing.JButton();
        jSearchButton = new javax.swing.JButton();
        jSearchField = new javax.swing.JTextField();
        jClientPanel = new javax.swing.JPanel();
        jAddShareOnButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        setMinimumSize(new java.awt.Dimension(600, 600));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jResultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Peers"));
        jResultsPanel.setLayout(new java.awt.GridBagLayout());

        jResultsScrollPane.setViewportView(jResultsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jResultsPanel.add(jResultsScrollPane, gridBagConstraints);

        jDownloadButton.setText("Download");
        jDownloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDownloadButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jResultsPanel.add(jDownloadButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jResultsPanel, gridBagConstraints);

        jSharePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("My shares"));
        jSharePanel.setLayout(new java.awt.GridBagLayout());

        jSharesScrollPane.setViewportView(jSharesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jSharePanel.add(jSharesScrollPane, gridBagConstraints);

        jShareControlPanel.setLayout(new java.awt.GridBagLayout());

        jRemoveShareButton.setText("Remove");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jShareControlPanel.add(jRemoveShareButton, gridBagConstraints);

        jAddShareButton.setText("Add...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jShareControlPanel.add(jAddShareButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jSharePanel.add(jShareControlPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSharePanel, gridBagConstraints);

        jHeadPanel.setLayout(new java.awt.GridBagLayout());

        jServerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Server Controls"));
        jServerPanel.setLayout(new java.awt.GridBagLayout());

        jStatusLabel.setText("Status:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 5.0;
        gridBagConstraints.weighty = 1.0;
        jServerPanel.add(jStatusLabel, gridBagConstraints);

        jLoginButton.setText("Login");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jServerPanel.add(jLoginButton, gridBagConstraints);

        jLogoutButton.setText("Logout");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jServerPanel.add(jLogoutButton, gridBagConstraints);

        jSearchButton.setText("Search!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jServerPanel.add(jSearchButton, gridBagConstraints);

        jSearchField.setText("Type file to search here!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 5.0;
        jServerPanel.add(jSearchField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 3.0;
        jHeadPanel.add(jServerPanel, gridBagConstraints);

        jClientPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Open Shareon File"));
        jClientPanel.setLayout(new java.awt.GridBagLayout());

        jAddShareOnButton.setText("Browse...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jClientPanel.add(jAddShareOnButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jHeadPanel.add(jClientPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jHeadPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jDownloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDownloadButtonActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jDownloadButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    /* Disabled, because this is not a standalone program, only a GUI.
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI().setVisible(true);
            }
        });
    }
    */ 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAddShareButton;
    private javax.swing.JButton jAddShareOnButton;
    private javax.swing.JPanel jClientPanel;
    private javax.swing.JButton jDownloadButton;
    private javax.swing.JPanel jHeadPanel;
    private javax.swing.JButton jLoginButton;
    private javax.swing.JButton jLogoutButton;
    private javax.swing.JButton jRemoveShareButton;
    private javax.swing.JList jResultsList;
    private javax.swing.JPanel jResultsPanel;
    private javax.swing.JScrollPane jResultsScrollPane;
    private javax.swing.JButton jSearchButton;
    private javax.swing.JTextField jSearchField;
    private javax.swing.JPanel jServerPanel;
    private javax.swing.JPanel jShareControlPanel;
    private javax.swing.JPanel jSharePanel;
    private javax.swing.JList jSharesList;
    private javax.swing.JScrollPane jSharesScrollPane;
    private javax.swing.JLabel jStatusLabel;
    // End of variables declaration//GEN-END:variables

}