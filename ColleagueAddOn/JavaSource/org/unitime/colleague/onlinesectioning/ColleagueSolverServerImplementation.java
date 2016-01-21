/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.colleague.onlinesectioning;

import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.jgroups.RemoteSolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainerWrapper;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ColleagueSolverServerImplementation {

	public static void main(String[] args) {
		SolverServerImplementation.main(args);;
		
		SolverServer server = (SolverServer)SolverServerImplementation.getInstance();
		SolverContainer<OnlineSectioningServer> container = new SolverContainerWrapper<OnlineSectioningServer>(
				((SolverServerImplementation)server).getDispatcher(),
				((RemoteSolverContainer<OnlineSectioningServer>)server.getOnlineStudentSchedulingContainer()),
				false);
				
		final ColleagueStudentUpdatesPoller poller = new ColleagueStudentUpdatesPoller(server, new ColleagueStudentUpdates(container));
		poller.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				poller.interrupt();
			}
		});
	}

}
