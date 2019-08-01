/**
 * A type of RuntimeException which occurs when a file is not shared. Possible
 * scenarios include a peer which does not exist or a peer which exists but
 * is not sharing a given file.
 * 
 * @author Oloff Biermann
 */

package org.biermann.tme3.p2pindex.exception;

public class FileNotSharedException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4623639569874811482L;

	public FileNotSharedException(String message)
	{
		super(message);
	}
}
