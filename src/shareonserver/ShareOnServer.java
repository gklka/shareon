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
    
    private final int iMaxConnections = 100;
    private int serverPort = 30000;
    private int iConnections = 0;
    private ServerSocket socketListen;
    private Connection dbConnection;
    
    public ShareOnServer() throws IOException
        {
        System.out.println("Loading MySQL JDBC driver...");
        try
            {
            // Load Sun's jdbc-odbc driver
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
        
        try
            {
            dbConnection = DriverManager.getConnection (sURL, "mk0804", "Lenuecxm");
            dbConnection.setAutoCommit(false);
            }
        catch (SQLException e) // driver not found
            {
            System.err.println ("Failed to connect to the database!");
            System.err.println ("Details : " + e.toString());
            System.exit(1);
            }
        System.out.println("Connected!");
        
        try
            {
            System.out.println("Checking if content-peer table exists...");
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
        System.out.println("Server successfully established!");
    
        // this server is an indeed ultimate one, it can manage up to 100 connections
        System.out.println("Awaiting connections!");
        
        //testUpload();
        //testQuery();
        
        runServer();
        }
    
    private void runServer()
        {
        try
            {
            Socket sServer;

            while(iConnections < iMaxConnections)
                {
                iConnections++;
                sServer = socketListen.accept();
                ClientEntity clientConnecting = new ClientEntity(sServer, this);
                Thread tClient = new Thread(clientConnecting);
                tClient.start();
                }
            }
         catch (IOException e)
            {
            System.err.println("Exception on socket listen: " + e.toString());
            }
    
    }       // end constructor
    
    public void disconnect() { iConnections--; }
    
    @Override
    protected void finalize()
        {
        try { dbConnection.close(); }
        catch (SQLException e) {}
        try { super.finalize(); }
        catch (Throwable t) {}
        }
    
    private void testUpload()
        {
        try
            {
            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO shares (id , ip, file, hash) VALUES (?, ?, ?, ?);");
            pstmt.setString(1, "42");
            pstmt.setString(2, "152.66.212.113");
            pstmt.setString(3, "ingyenporno.mkv");
            pstmt.setString(4, "ABBAEDDAACDC");
            pstmt.executeUpdate();
            dbConnection.commit();
            }
        catch (SQLException e) {}
        }
    
    private void testQuery()
        {
        try
            {
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT * FROM shares WHERE id = ?");
            pstmt.setString(1, "42");
            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            while (rs.next())
                {
                String s1 = rs.getString(2);
                String s2 = rs.getString(3);
                String s3 = rs.getString(4);
                System.out.println(s1 + "\t" + s2 + "\t" + s3);
                }
            }
        catch (SQLException e) {}
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