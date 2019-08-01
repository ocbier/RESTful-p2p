package org.biermann.tme3.p2pclient.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileMessage 
{
	private String fileName;
	private String hostAddress;
	
	
	public FileMessage()
	{
		
	}
	
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
