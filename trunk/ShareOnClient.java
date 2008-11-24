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

public class ShareOnClient {
    public static void main(String[] args) throws IOException
        {
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try 
            {
            echoSocket = new Socket("localhost", 30000);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            System.out.println("Connection established!\n");
            } 
        catch (UnknownHostException e)
            {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
            }
        catch (IOException e)
            {
            System.err.println("Couldn't get I/O for the connection to: localhost");
            System.err.println(e.toString());
            System.exit(1);
            }

	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userInput;
        String sEcho;
        
	while (!(userInput = stdIn.readLine()).equals("logout"))
            {
	    out.println(userInput);
            sEcho = in.readLine();
            if (!sEcho.equals("You are terminated!"))
                System.out.println("Echo: " + sEcho + "\n");
            else
                {
                System.out.println(sEcho + "\n");
                out.close();
                in.close();
                stdIn.close();
                echoSocket.close();
                System.exit(2);
                }
            }
        
        out.println("logout");
	out.close();
	in.close();
	stdIn.close();
	echoSocket.close();
    }
}


