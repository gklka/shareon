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
        String sClientIP;               //IP of the connected client

        //save the socket in the constructor
        ClientEntity(Socket sServerIn, ShareOnServer callerServerIn)
            {
            sServer = sServerIn;
            callerServer = callerServerIn;
            sClientIP = (sServer.getInetAddress()).toString().substring(1);
            }

        public void run ()
            {
            try 
                {
                //create socket and streams
                pwOut = new PrintWriter(sServer.getOutputStream(), true);
                buffIn = new BufferedReader(new InputStreamReader(sServer.getInputStream()));
                pwOut.println(sClientIP + "@" + callerServer.getRandomClient());
                String sLine;
                while(!(sLine = buffIn.readLine()).equals("logout"))
                    {
                    
                    //shared file added
                    if (sLine.startsWith("added@"))
                        {
                        String[] sSplit = sLine.split("@");
                        boolean bSuccess = callerServer.executeUpload(sClientIP, sSplit[1], "ABCD");
                        if (bSuccess)
                            {
                            pwOut.println("ACK");
                            System.out.println("Content shared by " + sClientIP + ": " + sSplit[1]);
                            }
                        else
                            {
                            pwOut.println("NACK");
                             System.err.println("Content share faliure with " + sClientIP + ": " + sSplit[1]);
                            }
                        }
                    
                    //shared file added via ALN
                    if (sLine.startsWith("alnadded@"))
                        {
                        String[] sSplit = sLine.split("@");
                        boolean bSuccess = callerServer.executeUpload(sSplit[2], sSplit[1], "ABCD");
                        if (bSuccess)
                            {
                            pwOut.println("ACK");
                            System.out.println("Content shared by " + sSplit[2] + ": " + sSplit[1]);
                            }
                        else
                            {
                            pwOut.println("NACK");
                             System.err.println("Content share faliure with " + sSplit[2] + ": " + sSplit[1]);
                            }
                        }
                    
                    //shared file removed
                    if (sLine.startsWith("removed@"))
                        {
                        String[] sSplit = sLine.split("@");
                        boolean bSuccess = callerServer.executeDelete(sSplit[1], sClientIP);
                        if (bSuccess)
                            {
                            pwOut.println("ACK");
                            System.out.println("Content removed by " + sClientIP + ": " + sSplit[1]);
                            }
                        else
                            {
                            pwOut.println("NACK");
                            System.out.println("Content remove faliure with " + sClientIP + ": " + sSplit[1]);
                            }
                        }
                    
                    //shared file removed via ALN
                    if (sLine.startsWith("alnremoved@"))
                        {
                        String[] sSplit = sLine.split("@");
                        boolean bSuccess = callerServer.executeDelete(sSplit[1], sSplit[2]);
                        if (bSuccess)
                            {
                            pwOut.println("ACK");
                            System.out.println("Content removed by " + sSplit[2] + ": " + sSplit[1]);
                            }
                        else
                            {
                            pwOut.println("NACK");
                            System.out.println("Content remove faliure with " + sSplit[2] + ": " + sSplit[1]);
                            }
                        }
                    
                    //request to identify the outer IP address of the connected client
                    /* deprecated since ALM implementation
                    if (sLine.equals("IP"))
                        pwOut.println(sClientIP);
                        //pwOut.println(sServer.getRemoteSocketAddress());
                    */
                    
                    //search request
                    if (sLine.startsWith("search@"))
                        {
                        String[] sSplit = sLine.split("@");
                        //System.out.println(sSplit[1]);
                        String sResults = callerServer.searchFile(sSplit[1]);
                        pwOut.println(sResults);
                        }
                    
                    //ALN disconnect request
                    if (sLine.startsWith("alnlogout@"))
                        {
                        String[] sSplit = sLine.split("@");
                        callerServer.removeClientShares(sSplit[1]);
                        }
                    }
                //once the client logs out, we clean up the mess
                callerServer.removeClientShares(sClientIP);
                callerServer.disconnect();
                pwOut.close();
                buffIn.close();
                sServer.close();
                return;
                }
            catch (IOException e)
                {
                System.err.println("Exception occured: " + e.toString());
                callerServer.removeClientShares(sClientIP);
                callerServer.disconnect();
                }
            catch (NullPointerException e)
                {
                System.err.println("Exception occured: " + e.toString());
                System.err.println("Connection may be lost to a client!");
                callerServer.removeClientShares(sClientIP);
                callerServer.disconnect();
                }
            }
        
        public String getClientIP()
            {
            return sClientIP;
            }
        }