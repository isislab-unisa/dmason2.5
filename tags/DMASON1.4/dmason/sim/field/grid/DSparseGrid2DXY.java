package dmason.sim.field.grid;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import javax.imageio.ImageIO;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.DistributedRegion;
import dmason.sim.field.Entry;
import dmason.sim.field.MessageListener;
import dmason.sim.field.Region;
import dmason.sim.field.UpdateMap;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.connection.ConnectionWithJMS;
import dmason.util.visualization.RemoteSnap;
import dmason.util.visualization.ZoomArrayList;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 *  <h3>This Field extends SparseGrid2D, to be used in a distributed environment. All the necessary informations for 
 *  the distribution of simulation are wrapped in this class.</h3>
 * <p> This version is for a distribution in a <i>square mode</i>.
 *  It represents the field managed by a single peer.
 *  This is an example for a square mode distribution with 9 peers (only to distinguish the regions):
 *  (for code down)
 *  <p>
 *
 *	<ul>
 *	<li>MYFIELD : Region to be simulated by peer.</li>
 *
 *	<li>LEFT_MINE, RIGHT_MINE, UP_MINE, DOWN_MINE,CORNER_MINE_LEFT_UP,CORNER_MINE_LEFT_DOWN,
 *		CORNER_MINE_RIGHT_UP,CORNER_MINE_RIGHT_DOWN :Boundaries Regions those must be simulated and sent to neighbors.</li>
 *	
 *	<li>LEFT_OUT, RIGHT_OUT, UP_OUT, DOWN_OUT, CORNER_OUT_LEFT_UP_DIAG, CORNER_OUT_LEFT_DOWN_DIAG,
 *		CORNER_OUT_RIGHT_UP_DIAG, CORNER_OUT_RIGHT_DOWN_DIAG : Boundaries Regions those must not be simulated and sent to neighbors to be simulated.</li>
 *   <li>
 *	All peers subscribes to the topic of boundary region which want the information and run a asynchronous thread
 *	to receive the updates, then publish a topic for every their border (or neighbor), that can be :
 *	<ul>
 *	<li> MYTOPIC L (LEFT BORDER)</li>
 *	<li> MYTOPIC R (RIGHT BORDER)</li>
 *	<li> MYTOPIC U (UPPER BORDER)</li>
 *	<li> MYTOPIC D (LOWER BORDER)</li>
 *
 *	<li> MYTOPIC CUDL (Corner Up Diagonal Left)</li>
 *	<li> MYTOPIC CUDR (Corner Up Diagonal Right)</li>
 *	<li> MYTOPIC CDDL (Corner Down Diagonal Left)</li>
 *	<li> MYTOPIC CDDR (Corner Down Diagonal Right)</li>
 *</ul>
 *</li>
 *	</ul></p>
 *
 * <PRE>
 * ---------------------------------------------------------------------------------------
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |         00             |  |  |          01          |  |  |            02           |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |   CORNER DIAG           |
 * |                        |  |  |                      |  |  |  /                      |
 * |                        |  |  |                      |  |  | /                       |
 * |________________________|__|__|______UP_OUT__________|__|__|/________________________|
 * |________________________|__|__|______UP_MINE_________|__|__|_________________________|
 * |________________________|__|__|______________________|__|__|_________________________|
 * |                        |  |  |                     /|  |  |                         |
 * |                        L  L  |                    / |  R  R                         |
 * |                        E  E  |                   /  |  I  I                         |
 * |         10             F  F  |         11   CORNER  |  G  G         12              |
 * |                        T  T  |               MINE   |  H  H                         |
 * |                        |  |  |                      |  T  T                         |
 * |                        O  M  |       MYFIELD        |  |  |                         |
 * |                        U  I  |                      |  M  O                         |
 * |                        T  N  |                      |  I  U                         |
 * |________________________|__|__|______________________|__|__|_________________________|
 * |________________________|__|__|___DOWN_MINE__________|__|__|_________________________|
 * |________________________|__|__|___DOWN_OUT___________|__|__|_________________________|
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |       20               |  |  |          21          |  |  |           22            |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * |                        |  |  |                      |  |  |                         |
 * ---------------------------------------------------------------------------------------
 * </PRE>
 */

