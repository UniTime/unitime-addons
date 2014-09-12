/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

package org.unitime.banner.model;

import org.unitime.banner.model.base.BaseQueue;



/**
 * 
 * @author says
 *
 */
public abstract class Queue extends BaseQueue {
	private static final long serialVersionUID = 1L;

	public static final String STATUS_POSTED = "POSTED";
	public static final String STATUS_PROCESSED = "PROCESSED";
	public static final String STATUS_INVALID = "INVALID";
	public static final String STATUS_STALE = "STALE";
	// posted message is ready to be processed (outside of the queue processor)
	public static final String STATUS_READY = "READY";
	// posted message failed to be processed (outside of the queue processor)
	public static final String STATUS_FAILED = "FAILED";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Queue () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Queue (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public abstract String getQueueType();

}