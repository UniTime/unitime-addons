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
package org.unitime.banner.onlinesectioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.solver.jgroups.SolverServer;

/**
 * @author Tomas Muller
 */
public class BannerStudentUpdatesPoller extends Thread {
	protected static Log sLog = LogFactory.getLog(BannerStudentUpdatesPoller.class);

	private SolverServer iServer;
	private BannerStudentUpdates iUpdates;
	private boolean iActive = true;
	
	public BannerStudentUpdatesPoller(SolverServer server, BannerStudentUpdates updates) {
		super("BannerStudentUpdatesPoller");
		iServer = server;
		iUpdates = updates;
		setDaemon(true);
	}
	
	protected long getSleepInterval() {
		return 1000l * Long.parseLong(ApplicationProperties.getProperty("banner.studentUpdates.pollInterval",  isEnabled() ? "10" : "60"));
	}
	
	protected boolean isEnabled() {
		if (!"true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdates.enabled", "false"))) return false;
		return iServer.isActive() && iServer.isCoordinator();
	}
	
	@Override
	public void interrupt() {
		iActive = false;
		super.interrupt();
		try { join(); } catch (InterruptedException e) {}
	}
	
	public void run() {
		sLog.info("Banner Student Updates Poller is up.");
		while (true) {
			try {
				sleep(getSleepInterval());
			} catch (InterruptedException e) {}
			
			if (!iActive) break;
			
			if (isEnabled()) {
				try {
					sLog.debug("Checking for update messages...");
					iUpdates.pollMessage();
				} catch (Exception e) {
					sLog.error("Failed to process update messages:" + e.getMessage(), e);
				}
			}
		}
		sLog.info("Banner Student Updates Poller is down.");
	}

}