/**
 * FileShared.java
 * 
 * Concrete subclass of SharingData which supports interaction with the fileshared table in
 * the database. Contains attributes for the GUID (primary key) of a peer which is
 * sharing the file, and the GUID (primary key) of that file.
 * 
 * There are two main cases where this class is used:
 * 1) Creating a new FileShared instance in memory and committing its attributes to the 
 *    database in a new fileshared record.
 * 2) Querying the database to obtain an existing record and then creating a fileshared instance in 
 *    memory with corresponding attributes.
 * 
 * @author Oloff Biermann
 * 
 */

package org.biermann.tme3.p2pindex.data;

import java.sql.*;

public class FileShared extends SharingData
{
	private Peer sharingPeer;
	private PeerFile sharedFile;
	
	public FileShared(Connection dbConnection, Peer sharingPeer, 
			PeerFile fileToShare, boolean commit) throws SQLException
	{
		databaseConnection = dbConnection;
		this.sharingPeer = sharingPeer;
		sharedFile = fileToShare;
	
		/*If record must be committed, and it doesn't already exists, store it in db */
		if (commit && !(checkExists()))
			commitData();
	}
	
		
	/**Deletes the relationship and the record for the PeerFile being shared. Note that
	 * the Peer is not deleted, as it may be involved in other relationships.
	 * 
	 * @return boolean true if relationship is deleted successfully, false otherwise
	 */
	public boolean delete() throws SQLException
	{
		if (!(checkExists()))				//Check if the relationship exists.
			return false;
		
		if (!(sharedFile.delete()))        //Attempt to delete the PeerFile record.
			return false;
		
		String query = "DELETE FROM fileshared "
						+ "WHERE peerID = ? AND fileID = ?";
				
		try (PreparedStatement deleteStatement = databaseConnection.prepareStatement(query);)
		{
			deleteStatement.setInt(1, this.sharingPeer.getGUID());
			deleteStatement.setInt(2, this.sharedFile.getGUID());
					
			deleteStatement.executeUpdate();                            //Execute the delete operation.
			return true;
		}
		
	}
	
	/**Search the FileShared table in the database to determine
	 * if there are any peer sharing the file with the name specified.
	 * If so, get a Peer sharing the desired file.
	 * 
	 * @param fileName String with the name of the file requested
	 * @return Peer representing a Peer which is currently sharing the file,
	 * or null if no peers are sharing the specified file.
	 */
	public static Peer getFilePeer(Connection dbConnection, String fileName) throws SQLException
	{
		Peer availablePeer = null;
		
		/*Uses inner joins to the fileshared table and peerfile table to determine
		 * if any of the records in fileshared associated with this Peer match
		 * files with names which match fileName.
		 */
		String query = "SELECT DISTINCT peerGUID, hostAddress "
						+ "FROM peer "
						+ "INNER JOIN fileshared ON peerGUID = fileshared.peerID "
						+ "INNER JOIN peerfile ON fileID = peerfile.fileGUID "
						+ "WHERE fileName = ?";
		
		try(PreparedStatement peerStatement = dbConnection.prepareStatement(query))
		{
			peerStatement.setString(1, fileName);
			ResultSet result = peerStatement.executeQuery();
			if (result.first())
			{
				availablePeer = new Peer(dbConnection, result.getString(2), result.getInt(1));    //Create new Peer with the host address and GUID.
			}
		}
		
		return availablePeer;
	}
	
	
	/**
	 * Attempts to destroy the relationship between a Peer and 
	 * a file being shared. The file is identified by a fileName.
	 * 
	 * All matching relationships will be destroyed, as will their
	 * associated "file" records.
	 * @param dbConnection Connection used to access database
	 * @param curPeer Peer which is currently sharing the file
	 * @param fileName String with name of file being shared
	 * @return boolean true on success, false otherwise
	 * @throws SQLException if database query fails
	 */
	public static boolean deleteAssociation(Connection dbConnection, Peer curPeer, String fileName) throws SQLException
	{
		boolean success = false;
		/*Determine if the specified peer is sharing the file by searching for matching
		 *records in the peerfile table. Specifically, delete a peerfile record where
		 *the filename matches the specified name and the fileGUID is among the files
		 *being shared by this peer.
		 *
		 * Deleting the peerfile record will trigger cascading delete on the fileshared table 
		 * to delete the relationship between the peer and the file.
		 */
		String deleteQuery = "DELETE FROM peerfile "
								+ "WHERE fileName = ? AND fileGUID IN "
								+ "(SELECT DISTINCT fileID "
								+ " FROM fileshared "
								+ " WHERE peerID = ?)";
		
		try (PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteQuery))
		{
			deleteStatement.setString(1, fileName);          //Bind 1st param for the file name.
			deleteStatement.setInt(2, curPeer.getGUID());    //Bind second param for peer ID.
			/*Set success to true true if a row is deleted, i.e. the query returned the row number of
			 * the deleted row so that executeUpdate() does not return 0. Otherwise set success to
			 * false.
			 */
			 success = ( (deleteStatement.executeUpdate() > 0) ? true : false) ;
		}
		
