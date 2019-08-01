/**
 * Maps WebApplicationExceptions to a particular response, thus avoiding the default server HTTP error
 * page and instead providing a custom ErrorMessage. The ErrorMessage is serialized and returned
 * with the appropriate HTTP status code which is obtained from the WebApplicationException instance.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.biermann.tme3.p2pindex.messages.ErrorMessage;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException>
{

	@Override
	public Response toResponse(WebApplicationException ex) 
	{
		int responseCode = ex.getResponse().getStatus();
		ErrorMessage errMsg = new ErrorMessage("Unable to process request. Exception: " +ex.getMessage(), responseCode);
		return Response.status(responseCode).entity(errMsg).build();
	}

}
