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

/**
 *
 * @author Bandita
 */
public class PseudoPingListener implements Runnable{
    private int iPingListenPort = 30001;    //port to listen to pseudoping
    private ServerSocket pingSocket;        //socket to listen to pseudoping
    private Socket clientSocket;            //socket to pong
    private PrintWriter pwPing;             //printwriter to pong
    private BufferedReader brPing;          //bufferedreader to hear the ping message
    
    public PseudoPingListener()
        {
        try
            {
            //create the listen socket
            pingSocket = new ServerSocket(iPingListenPort);
            run();
            }
        catch (IOException e)
            {
            System.err.println("Error on listening to pseudoping!");
            System.err.println("Details: " + e.toString());
            System.exit(1);
            }
        }
    
    public void run()
        {
        while (true)
            {
            try
                {
                /* We accept an incoming ping request here.
                 * This is not threaded, however the possibility 
                 * of receiving another ping while one is currently being ponged,
                 * is indeed close to zero. So it is acceptable :)
                 */ 
                clientSocket = pingSocket.accept();
                pwPing = new PrintWriter(clientSocket.getOutputStream(), true);
                brPing = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if (brPing.readLine() != null)
                    {
                    pwPing.println("pong");
                    }
                pwPing.close();
                brPing.close();
                clientSocket.close();
                }
            catch (IOException e)
                {
                System.err.println("Error on listening to pseudoping!");
                System.err.println("Details: " + e.toString());
                }
            }
        }
}
