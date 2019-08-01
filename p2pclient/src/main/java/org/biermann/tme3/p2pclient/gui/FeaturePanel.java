/**
 * A JPanel with a general label, a labeled input field, a button which may have an attached 
 * ActionListener for processing input. Also contains an optional label with instructions to the 
 * user. Other components may be added, as required.
 * 
 * 
 * @author Oloff Biermann
 */
package org.biermann.tme3.p2pclient.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


public class FeaturePanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3131782140880590330L;
	private JTextField inputField;                                               //Reference to text input element.
	private JPanel outputPanel;                                                  //Reference to the output element.
	
	/**
	 * Constructs a FeaturePanel with the title, input area label, submit button, instructions, and border specified.
	 * @param titleText String with component title
	 * @param inputLabelText The label for the input area.
	 * @param submitButton JButton which submits the input in this FeaturePanel.
	 * @param border Border which is painted around this FeaturePanel. The title will be added to the border.
	 * @param instructions String with instructions to users. Set to null if no instructions are required.
	 * as a TitleBorder.
	 */
	public FeaturePanel(String titleText, String inputLabelText, JButton submitButton, Border border, String instructions)
	{
		super();
		setupContent(inputLabelText, submitButton, instructions);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, titleText, TitledBorder.LEADING, TitledBorder.TOP);
		titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 18));                //Set title font.
		this.setBorder(titledBorder); 
	}
	
		
	/**
	 * Sets the justification for internal elements within FeaturePanel. Should be
	 * called if the layout manager is changed to ensure that
	 * @param justification int w
	 */
	public void setElementJustification(int justification)
	{
		((FlowLayout)getLayout()).setAlignment(justification);    
	}
		
	/**
	 * Removes any previous component from outputPanel and then adds the specified JComponent 
	 * to outputPanel. The hiearchy of this FeaturePanel is revalidated so that the layout 
	 * manager can adjust layout in response to the new element. The output panel is then 
	 * repainted to show new content.
	 * 
	 * @param outputComponent JComponent containing output to display within the output panel.
	 */
	public void setOutputComponent(JComponent outputComponent)
	{
		outputPanel.removeAll();
		outputPanel.add(outputComponent, BorderLayout.CENTER);
		validate();
		outputPanel.repaint();
	}
	
	/**
	 * Thread-safe method which sets the message displayed
	 * in the output component.
	 * @param message String with message to display
	 */
	public synchronized void setOutputMessage(String message)
	{
		setOutputComponent(new JLabel(message));
	}
	
	
	/**
	 * Thread-safe method which clears the content of the output component. The hierarchy
	 * of this FeaturePanel is revalidated so that layout can be adjusted, and the 
	 * outputPanel is then repainted.
	 */
	public synchronized void clearOutput()
	{
		outputPanel.removeAll();
		validate();
		outputPanel.repaint();
	}
	
	/**
	 * Gives access to the contents of the document for the user input field.
	 * @return String holding contents of user input or null if no input given
	 */
	public String getUserInput()
	{
		String input = "";
		try
		{			
			input = inputField.getText();
		}
		catch (NullPointerException npEx)
		{
			input = null;
		}
		
		return ( (input == null || input.isEmpty()) ? null : input);             //Return null if input is null or empty, else return input.
		
	}
	
	
	/**
	 * Service method which sets up the content for this FeaturePanel. This includes the
	 * input field label, input field, submit button, output panel. Optionally
	 * displays instructions, if provided. Uses a GridBagLayout to create an appropriate
	 * layout for components.
	 * @param inputFieldText String with input field label.
	 * @param submitButton JButton which may have an ActionListener to process input
	 * @param instructions String with instructions relating to this FeaturePanel. If null, 
	 * no instructions are displayed.
	 */
	private void setupContent(String inputFieldText, JButton submitButton, String instructions)
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c;		
		
		this.setLayout(gridBagLayout);                       //Set the layout manager
		
		outputPanel = new JPanel(new BorderLayout());        //Create the output panel with a Border Layout manager for layout.
		
		/*Add components to this FeaturePanel  with layout constrains in GridBagLayout specified*/
		
		float topLineWeight = 0.2f;                          //First row takes up 20% of container for input and instructions (optional).
		float bottomLineWeight = 0.8f;                       //Second row takes up 80% of container for results.
		
		/*Create constraints for the cell containing the input element, its label, and the submit button.
		 * These get 45% of the first line.*/
		JLabel inputLabel = new JLabel(inputFieldText);
		inputField = new JTextField(15);                    //Set value of member inputField here to create a JTextField with 15 column width.
		JPanel wrapper = new JPanel();                      //Wrapper for interaction elements and field label.
		wrapper.setAlignmentX(FlowLayout.LEADING);          //Justify all contents of wrapper to leading side (left in ltr)
			
		inputLabel.setLabelFor(inputField);                   //Mark inputLabel as the label for inputField.
		inputField.setMinimumSize(new Dimension(96, 12));     //Set minimum width and height for input field.
		inputField.setPreferredSize(new Dimension(128, 24));  //Set preferred size larger than minimum size for optimal use.
		
		/*Add elements to wrapper */
		wrapper.add(inputLabel);
		wrapper.add(inputField);
		wrapper.add(submitButton);
		
		/*Constraints for the wrapper */
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = topLineWeight;
		c.anchor = GridBagConstraints.LINE_START;             //Place content at start of line.
		
		this.add(wrapper, c);
		
		/*The instructions JLabel element is created if instructions is not null */
		if (instructions != null)
		{
			/*Constrains for instructions. */
			c = new GridBagConstraints();                    //Create new constraints object rather than reusing previous one to avoid errors.
			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 0.5;
			c.weighty = topLineWeight;
			c.fill = GridBagConstraints.BOTH;
			this.add(new JLabel(instructions), c);
		}
			
		/*Create constraints for the cell holding output. This cell takes up the entire second row and rest of available vertical space.*/
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(5, 5, 5, 5);                //Set insets for the output.
		c.weighty = bottomLineWeight;
		
		this.add(outputPanel, c);
			
	}
		
		
}
