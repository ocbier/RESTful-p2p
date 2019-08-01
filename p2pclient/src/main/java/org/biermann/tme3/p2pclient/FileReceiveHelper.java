/**
 * FileReceiveHelper.java
 * 
 * FileReceiveHelper is a Runnable which supports a file receive operation from a particular host.
 * The location in which to store the file and file name are considered when performing receive operation
 * to recreate the file on local storage from the received data.
 */
package org.biermann.tme3.p2pclient;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

public class FileReceiveHelper implements Runnable
{
	private Socket fileReceiveSock;
	private String remoteHost, receiveDir;
	private int remotePort;
	private DownloadStatus downloadStatus;
	
	/**
	 * Creates a FileReceiveHelper with the specified remote host and port, and directory
	 * for receiving files. The DownloadStatus instance is used to obtain the file name
	 * of the file which will be received.
	 * @param remoteHost String with host address of the sender
	 * @param remotePort int with remote port on sender
	 * @param receiveDir String holding path to directory for receiving file.
	 * @param downloadStatus DownloadStatus instance which holds name of file to be received, among other
	 * status data.
	 */
	public FileReceiveHelper(String remoteHost, int remotePort, String receiveDir, DownloadStatus downloadStatus) 
	{
		this.receiveDir = receiveDir;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.downloadStatus = downloadStatus;
	}
	
	
	/**Reads file data from the remote host and store data to disk in the receive
	 * directory 
	 */
	public void run()
	{
		String errMsg = null;
		InputStream inStream = null;
		String outcome = "";
		boolean success = false;
		String receiveFileName = downloadStatus.getFileName();            //Get name of file to download.
		
		try
		{
			fileReceiveSock = new Socket(remoteHost, remotePort);         //Create the socket.
		}
		catch (Exception socketCreationEx)
		{
			errMsg = "Error. Could not create connection to peer at " + remoteHost + ":" + remotePort + ". "
					+ "Exception: " + socketCreationEx.getMessage();
			return;
		}

		try
		{
			inStream = fileReceiveSock.getInputStream();
		} catch (IOException inStreamEx)
		{
			errMsg = "Error. Could not get the input stream for peer connection " + fileReceiveSock.getRemoteSocketAddress();
			return;
		}
		
		try
		{
			/*Set status message to indicate download is starting */
			downloadStatus.setStatusMessage("Download status for " + receiveFileName + ": Download starting...");
			
			success = receiveFileData(inStream);                      //Attempt to get data from peer.
			/*Outcome message is determined by success or failure of the receive operation */
			outcome = success ? "Finished downloading." :
				"Error. The shared file could not be transmitted. It may no longer be available from this peer.";
		}
		catch(Exception readEx)
		{
			errMsg = "Exception occurred while transmitting data from peer.";
		}
		
		finally
		{
			System.out.println(outcome);                                //Output the outcome to standard output stream.
			
			try
			{
			  inStream.close();
			}
			catch (IOException inStreamCloseEx)
			{
				System.err.println("Error closing the connection input stream");
			}
			
			if (errMsg != null)                                          //Output any specific error message.
			{
				System.err.println(errMsg);
				outcome = errMsg;                                        //Set outcome to error message.
			}
			
			downloadStatus.setStatusMessage("Download status for " + receiveFileName + ": " + outcome);   //Set status message of downloadStatus to outcome.
			downloadStatus.setTerminated();                            //ALWAYS notify waiting threads that download is terminated.
		}
		
	}
	
	
	
	/**Reads the file data from the InputStream and stores it in a new local file.
	 * 
	 * @param inputStream InputStream from which to read file bytes
	 * @return boolean true if transmission completes successfully, false otherwise.
	 */
	private boolean receiveFileData(InputStream inputStream)
	{
		String receiveFileName = downloadStatus.getFileName();
		FileOutputStream fileOutStream = null;
		File downloadedFile = null;
		String errMsg = null;
		ByteBuffer buf;
		byte[] bufBackingArr;
						
		try
		{
			/*Create a new file in the receive directory */
			downloadedFile = Paths.get(receiveDir, receiveFileName).toAbsolutePath().toFile();
			downloadedFile.createNewFile();
			fileOutStream = new FileOutputStream(downloadedFile);    //FileOutputStream to write to the new file.
		}
		catch (IOException fileOpenEx)
		{
			errMsg = "Error. Could not create the new file " + receiveFileName;
			return false;
		}
			
		try
		{
			buf = ByteBuffer.allocate(fileReceiveSock.getReceiveBufferSize());
			bufBackingArr = buf.array();
		}
		catch (SocketException sockEx)
		{
			errMsg = "Error getting receive buffer size for socket"; 
			return false;
		}
		
		/*Send request with file name to peer */
		try 
		{
			fileReceiveSock.getOutputStream().write((receiveFileName + "\n").getBytes());
		} catch (IOException e) 
		{
			errMsg = "Error sending file request to peer";
			return false;
		}
		
		/*Get response with file data, and store data to disk */
		try
		{
			BufferedReader headerReader = new BufferedReader(new InputStreamReader(inputStream));
			
			/*Read the simple header from the peer. If it begins with the code "ERR", an error occurred. In this case,
			 * output the header contents and return false. Otherwise, discard the header and begin reading file data.
			 */
			String header = headerReader.readLine().trim();
			if (header.startsWith("ERR"))
			{
				errMsg = header.substring(3, header.length());              //Get error message in header.
				return false;
			}
			
			downloadStatus.setStatusMessage("Download status for " + downloadStatus.getFileName() + "downloading...");
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(bufBackingArr)) > 0)
			{
				try
				{
					fileOutStream.write(bufBackingArr, 0, bytesRead);        //Write bytesRead bytes from buffer to fileOutStream, starting at index 0.
					
				} catch (IOException writeEx)
				  {
					errMsg = "Error writing to file " +receiveFileName;
					return false;
				  }
			}
			
			return true;	                                                 //Return true if transmission completes without exception thrown.
		} catch (IOException peerReadException)
		  {
			errMsg = "Error reading file data from peer " + fileReceiveSock.getRemoteSocketAddress();
			return false;
		  }
		
		finally
		{
			try
			{
			  fileOutStream.close();                                        //Ensure stream is closed.
			}
			catch (IOException fileCloseEx)
			{
				System.err.println("Error closing file output stream.");
			}
			
			/*If an exception occurred during transmission, output error message and delete file. */
			if (errMsg != null)
			{
				System.err.println(errMsg);                                   //Print the error message
				downloadedFile.delete();                                      //Delete the temporary file, if it exists.
			}
		}
					
	}
	
	
}
