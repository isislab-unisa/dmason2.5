package it.isislab.dmason.sim.engine.testing.util;

/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.experimentals.util.trigger.Trigger;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.network.DNetwork;
import it.isislab.dmason.sim.field.support.network.DNetworkJMSMessageListener;
import it.isislab.dmason.sim.field.support.network.UpdaterThreadJMSForNetworkListener;
import it.isislab.dmason.util.connection.Address;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.testconnection.VirtualConnectionNFieldsWithVirtualJMS;
import it.isislab.dmason.util.connection.testconnection.VirtualMessageListener;
import it.isislab.dmason.util.management.globals.util.UpdateGlobalVarAtStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.jms.JMSException;

/**
 * 
 * @param <E>
 *            the type of locations
 *            
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini       
 */
public class DistributedStateConnectionFake<E> extends DistributedStateConnectionJMS<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String ip;
	public String port;
	private ConnectionJMS connectionJMS;
	private ArrayList<VirtualMessageListener> listeners = new ArrayList<VirtualMessageListener>();
	private ArrayList<DNetworkJMSMessageListener> networkListeners = new ArrayList<DNetworkJMSMessageListener>();
	private FakeUpdaterThreadForListener u1;
	private FakeUpdaterThreadForListener u2;
	private FakeUpdaterThreadForListener u3;
	private FakeUpdaterThreadForListener u4;
	private FakeUpdaterThreadForListener u5;
	private FakeUpdaterThreadForListener u6;
	private FakeUpdaterThreadForListener u7;
	private FakeUpdaterThreadForListener u8;
	private DistributedState dm;
	private Trigger TRIGGER;
	private DistributedMultiSchedule<E> schedule;
	private String topicPrefix;
	private CellType TYPE;
	private int MODE;
	private int NUMPEERS;
	private int rows;
	private int columns;
	private HashMap<String, Integer> networkNumberOfSubscribersForField;

	public DistributedStateConnectionFake() {
		super();
		
	}

	public void setupfakeconnection(DistributedState dm)
	{

		this.dm=dm;
		connectionJMS = new VirtualConnectionNFieldsWithVirtualJMS();
		
		/**
		 * 		try {
			connectionJMS.setupConnection(new Address(ip, port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.TRIGGER = new Trigger(connectionJMS);
		 */
		schedule=(DistributedMultiSchedule<E>)dm.schedule;
		topicPrefix=dm.topicPrefix;
		TYPE=dm.TYPE;
		MODE=dm.MODE;
		NUMPEERS=dm.NUMPEERS;
		rows=dm.rows;
		columns=dm.columns;
		networkNumberOfSubscribersForField=dm.networkNumberOfSubscribersForField;
	}
	

	

	public void init_connection() {
		
		try {
			connectionJMS.setupConnection(new Address(ip, port));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.TRIGGER = new Trigger(connectionJMS);
		if(((DistributedMultiSchedule<E>)dm.schedule).fields2D.size()>0)
			init_spatial_connection();
		if(((DistributedMultiSchedule<E>)dm.schedule).fieldsNetwork.size()>0)
			init_network_connection();
	}

	private void init_spatial_connection() {
		boolean toroidal_need=false;
		for(DistributedField2D field : 
			((DistributedMultiSchedule<E>)dm.schedule).getFields())
		{
			if(field.isToroidal())
			{
				toroidal_need=true;
				break;
			}
		}
//		//only for global variables
		dm.upVar=new UpdateGlobalVarAtStep(dm);
//		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
//				connectionJMS,
//				((DistributedMultiSchedule) this.schedule));
//		thread.start();
//
//		try {
//			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
//					schedule.fields2D.size());
//			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
//			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
//					connectionJMS,
//					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
//			t_zoom.start();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		if (toroidal_need)
		{
			connection_IS_toroidal();
		}
		else
		{
			connection_NO_toroidal();
		}

//		// Support for Global Parameters
//		try {
//			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
//			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
//					connectionJMS,
//					topicPrefix + "GLOBAL_REDUCED",
//					schedule.fields2D,
//					listeners);
//			ug.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}

	private void connection_IS_toroidal() {

		if (MODE == 0 || MODE == 3) { // HORIZONTAL_MODE

			try {
				connectionJMS.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "L",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE.pos_i + "-" + TYPE.pos_j + "R",
						schedule.fields2D
						.size());

				connectionJMS.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j - 1 + NUMPEERS) % NUMPEERS) + "R");
				connectionJMS.subscribeToTopic(topicPrefix+TYPE.pos_i + "-"
						+ ((TYPE.pos_j + 1 + NUMPEERS) % NUMPEERS) + "L");

				FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+TYPE.pos_i + "-"
								+ (((TYPE.pos_j - 1 + NUMPEERS)) % NUMPEERS)
								+ "R",
								schedule.fields2D, listeners);
				u1.start();

				FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+TYPE.pos_i + "-"
								+ (((TYPE.pos_j + 1 + NUMPEERS)) % NUMPEERS)
								+ "L",
								schedule.fields2D, listeners);
				u2.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (MODE==1){// SQUARE_MODE

			try {

				connectionJMS.createTopic(topicPrefix+TYPE + "L",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "R",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "D",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "U",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "CUDL",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "CUDR",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "CDDL",
						schedule.fields2D
						.size());
				connectionJMS.createTopic(topicPrefix+TYPE + "CDDR",
						schedule.fields2D
						.size());

				int i = TYPE.pos_i, j = TYPE.pos_j;
				int sqrt = (int) Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "L");
				connectionJMS.subscribeToTopic(topicPrefix+((i + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "R");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + columns) % columns) + "U");
				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + columns) % columns) + "D");
				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "CDDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i - 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "CDDL");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j - 1 + columns) % columns) + "CUDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i + 1 + rows) % rows) + "-"
						+ ((j + 1 + columns) % columns) + "CUDL");

				u1 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "L",
								schedule.fields2D, listeners);
				u1.start();

				u2 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "R",
								schedule.fields2D, listeners);
				u2.start();

				u3 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j + columns) % columns) + "U",
								schedule.fields2D, listeners);
				u3.start();

				u4 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j + columns) % columns) + "D",
								schedule.fields2D, listeners);
				u4.start();

				u5 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "CDDR",
								schedule.fields2D, listeners);
				u5.start();

				u6 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i - 1 + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "CDDL",
								schedule.fields2D, listeners);
				u6.start();

				u7 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j - 1 + columns) % columns) + "CUDR",
								schedule.fields2D, listeners);
				u7.start();

				u8 = new FakeUpdaterThreadForListener(
						connectionJMS, topicPrefix+((i + 1 + rows) % rows) + "-"
								+ ((j + 1 + columns) % columns) + "CUDL",
								schedule.fields2D, listeners);
				u8.start();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (MODE == 2) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"L",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"R",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"D",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"U",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CUDL",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CUDR",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CDDL",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CDDR",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL");

				u1 = new FakeUpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	private void connection_NO_toroidal() {

		if (MODE == 0 || MODE == 3) { // HORIZONTAL_MODE

			try {

				if(TYPE.pos_j < columns){
					connectionJMS.createTopic(topicPrefix+TYPE + "R",
							schedule.fields2D
							.size());

					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
					FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "L",
							schedule.fields2D, listeners);
					u2.start();

				}
				if(TYPE.pos_j > 0){
					connectionJMS.createTopic(topicPrefix+TYPE + "L",
							schedule.fields2D
							.size());

					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
					FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "R",
							schedule.fields2D, listeners);
					u1.start();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (MODE == 1) { // SQUARE NOT BALANCED

			try {
				
				if(TYPE.pos_j > 0)					
					connectionJMS.createTopic(topicPrefix+TYPE + "L",
							schedule.fields2D
							.size());
				if(TYPE.pos_j < columns)
					connectionJMS.createTopic(topicPrefix+TYPE + "R",
							schedule.fields2D
							.size());
				if(TYPE.pos_i > 0)
					connectionJMS.createTopic(topicPrefix+TYPE + "U",
							schedule.fields2D
							.size());
				if(TYPE.pos_i < rows)
					connectionJMS.createTopic(topicPrefix+TYPE + "D",
							schedule.fields2D
							.size());
				if(TYPE.pos_i < rows && TYPE.pos_j < columns)
					connectionJMS.createTopic(topicPrefix+TYPE + "CDDR",
							schedule.fields2D
							.size());
				if(TYPE.pos_i > 0 && TYPE.pos_j < columns)
					connectionJMS.createTopic(topicPrefix+TYPE + "CUDR",
							schedule.fields2D
							.size());
				if(TYPE.pos_i < rows && TYPE.pos_j > 0)
					connectionJMS.createTopic(topicPrefix+TYPE + "CDDL",
							schedule.fields2D
							.size());
				if(TYPE.pos_i > 0 && TYPE.pos_j > 0)
					connectionJMS.createTopic(topicPrefix+TYPE + "CUDL",
							schedule.fields2D
							.size());

				if(TYPE.pos_i > 0 && TYPE.pos_j > 0){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftUp()
							+ "CDDR");
					FakeUpdaterThreadForListener u1 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftUp() + "CDDR",
							schedule.fields2D, listeners);
					u1.start();
				}
				if(TYPE.pos_i > 0 && TYPE.pos_j < columns){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightUp()
							+ "CDDL");
					FakeUpdaterThreadForListener u2 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightUp() + "CDDL",
							schedule.fields2D, listeners);
					u2.start();
				}

				if(TYPE.pos_i < rows && TYPE.pos_j > 0){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagLeftDown()
							+ "CUDR");
					FakeUpdaterThreadForListener u3 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourDiagLeftDown() + "CUDR",
							schedule.fields2D, listeners);
					u3.start();
				}
				if(TYPE.pos_i < rows && TYPE.pos_j < columns){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDiagRightDown()
							+ "CUDL");
					FakeUpdaterThreadForListener u4 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourDiagRightDown() + "CUDL",
							schedule.fields2D, listeners);
					u4.start();
				}
				if(TYPE.pos_j > 0){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourLeft() + "R");
					FakeUpdaterThreadForListener u5 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourLeft() + "R",
							schedule.fields2D, listeners);
					u5.start();
				}
				if(TYPE.pos_j < columns){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourRight() + "L");
					FakeUpdaterThreadForListener u6 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourRight() + "L",
							schedule.fields2D, listeners);
					u6.start();
				}
				if(TYPE.pos_i > 0){	
					connectionJMS.subscribeToTopic(topicPrefix+(TYPE.getNeighbourUp() + "D"));
					FakeUpdaterThreadForListener u7 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourUp() + "D",
							schedule.fields2D, listeners);
					u7.start();
				}
				if(TYPE.pos_i < rows){
					connectionJMS.subscribeToTopic(topicPrefix+TYPE.getNeighbourDown() + "U");
					FakeUpdaterThreadForListener u8 = new FakeUpdaterThreadForListener(
							connectionJMS, topicPrefix+TYPE.getNeighbourDown() + "U",
							schedule.fields2D, listeners);
					u8.start();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (MODE == 2) { // SQUARE BALANCED

			try {

				connectionJMS.createTopic(topicPrefix+TYPE+"L",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"R",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"D",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"U",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CUDL",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CUDR",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CDDL",((DistributedMultiSchedule)schedule).fields2D.size());
				connectionJMS.createTopic(topicPrefix+TYPE+"CDDR",((DistributedMultiSchedule)schedule).fields2D.size());

				int i=TYPE.pos_i,j=TYPE.pos_j;
				int sqrt=(int)Math.sqrt(NUMPEERS);

				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L");
				connectionJMS.subscribeToTopic(topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR");
				connectionJMS.subscribeToTopic(topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL");

				u1 = new FakeUpdaterThreadForListener(connectionJMS,topicPrefix+((i+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"L",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u1.start();

				u2 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"R",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u2.start();

				u3 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"U",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u3.start();

				u4 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+sqrt)%sqrt)+"D",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u4.start();

				u5 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CDDR",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u5.start();

				u6 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i-1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CDDL",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u6.start();	

				u7 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j-1+sqrt)%sqrt)+"CUDR",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u7.start();		

				u8 = new FakeUpdaterThreadForListener(connectionJMS, topicPrefix+((i+1+sqrt)%sqrt)+"-"+((j+1+sqrt)%sqrt)+"CUDL",((DistributedMultiSchedule)schedule).fields2D,listeners);
				u8.start();

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@AuthorAnnotation(
			author = {"Francesco Milone","Carmine Spagnuolo"},
			date = "6/3/2014"
			)
	/**
	 * Setup topics for a  distributed field Network.
	 */
	private void init_network_connection()
	{
		DistributedMultiSchedule dms = schedule;
		ArrayList<DNetwork> networkLists = dms.fieldsNetwork;
		String toSubscribe;

		HashMap<String, ArrayList<DistributedField>> listToPublish = new HashMap<String, ArrayList<DistributedField>>();
		HashMap<String, ArrayList<DNetwork>> listToSubscribe = new HashMap<String, ArrayList<DNetwork>>();
		
		
		for (DNetwork distributedNetwork : networkLists) {
			int my_community = (TYPE.pos_i*rows)+TYPE.pos_j;
			ArrayList<Integer> myPublishNeighborhood = distributedNetwork.grpsub.getSubscribers(my_community);
			for (Integer integer : myPublishNeighborhood)
			{
				String my_topic = topicPrefix+"-Network-"+my_community+"-"+integer;
				if(listToPublish.get(my_topic)==null)
				{
					listToPublish.put(my_topic, new ArrayList<DistributedField>());
				}
				listToPublish.get(my_topic).add(distributedNetwork);
			}
			ArrayList<Integer> myNeighborhood = distributedNetwork.grpsub.getPublisher(my_community);
			for (Integer integer : myNeighborhood) {
				
				toSubscribe=topicPrefix+"-Network-"+integer+"-"+my_community;
				if(listToSubscribe.get(toSubscribe)==null)
				{
					listToSubscribe.put(toSubscribe, new ArrayList<DNetwork>());

				}
				listToSubscribe.get(toSubscribe).add(distributedNetwork);

				if(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())==null)
					networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(), new Integer(0));
				networkNumberOfSubscribersForField.put(distributedNetwork.getDistributedFieldID(), 
						(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID())+1));
			}	
		}

		for (DNetwork distributedNetwork : networkLists) {
			((DistributedFieldNetwork)distributedNetwork).setNumberOfUpdatesToSynchro(networkNumberOfSubscribersForField.get(distributedNetwork.getDistributedFieldID()));
		}

		Set<String> keySetToPublish = listToPublish.keySet();
		for(String topicName : keySetToPublish)
		{
			ArrayList<DistributedField> publishers = listToPublish.get(topicName);
			try {
				connectionJMS.createTopic(topicName, publishers.size());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Set<String> keySetToSubscribe = listToSubscribe.keySet();
		for(String topicName : keySetToSubscribe)
		{
			ArrayList<DNetwork> subscribers = listToSubscribe.get(topicName);
			try {
				connectionJMS.subscribeToTopic(topicName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			UpdaterThreadJMSForNetworkListener utfl = new UpdaterThreadJMSForNetworkListener(connectionJMS, topicName, subscribers, networkListeners);
			utfl.start();
		}
	}

	public CellType getType() {
		return TYPE;
	}

	public ConnectionJMS getConnection() {
		return connectionJMS;
	}


	public Trigger getTrigger() {
		return TRIGGER;
	}

	//added for close connection of current simulation after reset
	public void closeConnectionJMS() throws JMSException
	{
		connectionJMS.close();
	}

	public void init_service_connection() {
//		dm.upVar=new UpdateGlobalVarAtStep(dm);
//	
//		ThreadVisualizationCellMessageListener thread = new ThreadVisualizationCellMessageListener(
//				connectionJMS,
//				((DistributedMultiSchedule) this.schedule));
//		thread.start();
//
//		try {
//			boolean a = connectionJMS.createTopic(topicPrefix+"GRAPHICS" + TYPE,
//					schedule.fields2D.size());
//			connectionJMS.subscribeToTopic(topicPrefix+"GRAPHICS" + TYPE);
//			ThreadZoomInCellMessageListener t_zoom = new ThreadZoomInCellMessageListener(
//					connectionJMS,
//					TYPE.toString(), (DistributedMultiSchedule) this.schedule);
//			t_zoom.start();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// Support for Global Parameters
//		try {
//			connectionJMS.subscribeToTopic(topicPrefix + "GLOBAL_REDUCED");
//			UpdaterThreadForGlobalsListener ug = new UpdaterThreadForGlobalsListener(
//					connectionJMS,
//					topicPrefix + "GLOBAL_REDUCED",
//					schedule.fields2D,
//					listeners);
//			ug.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}
}