/**
 * FeaturePanelListener.java
 * 
 * Contains several classes which implement the ActionListener or MouseListener interfaces. 
 * These are all subclasses of the FeaturePanelListener class.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.biermann.tme3.p2pclient.DownloadStatus;
import org.biermann.tme3.p2pclient.P2PPeerController;
import org.biermann.tme3.p2pclient.gui.FeaturePanel;


/**
 * A general base class for listeners which are used in a FeaturePanel. Contains an attribute for
 * the p2pclient.gui.FeaturePanel which attaches the listener to one or more of its components, and the
 * p2pclient.P2PPeerController which is used to invoke methods that modify internal system state. 
 *
 */
public class FeaturePanelListener 
{
	protected FeaturePanel featurePanel;                 //Feature panel using this listener.
	protected P2PPeerController controller;              //Controller used to invoke methods to modify system state from the listener.
	
	public FeaturePanelListener(FeaturePanel featurePanel, P2PPeerController controller)
	{
		this.featurePanel = featurePanel;              
		this.controller = controller;
	}
	
	/**
	 * Get the user input from the input field of the FeaturePanel instance.
	 * @return String containing user input or null if input empty
	 */
	public String getUserInput()
	{
		String input = featurePanel.getUserInput();
		return ( (input == null || input.isEmpty()) ? null : input.trim());
	}
}



/**
 * ShareListener is a FeaturePanelListener which implements the ActionListener interface.
 * The handler shares (registers) the file specified by the user input in the FeaturePanel.
 *
 */
class ShareListener extends FeaturePanelListener implements ActionListener
{
	public ShareListener(FeaturePanel featurePanel, P2PPeerController controller)
	{
		super(featurePanel, controller);
	}
	
	/**
	 * Shares the file specified by user input file name.
	 * 
	 * @param event ActionEvent which submits input.
	 */
	public void actionPerformed(ActionEvent event)
	{
		String message = "";
		String fileName = ""; 
		Path filePath;       
			
		try
		{
			/*No input given */
			if ((fileName = getUserInput()) == null )
			{
				message = "Please enter a valid file name.";
			}
			
			/*File doesn't exist in sharing directory */
			else if (!(controller.checkLocalFileExists(fileName)))
			{
				message = "The specified file does not exist in the sharing directory.";
			}
			
			/*Share the file, since it exists locally in the sharing directory*/
			else
			{
				filePath = Paths.get(controller.getSharingDir(), fileName).toAbsolutePath();      //Create absolute path for file to share.
				
				/*Attempt to share the file. Get appropriate output message based on outcome */
				message = controller.shareFile(filePath) ?
						"Success. The file " + fileName + " is now shared." : "Sharing " + fileName + " failed. "
						+ "The host is already sharing this file.";
			}
		}
		catch (Exception shareEx)
		{
			message = "An error occured while attempting to share the file " + fileName +".";
		}
		finally
		{
			/*Add a JLabel containing the output message text as the output in featurePanel */
			JLabel outputMessage = new JLabel(message);
			featurePanel.setOutputComponent(outputMessage);
		}
	}
}


/**
 * UnshareListener is a FeaturePanelListener which is an implementation of ActionListener 
 * that that unshares (deregisters)the file specified by user input in the FeaturePanel interface
 * element.
 *
 */
class UnshareListener extends FeaturePanelListener implements ActionListener
{
	public UnshareListener(FeaturePanel featurePanel, P2PPeerController controller)
	{
		super(featurePanel, controller);
	}
	
	/**
	 * Unshares the file specified by user input file name.
	 * 
	 * @param event ActionEvent which submits input.
	 */
	public void actionPerformed(ActionEvent event)
	{
		String message = "";
		String fileName = "";
				
		try
		{
			if ( (fileName = getUserInput()) == null)
			{
				message = "Please enter a valid file name.";
			}
			else
			{
				/*Attempt to stop sharing the file. Create an appropriate message to display to indicate success or failure.*/
				message = controller.unshareFile(fileName) ? "Success. The file " + fileName + " is no longer shared." :
					"The attempt to stop sharing the file " + fileName + " failed.";
			}
		}
		catch (Exception unshareEx)
		{
			message = "The attempt to stop sharing the file " + fileName + " failed.";
		}
		finally
		{
			/*Add a JLabel containing the output message text as the output in featurePanel */
			JLabel outputMessage = new JLabel(message);
			featurePanel.setOutputComponent(outputMessage);
		}
	}
}

/**
 * SearchListener is a FeaturePanelListener that implements ActionListener 
 * to search for a file shared by a peer. The file name is specified by user input.
 *
 */
