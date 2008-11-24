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
            echoSocket = new Socket("89.132.127.137", 30000);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            } 
        catch (UnknownHostException e)
            {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
            }
        catch (IOException e)
            {
            System.err.println("Couldn't get I/O for the connection to: 89.132.127.137");
            System.err.println(e.toString());
            System.exit(1);
            }

	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userInput;

	while ((userInput = stdIn.readLine()) != null)
            {
	    out.println(userInput);
	    System.out.println("echo: " + in.readLine() + "\n");
            }

	out.close();
	in.close();
	stdIn.close();
	echoSocket.close();
    }
}


