/**
 *  Provides a simple REST API to clients to support file sharing. Sharing peers may register a new file for sharing,
 *  unregister a shared file, and perform host address lookup for a shared file. 
 *  
 *  All operations are mapped to REST resource paths. Clients must use the appropriate HTTP method for
 *  each operation.
 *  
 *  @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.resources;

import java.net.URI;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.biermann.tme3.p2pindex.controllers.IndexServiceController;
import org.biermann.tme3.p2pindex.messages.FileMessage;

@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
@Path("/sharedfiles")
public class SharedFiles 
{

	private IndexServiceController controller;                                    //Controller for interacting with data classes.
		
	public SharedFiles()
	{
		controller = new IndexServiceController();                               //Initialize the controller.
	}
	
	
	@GET
	@Path("/{filename}")
	public Response getSharingPeer(@PathParam("filename") String fileName, @Context UriInfo uriInfo)
	{
		FileMessage message = new FileMessage(fileName, controller.getPeerHost(fileName));
		
		/* Return a "200 OK" response containing the FileMessage and the URL for accessing
		 * the resource.
		 */
		return Response.ok(message)
				.location(createSharedFileURI(fileName, message.getHostAddress(), uriInfo))
				.build();
	}
	
	
	@GET
	@Path("/filename={filename}/peeraddress={peeraddress}")
	public Response checkSharingPeer(@PathParam("filename") String fileName, @PathParam("peeraddress") String peerAddress, @Context UriInfo uriInfo)
	{
		controller.affirmPeerSharing(fileName, peerAddress);                     //Throws a FileNotSharedException if file not shared.
		FileMessage message = new FileMessage(fileName, peerAddress);
		
		/* Return a "200 OK" response containing the FileMessage and the URL for accessing
		 * the resource. This URL should be the same as the one mapped to this method, including
		 * the path params.
		 */
		return Response.ok(message)
				.location(createSharedFileURI(fileName, peerAddress, uriInfo))
				.build();
	}
	
	
	@POST
	public Response shareFile(FileMessage clientMessage, @Context UriInfo uriInfo)
	{
		String fileName = clientMessage.getFileName();
		String hostAddress = clientMessage.getHostAddress();
		
		/*Attempt to register file in db. Throws a FileAlreadySharedException if the file is already shared. */
		controller.registerFile(fileName, hostAddress);           
		
		/*Return a "201 Created" response with the location header set to the URL that
		 * can be used to access the resource.
		 */
		return Response.created(createSharedFileURI(fileName, hostAddress, uriInfo)).build();
	}
	
	
	@DELETE
	@Path("/filename={filename}/peeraddress={peeraddress}")
	public Response unshareFile(@PathParam("filename") String fileName, @PathParam("peeraddress") String peerAddress)
	{
		/*Attempt to deregister file by removing database record. Throws a FileNotSharedException if the file
		 * is not shared by the specified peer.
		 */
		controller.deregisterFile(fileName, peerAddress);                    
		
		/*Return a "204 No Content" response on succesful deregistration */
		return Response.noContent().build();
	}
	
	
	private static URI createSharedFileURI(String fileName, String peerAddress, UriInfo uriContext)
	{
		/*Return a URI constructed starting with the base URL for accessing services on the server. */
		return uriContext.getBaseUriBuilder()
				.path(SharedFiles.class)                                   //Add path element for accessing resources in this class
				.path(SharedFiles.class, "checkSharingPeer")               //Add specific path mapped to the checkSharingPeer() method
				.resolveTemplate("filename", fileName)                     //Resolve the filename path param required by checkSharingPeer()
				.resolveTemplate("peeraddress", peerAddress)               //Resolve the peeraddress path param also required by checkSharingPeer()
				.build();
			
	}
	
	
	
}
