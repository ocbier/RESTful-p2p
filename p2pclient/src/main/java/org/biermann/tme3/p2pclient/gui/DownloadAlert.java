/**
 * A Runnable which awaits a Condition indicating that a download has completed. 
 * Updates the GUI FeaturePanel with an appropriate message.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient.gui;

import org.biermann.tme3.p2pclient.DownloadStatus;

public class DownloadAlert implements Runnable
{
	private FeaturePanel output;
	private DownloadStatus status;
	
	public DownloadAlert(DownloadStatus downloadStatus, FeaturePanel outputPanel)
	{
		status = downloadStatus;
		output = outputPanel;
	}
	
	public void run()
	{
		/*When the download completes, then output message indicating status in the GUI FeaturePanel output component. */
		if(status.waitForTermination())
		{
			output.setOutputMessage(status.getStatusMessage());
		}
		
	}
}
