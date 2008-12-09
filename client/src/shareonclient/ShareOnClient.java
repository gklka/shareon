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
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.regex.Pattern;

public class ShareOnClient {
    
    private Socket serverSocket;                    //socket to connect to the server
    private PrintWriter out;                        //printwriter to communicate with the server
    private BufferedReader in;                      //bufferedreader to communicate with the server
    private ClientGUI currentGUI;                   //GUI of the client              
    private boolean bConnected;                     //server connection status
    private PseudoPingListener currentListener;     //listener class to listen to pseudopings
    private Thread tPseudoPingListener;             //thread to run the listener
    private int iPingListenPort = 30001;            //port to listen to pseudoping
    private String sLocalIP;                        //string representing the local IP address
    private int iFileTransferPort = 30005;          //port for uploading file
    private UploadListenerThread uploadListener;   //listener for file uploads
    private Thread tUploadListener;
    private final int iBufferSize = 1048576;        //buffer size for file transfer
    private String sServerIP;                       //IP address of server
        
    public ShareOnClient(String sIP)
        {
        //init some values
        sServerIP = sIP;
        serverSocket = null;
        out = null;
        currentGUI = new ClientGUI(this);
        currentGUI.setVisible(true);
        bConnected = false;
        currentGUI.setStatusText("offline mode");
        uploadListener = new UploadListenerThread();
        tUploadListener = new Thread(uploadListener);
        tUploadListener.start();
        currentListener = new PseudoPingListener();
        tPseudoPingListener = new Thread(currentListener);
        tPseudoPingListener.start();
        }
    
