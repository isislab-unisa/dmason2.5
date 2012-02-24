package dmason.util.visualization;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.kahadb.util.ByteArrayInputStream;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;


public class Display  {

	private void DisplayWindowClosing(WindowEvent e) {
		
		try {
			con.publishToTopic("EXIT", "GRAPHICS", "GRAPHICS");
			JOptionPane.showMessageDialog(null,"Successfully Send Disconnection ack!");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null,"Disconnection not completed, possible problems in future views!");
			e1.printStackTrace();
		}
		System.exit(0);
	}

	private void cameraSnapActionPerformed(ActionEvent e) {
		// TODO add your code here
		try {
			GregorianCalendar gc = new GregorianCalendar();
			String date="Date="+gc.get(Calendar.YEAR)+"-"+gc.get(Calendar.MONTH)+"-"+gc.get(Calendar.DAY_OF_MONTH)+
			"Time="+gc.get(Calendar.HOUR)+":"+gc.get(Calendar.MINUTE)+":"+gc.get(Calendar.SECOND);
			File outputfile = new File(System.getProperty("user.dir")+"/snap_"+date+".png");
		    ImageIO.write(actualSnap, "png", outputfile);
	
		    int val = JOptionPane.showConfirmDialog(
		    	    null,
		    	    System.getProperty("user.dir")+"/snap_"+date+".jpg" +" Snap saved, show this snap?",
		    	    "Open Snap",
		    	    JOptionPane.YES_NO_OPTION);
			
			if(val == JOptionPane.YES_OPTION)
			{
				
				Desktop dt = Desktop.getDesktop();
			    dt.open(outputfile);
			}
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void imageViewMouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void comboBoxCellItemStateChanged(ItemEvent e) {
		// TODO add your code here
	}
	
	private void onMouseEnterListener(MouseEvent e) {
		xuLabel.setText(""+e.getX());
		yuLabel.setText(""+e.getY());
	}

	public void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Mario Rossi
		Display = new JFrame();
		masterPanel = new JPanel();
		topPanel = new JPanel();
		coordinatedPanel = new JPanel();
		label2 = new JLabel();
		xuLabel = new JLabel();
		label3 = new JLabel();
		yuLabel = new JLabel();
		label4 = new JLabel();
		numStep = new JLabel();
		coordinatedPanel2 = new JPanel();
		cameraSnap = new JButton();
		comboBoxCell = new JComboBox();
		scrollPaneFather = new JPanel();
		
		comboBoxCell.addItem("ALL");
		comboBoxCell.setSelectedIndex(0);
		
		if(mode==0)
		{
			for (int i = 0; i < numCell; i++) {
				comboBoxCell.addItem("0-"+i);
			}
		}
		else
			if(mode==1)
			{
				for (int i = 0; i < (int)Math.sqrt(numCell); i++) {
					for (int j = 0; j < (int)Math.sqrt(numCell); j++) {
						
						comboBoxCell.addItem(i+"-"+j);	
					}
				}
			}
		
		imageView = new JPanel(){
			
			private static final long serialVersionUID = 1L;
			
			public void paintComponent(Graphics g){
				
				g.drawImage(actualSnap, 0, 0, null);
			}
		};
		
		imageView.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				onMouseEnterListener(e);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		scrollPane = new JScrollPane();

		//======== Display ========
		{
			Display.setResizable(false);
			Display.setBackground(new Color(238, 238, 238));
			Display.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			Display.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					DisplayWindowClosing(e);
				}
			});
			Container DisplayContentPane = Display.getContentPane();

			//======== masterPanel ========
			{
				//======== topPanel ========
				{
					topPanel.setBackground(new Color(238, 238, 238));
					topPanel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

					//======== coordinatedPanel ========
					{
						coordinatedPanel.setPreferredSize(new Dimension(480, 50));
						coordinatedPanel.setBorder(new MatteBorder(0, 2, 2, 0, Color.black));

						//---- label2 ----
						label2.setText("X:");

						//---- xuLabel ----
						xuLabel.setText("0000000000");

						//---- label3 ----
						label3.setText("Y:");

						//---- yuLabel ----
						yuLabel.setText("0000000000");

						//---- label4 ----
						label4.setText("Step:");

						//---- numStep ----
						numStep.setText("0000000000");

						GroupLayout coordinatedPanelLayout = new GroupLayout(coordinatedPanel);
						coordinatedPanel.setLayout(coordinatedPanelLayout);
						coordinatedPanelLayout.setHorizontalGroup(
							coordinatedPanelLayout.createParallelGroup()
								.addGroup(coordinatedPanelLayout.createSequentialGroup()
									.addGap(11, 11, 11)
									.addComponent(label2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(xuLabel, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(label3)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(yuLabel)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 146, Short.MAX_VALUE)
									.addComponent(label4)
									.addGap(18, 18, 18)
									.addComponent(numStep, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
									.addGap(38, 38, 38))
						);
						coordinatedPanelLayout.setVerticalGroup(
							coordinatedPanelLayout.createParallelGroup()
								.addGroup(coordinatedPanelLayout.createSequentialGroup()
									.addGroup(coordinatedPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(label2, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addComponent(xuLabel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addComponent(label3, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addComponent(yuLabel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addComponent(label4, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
										.addComponent(numStep, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE))
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
					}

					//======== coordinatedPanel2 ========
					{
						coordinatedPanel2.setPreferredSize(new Dimension(480, 50));
						coordinatedPanel2.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

						//---- cameraSnap ----
						cameraSnap.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("dmason/resource/image/camerasnap.png")));
						cameraSnap.setBackground(Color.yellow);
						cameraSnap.setBorder(null);
						cameraSnap.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cameraSnapActionPerformed(e);
							}
						});

						//---- comboBoxCell ----
						comboBoxCell.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(ItemEvent e) {
								comboBoxCellItemStateChanged(e);
							}
						});

						GroupLayout coordinatedPanel2Layout = new GroupLayout(coordinatedPanel2);
						coordinatedPanel2.setLayout(coordinatedPanel2Layout);
						coordinatedPanel2Layout.setHorizontalGroup(
							coordinatedPanel2Layout.createParallelGroup()
								.addGroup(coordinatedPanel2Layout.createSequentialGroup()
									.addComponent(cameraSnap)
									.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 291, Short.MAX_VALUE)
									.addComponent(comboBoxCell, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
						);
						coordinatedPanel2Layout.setVerticalGroup(
							coordinatedPanel2Layout.createParallelGroup()
								.addGroup(coordinatedPanel2Layout.createSequentialGroup()
									.addGroup(coordinatedPanel2Layout.createParallelGroup()
										.addComponent(cameraSnap, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
										.addComponent(comboBoxCell, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
									.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						);
					}

					GroupLayout topPanelLayout = new GroupLayout(topPanel);
					topPanel.setLayout(topPanelLayout);
					topPanelLayout.setHorizontalGroup(
						topPanelLayout.createParallelGroup()
							.addGroup(topPanelLayout.createSequentialGroup()
								.addComponent(coordinatedPanel2, GroupLayout.PREFERRED_SIZE, 451, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(coordinatedPanel, GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE))
					);
					topPanelLayout.setVerticalGroup(
						topPanelLayout.createParallelGroup()
							.addGroup(topPanelLayout.createSequentialGroup()
								.addGap(0, 0, Short.MAX_VALUE)
								.addGroup(topPanelLayout.createParallelGroup()
									.addComponent(coordinatedPanel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
									.addComponent(coordinatedPanel2, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
					);
				}

				//======== scrollPaneFather ========
				{

					//======== scrollPane ========
					{

						//======== imageView ========
						{
							imageView.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseClicked(MouseEvent e) {
									imageViewMouseClicked(e);
								}
							});

							GroupLayout imageViewLayout = new GroupLayout(imageView);
							imageView.setLayout(imageViewLayout);
							imageViewLayout.setHorizontalGroup(
								imageViewLayout.createParallelGroup()
									.addGap(0, width, Short.MAX_VALUE)
							);
							imageViewLayout.setVerticalGroup(
								imageViewLayout.createParallelGroup()
									.addGap(0, height, Short.MAX_VALUE)
							);
						}
						scrollPane.setViewportView(imageView);
					}

					GroupLayout scrollPaneFatherLayout = new GroupLayout(scrollPaneFather);
					scrollPaneFather.setLayout(scrollPaneFatherLayout);
					scrollPaneFatherLayout.setHorizontalGroup(
						scrollPaneFatherLayout.createParallelGroup()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
					);
					scrollPaneFatherLayout.setVerticalGroup(
						scrollPaneFatherLayout.createParallelGroup()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
					);
				}

				GroupLayout masterPanelLayout = new GroupLayout(masterPanel);
				masterPanel.setLayout(masterPanelLayout);
				masterPanelLayout.setHorizontalGroup(
					masterPanelLayout.createParallelGroup()
						.addComponent(topPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(scrollPaneFather, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
				masterPanelLayout.setVerticalGroup(
					masterPanelLayout.createParallelGroup()
						.addGroup(masterPanelLayout.createSequentialGroup()
							.addComponent(topPanel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(scrollPaneFather, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
			}

			GroupLayout DisplayContentPaneLayout = new GroupLayout(DisplayContentPane);
			DisplayContentPane.setLayout(DisplayContentPaneLayout);
			DisplayContentPaneLayout.setHorizontalGroup(
				DisplayContentPaneLayout.createParallelGroup()
					.addComponent(masterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			);
			DisplayContentPaneLayout.setVerticalGroup(
				DisplayContentPaneLayout.createParallelGroup()
					.addComponent(masterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			);
			Display.pack();
			Display.setLocationRelativeTo(Display.getOwner());
		}
		
		Viewer view = new Viewer();
		view.start();
	}


	public JFrame Display;
	private JPanel masterPanel;
	private JPanel topPanel;
	private JPanel coordinatedPanel;
	private JLabel label2;
	private JLabel xuLabel;
	private JLabel label3;
	private JLabel yuLabel;
	private JLabel label4;
	private JLabel numStep;
	private JPanel coordinatedPanel2;
	private JButton cameraSnap;
	private JComboBox comboBoxCell;
	private JPanel scrollPaneFather;
	private JScrollPane scrollPane;
	private JPanel imageView;
	
	public ConnectionNFieldsWithActiveMQAPI con;
	public int mode, numCell, width, height;
	public String absolutePath;
	public VisualizationUpdateMap<Long, RemoteSnap> updates;
	public long STEP;
	public boolean STARTED=false;
	public BufferedImage actualSnap;
	public ReentrantLock lock=new ReentrantLock();
	public Condition sin=lock.newCondition();
	public void sblock()
	{
		lock.lock();
		sin.signal();
		lock.unlock();
	}
	class Viewer extends Thread{
	
		public void run(){
			actualSnap=new BufferedImage(100,100,BufferedImage.TYPE_3BYTE_BGR);
			if(STARTED==false)
			{
				lock.lock();
				try {
					sin.await();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				lock.unlock();
			}
			while(true)
			{
				if(STARTED) 
				{
					
					try 
					{
						HashMap<String,Object> snaps = (HashMap<String,Object>)updates.getUpdates(STEP, numCell);
						BufferedImage tmpImage = 
								new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
						String comboCell = (String)comboBoxCell.getSelectedItem();
						try {
							if(mode==1)
							{
								if(comboCell.equalsIgnoreCase("ALL"))
								{
									for (int i = 0; i < Math.sqrt(numCell); i++) {
										for (int j = 0; j < Math.sqrt(numCell); j++) {
											
											byte[] array = ((RemoteSnap)snaps.get(i+"-"+j)).image;
											BufferedImage image;
											image = ImageIO.read(new ByteArrayInputStream(array));
											tmpImage.createGraphics().drawImage(image, 
													(width/(int)Math.sqrt(numCell))*j, (height/(int)Math.sqrt(numCell))*i, null);
										}
									}
								}
								else
								{
									byte[] array = ((RemoteSnap)snaps.get(comboCell)).image;
									BufferedImage image;
									image = ImageIO.read(new ByteArrayInputStream(array));
									tmpImage.createGraphics().drawImage(image, 
											(width/(int)Math.sqrt(numCell))*Integer.parseInt(""+comboCell.charAt(0)), 
											(height/(int)Math.sqrt(numCell))*Integer.parseInt(""+comboCell.charAt(2)), null);		
								}
							}
							else
							{
								if(comboCell.equalsIgnoreCase("ALL"))
								{
									for (int i = 0; i < numCell; i++) {

											
											byte[] array = ((RemoteSnap)snaps.get("0-"+i)).image;
											BufferedImage image;
											image = ImageIO.read(new ByteArrayInputStream(array));
											tmpImage.createGraphics().drawImage(image, 
													(width/numCell)*i, 0, null);
										
									}
								}
								else
								{
									byte[] array = ((RemoteSnap)snaps.get(comboCell)).image;
									BufferedImage image;
									image = ImageIO.read(new ByteArrayInputStream(array));
									tmpImage.createGraphics().drawImage(image, 
											(width/numCell)*Integer.parseInt(""+comboCell.charAt(0)), 
											0, null);		
								}
							}
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Problem with the rendering");
							System.exit(-1);
						}
						//Aggiornamento Snap
						numStep.setText(STEP+"");
						actualSnap = tmpImage;
						
						imageView.repaint();
						STEP++;
					} 
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
				
		}
	}
	
	public Display(ConnectionNFieldsWithActiveMQAPI con, int mode, int numCell,
			int width, int height, String absolutePath) {
		super();
		this.con = con;
		this.mode = mode;
		this.numCell = numCell;
		this.width = width;
		this.height = height;
		this.absolutePath = absolutePath;
		updates = new VisualizationUpdateMap<Long, RemoteSnap>();
		
		try {
			con.createTopic("GRAPHICS",1);
			con.subscribeToTopic("GRAPHICS");
			con.publishToTopic("ENTER", "GRAPHICS", "GRAPHICS");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		ThreadVisualizationMessageListener thread = new ThreadVisualizationMessageListener(con, this);
		
		thread.start();
	}
	
	public void addSnapShot(RemoteSnap remSnap){
		
		updates.put(remSnap.step, remSnap);
	
	}

}