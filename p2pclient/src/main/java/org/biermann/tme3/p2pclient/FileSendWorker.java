/**
 * FileSendWorker.java
 * 
 * FileSendWorker is a Runnable which implements a file transfer operation between this host and 
 * a peer. 
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSendWorker implements Runnable
{
	private Socket peerSocket;
	private String shareFilePath;
	
	/**
	 * Sets the Socket to use to transfer file data, and the path
	 * from which data is to be transferred.
	 * @param peerSocket Socket which will be used to transfer data to the peer.
	 * @param shareFilePath String with path to the file to transfer
	 */
	public FileSendWorker(Socket peerSocket, String shareFilePath)
	{
		this.peerSocket = peerSocket;
		this.shareFilePath = shareFilePath;
	}
	
	public void run()
	{
		String fileName = null;
		OutputStream outStream = null;
		
		try
		{
			fileName = readFileName(peerSocket.getInputStream());                         //Read the file name from peer.
		}catch (Exception fnReadEx)
		 {
			System.err.println("Error reading the file name from peer " + peerSocket.getRemoteSocketAddress());
			System.err.println("Receive data is " + fileName);
			System.err.println(fnReadEx.getMessage());
			fnReadEx.printStackTrace();
			return;
		 }
		
		/*Ensure that fileName has been set to file name received from peer */
		if (fileName != null)
		{
			String errMsg = null;
			Path filePath = Paths.get(shareFilePath, fileName.trim());               //Create a path consisting of receive directory and the file name.
			String filePathStr = filePath.normalize().toAbsolutePath().toString();   //Normalize file path and ensure it is absolute before stringifying it.
			try
			{
				outStream = peerSocket.getOutputStream();
				sendFile(outStream, filePathStr, peerSocket.getSendBufferSize());
			}
			catch(FileNotFoundException notFoundEx)
			{
				errMsg = "File " + fileName + " could not be found.";
			}
			catch (Exception fileTransferEx)
			{
				errMsg = "Error transferring file to peer " + peerSocket.getRemoteSocketAddress() + " " + 
						fileTransferEx.getMessage();
			}
			
			if (errMsg != null)
			{
				System.err.println(errMsg);                                                       //Output error message.
				try
				{
					peerSocket.getOutputStream().write(("ERR " + errMsg + "\n").getBytes() );    //Write error message to peer in basic header "ERR".
				}
				catch(Exception ex)
				{
					System.err.println("Could not write error message to peer: " +
							peerSocket.getRemoteSocketAddress() + ex.getMessage());
				}
			}//End-if
		}//End-if
		
		try
		{
			outStream.close();                                                       //Close the OutputStream to signal end of transmission
		} catch (IOException closeEx)
		 {
			System.err.println("Unable to close connection to peer: " + closeEx.getMessage());
		 }
		
		
	}
	
	
	private static String readFileName(InputStream inStream) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(inStream));
		return bufReader.readLine().trim();      //Return the received filename from peer, with whitespace at ends trimmed.                        
	}
	
	
	/**
	 * 
	 * @param outStream OutputStream which will be used to output file data.
	 * @param filePath String holding local absolute file path
	 * @param bufSize int Size of the buffer used for file data
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void sendFile(OutputStream outStream, String filePath, int bufSize) throws IOException, FileNotFoundException
	{
		try(FileInputStream fileInStream = new FileInputStream(filePath);)
		{		
			/*Create a buffer and get the reference to the backing array
			 * to allow direct write from the FileInputStream.
			 */
			ByteBuffer buf = ByteBuffer.allocate(bufSize);
			byte[] bufBackingArr = buf.array();                      //Reference to byte[] backing buf
			int bytesRead = 0;
			
			outStream.write("OK \n".getBytes());                     //Write success header before writing data to peer.
			while ((bytesRead = fileInStream.read(bufBackingArr)) > 0)
			{
				outStream.write(bufBackingArr, 0, bytesRead);       //Write bytesRead bytes from the buffer to peer, starting at index 0.
			}
		}
		
	}
	
}