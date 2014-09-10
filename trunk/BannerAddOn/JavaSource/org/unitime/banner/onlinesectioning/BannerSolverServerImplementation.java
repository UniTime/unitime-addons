/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.onlinesectioning;

import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.jgroups.RemoteSolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainerWrapper;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;

/**
 * @author Tomas Muller
 */
public class BannerSolverServerImplementation {

	public static void main(String[] args) {
		SolverServerImplementation.main(args);;
		
		SolverServer server = (SolverServer)SolverServerImplementation.getInstance();
		SolverContainer<OnlineSectioningServer> container = new SolverContainerWrapper<OnlineSectioningServer>(
				((SolverServerImplementation)server).getDispatcher(),
				((RemoteSolverContainer<OnlineSectioningServer>)server.getOnlineStudentSchedulingContainer()),
				false);
				
		final BannerStudentUpdatesPoller pooler = new BannerStudentUpdatesPoller(server, new BannerStudentUpdates(container));
		pooler.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				pooler.interrupt();
			}
		});
	}

}
