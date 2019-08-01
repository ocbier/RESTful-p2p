/**
 * Maps FileNotSharedException to a particular response, thus avoiding the default server HTTP error
 * page and instead providing a custom ErrorMessage. The ErrorrMessage is serialized and returned
 * with a "404 Not Found" status in the HTTP response.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.biermann.tme3.p2pindex.messages.ErrorMessage;

@Provider
public class FileNotSharedExceptionMapper implements ExceptionMapper<FileNotSharedException>
{

	@Override
	public Response toResponse(FileNotSharedException ex) 
	{
		int responseCode = Status.NOT_FOUND.getStatusCode();
		ErrorMessage msg = new ErrorMessage(ex.getMessage(), responseCode);
		return Response.status(responseCode).entity(msg).build();
	}
	
}
