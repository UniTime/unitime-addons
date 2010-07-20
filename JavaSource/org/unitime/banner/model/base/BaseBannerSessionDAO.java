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

package org.unitime.banner.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.unitime.banner.model.dao.BannerSessionDAO;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseBannerSessionDAO extends org.unitime.banner.model.dao._RootDAO {

	// query name references


	public static BannerSessionDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static BannerSessionDAO getInstance () {
		if (null == instance) instance = new BannerSessionDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.banner.model.BannerSession.class;
	}

    public Order getDefaultOrder () {
		return null;
    }

	/**
	 * Cast the object as a org.unitime.banner.model.BannerSession
	 */
	public org.unitime.banner.model.BannerSession cast (Object object) {
		return (org.unitime.banner.model.BannerSession) object;
	}

	public org.unitime.banner.model.BannerSession get(java.lang.Long key)
	{
		return (org.unitime.banner.model.BannerSession) get(getReferenceClass(), key);
	}

	public org.unitime.banner.model.BannerSession get(java.lang.Long key, Session s)
	{
		return (org.unitime.banner.model.BannerSession) get(getReferenceClass(), key, s);
	}

	public org.unitime.banner.model.BannerSession load(java.lang.Long key)
	{
		return (org.unitime.banner.model.BannerSession) load(getReferenceClass(), key);
	}

	public org.unitime.banner.model.BannerSession load(java.lang.Long key, Session s)
	{
		return (org.unitime.banner.model.BannerSession) load(getReferenceClass(), key, s);
	}

	public org.unitime.banner.model.BannerSession loadInitialize(java.lang.Long key, Session s) 
	{ 
		org.unitime.banner.model.BannerSession obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param bannerSession a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.banner.model.BannerSession bannerSession)
	{
		return (java.lang.Long) super.save(bannerSession);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param bannerSession a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.banner.model.BannerSession bannerSession, Session s)
	{
		return (java.lang.Long) save((Object) bannerSession, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param bannerSession a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.banner.model.BannerSession bannerSession)
	{
		saveOrUpdate((Object) bannerSession);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param bannerSession a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.banner.model.BannerSession bannerSession, Session s)
	{
		saveOrUpdate((Object) bannerSession, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param bannerSession a transient instance containing updated state
	 */
	public void update(org.unitime.banner.model.BannerSession bannerSession) 
	{
		update((Object) bannerSession);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param bannerSession a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.banner.model.BannerSession bannerSession, Session s)
	{
		update((Object) bannerSession, s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param id the instance ID to be removed
	 */
	public void delete(java.lang.Long id)
	{
		delete((Object) load(id));
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param id the instance ID to be removed
	 * @param s the Session
	 */
	public void delete(java.lang.Long id, Session s)
	{
		delete((Object) load(id, s), s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param bannerSession the instance to be removed
	 */
	public void delete(org.unitime.banner.model.BannerSession bannerSession)
	{
		delete((Object) bannerSession);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param bannerSession the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.banner.model.BannerSession bannerSession, Session s)
	{
		delete((Object) bannerSession, s);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
	 * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
	 * For example 
	 * <ul> 
	 * <li>where a database trigger alters the object state upon insert or update</li>
	 * <li>after executing direct SQL (eg. a mass update) in the same session</li>
	 * <li>after inserting a Blob or Clob</li>
	 * </ul>
	 */
	public void refresh (org.unitime.banner.model.BannerSession bannerSession, Session s)
	{
		refresh((Object) bannerSession, s);
	}


}