public class DSparseGrid2DXY extends DSparseGrid2D
{	

	private ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
	private ConnectionWithJMS con = new ConnectionNFieldsWithActiveMQAPI();
	private String NAME;
	
	private BufferedImage actualSnap;
	private WritableRaster writer;
	private int white[]={255,255,255};

	/*
	private FileOutputStream file;
	private PrintStream ps;
	*/
	private ZoomArrayList<RemoteAgent> tmp_zoom=new ZoomArrayList<RemoteAgent>();
	
	
	
	/**
	 * @param width field's width  
	 * @param height field's height
	 * @param sm The SimState of simulation
	 * @param max_distance maximum shift distance of the agents
	 * @param i i position in the field of the cell
	 * @param j j position in the field of the cell
	 * @param num_peers number of the peers
	 * @param name identifier of the field
	 */	
	public DSparseGrid2DXY(int width, int height,SimState sm,int max_distance,int i,int j,int num_peers, String name) 
	{		
		super(width, height);
		this.NAME = name;
		this.sm=sm;
		cellType = new CellType(i, j);
		MAX_DISTANCE=max_distance;
		NUMPEERS=num_peers;
		
		/*
		try {
			file = new FileOutputStream("Region-"+cellType+".txt");
			ps = new PrintStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		updates_cache=new ArrayList<Region<Integer,Int2D>>();
		
		setConnection(((DistributedState)sm).getConnection());
		
		createRegion();	
		
	}
	
	
	/**
	 * This method first calculates the upper left corner's coordinates, so the regions where the field is divided
	 * @return true if all is ok
	 */
	private boolean createRegion()
	{
		//upper left corner's coordinates
		own_x=(width/((int)Math.sqrt(NUMPEERS)))*cellType.pos_j; //inversione
		own_y=(width/((int)Math.sqrt(NUMPEERS)))*cellType.pos_i;
		
		// own width and height
		my_width=(int) (width/Math.sqrt(NUMPEERS));
		my_height=(int) (height/Math.sqrt(NUMPEERS));
		
		//calculating the neighbors
		for (int k = -1; k <= 1; k++) 
		{
			for (int k2 = -1; k2 <= 1; k2++) 
			{				
				int v1=cellType.pos_i+k;
				int v2=cellType.pos_j+k2;
				if(v1>=0 && v2 >=0 && v1<Math.sqrt(NUMPEERS) && v2<Math.sqrt(NUMPEERS))
					if( v1!=cellType.pos_i || v2!=cellType.pos_j)
					{
						neighborhood.add(v1+""+v2);
					}	
			}
		}
		
		actualSnap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
		writer=actualSnap.getRaster();
		
		
		// Building the regions
		rmap.left_out=RegionInteger.createRegion(own_x-MAX_DISTANCE,own_y,own_x-1, (own_y+my_height-1),my_width, my_height, width, height);
		if(rmap.left_out!=null)
		{
			rmap.left_mine=RegionInteger.createRegion(own_x,own_y,own_x + MAX_DISTANCE -1, own_y+my_height-1,my_width, my_height, width, height);
		}
		
		rmap.right_out=RegionInteger.createRegion(own_x+my_width,own_y,own_x+my_width+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
		if(rmap.right_out!=null)
		{
			rmap.right_mine=RegionInteger.createRegion(own_x + my_width - MAX_DISTANCE,own_y,own_x +my_width - 1, own_y+my_height-1,my_width, my_height, width, height);
		}
		
		rmap.up_out=RegionInteger.createRegion(own_x, own_y - MAX_DISTANCE,own_x+ my_width -1,own_y-1,my_width, my_height, width, height);
		if(rmap.up_out!=null)
		{
			rmap.up_mine=RegionInteger.createRegion(own_x ,own_y,own_x+my_width-1, own_y + MAX_DISTANCE -1,my_width, my_height, width, height);
		}
		
		rmap.down_out=RegionInteger.createRegion(own_x,own_y+my_height,own_x+my_width-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
		if(rmap.down_out!=null)
		{
			rmap.down_mine=RegionInteger.createRegion(own_x,own_y+my_height-MAX_DISTANCE,own_x+my_width-1, (own_y+my_height)-1,my_width, my_height, width, height);
		}
				
		if(rmap.left_out == null)
		{
			if(rmap.up_out == null)
			{
				//peer 0
				myfield=new RegionInteger(own_x,own_y, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);
								
				//corner down right
				rmap.corner_out_down_right_diag=RegionInteger.createRegion(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
				rmap.corner_mine_down_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);
			}
			else
			if(rmap.down_out==null)
			{
				//peer 6
				myfield=new RegionInteger(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-1);
				
				//corner up right
				rmap.corner_out_up_right_diag = RegionInteger.createRegion(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
				rmap.corner_mine_up_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
			}
			else
			{
				//peer 3
			    myfield=new RegionInteger(own_x,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE-1, own_y+my_height-MAX_DISTANCE-1);
									
				//corner up right
				rmap.corner_out_up_right_diag = RegionInteger.createRegion(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1,my_width, my_height, width, height);
				rmap.corner_mine_up_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);
				
				//corner down right
				rmap.corner_out_down_right_diag=RegionInteger.createRegion(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
				rmap.corner_mine_down_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);
			}			
		}
		else
		if(rmap.right_out==null)
		{
			if(rmap.up_out==null)
			{
				//peer 2
				myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);
					
				//corner down left
				rmap.corner_out_down_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
				rmap.corner_mine_down_left=RegionInteger.createRegion(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
			}
			else
			if(rmap.down_out==null)
			{
				//peer 8
				myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-1);
			
				//corner up left	
				rmap.corner_out_up_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
				rmap.corner_mine_up_left=RegionInteger.createRegion(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
				
			}
			else
			{	
				//peer 5
				myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-1, own_y+my_height-MAX_DISTANCE-1);
				
				//corner up left					
				rmap.corner_out_up_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
				rmap.corner_mine_up_left=RegionInteger.createRegion(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
				
				//corner down left
				rmap.corner_out_down_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
				rmap.corner_mine_down_left=RegionInteger.createRegion(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1, my_width, my_height, width, height);
			}
		}
		else
		if(rmap.up_out==null)
		{
			//peer 1
			myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);
	
			//corner down left
			rmap.corner_out_down_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width, height);
			rmap.corner_mine_down_left=RegionInteger.createRegion(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
			
			//corner down right
			rmap.corner_out_down_right_diag=RegionInteger.createRegion(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
			rmap.corner_mine_down_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1, my_width, my_height, width,height);
		}
		else
		if(rmap.down_out==null)
		{
			//peer 7
			myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-1);
			
			//corner up left	
			rmap.corner_out_up_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1,my_width, my_height, width, height);
			rmap.corner_mine_up_left=RegionInteger.createRegion(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);

			//corner up right
			rmap.corner_out_up_right_diag = RegionInteger.createRegion(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
		}
		else
		{
			myfield=new RegionInteger(own_x+MAX_DISTANCE,own_y+MAX_DISTANCE, own_x+my_width-MAX_DISTANCE -1, own_y+my_height-MAX_DISTANCE-1);
		
			//corner up left
			rmap.corner_out_up_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y-MAX_DISTANCE, own_x-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_left=RegionInteger.createRegion(own_x, own_y, own_x+MAX_DISTANCE-1, own_y+MAX_DISTANCE-1,my_width, my_height, width, height);
						
			//corner up right
			rmap.corner_out_up_right_diag = RegionInteger.createRegion(own_x+my_width, own_y-MAX_DISTANCE, own_x+my_width+MAX_DISTANCE-1, own_y-1, my_width, my_height, width, height);
			rmap.corner_mine_up_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y, own_x+my_width-1, own_y+MAX_DISTANCE-1, my_width, my_height, width, height);
			
			//corner down left
			rmap.corner_out_down_left_diag=RegionInteger.createRegion(own_x-MAX_DISTANCE, own_y+my_height,own_x-1, own_y+my_height+MAX_DISTANCE-1,my_width, my_height, width, height);
			rmap.corner_mine_down_left=RegionInteger.createRegion(own_x, own_y+my_height-MAX_DISTANCE,own_x+MAX_DISTANCE-1, own_y+my_height-1,my_width, my_height, width, height);
			
			//corner down right
			rmap.corner_out_down_right_diag=RegionInteger.createRegion(own_x+my_width, own_y+my_height, own_x+my_width+MAX_DISTANCE-1,own_y+my_height+MAX_DISTANCE-1, my_width, my_height, width,height);
			rmap.corner_mine_down_right=RegionInteger.createRegion(own_x+my_width-MAX_DISTANCE, own_y+my_height-MAX_DISTANCE, own_x+my_width-1,own_y+my_height-1,my_width, my_height, width,height);
						
		}
		return true;
	}
		
