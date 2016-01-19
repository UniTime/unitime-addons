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

package org.unitime.colleague.model.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.colleague.model.QueueIn;
import org.unitime.colleague.model.base.BaseQueueInDAO;
import org.unitime.colleague.queueprocessor.exception.LoggableException;



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