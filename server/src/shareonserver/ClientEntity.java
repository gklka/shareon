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
        PrintWriter pwOut = null;       //printwriter to communicate with the client
        BufferedReader buffIn = null;   //bufferedreader to communicate with the client
        ShareOnServer callerServer;     //server who owns the thread

        //save the socket in the constructor
        ClientEntity(Socket sServerIn, ShareOnServer callerServerIn)
            {
            sServer = sServerIn;
            callerServer = callerServerIn;
            }

        public void run ()
            {
            try 
                {
                //create socket and streams
                pwOut = new PrintWriter(sServer.getOutputStream(), true);
                buffIn = new BufferedReader(new InputStreamReader(sServer.getInputStream()));
                String sLine;
                /**
                 * @TODO
                 * server-client communication
                 */
                while(!(sLine = buffIn.readLine()).equals("logout"))
                    {
                    }
                //once the client logs out, we clean up the mess
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