/**
 * A type of RuntimeException which occurs when a file is already shared by a
 * given peer. 
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.exception;

public class FileAlreadySharedException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3220839977227313169L;

	public FileAlreadySharedException(String message)
	{
		super(message);
	}
	
}
