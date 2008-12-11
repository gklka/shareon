/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Bandita
 */
public class ALNListenerThread extends Thread{
    
    private ShareOnClient ownerClient;          //client running the listener
    //private Vector<Socket> vALNSockets;  //active listeners
    private int iALNPort;                       //ALN listener port
    private ServerSocket alnSocket = null;      //ALN listener socket
    
    public ALNListenerThread(ShareOnClient ownerClientIn, int iALMPortIn)
        {
        ownerClient = ownerClientIn;
        iALNPort = iALMPortIn;
        //vALNSockets = new Vector<Socket>();
        }
    
    @Override
    public void run()
        {
        //infinite loop, waiting for ALN connections
        while (true)
            {
            try
                {
                //waiting for incoming connections
                System.out.println("Creating ALN listener socket on port "+ iALNPort);
                alnSocket = new ServerSocket(iALNPort);
                System.out.println("ALN listener socket created!");
                Socket almClient = alnSocket.accept();
                System.out.println("Incoming ALN connection from: " + almClient.toString());
                //vALNSockets.add(almClient);
                //start a new thread with the new connection
                ALNThread almThread = new ALNThread(almClient, ownerClient);
                almThread.start();
                alnSocket.close();
                }
            catch (IOException e)
                {
                System.err.println("Error listening to ALN connections!");
                System.err.println("Details: " + e.toString());
                System.exit(1);
                }
            }
        }
    
    class ALNThread extends Thread
        {
        
        private Socket clientSocket;
        private ShareOnClient ownerClient;
        private PrintWriter pwAML;
        private BufferedReader brAML;
        private String sALNClientIP;
        
        public ALNThread(Socket clientSocketIn, ShareOnClient ownerClientIn)
            {
            clientSocket = clientSocketIn;
            ownerClient = ownerClientIn;
            sALNClientIP = (clientSocket.getInetAddress()).toString().substring(1);
            }
        
        @Override
        public void run()
            {
            try
                {
                //handling communication with the ALM client
                pwAML = new PrintWriter(clientSocket.getOutputStream(), true);
                brAML = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                pwAML.println(sALNClientIP);
                System.out.println("ALN client successfully connected: " + sALNClientIP);
                String sLine;
                boolean bBreak = false;
                while (!(sLine = brAML.readLine()).equals("logout"))
                        {
                        while (ownerClient.isServerSocketUsed())
                            {
                            try
                                { Thread.sleep(10); }
                            catch (InterruptedException e) {}
                            }
                        //System.out.println(sLine);
                        ownerClient.setServerSocketUsage(true);
                        String sReply = ownerClient.forwardALNMessage(sLine, sALNClientIP);
                        if (sReply.equals("@error"))
                            {
                            JOptionPane.showMessageDialog(ownerClient.getGUI(), "ALN provider unreachable!\nClient will now disconnect (if it hasn't already)!", "Error!", JOptionPane.ERROR_MESSAGE);
                            bBreak = true;
                            break;
                            }
                        pwAML.println(sReply);
                        ownerClient.setServerSocketUsage(false);
                        }
                if (!bBreak)
                    {
                    System.out.println("ALN client disconnected: " + sALNClientIP);
                    ownerClient.setServerSocketUsage(true);
                    ownerClient.forwardALNMessage("logout", sALNClientIP);
                    ownerClient.setServerSocketUsage(false);
                    }
                pwAML.close();
                brAML.close();
                clientSocket.close();
                return;
                }
            catch (IOException e)
                {
                System.err.println("Exception occured: " + e.toString());
                ownerClient.forwardALNMessage("logout", sALNClientIP);
                }
            catch (NullPointerException e)
                {
                System.err.println("Exception occured: " + e.toString());
                System.err.println("Connection may be lost to an ALN client!");
                ownerClient.forwardALNMessage("logout", sALNClientIP);
                }
            }
        }

}
