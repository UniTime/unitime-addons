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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
@Service("bannerStudentUpdatesService")
@DependsOn("solverServerService")
public class BannerStudentUpdatesService implements InitializingBean, DisposableBean {
	protected BannerStudentUpdatesPoller iPoller;
	
	@Autowired SolverServerService solverServerService;

	@Override
	public void destroy() throws Exception {
		if (iPoller != null)
			iPoller.interrupt();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		iPoller = new BannerStudentUpdatesPoller(solverServerService.getLocalServer(),
				new BannerStudentUpdates(solverServerService.getOnlineStudentSchedulingContainer()));
		iPoller.start();
	}
}
