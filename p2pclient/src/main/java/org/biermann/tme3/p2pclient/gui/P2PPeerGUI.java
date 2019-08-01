/**
 * P2PPeerGUI.java
 * 
 * P2PPeerGUI provides a Swing-based GUI for a P2PPeer. This interface makes the share, unshare, 
 * and download features available to users. Creates FeaturePanel instances within the main JFrame
 * (window). Uses instances of different labeled FeaturePanelListener subclasses to handle
 * click events on buttons which are used to submit the input within each FeaturePanel. An
 * appropriate output message or interactive element is displayed in response to user input.
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.Border;

import org.biermann.tme3.p2pclient.P2PPeerController;

public class P2PPeerGUI
{
	private JFrame mainFrame;
	private P2PPeerController controller;
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	
		
	/**Create the JFrame and contained elements for the GUI.
	 * @param appName String holding name of the the application. The name will displayed in
	 * the top window bar.
	 * @param controller P2PPeerController to use to access system functionality. 
	 */
	public P2PPeerGUI(String appName, P2PPeerController controller, int minWidth, int minHeight)
	{
		mainFrame = new JFrame(appName);
		this.controller = controller;
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);             //Peer GUI will exit when window closed.
		
		/*Use createContentPane() to create a JPanel which is then set as the content pane for mainFrame.*/
		mainFrame.setContentPane(createContentPane());
		
		mainFrame.setMinimumSize(new Dimension(minWidth, minHeight));         //Set minimum size pixels.
		mainFrame.pack();                                                    //Size window to fit components, given preferred size and layout.
		mainFrame.setVisible(true);                                          //Make the window visible.
	}
	
	
	/**
	 * Simplified constructor which uses default frame width and height *
	 * 
	 * @param appName String holding name of the the application. The name will displayed in
	 * the top window bar.
	 * @param controller P2PPeerController to use to access system functionality
	 */
	public P2PPeerGUI(String appName, P2PPeerController controller)
	{
		this(appName, controller, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	
	/**Creates the JPanel content pane with a BoxLayout layout manager. Adds 3 JPanels
	 * for the share, unshare, and download features. The JPanels will be displayed
	 * vertically.
	 * 
	 * @return JPanel which is the content pane.
	 */
	private JPanel createContentPane()
	{
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));              //Set layout manager for content pane, with vertical layout.   
		
		/*Create submit buttons for the 3 FeaturePanel instances */
		JButton shareSubmit = new JButton("Share");
		JButton unshareSubmit = new JButton("Unshare");
		JButton downloadSubmit = new JButton("Search");
		
		Border greyBorder = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);             //Border for the FeaturePanels.
		
		/*Create the 3 FeaturePanel instances for the 3 different features of the app. User instructions
		 * are provided for the first instance. */
		FeaturePanel shareFilePanel = new FeaturePanel("Share a file", "File name:", shareSubmit, greyBorder, 
				"Note that shared files must be in the \"files/sharing\" folder.");
		FeaturePanel unshareFilePanel = new FeaturePanel("Unshare a file", "File name:", unshareSubmit, greyBorder, null);
		FeaturePanel downloadFilePanel = new FeaturePanel("Download a file", "File name:", downloadSubmit, greyBorder, null);
		
		/*Create and add action listeners for each of the submit buttons in the 3 FeaturePanel objects */
		shareSubmit.addActionListener(new ShareListener(shareFilePanel, controller));
		unshareSubmit.addActionListener(new UnshareListener(unshareFilePanel, controller));
		downloadSubmit.addActionListener(new SearchListener(downloadFilePanel, controller));
		
		/*Add event listeners for mouse leave on each feature panel. These will clear the output of the given 
		 * FeaturePanel when mouse leaves that element.	 */
		shareFilePanel.addMouseListener(new MouseLeaveListener(shareFilePanel, controller));
		unshareFilePanel.addMouseListener(new MouseLeaveListener(unshareFilePanel, controller));
		downloadFilePanel.addMouseListener(new MouseLeaveListener(downloadFilePanel, controller));
		
		/*Add the 3 FeaturePanel instances to the content pane */
		contentPane.add(shareFilePanel);
		contentPane.add(unshareFilePanel);
		contentPane.add(downloadFilePanel);
		
		return contentPane;
	}
	
	
}