/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonclient;

/**
 *
 * @author Bandita
 */
import java.io.*;
import java.net.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ShareOnClient {
    
    private Socket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientGUI currentGUI;
    private boolean bRetry;
    private boolean bConnected;
    private PseudoPingListener currentListener;
    private Thread tPseudoPingListener;
    private int iPingListenPort = 30001;
        
    public ShareOnClient()
        {
        serverSocket = null;
        out = null;
        currentGUI = new ClientGUI(this);
        currentGUI.setVisible(true);
        bConnected = false;
        currentGUI.setStatusText("offline mode");
        currentListener = new PseudoPingListener();
        tPseudoPingListener = new Thread(currentListener);
        tPseudoPingListener.start();
        }
    
    public void connectToServer()
        {
        if (bConnected)
            return;
        while (true)
            {
            try 
                {
                serverSocket = new Socket("localhost", 30000);
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                bConnected = true;
                break;
                } 
            catch (UnknownHostException e)
                {
                currentGUI.setStatusText("connection falied");
                int answer = JOptionPane.showConfirmDialog(currentGUI,
                                                           "Don't know about host: localhost.\nPress YES to try again or NO otherwise!",
                                                           "Connection error!",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.ERROR_MESSAGE);
                if (answer == JOptionPane.NO_OPTION)
                    {
                    currentGUI.setStatusText("offline mode");
                    return;
                    }
                }
            catch (IOException e)
                {
                currentGUI.setStatusText("connection falied");
                int answer = JOptionPane.showConfirmDialog(currentGUI,
                                                           "Couldn't get I/O for the connection to: localhost.\nPress YES to try again or NO otherwise!",
                                                           "Connection error!",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.ERROR_MESSAGE);
                if (answer == JOptionPane.NO_OPTION)
                    {
                    currentGUI.setStatusText("offline mode");
                    return;
                    }
                }
            }
            currentGUI.setStatusText("connected to server");
        }
    
    public void disconnectFromServer()
        {
        if (!bConnected)
            return;
        out.println("logout");
        bConnected = false;
        currentGUI.setStatusText("offline mode");
        }
    
    public boolean isConnectedToServer() { return bConnected; }
    
    public File chooseFile(boolean bIsShareOn)
        {
        JFileChooser chooseFile = new JFileChooser();
        if (bIsShareOn)
            chooseFile.setFileFilter(new ShareOnFileFilter());
        chooseFile.showOpenDialog(currentGUI);
        return chooseFile.getSelectedFile();
        }
    
    public String pseudoPing(String sHost)
        {
        PrintWriter pwPing;
        BufferedReader brPing;
        Socket pingSocket;

        try
            {
            pingSocket = new Socket(sHost, iPingListenPort);
            pwPing = new PrintWriter(pingSocket.getOutputStream(), true);
            brPing = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
            }
        catch (UnknownHostException e)
            {
            System.err.println("Don't know about host: " + sHost);
            return ("doesn't exist");
            }
        catch (IOException e)
            {
            System.err.println("Host unreachable: " + sHost);
            return ("unreachable");
            }

        try
            {
            long lEndTime = 0;
            long lStartTime = System.currentTimeMillis();
            pwPing.println("ping");
            String sPong = brPing.readLine();
            if (sPong.equals("pong"))
                lEndTime = System.currentTimeMillis();
            pwPing.close();
            brPing.close();
            pingSocket.close();
            return String.valueOf(lEndTime - lStartTime);
            }
        catch (IOException e)
            {
            System.err.println("Couldn't ping host: " + sHost);
            return ("unreachable");
            }
        }

    public void search(String sToSearch)
        {
        }
    
    public void updateShares()
        {
        }
    
    public void exit()
        {
        try
            {
            tPseudoPingListener.interrupt();
            disconnectFromServer();
            out.close();
            in.close();
            serverSocket.close();
            }
        catch (IOException e) {}
        System.exit(0);
        }
    
    public static void main(String args[]) {
        ShareOnClient clientInstance = new ShareOnClient();
    }
}