class SearchListener extends FeaturePanelListener implements ActionListener
{
	public SearchListener(FeaturePanel featurePanel, P2PPeerController controller)
	{
		super(featurePanel, controller);
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String errMsg = "";
		String fileName = "";
		boolean success = false;
		
		try
		{
			/*Attempt to get user input. Set error message if no input given.*/
			if ( (fileName = getUserInput()) == null)
			{
				errMsg = "Please enter a valid file name.";
			}
			else
			{
				success = (controller.getFilePeer(fileName) == null) ? false : true;                  //Determine if at least one peer is sharing file.
				if (!success)
					errMsg = "The file " + fileName + " is not currently being shared by any peer.";
			}
		}
		catch (Exception searchEx)
		{
			success = false;
			errMsg = "An error occurred while searching for the file.";
			System.out.println(searchEx.getMessage());
			searchEx.printStackTrace();
		}
		finally
		{
			/* If file is available for download, create a button with the file name as part of text. 
			 * This button is set as the output featurePanel. This button has another FeaturePanelListener 
			 * (instance of DownloadListener) as its ActionListener to handle clicks to download a file. */
			if (success)
			{
				JButton downloadButton = new JButton ("Download " +fileName);
				downloadButton.addActionListener(new DownloadListener(featurePanel, controller, fileName));
				featurePanel.setOutputComponent(downloadButton);            //Add button to output component.
			}
			/*Otherwise display an error message as output */
			else
			{
				featurePanel.setOutputComponent(new JLabel(errMsg));
			}
		}
	}
	
}


/**
 * Implementation of ActionListener which attempts to download a specified file. 
 * Appropriate output is displayed in response to the outcome of the request. Note that
 * this operation is asynchronous and multithreaded to avoid blocking the main thread.
 *
 */
class DownloadListener extends FeaturePanelListener implements ActionListener
{
	private String downloadTarget;
	
	public DownloadListener(FeaturePanel featurePanel, P2PPeerController controller, String downloadTarget)
	{
		super(featurePanel, controller);
		this.downloadTarget = downloadTarget;
		
	}
	
	public void actionPerformed(ActionEvent event)
	{
		String message = "";
		DownloadStatus downloadStatus = new DownloadStatus(downloadTarget);        //Holds shared download status and file name.
			
		/*Create and start a new thread which will wait for the download operation to complete before displaying 
		 * an appropriate message. The message is displayed in the output component of the FeaturePanel.*/
		Thread downloadAlert = new Thread(new DownloadAlert(downloadStatus, featurePanel));
		downloadAlert.start();
				
		try
		{
			/*Get the file name input by user and attempt to download it from a peer. Pass reference to downloadStatus 
			 * so that that this shared instance can be used to signal the download alert thread downloadAlert 
			 * when the download completes.*/
			controller.downloadFile(downloadStatus);
		}
		catch (Exception downloadException)
		{
			/*Display an error message in output component if an exception occurs in block above */
			message = "Error downloading file " + downloadTarget;
			downloadStatus.setStatusMessage(message);
		}
	}
	
	/**
	 * Operation not supported on this type of FeaturePanelListener
	 */
	public String getUserInput()
	{
		throw new UnsupportedOperationException("getUserInput() not implemented on FeaturePanelListener MouseLeaveListener");
	}
}

/**
 * MouseLeaveListener is a FeaturePanelListener that implements MouseListener to listen
 * for mouse leave events on a FeaturePanel instance. The output of the FeaturePanel
 * is cleared in response to this event.
 *
 */
class MouseLeaveListener extends FeaturePanelListener implements MouseListener
{
	public MouseLeaveListener(FeaturePanel featurePanel, P2PPeerController controller)
	{
		super(featurePanel, controller);
	}
	
	public void mouseEntered(MouseEvent event) {}
	
	public void mousePressed(MouseEvent event) {}
	
	public void mouseClicked(MouseEvent event) {}
	
	public void mouseReleased(MouseEvent event) {}
	
	public void mouseExited(MouseEvent event)
	{
		/*If mouse is outside of the FeaturePanel, clear the output component. Otherwise, do nothing. */
		if(!(featurePanel.contains(event.getPoint())))
		{
			featurePanel.clearOutput();
		}
	}
	
	
	/**
	 * Operation not supported on this type of FeaturePanelListener
	 */
	public String getUserInput()
	{
		throw new UnsupportedOperationException("getUserInput() not implemented on FeaturePanelListener MouseLeaveListener");
		
	}
	
}

