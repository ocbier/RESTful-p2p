/**
 * FileMessage.java
 * 
 * Represents a message containing the name of a file and the host which 
 * shares that file. May be serialized as XML for transmission. Contains 
 * fields for the file name and the host address, as well as methods for 
 * accessing and modifying these values.
 * 
 * @author Oloff Biermann
 */

package org.biermann.tme3.p2pindex.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileMessage 
{
	private String fileName;
	private String hostAddress;
	
	
	public FileMessage()
	{}
	
	public FileMessage(String fileName, String hostAddress)
	{
		this.fileName = fileName;
		this.hostAddress = hostAddress;
	}

	public String getFileName() 
	{
		return fileName;
	}

	public void setFileName(String fileName) 
	{
		this.fileName = fileName;
	}

	public String getHostAddress() 
	{
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) 
	{
		this.hostAddress = hostAddress;
	}
	
	
}
