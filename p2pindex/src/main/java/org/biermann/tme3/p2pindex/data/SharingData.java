/**
 * SharingData.java
 * 
 * SharingData is an abstract base class for data classes. Contains an sql.Connection instance
 * which is used to interact with a given database using JDBC. 
 * 
 * Concrete subclasses representing a database record must provide an implementation for the 
 * abstract commitData() and delete() methods which determine how data is committed and deleted.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pindex.data;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class SharingData 
{
	protected Connection databaseConnection;
	
	/**Implementation should store data to DB and return
	 * an identifier for the record.
	 */
	protected abstract int commitData() throws SQLException;
	
	/**
	 * Deletes the record for this data object from the database.
	 */
	public abstract boolean delete() throws SQLException;
	
	
}
