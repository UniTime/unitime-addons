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

package org.unitime.banner.model.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.base.BaseQueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;



public class QueueOutDAO extends BaseQueueOutDAO {

	/**
	 * Default constructor.  Can be used in place of getInstance()
	 */
	public QueueOutDAO () {}

    /**
     * Finds all QueueOuts in the database based on status
     * @param status
     * @return
     * @throws LoggableException 
     */
    public List findByStatus(String status) throws LoggableException {
        List queueOuts = null;
        Transaction tx = null;
        try {
            
            tx = getSession().beginTransaction();
            
            String hql = "from QueueOut "
            			+ "where status = :status " +
            					"order by postDate ";
            
            Query query = getSession().createQuery(hql);
            query.setString("status", status);
            
            queueOuts =  query.list();
            tx.commit();
            
        } catch (HibernateException e) {
        	tx.rollback();
        	throw new LoggableException(e);
        } finally {
        	getSession().close();
        }
        return queueOuts;
    }
    
	public QueueOut findById(Long queueId) throws LoggableException {

        List list = null;
        Transaction tx = null;
        try {
            
            tx = getSession().beginTransaction();
            
            String hql = "from QueueOut "
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
		
        if(list.size() > 0) return (QueueOut) list.get(0);
        
        return null;
	}
	
	public QueueOut findFirstByStatus(String status) throws LoggableException {
		List<QueueOut> list = null;
		Transaction tx = null;
		try {
			tx = getSession().beginTransaction();
			
			Query query = getSession().createQuery("from QueueOut where status = :status order by uniqueId");
			query.setString("status", status);
			query.setMaxResults(1);
			
			list = query.list();
			tx.commit();
        } catch (HibernateException e) {
        	tx.rollback();
        	throw new LoggableException(e);
        } finally {
        	getSession().close();
        }
		return (list == null || list.isEmpty() ? null : list.get(0));
	}
}