    public void connectToServer()
        {
        //if we are connected, we don't connect again
        if (bConnected)
            return;
        while (true)
            {
            try 
                {
                serverSocket = new Socket(sServerIP, 30000);
                out = new PrintWriter(serverSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                bConnected = true;
                //obtain the ip address of the client
                out.println("IP");
                sLocalIP = in.readLine();
                break;
                }
            //if there are errors we show some error messages
            catch (UnknownHostException e)
                {
                currentGUI.setStatusText("connection falied");
                int answer = JOptionPane.showConfirmDialog(currentGUI,
                                                           "The servers host couldn't be found!.\nPress YES to try again or NO otherwise!",
                                                           "Connection error!",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.ERROR_MESSAGE);
                if (answer == JOptionPane.NO_OPTION)
                    {
                    currentGUI.setStatusText("offline mode");
                    return;
                    }
                }
            catch (IOException e)
                {
                currentGUI.setStatusText("connection falied");
                int answer = JOptionPane.showConfirmDialog(currentGUI,
                                                           "Couldn't get I/O for the connection to the server.\nPress YES to try again or NO otherwise!",
                                                           "Connection error!",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.ERROR_MESSAGE);
                if (answer == JOptionPane.NO_OPTION)
                    {
                    currentGUI.setStatusText("offline mode");
                    return;
                    }
                }
            }
            //if we connected successfully we update the status text
            currentGUI.setStatusText("connected to server");
        }
    
    //unction to disconnect from server
    public void disconnectFromServer()
        {
        if (!bConnected)
            return;
        out.println("logout");
        bConnected = false;
        currentGUI.setStatusText("offline mode");
        }
    
    //this function is used to flush the connection if the server becomes unreachable
    public void flushConnection()
        {
        try
            {
            in.close();
            out.close();
            serverSocket.close();
            bConnected = false;
            currentGUI.setStatusText("offline mode");
            }
        catch (IOException e)
            {
            System.err.println("Error while flushing connection!");
            System.err.println(e.toString());
            }
        }
    
    public boolean isConnectedToServer() { return bConnected; }
    
    //file chooser function
    public File chooseFile(boolean bIsShareOn)
        {
        JFileChooser chooseFile = new JFileChooser();
        if (bIsShareOn)
            chooseFile.setFileFilter(new ShareOnFileFilter());
        chooseFile.showOpenDialog(currentGUI);
        return chooseFile.getSelectedFile();
        }
    
    //pseudoping a host
    public String pseudoPing(String sHost)
        {
        PrintWriter pwPing;
        BufferedReader brPing;
        Socket pingSocket;

        try
            {
            //create socket and streams
            pingSocket = new Socket(sHost, iPingListenPort);
            pwPing = new PrintWriter(pingSocket.getOutputStream(), true);
            brPing = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
            }
        catch (UnknownHostException e)
            {
            System.err.println("Don't know about host: " + sHost);
            return null;
            }
        catch (IOException e)
            {
            System.err.println("Host unreachable: " + sHost);
            return null;
            }

        try
            {
            //pseudo ping time is an RTT time:
            //time elapsed between the outgoing and the incoming message
            long lEndTime = -1;
            long lStartTime = System.currentTimeMillis();
            pwPing.println("ping");
            String sPong = brPing.readLine();
            if (sPong.equals("pong"))
                lEndTime = System.currentTimeMillis();
            if (lEndTime == -1)
                return null;
            pwPing.close();
            brPing.close();
            pingSocket.close();
            return String.valueOf(lEndTime - lStartTime);
            }
        catch (IOException e)
            {
            System.err.println("Couldn't ping host: " + sHost);
            return null;
            }
        }
        
    //search files on the server
    public String search(String sToSearch)
        {
        try
            {
            out.println("search@" + sToSearch);
            String sReply = in.readLine();
            if (sReply.equals(""))
                return null;
            else
                return sReply;
            }
        catch (IOException e)
            {
            System.err.println("Error executing search!");
            System.err.println("Details: " + e.toString());
            JOptionPane.showMessageDialog(currentGUI, "Couldn't perform search!\nClient will now disconnect!", "Error!", JOptionPane.ERROR_MESSAGE);
            flushConnection();
            return null;
            }
        }
    
    /**function to return local IP
     * Features that are unsupported at the moment:
     * - use of NATs (more exactly the connection of multiple users behind the same NAT)
     * @TODO: handling NATs, handling everything, saving the world!
     */
    public String getLocalIP()
        {
        return sLocalIP;
        }
    
    //send shared file updates to the server
    public boolean updateShares(String sUpdate)
        {
        try
            {
            //we send the update string
            out.println(sUpdate);
            //and wait for answer
            String sReply = in.readLine();
            if (sReply.equals("ACK"))
                return true;
            else
                return false;
            }
        catch (IOException e)
            {
            System.err.println("Error updating shares!");
            System.err.println("Details: " + e.toString());
            flushConnection();
            return false;
            }
        }
    
    //function to parse a .shareon file
    public ParsedShareOn parseShareOn(File fIn)
        {
        //variables needed to create a ParsedShareOn class
        Vector<String> vRTTs = new Vector<String>();
        Hashtable<String, String> hPeers =  new Hashtable<String, String>();
        String sFileName;
        
        try
            {
            //create a file reader
            BufferedReader fReader = new BufferedReader(new FileReader(fIn));
            String sLine = fReader.readLine();
            //the SHAREON header is a must have thing :)
            if (!sLine.equals("SHAREON1"))
                {
                fReader.close();
                return null;
                }
            //if no file name is specified we can't do anything
            sFileName = fReader.readLine();
            if (sFileName == null)
                {
                fReader.close();
                return null;
                }
            //we collect the seeders and their RTTs
            while(!(sLine = fReader.readLine()).equals("END"))
                {
                if (sLine == null)
                    {
                    fReader.close();
                    return null;
                    }
                String sRTT = pseudoPing(sLine);
                if (sRTT != null)
                    {
                    vRTTs.add(sRTT);
                    hPeers.put(sRTT, sLine);
                    }
                }
            fReader.close();
            //and finally its showtime!
            ParsedShareOn psCurrent = new ParsedShareOn(vRTTs, hPeers, sFileName);
            return psCurrent;
            }
        catch (FileNotFoundException fe)
            {
            System.err.println("File not found!");
            System.err.println("Details: " + fe.toString());
            }
        catch (IOException ie)
            {
            System.err.println("I/O error while parsing the .shareon file!");
            System.err.println("Details: " + ie.toString());
            }
        return null;
        }
    
    //class to represent a parsed shareon file
    public class ParsedShareOn
        {
        private Vector<String> vRTTs;               //vector to store the rRTTs
        private Hashtable<String, String> hPeers;   //hashtable to store the peers with their RTTs as keys
        private String sFileName;                   //string to store the filename
        //file names + RTTs sorted in ascending order of the RTTs
        private Vector<String> vDisplayableResults = new Vector<String>();
        
        public ParsedShareOn(Vector<String> vRTTsIn, Hashtable<String, String> hPeersIn, String sFileNameIn)
            {
            vRTTs = vRTTsIn;
            hPeers = hPeersIn;
            sFileName = sFileNameIn;
            //sort the RTT vector
            Comparator cDescending = Collections.reverseOrder();
            Collections.sort(vRTTs, cDescending);
            Collections.reverse(vRTTs);
            Iterator vIter = vRTTs.iterator();
            while (vIter.hasNext())
                {
                String sRTT = (String)vIter.next();
                vDisplayableResults.add("RTT(ms): " + sRTT + " Peer: " + hPeers.get(sRTT));
                }
            }
        public String getFilename() { return sFileName; }
        public Vector<String> getRoundTripTimes() { return vRTTs; }
        public Hashtable<String, String> getPeersHashedWithRTTs() { return hPeers; }
        public Vector<String> getDisplayableResults() { return vDisplayableResults; }
        public String getBestPeer() { return hPeers.elements().nextElement(); }
        }
    
    //in case of exit, everything must be cleaned up
    public void exit()
        {
        try
            {
            tPseudoPingListener.interrupt();
            disconnectFromServer();
            out.close();
            in.close();
            serverSocket.close();
            }
        catch (IOException e) {}
        System.exit(0);
        }
    
    //class for downloading
    class DownThread extends Thread {

        String sPeer;       //source peer ip address
        String sFile;       //filename to download
        Socket sSocket;     //socket for transfer
        
        public DownThread(String _sPeer, String _sFile) {
            sPeer = _sPeer;
            sFile = _sFile;
            run();
        }

        //the actual downloading happens below
        @Override
        public void run() {
            try {
                //System.out.println("  (" + this.getId() + ") Attempting download of (" + sFile + ") from <" + sPeer + ":" + iFileTransferPort + ">");
                sSocket = new Socket(sPeer, iFileTransferPort);
                //System.out.println("  (" + this.getId() + ") Downloading (" + sFile + ") from <" + sSocket.getInetAddress() + ">");
                DataOutputStream dos = new DataOutputStream(sSocket.getOutputStream());
                dos.writeUTF(sFile);
                dos.flush();
                BufferedInputStream bis = new BufferedInputStream(sSocket.getInputStream());

                byte[] buffer = new byte[iBufferSize];
                FileOutputStream fos = new FileOutputStream(sFile);
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.flush();
                fos.close();
                dos.close();
                //System.out.println(" (" + this.getId() + ") completed");
                sSocket.close();
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    sSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
    
    //class for the uploadlistener
    class UploadListenerThread extends Thread
    {
            //class to manage file uploads
            class UploadThread extends Thread {

                Socket sSocket;

                public UploadThread(Socket s) {
                    sSocket = s;
                    run();
                }

                //the code to handle the upload
                @Override
                public void run() {
                    try {
                        DataInputStream dis = new DataInputStream(sSocket.getInputStream());
                        String filename = dis.readUTF();
                        //System.out.println("  (" + this.getId() + ") Uploading (" + filename + ") to <" + sSocket.getInetAddress() + ">");
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
                        BufferedOutputStream bos = new BufferedOutputStream(sSocket.getOutputStream());

                        byte[] buffer = new byte[iBufferSize];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }

                        bos.flush();
                        dis.close();
                        bis.close();
                        bos.close();
                        //System.out.println("  (" + this.getId() + ") completed");
                        sSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            sSocket.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
            //server socket for managing downloads
            ServerSocket uploadsocket = null;

            @Override
            public void run() {
                //infinte loop, waiting for connections
                while (true) {
                    try {
                        //System.out.println("Creating serversocket on port"+iFileTransferPort);
                        uploadsocket = new ServerSocket(iFileTransferPort);
                        //System.out.println("Uploadsocket created: ---"+uploadsocket.toString());
                        Socket client = uploadsocket.accept();
                        //System.out.println("Uploadsocket accept happened ---"+client.toString());
                        UploadThread upload_thread = new UploadThread(client);
                        upload_thread.start();
                        uploadsocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            uploadsocket.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
            }
        }
    }
    
    //start a new thread for download
    public void startDownload(String sSourceIP, String sFileName)
    {
        DownThread download_thread = new DownThread(sSourceIP, sFileName);
        download_thread.start();
    }
    
    //validate optional IP address
    private static boolean validIPAddress(String sIPAddress)
    {
        final Pattern IP_PATTERN = Pattern.compile("b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)b");
        return IP_PATTERN.matcher(sIPAddress).matches();
    }
    
    public static void main(String args[]) {
        if (args.length > 0) {
            String sServerAddr = args[0];
            validIPAddress(sServerAddr);
            //if (validIPAddress(sServerAddr)) {ShareOnClient clientInstance = new ShareOnClient(sServerAddr);}
            ShareOnClient clientInstance = new ShareOnClient(sServerAddr);
        } else {ShareOnClient clientInstance = new ShareOnClient("192.168.1.200");}

        
    }
}