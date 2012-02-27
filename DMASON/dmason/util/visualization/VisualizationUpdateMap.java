package dmason.util.visualization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import dmason.sim.field.CellType;

public class VisualizationUpdateMap<E,F> extends HashMap<Long,HashMap<String,Object>> implements Serializable
{
    private final ReentrantLock lock;
    private final Condition block;
    

    
    public VisualizationUpdateMap()
    {
    	lock=new ReentrantLock();
    	block=lock.newCondition();
    }
    private boolean FORCE=false;

	public HashMap<String,Object> getUpdates(long step,int num_cell) throws InterruptedException
	{
		System.out.println("Entro per step "+step);
		long nowStep=step;
		lock.lock();
		HashMap<String,Object> tmp = this.get(nowStep);

		while(tmp==null)
		{
			block.await();
			if(FORCE)
			{
				FORCE=false;
				return null;
			}
			
			tmp=this.get(nowStep);
		}
		while(tmp.size()!=num_cell)
		{
			block.await();
			if(FORCE){
				FORCE=false;
			return null;
			}
		}
		this.remove(nowStep);
		lock.unlock();
		return tmp;
	}
	public void forceSblock()
	{
		lock.lock();
			FORCE=true;
			System.out.println("sbloccoooooooooooo");
			block.signal();
			
		lock.unlock();
	}
	
	public void put(long step, Object d_reg)
	{		
		lock.lock();
		
		CellType ct = new CellType(((RemoteSnap)d_reg).i, ((RemoteSnap)d_reg).j);
		
		HashMap<String,Object> tmp=this.get(step);
		
		if(tmp!=null)
		{
			tmp.put(((RemoteSnap)d_reg).i+"-"+((RemoteSnap)d_reg).j,d_reg);
		}
		else
		{
			HashMap<String,Object> queue_update=new HashMap<String,Object>();
			
			queue_update.put(((RemoteSnap)d_reg).i+"-"+((RemoteSnap)d_reg).j,d_reg);

			super.put(step,queue_update); 
		}
		
		block.signal();	
		lock.unlock();
	}
}