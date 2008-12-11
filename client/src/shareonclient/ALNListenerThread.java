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

/**
 *
 * @author Bandita
 */
public class ALNListenerThread extends Thread{
    
    private ShareOnClient ownerClient;          //client running the listener
    private Vector<Socket> vALNSockets;  //active listeners
    private int iALNPort;                       //ALM listener port
    private ServerSocket alnSocket = null;      //ALM listener socket
    
    public ALNListenerThread(ShareOnClient ownerClientIn, int iALMPortIn)
        {
        ownerClient = ownerClientIn;
        iALNPort = iALMPortIn;
        vALNSockets = new Vector<Socket>();
        }
    
    @Override
    public void run()
        {
        //infinite loop, waiting for ALM connections
        while (true)
            {
            try
                {
                //waiting for incoming connections
                System.out.println("Creating ALM listener socket on port "+ iALNPort);
                alnSocket = new ServerSocket(iALNPort);
                System.out.println("ALM listener socket created!");
                Socket almClient = alnSocket.accept();
                System.out.println("Incoming ALM connection from: " + almClient.toString());
                vALNSockets.add(almClient);
                //start a new thread with the new connection
                ALNThread almThread = new ALNThread(almClient, ownerClient);
                almThread.start();
                alnSocket.close();
                }
            catch (IOException e)
                {
                System.err.println("Error listening to ALM connections!");
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
                String sLine;
                while (!(sLine = brAML.readLine()).equals("logout"))
                        {
                        while (ownerClient.isServerSocketUsed())
                            {
                            try
                                { Thread.sleep(10); }
                            catch (InterruptedException e) {}
                            }
                        ownerClient.setServerSocketUsage(true);
                        String sReply = ownerClient.forwardALMMessage(sLine);
                        pwAML.println(sReply);
                        ownerClient.setServerSocketUsage(false);
                        }
                pwAML.close();
                brAML.close();
                clientSocket.close();
                }
            catch (IOException e)
                {
                System.err.println("Exception occured: " + e.toString());
                // TODO: remove client shares
                }
            catch (NullPointerException e)
                {
                System.err.println("Exception occured: " + e.toString());
                System.err.println("Connection may be lost to a client!");
                // TODO: remove client shares 
                }
            }
        }

}