		/*If query failed to delete relationship, return false */
		 if (!success)
			 return false;
		
		 
		 
		return true;
	}
	
	
	/**
	 * Determines if the specified peer is sharing the file with the specified name. The
	 * collation of the database will determine if match is case-insensitive or case-sensitive.
	 * 
	 * @param peerId int GUID of the peer for which we want to check sharing status
	 * @param fileName String holding the name of the file  of which the sharing status
	 * in relation to the peer is to be checked
	 * @return boolean true if the peer is sharing the file, false otherwise
	 * @throws SQLException If the database query fails or some other database-related exception
	 * occurs.
	 */
	public static boolean checkFileShared(Connection dbConnection, int peerId, String fileName) throws SQLException
	{
		String query = "SELECT fileName "
						+"FROM peerfile "
						+ "INNER JOIN fileshared ON peerfile.fileGUID = fileshared.fileID "
						+ "WHERE peerID = ? AND fileName = ?";
		
		
		try (PreparedStatement checkStatement = dbConnection.prepareStatement(query);)
		{
			/*Set params to peerId and fileName for the prepared statement */
			checkStatement.setInt(1, peerId);
			checkStatement.setString(2, fileName);
			/*Return true if result contains a row, indicating that the peer is sharing a file
			 * with the specified name. Otherwise, return false. */
			return checkStatement.executeQuery().first(); 
		}
					
	}
	
	/**
	 * Checks if the relationship between Peer and PeerFile already exists in
	 * fileshared table. This check is done using the peerID and fileID
	 * for this FileShared instance.
	 * 
	 * @return boolean true if relationship between Peer and Peerfile exists in database, false otherwise
	 */
	private boolean checkExists() throws SQLException
	{
		String query = "SELECT * "
						+ "FROM fileshared "
						+  "WHERE peerID = ? AND fileID = ? ";
		
		try (PreparedStatement checkStatement = databaseConnection.prepareStatement(query);)
		{
			checkStatement.setInt(1, this.sharingPeer.getGUID());
			checkStatement.setInt(2, this.sharedFile.getGUID());
			
			ResultSet result = checkStatement.executeQuery();
			if (result.first())
				return true;
		}
		
		return false;
		
	}
	
	
	/** Creates a new entry in the database for this FileShared instance.
	 *  Both the peer ID and the file ID are stored as foreign keys
	 *  to establish a relationship between the corresponding 
	 *  files and the peers.
	 *  
	 *  @return int 0 on success
	 */
	protected int commitData() throws SQLException
	{
		String query = "INSERT INTO fileshared (peerID, fileID) "
				 + "VALUES (" + this.sharingPeer.getGUID() +", " + this.sharedFile.getGUID() + "); ";
	
		try (Statement storeStatement = databaseConnection.createStatement();)
		{
			storeStatement.executeQuery(query);
			
			return 0;
		}
	}
	
	
	
	
	
	
}
