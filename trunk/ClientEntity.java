/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shareonserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Bandita
 */

// class to represents clients to connect to the ShareOn server
class ClientEntity implements Runnable
        {
        Socket sServer;                 //socket to communicate with the client
        String sFromClient;             //message received from the client
        PrintWriter pwOut = null;       //PrintWriter to wrtite
        BufferedReader buffIn = null;   //BufferedReader to read
        ShareOnServer callerServer;
        String sLine;

        //save the socket in the constructor
        ClientEntity(Socket sServerIn, ShareOnServer callerServerIn)
            {
            sServer = sServerIn;
            callerServer = callerServerIn;
            }

        public void run ()
            {
            //we just send back what we have received
            try 
                {
                pwOut = new PrintWriter(sServer.getOutputStream(), true);
                buffIn = new BufferedReader(new InputStreamReader(sServer.getInputStream()));
                
                while(!(sLine = buffIn.readLine()).equals("logout"))
                    {
                    pwOut.println(sLine);
                    }
                
                callerServer.disconnect();
                pwOut.close();
                buffIn.close();
                sServer.close();
                }
            catch (IOException e)
                {
                System.err.println("Exception occured: " + e.toString());
                callerServer.disconnect();
                }
            catch (NullPointerException e)
                {
                System.err.println("Exception occured: " + e.toString());
                System.err.println("Connection may be lost to a client!");
                callerServer.disconnect();
                }
            }
        }
