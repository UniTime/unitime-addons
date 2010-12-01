/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.banner.model.base;

import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.QueueOutDAO;

public abstract class BaseQueueOutDAO extends _RootDAO<QueueOut,Long> {

	private static QueueOutDAO sInstance;

	public static QueueOutDAO getInstance() {
		if (sInstance == null) sInstance = new QueueOutDAO();
		return sInstance;
	}

	public Class<QueueOut> getReferenceClass() {
		return QueueOut.class;
	}
}
