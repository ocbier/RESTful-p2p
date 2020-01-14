# RESTful-p2p

P2P file sharing system with RESTful API using JAX-RS. 

Provides a basic GUI on the client side to support interaction with the central directory and with other peers.


**************************************************************************************************************
1. System Components:
**************************************************************************************************************

Programs were developed using Java SDK 1.8.0_191 and testing was performed using JRE 1.8.0_191. The IDE used was Eclipse EE IDE for Web Developers Oxygen release (4.7.3a).
The WildFly 12.0 Application Server was used to host the file index service.
MariaDB 10.3 was used to control persistent peer file sharing data.
The GUI was built using the Swing GUI toolkit.


Please see the UML class diagram in [specification.pdf](https://github.com/OCBier/RESTful-p2p/blob/master/Specification.pdf) for an overview of the system architecture. The system consists of the following main components:

•	**org.biermann.tme3.p2pclient.P2PPeer.java** (source code found in p2ppeer\src\p2pclient; See execution instructions under "2. Application Execution" below): Reused from TME 2, with modifications. Represents a peer within the peer to peer sharing system. Peers can act both as clients and servers since they can request and receive files while also transmitting files to other peers in response to requests. 

The client utilizes an instance of org.biermann.tme3.p2pclient.P2PPeerController which is responsible for managing interaction with the index service (see IndexServer.java below). Specifically, the desired resources are requested using the p2pindex REST API. Each request uses the appropriate HTTP method for the operation type. This allows the peer to register a shared file (POST), unregistering a file (DELETE), or requesting an address for a peer which is sharing a given file (GET). 

Additionally, when downloading a file, the P2PPeerController manages the interaction with other peers. Note that listening, sending, and receiving are handled on separate threads to maintain responsiveness in the main thread. 

User interaction with the P2PPeerController is mediated by a simple GUI interface, which is an instance of org.biermann.tme3.p2pclient.gui.P2PPeerGui. This interface presents the three main system features (sharing, unsharing, and downloading) in 3 different panels with input fields and submit buttons. The appropriate action listeners are invoked when input is given, which subsequently call one or more methods of P2PPeerController. An appropriate response message is displayed to the user when input is submitted, or an operation is completed.

•	**org.biermann.tme3.p2pindex package** (source code found in p2pindex\src\main\java\org\biermann\tme3\p2pindex; execute as a servlet in WildFly Application Server): Supports resource requests via a REST API which uses JAX-RS (Jersey framework). The API allows object sharing, unsharing, and peer index lookup for file download. As mentioned above, clients must use the appropriate HTTP method and resource name in the URL for each request. The resource API is specified in org.biermann.tme3.p2pindex.resources.SharedFiles where HTTP methods and paths are mapped to methods. The methods of the SharedFiles performs the required invocations on an instance of the class org.biermann.tme3.p2pindex.controllers.IndexServiceController which contains logic for interacting with the data (model) classes.

All communication between the client and the service uses XML. Standard communication between the index service and clients uses org.biermann.tme3.p2pindex.messages.FileMessages to transmit file name and peer address data, where required. FileMessage instances are serialized as XML before transmission and then deserialized by the receiver.

Custom exception handling is used to avoid returning the default WildFly HTML error documents. Instead, custom exceptions, javax.ws.rs.WebApplicationExceptions, and java.lang.RuntimeExceptions are mapped to custom responses in different implementations of javax.ws.rs.ext.ExceptionMapper<T>. This ensures that XML serializations of org.biermann.tme3.messages.ErrorMessage instances are returned to the client in the HTTP response body. These instances contain a descriptive error message and the associated HTTP status code.
	
•	**org.biermann.tme3.p2pindex.data** The JDBC data classes which encapsulate interaction with the database driver are found in this subpackage. These classes correspond to the the tables in the database and cache attributes which are retrieved when they are instantiated. Data is cached in memory for duration of the object lifetime. These classes also offer convenient methods to perform various queries. All classes are derived from SharingData in SharingData.java. This abstract super class contains a “java.sql.Connection” instance and two abstract methods which are inherited by its subclasses. The implementation of all subclasses allows this Connection object to be shared. This is not required or enforced, although it is often done here to improve performance and avoid creating more database connections than required.

•	**sharing_index database:** A simple MariaDB database containing records of shared files. Each peer may share 0 or more files. A relationship is established each time a peer wishes to share a file. That relationship and the file record are removed if the peer wishes to stop sharing the file. If a file must be downloaded, the appropriate peer is found by searching for a matching relationship. 

The MariaDB JDBC driver is packaged with the p2pindex.war and is also included as a Maven dependency in the Eclipse project. Therefore, it should is not necessary to manually download the driver or add it to the classpath.  For reference, the driver can be found on the MariaDB site HERE.


***********************************************************************************************
2. Application Execution:
***********************************************************************************************

**1. Start the MariaDB database server:**
Note that the sharing_index database must be imported by either:
- **Using the “sharing_index.sql” file to import the sharing_index database.** This approach will also automatically create the “index_user” user with the correct password and required privileges on “sharing_index”.

- **Extracting the “mariadb/sharing_index database.zip” archive and copying the directory holding the database files into the MariaDB data directory directly. (not recommended)** The MariaDB configuration file shows the correct data directory for MariaDB database (e.g. “..\MariaDB 10.3\data”). This approach recreating the user “index_user” with read and write privileges on the “sharing_index” database. See credentials and privileges below.

The user credentials for the sharing_index database used in the p2pindex application are as follows:
- User: index_user
- Password: d44d614319262e21363be1c86d6f9fc2 

The privileges on “sharing_index” for “index_user” are: INSERT, UPDATE, DELETE, SELECT, and SHOW VIEW.

	
Admin credentials for the database are:

- User: root
- Password: y1kax24uqaMIf3r27Ar2B2CEqO1aTE

**2. Start the WildFly Application Server and configure the p2pindex service**
This may be done by deploying p2pindex service to a separate WildFly server or creating a WildFly instance within Eclipse and then deploying the service.

**Option 1: Deploying P2PIndex to a standalone WildFly server:**
The steps may vary, depending on the version used. See the WildFly documentation for more information, if required. The general steps for deploying P2PIndex to WildFly 12.0 are as follows:
- i. Ensure that the WildFly server is started. 
- ii. Open the WildFly Admin Console in a browser by navigating to http://localhost:8080/  and then clicking on “Administration Console”
- iii. Select the “Deployments” tab
- iv. In the “New Deployment” wizard, select “Upload a new deployment” and click “next”
- v. Browse to find the “p2pindex.war” file for P2PIndex; this should be located under the main TME3 directory
- vi. In the following screen, leave all options as-is, unless a different name for the deployment is desired, and then click finish.
- vii. The p2pindex service is now deployed.

**Option 2: The steps for testing by deploying the P2PIndex to WildFly via Eclipse are as follows:**
- i. Ensure that the p2pindex project is imported to Eclipse 
- ii. Skip this step if WildFly has already been added to Eclipse
   - a. Under the “Servers” tab, right click and select New->Server
   - b. Under “JBoss Community”, select “WildFly 12”
   - c. Enter an appropriate host name (“Server’s host name”) for the server and an identifier to use in Eclipse (“Server name”)
   - d. Click “next” and leave all options set to default in the next screen
   - e. “Click “next” and select the “p2pindex” resource and at it to the configured resources for the server

- iii. If WildFly has already been added to Eclipse (step 1 was skipped) do the following, else skip to step 3:
    - a. Under the “Servers” stab, right click on the WildFly server and select “Add and Remove”
    - b. Under “Available”, select the p2pindex resource and add it to the configured resources for the server
    - c. Click “Finish”

- iv. In the “Servers” tab, right click on the “WildFly” server and select “Start”

**3. Ensure files to be shared are in “../files/sharing” directory on each peer**
This path is relative to the parent directory of the directory containing the P2PPeer.jar file. The parent directory is currently the “TME3/p2ppeer/jar” directory. Therefore, the “files” directory should be in the same directory as the “jar” directory.

**4. Start one or more instances of org.biermann.tme3.p2pclient.P2PPeer**
As mentioned in the previous section, P2PPeer can be started from the P2PPeer.jar located in p2pclient/jar/P2PPeer.jar.
(Optional) Pass the URL of the index service shared files resource as a parameter on the command line when starting P2PPeer.jar.
- If no parameter is given, the default is http://localhost:8080/p2pindex/webapi/sharedfiles
- If the endpoint providing the index service is a remote host and/or the service port is modified, the URL must be explicitly provided
  E.g. If the remote host providing the service is 192.168.1.7, the P2PPeer.jar should be executed as follows:
   P2PPeer.jar http://192.168.1.7:8080/p2pindex/webapi/sharedfiles 

Alternatively, the P2PPeer client can be started using the P2PPeer.class Java class file found in “p2pclient/target/classes/org/Biermann/tme3/p2pclient”. The optional parameter for the index service URL may also be used here.  
