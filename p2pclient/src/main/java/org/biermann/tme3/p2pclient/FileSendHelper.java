/**
 * FileSendHelper.java
 * 
 * FileSendHelper is a Runnable which supports the file send operations from this host
 * to another host. Listens for connections from peers. When a connection is accepted,
 * the send operation is handled by a worker thread.
 * 
 * Considers the specified sharing directory which is used as the source to locate
 * the file to transfer. Also considers the specified port when listening for connections.
 
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSendHelper implements Runnable
{
	private ServerSocket fileSendSock;
	private String shareDir;
	private ExecutorService threadPool;
	
	/**
	 * Creates a FileSendHelper which uses the specified location to locate
	 * files to send. Uses the server port indicated to listen for client connections on
	 * a ServerSocket.
	 * @param sharedFiles String containing path to directory in which shared files are located
	 * @param serverPort int TCP port number to use to listen for client connections
	 * @throws IOException If creation of the ServerSocket fails.
	 */
	public FileSendHelper(String sharedFiles, int serverPort) throws IOException
	{
		shareDir = sharedFiles;
		fileSendSock = new ServerSocket(serverPort);
		threadPool = Executors.newCachedThreadPool();
	}
	
	public void run()
	{
		boolean done = false;
		Socket clientSock = null;
		while (!done)
		{
			/*Wait for client and attempt to establish connection */
			try
			{
				clientSock = fileSendSock.accept();
			} catch (Exception acceptEx)
			{
				System.err.println("Error establishing peer connection: " + acceptEx.getMessage());
				clientSock = null;
			}
			
			/*Pass the socket for the connection to a FileSendWorker and continue listening for connections
			 * on the current thread to allow other peers to connect. If the clientSock is null,
			 * an error has occurred, so in that case do not submit a worker.
			 */
			if (clientSock != null)
				threadPool.submit(new FileSendWorker(clientSock, shareDir));
			
		}
	}
	
	
}
