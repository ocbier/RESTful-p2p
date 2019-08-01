/**
 * PeerFile.java
 * 
 * Concrete subclass of SharingData which represents a file. Contains attributes for the file GUID
 * (primary key) and file name.
 * 
 * The two main cases where this class is used are:
  * 1) Creating a new PeerFile instance in memory and committing its attributes to the 
 *    database in a new peerfile record.
 * 2) Querying the database to obtain an existing record and then creating a PeerFile instance in 
 *    memory with corresponding attributes.
 *    
 * Provides methods for deleting the corresponding record (implements delete()), querying the database
 * to look up a file name by GUID, and checking if a file with a given name exists. Also provides
 * accessors methods for the GUID and file name attributes.
 * 
 * @author Oloff Biermann
 */

package org.biermann.tme3.p2pindex.data;

import java.sql.*;

public class PeerFile extends SharingData
{
	private String fileName;
	private int fileGUID;
	
	/**
	 * Creates a PeerFile instance for a file record which already exists
	 * in the database. 
	 * 
	 * @param dbConnection Connection for the database.
	 * @param fileGUID int value for GUID of file
	 * @throws SQLException If writing to database fails.
	 */
	public PeerFile(Connection dbConnection, int fileGUID) throws SQLException
	{
		databaseConnection = dbConnection;
		this.fileGUID = fileGUID;
		fileName = queryFileName();
	}
	
	/**Creates a PeerFile instance in memory and commits the corresponding
	 * record to the database.
	 * @param dbConnection Connection for the database.
	 * @param fileName String with name identifying file for sharing
	 * @throws SQLException If writing to database fails.
	 */
	public PeerFile(Connection dbConnection, String fileName) throws SQLException
	{
		databaseConnection = dbConnection;
		this.fileName = fileName;
		fileGUID = commitData();     //Store record in DB, set the fileGUID attribute.
	}
	
	/**
	 * Get the GUID of this cached PeerFile instance. 
	 * @return int value of file GUID.
	 */
	public int getGUID()
	{
		return fileGUID;
	}
	
	public boolean delete() throws SQLException
	{
		/*If the record does not exist, return false */
		if (!fileExists(this.databaseConnection, this.fileName))
			return false;
		
		String query = "DELETE FROM peerFile "
					   + "WHERE fileGUID = ? ";
				
		try (PreparedStatement deleteStatement = databaseConnection.prepareStatement(query);)
		{
			deleteStatement.setInt(1, this.fileGUID);                       //Set the value of the first (and only) parameter to GUID for this Peer.
			 
			/*Execute the prepared statement to delete the matching record */
			deleteStatement.executeUpdate();
			return true;
		}
	}
	
	
	/**Checks if a file with the given name exists.
	 * 
	 * @param dbConnection Connection to use to query the database.
	 * @param name String name of the file to check.
	 * @return boolean true if file exists, false otherwise
	 * @throws SQLException If error occurs while querying database.
	 */
	public static boolean fileExists(Connection dbConnection, String name) throws SQLException
	{
		String query = "SELECT fileName "
						+ "FROM peerfile "
						+  "WHERE fileName = ?;";
		
		try (PreparedStatement checkStatement = dbConnection.prepareStatement(query);)
		{
			checkStatement.setString(1, name);                       //Set value of parameter to provided name.
			 
			/*Execute the prepared statement to try to get a matching record. */
			ResultSet result = checkStatement.executeQuery();
			if(result.first())                                        //If there is a row in the result set, we know the file exists, so return true.
				return true;
		}
		return false;                                                 //Otherwise, return false.
	}
	
	
	
	/**Retrieves the filename with GUID fileGUID for this Peerfile.
	 * 
	 * @return String file name for the file associated with the GUID.
	 */
	private String queryFileName() throws SQLException
	{
		String query = "SELECT fileName "
						+ "FROM peerfile "
						+ "WHERE fileGUID = ? ";

		try (PreparedStatement fnStatement = databaseConnection.prepareStatement(query);)
		{
			fnStatement.setInt(1, this.fileGUID);             //Set the value of the first (and only) parameter to GUID for this Peer.
			
			ResultSet result = fnStatement.executeQuery();
			
			if (!(result.first()))                            //No matching result.
				return null;
			
			/*Return the filename with any whitespace trimmed from the ends. */
			return result.getString(1).trim();
		}
	}
	
	
	/**Commits the file with the fileName for this PeerFile to
	 * the database.
	 * @return int GUID of the file.
	 */
	protected int commitData() throws SQLException
	{
		String query = "INSERT INTO peerFile (fileName) "
						+ "VALUES (\'" + this.fileName + "\') ";
		
		try(Statement fileStatement = databaseConnection.createStatement();)
		{
			fileStatement.execute(query, Statement.RETURN_GENERATED_KEYS);   //Execute the query and ensure that returned auto generated keys are available.
			ResultSet result = fileStatement.getGeneratedKeys();
			
			/*Cursor is advanced to first row to check if any generated key is available */
			if (!(result.first()))
				return -1;
		
			
			return result.getInt(1);
		}
	}
	
		
	
	
}
