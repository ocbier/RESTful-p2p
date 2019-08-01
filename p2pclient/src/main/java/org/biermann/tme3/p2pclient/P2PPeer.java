/**
 * P2PPeer.java
 * 
 * Class P2PPeer represents a peer in the P2P File Sharing application. Supports sharing files and downloading files from other peers. Provides
 * a GUI interface which is managed as an instance of p2pclient.gui.P2PPeerGui. Specifically, the P2PPeerGUI allows:
 * 1) Sharing a file to make it available to all peers
 * 2) Unsharing a shared file to deny access to peers
 * 3) Downloading a file from a peer
 * 
 * The P2PPeerGui instance interacts with the controller (p2pclient.P2PPeerController instance), providing users access to the system functionality.
 * 
 * Shared files must be stored in the DEFAULT_SHARE_DIR directory. Downloaded files will be stored in the DEFAULT_RECEIVE_DIR directory. Most details of
 * the download operation are handled by p2pclient.P2PPeerController. An important point here is that downloads are handled on separate threads to maintain
 * responsiveness in the main thread.  
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * The peer will listen for connections on the port DEFAULT_LISTEN_PORT. Like the download operation, the details are handled by the P2PPeerController
 * and incoming requests are handled on a seperate thread which will perform data transfers between this peer and the remote peer.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
 
import org.biermann.tme3.p2pclient.gui.P2PPeerGUI;

public class P2PPeer
{
	public static final String DEFAULT_SHARE_DIR = "../files/sharing";
	public static final String DEFAULT_RECEIVE_DIR = "../files/received";
	public static final String APP_NAME = "P2P File Sharing";
	public static final int DEFAULT_LISTEN_PORT = 3333;
	public static final String DEFAULT_INDEX_URI = "http://localhost:8080/p2pindex/webapi/sharedfiles";
	
	
	
	public static void main(String[] args) 
	{
		/*Ensure that the receive directory is created if it does not exist */
		File receiveDir = Paths.get(DEFAULT_RECEIVE_DIR).toAbsolutePath().toFile();
		receiveDir.mkdirs();                                                                //Make directories if needed for receive dir.
	
		String indexServiceUri = (args.length > 0 ? args[0] : DEFAULT_INDEX_URI);           //Use default path for index service if none is provided as arg.
		
		try
		{
			new URL(indexServiceUri);
		}
		catch (MalformedURLException invalidURL)
		{
			System.err.println("The URL "+ indexServiceUri + " is not valid.");
			System.exit(-1);
		}
		
		
		/*Pass default args for receive directory, send directory, listening port, and URI for the index service to
		 *the P2PPeerController ctor*/
		P2PPeerController peerController = new P2PPeerController(DEFAULT_SHARE_DIR, DEFAULT_RECEIVE_DIR, DEFAULT_LISTEN_PORT, indexServiceUri);
		try
		{
			peerController.listenForPeers();                //Listen for peer connections on separate thread.
		} catch (IOException ioEx)
		{
			System.err.println("Initialization of file sharing failed due to connection error.");
		}
		
		/*Initialize the GUI, creating a window (frame) to interact with user. Pass the name
		 * of the application and reference to the P2PPerrController instance. */
		new P2PPeerGUI(APP_NAME, peerController);
	}
	
	
	
	
	
}
