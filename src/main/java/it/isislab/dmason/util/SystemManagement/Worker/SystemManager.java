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
package it.isislab.dmason.util.SystemManagement.Worker;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
/**
* @author Michele Carillo
* @author Ada Mancuso
* @author Dario Mazzeo
* @author Francesco Milone
* @author Francesco Raia
* @author Flavio Serrapica
* @author Carmine Spagnuolo
*/
public class SystemManager {
	
	MemoryMXBean memory;
	OperatingSystemMXBean os;
	String id;
	
	
	public SystemManager(String id) {
		memory = ManagementFactory.getMemoryMXBean();
		os = ManagementFactory.getOperatingSystemMXBean();
		this.id = id;
	}
	
	public PeerStatusInfo generate() throws Exception{
		PeerStatusInfo info = new PeerStatusInfo();
		info.setArchitecture(os.getArch());
		info.setAddress(InetAddress.getLocalHost().getHostAddress());
		info.setNumCores(os.getAvailableProcessors());
		OperatingSystemMXBean sunBean = ManagementFactory.getOperatingSystemMXBean();
		//info.setMemory(sunBean.getTotalPhysicalMemorySize());
		info.setOS(os.getName()+" v"+os.getVersion());
		info.setHostname(InetAddress.getLocalHost().getHostName());
		info.setId(id);
		info.setStatus("Idle");
		return info;
	}

}