	/**
	 * Set up the new position in a distributed environment in the space of expertise.
	 * It uses the Java Reflect to find the region which 
	 * the new location belongs.
	 * The method uses the method isMine() of class Region to do this.
	 * 
	 * @param location The new location
	 * @param rm The remote agent that have be stepped
	 * @param sm The SimState of simulation
	 * @return false if the object is null (null objects cannot be put into the grid)
	 */
	public boolean setDistributedObjectLocationForPeer(final Int2D location,RemoteAgent<Int2D> rm,SimState sm)
	{		
		
	    if(myfield.isMine(location.x,location.y) && this.getObjectsAtLocation(location)==null)
	    	return setObjectLocation(rm,
        			new Int2D(location.x,location.y));  
	 
	    Class o=rmap.getClass();
		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
			fields[z].setAccessible(true);
		    try
		    {
		    	String name=fields[z].getName();
		    	Method method = o.getMethod("get"+name, null);
		    	Object returnValue = method.invoke(rmap, null);
		    	if(returnValue!=null && name.contains("mine"))
 		    	{
		    		RegionInteger region=((RegionInteger)returnValue);
		 	    	if(region.isMine(location.x,location.y) && this.getObjectsAtLocation(location)==null)
		    				return setObjectLocation(rm, new Int2D(location.x,location.y)); 
 		    	}
		     }
		     catch (IllegalArgumentException e){ e.printStackTrace();} 
		     catch (IllegalAccessException e) { e.printStackTrace();} 
		     catch (SecurityException e) { e.printStackTrace();} 
		     catch (NoSuchMethodException e) {e.printStackTrace();} 
		     catch (InvocationTargetException e) {e.printStackTrace();}
		}                
	 return false;
	}
	
	/**
	 * Set a available location to a Remote Agent:
	 * it generates the location depending on the field of expertise
	 * @return The location assigned to Remote Agent
	 */
	public Int2D setAvailableRandomLocation(RemoteAgent<Int2D> rm)
	{
		int x=(((DistributedState)sm).random.nextInt(width)%(my_width-1))+own_x;
    	if(x>(width-1)) x--;
        int y=(((DistributedState)sm).random.nextInt(height)%(my_height-1))+own_y;
        if(y>(height-1)) y--;
        rm.setPos(new Int2D(x, y));
        
        return (new Int2D(x, y));
	}
	
	/**  
	 * Provide the shift logic of the agents among the peers
	 * @param location The new location of the remote agent
	 * @param rm The remote agent to be stepped
	 * @param sm SimState of simulation
	 * @return 1 if it's in the field, -1 if there's an error (setObjectLocation returns null)
	 */
    public boolean setDistributedObjectLocation(final Int2D location,RemoteAgent<Int2D> rm,SimState sm)
    {
    	if(myfield.isMine(location.x,location.y))
    	{    		
    		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).NUMVIEWER.getCount()>0)
    		{
    			writer.setPixel((int)(location.x%my_width), (int)(location.y%my_height), white);
    		}
    		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
				tmp_zoom.add(rm);
    		return myfield.addAgents(new Entry<Int2D>(rm, location));
    	}
    	else
    		if(setAgents(rm, location))
    			return true;
    		else
    				System.out.println(cellType+")OH MY GOD!"); // it should never happen (don't tell it to anyone shhhhhhhh! ;P)
    	return false;
    }

	/**
	 * 	This method provides the synchronization in the distributed environment.
	 * 	It's called after every step of schedule.
	 */
	public synchronized boolean synchro() 
	{		
		if(((DistributedMultiSchedule)((DistributedState)sm).schedule).NUMVIEWER.getCount()>0)
		{
			try {
				ByteArrayOutputStream by = new ByteArrayOutputStream();
				ImageIO.write(actualSnap, "png", by);
				by.flush();
				connection.publishToTopic(new RemoteSnap(cellType, sm.schedule.getSteps()-1, by.toByteArray()), "GRAPHICS", "GRAPHICS");
				by.close();
				actualSnap = new BufferedImage((int)my_width, (int)my_height, BufferedImage.TYPE_3BYTE_BGR);
				writer=actualSnap.getRaster();

			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		for(Region<Integer,Int2D> region : updates_cache)
		{
			for(Entry<Int2D> remote_agent : region)
			{
				this.remove(remote_agent.r);
			}
		}

		//every agent in the myfield region is scheduled
		for(Entry<Int2D> e: myfield)
		{
			RemoteAgent<Int2D> rm=e.r;
			Int2D loc=e.l;
			rm.setPos(loc);
			this.remove(rm);
			sm.schedule.scheduleOnce(rm);
			setObjectLocation(rm,loc);	
		}   

		updateFields(); //update fields with java reflect
		updates_cache=new ArrayList<Region<Integer,Int2D>>();

		memorizeRegionOut();

		//--> publishing the regions to correspondent topics for the neighbors
		if(rmap.left_out!=null)
		{
			try 
			{
				DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.left_mine,rmap.left_out,
						(sm.schedule.getSteps()-1),cellType,DistributedRegion.LEFT);

				connection.publishToTopic(dr,cellType+"L", NAME);



			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.right_out!=null)
		{
			try 
			{
				DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.right_mine,rmap.right_out,(sm.schedule.getSteps()-1),cellType,DistributedRegion.RIGHT);				

				connection.publishToTopic(dr,cellType.toString()+"R", NAME);

			} catch (Exception e1) {e1.printStackTrace(); }
		}
		if(rmap.up_out!=null )
		{
			try 
			{
				DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.up_mine,rmap.up_out,(sm.schedule.getSteps()-1),cellType,DistributedRegion.UP);

				connection.publishToTopic(dr,cellType.toString()+"U", NAME);

			} catch (Exception e1) {e1.printStackTrace();}
		}

		if(rmap.down_out!=null )
		{
			try 
			{
				DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.down_mine,rmap.down_out,(sm.schedule.getSteps()-1),cellType,DistributedRegion.DOWN);

				connection.publishToTopic(dr,cellType.toString()+"D", NAME);

			} catch (Exception e1) { e1.printStackTrace(); }
		}

		if(rmap.corner_out_up_left_diag!=null)
		{
			DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.corner_mine_up_left,rmap.corner_out_up_left_diag,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_UP_LEFT);
			try 
			{

				connection.publishToTopic(dr,cellType.toString()+"CUDL", NAME);

			} catch (Exception e1) { e1.printStackTrace();}
		}
		if(rmap.corner_out_up_right_diag!=null)
		{
			DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.corner_mine_up_right,rmap.corner_out_up_right_diag,
					(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_UP_RIGHT);
			try 
			{

				connection.publishToTopic(dr,cellType.toString()+"CUDR", NAME);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if( rmap.corner_out_down_left_diag!=null)
		{
			DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.corner_mine_down_left,
					rmap.corner_out_down_left_diag,(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_DOWN_LEFT);
			try 
			{
				connection.publishToTopic(dr,cellType.toString()+"CDDL", NAME);

			} catch (Exception e1) {e1.printStackTrace();}
		}
		if(rmap.corner_out_down_right_diag!=null)
		{
			DistributedRegion<Integer,Int2D> dr=new DistributedRegion<Integer,Int2D>(rmap.corner_mine_down_right,
					rmap.corner_out_down_right_diag,(sm.schedule.getSteps()-1),cellType,DistributedRegion.CORNER_DIAG_DOWN_RIGHT);

			try 
			{

				connection.publishToTopic(dr,cellType.toString()+"CDDR", NAME);

			} catch (Exception e1) { e1.printStackTrace(); }
		}//<--

		//take from UpdateMap the updates for current last terminated step and use 
		//verifyUpdates() to elaborate informations

		PriorityQueue<Object> q;
		try 
		{
			q = updates.getUpdates(sm.schedule.getSteps()-1, neighborhood.size());

			while(!q.isEmpty())
			{
				DistributedRegion<Integer, Int2D> region=(DistributedRegion<Integer,Int2D>)q.poll();
				verifyUpdates(region);	
			}			

		}catch (InterruptedException e1) {e1.printStackTrace(); }

		for(Region<Integer,Int2D> region : updates_cache)
			for(Entry<Int2D> e_m: region)
			{
				RemoteAgent<Int2D> rm=e_m.r;
				((DistributedState<Int2D>)sm).addToField(rm,e_m.l);	
			}

		this.reset();
		/*
			ps.println(sm.schedule.getSteps()+";"+System.currentTimeMillis());
		*/
		if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)
		{
			try {
				tmp_zoom.STEP=((DistributedMultiSchedule)sm.schedule).getSteps()-1;
				connection.publishToTopic(tmp_zoom,"GRAPHICS"+cellType,NAME);
				System.out.println("pubblico per cella "+"GRAPHICS"+cellType+" con step"+tmp_zoom.STEP+" campo:"+NAME);
				tmp_zoom=new ZoomArrayList<RemoteAgent>();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return true;
	}
	
	public void memorizeRegionOut()
	{
		Class o=rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
	    for (int z = 0; z < fields.length; z++)
	    {
	      fields[z].setAccessible(true);
	      try
	      {
	    	 String name=fields[z].getName();
		     Method method = o.getMethod("get"+name, null);
		     Object returnValue = method.invoke(rmap, null);
		     if(returnValue!=null)
		     {
		    	 Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
		    	 if(name.contains("out"))
			  	 {
		    		 updates_cache.add(region.clone());
			  	 }
		     }
	       }
	       catch (IllegalArgumentException e){e.printStackTrace();} 
	       catch (IllegalAccessException e) {e.printStackTrace();} 
	       catch (SecurityException e) {e.printStackTrace();} 
	       catch (NoSuchMethodException e) {e.printStackTrace();} 
	       catch (InvocationTargetException e) {e.printStackTrace();}
	    }	     
	}
	/**
	 * This method takes updates from box and schedules every agent in the regions out.
	 * Every agent in the regions mine is compared with every agent in the updates_cache:
	 * if they are not equals, that in box mine is added.
	 * 
	 * @param box A Distributed Region that contains the updates
	 */
	public void verifyUpdates(DistributedRegion<Integer,Int2D> box)
	{
	
		Region<Integer,Int2D> r_mine=box.out;
		Region<Integer,Int2D> r_out=box.mine;
		
		for(Entry<Int2D> e_m: r_mine)
		{
				RemoteAgent<Int2D> rm=e_m.r;
				((DistributedState<Int2D>)sm).addToField(rm,e_m.l);
		  		rm.setPos(e_m.l);
		  		sm.schedule.scheduleOnce(rm);
		  		
		}
	
		updates_cache.add(r_out);
	}
	
	/**
	 * This method, written with Java Reflect, follows two logical ways for all the regions:
	 * - if a region is an out one, the agent's location is updated and it's insert a new Entry 
	 * 		in the updates_cache (cause the agent is moving out and it's important to maintain the information
	 * 		for the next step)
	 * - if a region is a mine one, the agent's location is updated and the agent is scheduled.
	 */
	public void updateFields()
	{
		Class o=rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
	    for (int z = 0; z < fields.length; z++)
	    {
	      fields[z].setAccessible(true);
	      try
	      {
	    	 String name=fields[z].getName();
		     Method method = o.getMethod("get"+name, null);
		     Object returnValue = method.invoke(rmap, null);
		     if(returnValue!=null)
		     {
		    	 Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
		    	 if(name.contains("out"))
			  	 {
		    		 for(Entry<Int2D> e: region)
		   			 {
		    			 RemoteAgent<Int2D> rm=e.r;
		    			 rm.setPos(e.l);
			    		 this.remove(rm);
		   			 } 
			  	 }
		    	 else
			     if(name.contains("mine"))
			     {

			    	for(Entry<Int2D> e: region)
			 	    {
			    		RemoteAgent<Int2D> rm=e.r;
			 	    	Int2D loc=e.l;
			 	    	rm.setPos(loc);
			 	    	this.remove(rm);
			 	    	sm.schedule.scheduleOnce(rm);
			 	    	setObjectLocation(rm,loc);
   	    			 } 
			      }
		     }
	      }
	      catch (IllegalArgumentException e){e.printStackTrace();} 
	      catch (IllegalAccessException e) {e.printStackTrace();} 
	      catch (SecurityException e) {e.printStackTrace();} 
	      catch (NoSuchMethodException e) {e.printStackTrace();} 
	      catch (InvocationTargetException e) {e.printStackTrace();}
	   }	    
	    
	}
	
	/**
	 * This method, written with Java Reflect, provides to add the Remote Agent
	 * in the right Region.
	 * @param rm The Remote Agent to add
	 * @param location The new location of the Remote Agent
	 * @return true if the agent is added in right way
	 */
	private boolean setAgents(RemoteAgent<Int2D> rm,Int2D location)
	{
		Class o=rmap.getClass();
	
		Field[] fields = o.getDeclaredFields();
	    for (int z = 0; z < fields.length; z++)
	    {
	      fields[z].setAccessible(true);
	      try
	      {
	       	String name=fields[z].getName();
	    	Method method = o.getMethod("get"+name, null);
	    	Object returnValue = method.invoke(rmap, null);
	    	
	    	if(returnValue!=null)
	    	{
	    		Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
	    		if(region.isMine(location.x,location.y))
	    	    {   	 
	    			if(name.contains("mine")){
	    				if(((DistributedMultiSchedule)sm.schedule).monitor.ZOOM)		
	    					tmp_zoom.add(rm);
	    				
	    				if(((DistributedMultiSchedule)((DistributedState)sm).schedule).NUMVIEWER.getCount()>0)
	    	    			writer.setPixel((int)(location.x%my_width), (int)(location.y%my_height), white);
	    			}
	    			return region.addAgents(new Entry<Int2D>(rm, location));
	    	    }    
	    	}
	      }
	      catch (IllegalArgumentException e){e.printStackTrace();} 
	      catch (IllegalAccessException e) {e.printStackTrace();} 
	      catch (SecurityException e) {e.printStackTrace();} 
	      catch (NoSuchMethodException e) {e.printStackTrace();} 
	      catch (InvocationTargetException e) {e.printStackTrace();}
	    }
	    return false;
	}
	
	/**
	 * Clear all Regions.
	 * @return true if the clearing is successful, false if exception is generated
	 */
	public boolean reset()
	{
		myfield.clear();	
		
		Class o=rmap.getClass();
		
		Field[] fields = o.getDeclaredFields();
		for (int z = 0; z < fields.length; z++)
		{
		    fields[z].setAccessible(true);
		    try
		    {
		    	String name=fields[z].getName();
		    	Method method = o.getMethod("get"+name, null);
		    	Object returnValue = method.invoke(rmap, null);
		    	if(returnValue!=null)
		    	{
		    		Region<Integer,Int2D> region=((Region<Integer,Int2D>)returnValue);
		    		region.clear();    
		    	}
		    }
		    catch (IllegalArgumentException e){e.printStackTrace(); return false;} 
		    catch (IllegalAccessException e) {e.printStackTrace();return false;} 
		    catch (SecurityException e) {e.printStackTrace();return false;} 
		    catch (NoSuchMethodException e) {e.printStackTrace();return false;} 
		    catch (InvocationTargetException e) {e.printStackTrace();return false;}
		}
	 return true;
	}
	
	/**
	 * Implemented method from the abstract class.
	 */
	public DistributedState getState() { return (DistributedState)sm; }
    
	//getters and setters
	public int getOwn_x() { return own_x; }
	public void setOwn_x(int own_x) { this.own_x = own_x; }
	public int getOwn_y() {	return own_y; }
	public void setOwn_y(int own_y) { this.own_y = own_y; }

	@Override
	public ArrayList<MessageListener> getLocalListener() {
		return listeners;
	}
	
	@Override
	public void setTable(HashMap table) {
		connection.setTable(table);
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	@Override
	public void setConnection(Connection con) {
		// TODO Auto-generated method stub
		connection=(ConnectionNFieldsWithActiveMQAPI)con;
	}
}