/*
 * UniTime 3.1 (University Timetabling Application)
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

package org.unitime.banner.model.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.base.BaseQueueInDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;



public class QueueInDAO extends BaseQueueInDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public QueueInDAO () {}

	public QueueIn findById(Long queueId) throws LoggableException {

        List list = null;
        Transaction tx = null;
        try {
            
            tx = getSession().beginTransaction();
            
            String hql = "from QueueIn "
            			+ "where uniqueId = :queueId ";
            
            Query query = getSession().createQuery(hql);
            query.setLong("queueId", queueId);
            
            list = query.list();
            tx.commit();
            
        } catch (HibernateException e) {
        	tx.rollback();
        	throw new LoggableException(e);
        } finally {
        	getSession().close();
        }
		
		if(list.size() > 0) return (QueueIn) list.get(0);
		return null;
	}

	public QueueIn findByMatchId(Long matchId) throws LoggableException {

        List list = null;
        Transaction tx = null;
        try {
            
            tx = getSession().beginTransaction();
            
            String hql = "from QueueIn "
            			+ "where matchId = :matchId ";
            
            Query query = getSession().createQuery(hql);
            query.setLong("matchId", matchId);
            
            list = query.list();
            tx.commit();
            
        } catch (HibernateException e) {
        	tx.rollback();
        	throw new LoggableException(e);
        } finally {
        	getSession().close();
        }
		
		if(list.size() > 0) return (QueueIn) list.get(0);
		
		return null;
	}

}