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
    private int iPingListenPort = 30001;
    private ServerSocket pingSocket;
    private Socket clientSocket;
    private PrintWriter pwPing;
    private BufferedReader brPing;
    
    public PseudoPingListener()
        {
        try
            {
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
