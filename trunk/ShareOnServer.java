/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonserver;

/**
 *
 * @author Bandita
 */
/* File simpleSocketServer.java*/

import java.net.*;
import java.io.*;

public class ShareOnServer {
    
    static final int iMaxConnections = 100;
    
    public static void main (String args[]) throws IOException
        {
        int serverPort = 30000;
        int iConnections = 0;
    
        System.out.println("Establishing ShareOn server socket at port " + serverPort);
    
        ServerSocket socketListen = new ServerSocket(serverPort);
        System.out.println("Server successfully established!");
    
        // this server is an indeed ultimate one, it can manage 100 connections
        System.out.println("Awaiting connections!");
        
        try
            {
            Socket sServer;

            while(iConnections < iMaxConnections)
                {
                iConnections++;
                sServer = socketListen.accept();
                //TODO: a connection karbantartás!
                ClientEntity clientConnecting = new ClientEntity(sServer);
                Thread tClient = new Thread(clientConnecting);
                tClient.start();
                }
            }
            catch (IOException e)
                {
                System.out.println("Exception on socket listen: " + e.toString());
                }
    
    }       // end main()

}       // end class definition