/**
 * Maps FileAlreadySharedException to a particular response, thus avoiding the default server HTTP error
 * page and instead providing a custom ErrorMessage. The ErrorMessage is serialized and returned with
 * a "409 Conflict" status code.
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
public class FileAlreadySharedExceptionMapper implements ExceptionMapper<FileAlreadySharedException> 
{
	@Override
	public Response toResponse(FileAlreadySharedException ex) 
	{
		int responseCode = Status.CONFLICT.getStatusCode();
		ErrorMessage msg = new ErrorMessage(ex.getMessage(), responseCode);
		return Response.status(responseCode).entity(msg).build();
	}
}
