/**
 * Maps RuntimeException to a custom response to prevent the default server HTTP error page. The ErrorMessage 
 * is serialized and returned with an "500 Internal Server Error" response.
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
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> 
{

	@Override
	public Response toResponse(RuntimeException ex) 
	{
		int statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
		ErrorMessage generalErr = new ErrorMessage("A server error occurred.", statusCode);
		return Response.status(statusCode).entity(generalErr).build();
				
	}
	
}
