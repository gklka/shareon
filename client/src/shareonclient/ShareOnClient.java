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
import javax.swing.JOptionPane;

public class ShareOnClient {
    
    Socket serverSocket;
    ServerSocket pingListen;
    PrintWriter out;
    BufferedReader in;
    ClientGUI currentGUI;
    boolean bRetry;
    boolean bConnected;
        
    public ShareOnClient()
        {
        serverSocket = null;
        out = null;
        currentGUI = new ClientGUI(this);
        currentGUI.setVisible(true);
        bConnected = false;
        currentGUI.setStatusText("offline mode");
        
        // listen to pseudopings from peers
        //listenToPseudoPing();
        //long lTmp = pseudoPing("localhost");
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
                                                           JOptionPane.YES_NO_OPTION);
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
                                                           JOptionPane.YES_NO_OPTION);
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
    
    public long pseudoPing(String sHost)
        {
        long lRTT =  0;
        try
            {
            Socket p2pSocket = new Socket(sHost, 30001);
            PrintWriter pwP2P = new PrintWriter(p2pSocket.getOutputStream(), true);
            BufferedReader brP2P = new BufferedReader(new InputStreamReader(p2pSocket.getInputStream()));
            long lStartTime = System.currentTimeMillis();
            long lEndTime = 0;
            pwP2P.write("ping");
            String sPong;
            while ((sPong = brP2P.readLine()) != null )
                {
                lEndTime = System.currentTimeMillis();
                if (sPong.equals("pong"))
                    break;
                }
            lRTT = lEndTime - lStartTime;
            pwP2P.close();
            brP2P.close();
            p2pSocket.close();
            }
        catch (IOException e)
            {
            System.err.println("Error occured while measuring RTT to: " + sHost);
            System.err.println("Detalis: " + e.toString());
            }
        return lRTT;
        }
    
    public void listenToPseudoPing()
        {
        try
            {
            pingListen = new ServerSocket(30001);
            while (true)
                {
                Socket sCurrent = pingListen.accept();
                PrintWriter pwPong = new PrintWriter(sCurrent.getOutputStream(), true);
                BufferedReader brPing = new BufferedReader(new InputStreamReader(sCurrent.getInputStream()));
                while (brPing.readLine() != null)
                    pwPong.write("pong");
                pwPong.close();
                brPing.close();
                sCurrent.close();
                }
            }
        catch (IOException e)
            {
            System.err.println("Error occured while listening to pseudoping!");
            System.err.println("Detalis: " + e.toString());
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
            disconnectFromServer();
            out.close();
            in.close();
            serverSocket.close();
            pingListen.close();
            }
        catch (IOException e) {}
        System.exit(0);
        }
    
    public static void main(String args[]) {
        ShareOnClient clientInstance = new ShareOnClient();
    }
}