/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonserver;

/**
 *
 * @author Bandita
 */

import java.net.*;
import java.io.*;

public class ShareOnServer {
    
    private final int iMaxConnections = 100;
    private int serverPort = 30000;
    private int iConnections = 0;
    private ServerSocket socketListen;
    
    public ShareOnServer() throws IOException
        {
        System.out.println("Establishing ShareOn server socket at port " + serverPort);
    
        socketListen = new ServerSocket(serverPort);
        System.out.println("Server successfully established!");
    
        // this server is an indeed ultimate one, it can manage up to 100 connections
        System.out.println("Awaiting connections!");
        
        runServer();
        }
    
    private void runServer()
        {
        try
            {
            Socket sServer;

            while(iConnections < iMaxConnections)
                {
                iConnections++;
                sServer = socketListen.accept();
                ClientEntity clientConnecting = new ClientEntity(sServer, this);
                Thread tClient = new Thread(clientConnecting);
                tClient.start();
                }
            }
         catch (IOException e)
            {
            System.err.println("Exception on socket listen: " + e.toString());
            }
    
    }       // end constructor
    
    public void disconnect() { iConnections--; }
    
    public static void main (String args[])
        {
        try
            {
            ShareOnServer serverInstance = new ShareOnServer();
            }
        catch (IOException e)
            {
            System.err.println("Exception occured while establishing server: " + e.toString());
            System.err.println("The program will now exit!");
            System.exit(1);
            }
        }

}       // end class definition