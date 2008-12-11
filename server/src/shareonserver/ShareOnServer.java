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
import java.sql.*;

public class ShareOnServer {
    
    private final int iMaxConnections = 100;    //number of max simultaneous connections
    private int serverPort = 30000;             //listen port of the server
    private int iConnections = 0;               //number of active connections
    private ServerSocket socketListen;          //listen socket of the server
    private Connection dbConnection;            //connection to the MySQL database
    
    public ShareOnServer() throws IOException
        {
        System.out.println("Loading MySQL JDBC driver...");
        try
            {
            // load Sun's jdbc-odbc driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            }
        catch (Exception e) // driver not found
            {
            System.err.println ("Unable to load database driver!");
            System.err.println ("Details : " + e.toString());
            System.exit(1);
            }
        System.out.println("Done loading driver!");
        System.out.println("Connecting to MySQL server...");
        String sURL = "jdbc:mysql://turul.eet.bme.hu:3306/db_mk0804";
        
        //connect to the database
        try
            {
            dbConnection = DriverManager.getConnection (sURL, "mk0804", "Lenuecxm");
            //dbConnection.setAutoCommit(false);
            }
        catch (SQLException e) // driver not found
            {
            System.err.println ("Failed to connect to the database!");
            System.err.println ("Details : " + e.toString());
            System.exit(1);
            }
        System.out.println("Connected!");

        //check if the shares maintenance table exists
        try
            {
            System.out.println("Checking if the shares table exists...");
            DatabaseMetaData dbM = dbConnection.getMetaData();
            ResultSet rs = dbM.getTables(null, null, "shares", null);
            if (!rs.next())
                {
                System.out.println("Table not found, creating...");
                PreparedStatement pstmt = dbConnection.prepareStatement("CREATE TABLE shares (id INTEGER, file VARCHAR(255), hash VARCHAR(128), ip VARCHAR(15), PRIMARY KEY(id));");
                pstmt.execute();
                dbConnection.commit();
                System.out.println("Table created!");
                }
            else
                System.out.println("Table found!");
            }
        catch (SQLException e)
            {
            System.err.println ("A database error has occured!");
            System.err.println ("Details : " + e.toString());
            System.exit(1);
            }

        System.out.println("Establishing ShareOn server socket at port " + serverPort);
    
        socketListen = new ServerSocket(serverPort);
        try
        {
            InetAddress thisIp = InetAddress.getLocalHost();
            System.out.println("Server successfully established! ("+thisIp.getHostAddress()+")");
        } catch(UnknownHostException e)
        {
            System.err.println("Unknown host error! ("+e.getLocalizedMessage()+")");
        }

        
    
        // this server is an indeed ultimate one, it can manage up to 100 connections
        System.out.println("Awaiting connections!");
        
        //testUpload();
        //testQuery();
        
        runServer();
        }
    
    //run the server
    private void runServer()
        {
        try
            {
            Socket sServer;
            //while there are free connections we accept them
            while(iConnections < iMaxConnections)
                {
                iConnections++;
                sServer = socketListen.accept();
                //and start threads to interact with them simultaneously
                ClientEntity clientConnecting = new ClientEntity(sServer, this);
                Thread tClient = new Thread(clientConnecting);
                tClient.start();
                System.out.println(clientConnecting.getClientIP() + ": client successfully connected!");
                }
            }
         catch (IOException e)
            {
            System.err.println("Exception on socket listen: " + e.toString());
            }
    
    }       // end constructor
    
    //function to decrease number of activ connections in case somebody disconnects
    public void disconnect() { iConnections--; }
    
    //@Override
    /**
     * If the server is shut down, we must clean up all the mess we made,
     * because this is a well behaving server program :)
     */
    protected void finalize()
        {
        try { dbConnection.close(); }
        catch (SQLException e) {}
        try { super.finalize(); }
        catch (Throwable t) {}
        }
    
    //function to create database reference of a shared file
    public boolean executeUpload(String sIP, String sFileName, String sHash)
        {
        try
            {
            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO shares (ip, file, hash) VALUES (?, ?, ?);");
            pstmt.setString(1, sIP);
            pstmt.setString(2, sFileName);
            pstmt.setString(3, sHash);
            pstmt.executeUpdate();
            return true;
            }
        catch (SQLException e)
            {
            System.err.println("Unable to execute INSERT!");
            System.err.println("Details: " + e.toString());
            return false;
            }
        }
    
    //function to delete database reference of a removed object
    public boolean executeDelete(String sFileName, String sIP)
        {
        try
            {
            PreparedStatement pstmt = dbConnection.prepareStatement("DELETE FROM shares WHERE file = ? AND ip = ?;");
            pstmt.setString(1, sFileName);
            pstmt.setString(2, sIP);
            pstmt.executeUpdate();
            return true;
            }
        catch (SQLException e) 
            {
            System.err.println("Unable to execute DELETE!");
            System.err.println("Details: " + e.toString());
            return false;
            }
        }
    
    //function to delete database references of a logged out client
    public void removeClientShares(String sIP)
        {
        try
            {
            PreparedStatement pstmt = dbConnection.prepareStatement("DELETE FROM shares WHERE ip = ?;");
            pstmt.setString(1, sIP);
            pstmt.executeUpdate();
            System.err.println("Shares of " + sIP + " removed successfully after logout!");
            }
        catch (SQLException e)
            {
            System.err.println("Unable to remove shares of " + sIP + " on logout!");
            System.err.println("Details: " + e.toString());
            }
        }
    
    //functin to search for files
    public String searchFile(String sFileName)
        {
        try
            {
            String sResults = "";
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT * FROM shares WHERE file LIKE ?;");
            pstmt.setString(1, "%" + sFileName + "%");
            pstmt.execute();
            ResultSet searchResults = pstmt.getResultSet();
            while (searchResults.next())
                {
                sResults += searchResults.getString(4) + "#-#" + searchResults.getString(2) + "@";
                }
            //return the results (cut down the last '@')
            if (sResults.equals(""))
                return "";
            else
                return sResults.substring(0, sResults.length() - 1);
            }
        catch (SQLException e)
            {
            System.err.println("Error occured while searching!");
            System.err.println("Details: " + e.toString());
            return "";
            }
        }
    
    //function to get random ALM client for the newly joined one
    public String getRandomClient()
        {
        try
            {
            String sResult = "";
            //we choose a random IP
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT ip FROM shares ORDER BY RAND() LIMIT 1;");
            pstmt.execute();
            ResultSet searchResult = pstmt.getResultSet();
            if (searchResult.next())
                {
                sResult = searchResult.getString(1);
                return sResult;
                }
            else
                {
                return "";
                }
            }
        catch (SQLException e)
            {
            System.err.println("Error occured while returning ALM server candidate!");
            System.err.println("Details: " + e.toString());
            return "";
            }
        }
    
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