/**
 * DownloadStatus.java
 * 
 * DownloadStatus represents the basic download status of a file. The file is identified by name and may either be 
 * downloading or completely downloaded. Allows threads to communicate the status of a download
 * between each other.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadStatus 
{
	private String fileName;
	private String statusMessage;
	private Condition terminatedCondition;
	private Lock lock;
	private boolean downloadTerminated;
	
	public DownloadStatus(String fileName)
	{
		this.fileName = fileName;
		statusMessage = "";                 //Set empty status message.
		lock = new ReentrantLock();
		terminatedCondition = lock.newCondition();
		downloadTerminated = false;
	}
	
	/**Call to mark download as terminated. This will notify any threads
	 * waiting on Condition TerminatedCondition.
	 */
	public void setTerminated()
	{
		try
		{
			lock.lock();
			downloadTerminated = true;
			
			terminatedCondition.signal();                                 //Signal waiting threads that download finished
		}
		finally
		{
			lock.unlock();
		}
		
	}
	
	/**
	 * Awaits the completion of the download and returns true at that point.
	 * @return true when download completes
	 */
	public boolean waitForTermination()
	{
		try
		{
			lock.lock();
			while(!downloadTerminated)
			{
				terminatedCondition.awaitUninterruptibly();
			}
			
		}
		finally
		{
			lock.unlock();
		}
		return downloadTerminated;
	}
	
	/**
	 * Get the file name for the file being downloaded.
	 * @return String with file name
	 */
	public String getFileName()
	{
		return fileName;
	}
	
	/**
	 * Message which returns the download status. Threads always acquire the monitor when
	 * reading the message.
	 * @return String with download status
	 */
	public synchronized String getStatusMessage()
	{
		return statusMessage;
	}
	
	
	/**
	 * Sets the download status message to the specified String. Threads always acquire the
	 * monitor when updating the message.
	 * @param message String containing download status message.
	 */
	public synchronized void setStatusMessage(String message)
	{
		statusMessage = message;
		
	}
	
	
}
