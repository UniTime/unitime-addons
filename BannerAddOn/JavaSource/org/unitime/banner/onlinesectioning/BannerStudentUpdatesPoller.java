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