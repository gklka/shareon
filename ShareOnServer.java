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
  public static void main (String args[]) throws IOException {

    int serverPort = 30000;
    
    System.out.println("Establishing ShareOn server socket at port " + serverPort);
    
    ServerSocket serverSocket = new ServerSocket(serverPort);

    // a real server would handle more than just one client like this...
    Socket s = serverSocket.accept();
    BufferedInputStream is = new BufferedInputStream(s.getInputStream());
    BufferedOutputStream os = new BufferedOutputStream(s.getOutputStream());

    // This server just echoes back what you send it...
    byte buffer[] = new byte[4096];
    int bytesRead;

    // read until "eof" returned
    while ((bytesRead = is.read(buffer)) > 0)
        {
        os.write(buffer, 0, bytesRead); // write it back
        os.flush();    // flush the output buffer
        }


    s.close();
    serverSocket.close();
  }       // end main()

}       // end class definition