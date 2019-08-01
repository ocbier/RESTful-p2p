/**
 * ErrorMessage.java
 * 
 * Represents an error message which my be serialized as XML for transmission. Contains fields
 * for the message text and a custom status code.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorMessage 
{
	private String message;
	private int statusCode;
	
	public ErrorMessage()
	{}
	
	public ErrorMessage(String message, int statusCode)
	{
		this.message = message;
		this.statusCode = statusCode;
	}
	
	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public int getStatusCode() 
	{
		return statusCode;
	}

	public void setStatusCode(int statusCode) 
	{
		this.statusCode = statusCode;
	}

	
	
	
	
}
