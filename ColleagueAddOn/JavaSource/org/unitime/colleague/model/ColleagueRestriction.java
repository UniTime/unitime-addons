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

package org.unitime.colleague.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.hibernate.Session;
import org.unitime.colleague.model.base.BaseColleagueRestriction;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;


/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "colleague_restriction")
public class ColleagueRestriction extends BaseColleagueRestriction {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ColleagueRestriction () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ColleagueRestriction (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public static ColleagueRestriction findColleagueRestrictionTermCode(String restrictionCode,
			String termCode, Session session) {

		return ColleagueRestrictionDAO.getInstance().getSession()
				.createQuery("from ColleagueRestriction cr where cr.code = :code and cr.termCode = :termCode", ColleagueRestriction.class)
				.setParameter("code", restrictionCode)
				.setParameter("termCode", termCode)
				.uniqueResult();
	}

	public static ColleagueRestriction getColleagueRestrictionById(Long id) {
		return(ColleagueRestrictionDAO.getInstance().get(id));
	}

	@Transient
	public static List<ColleagueRestriction> getAllColleagueRestrictions() {
		return ColleagueRestrictionDAO.getInstance().getSession().createQuery("from ColleagueRestrictions", ColleagueRestriction.class).list();
	}

	public static List<ColleagueRestriction> getAllColleagueRestrictionsForTerm(String termCode) {
		return ColleagueRestrictionDAO.getInstance().getSession()
				.createQuery("from ColleagueRestriction cr where cr.termCode = :termCode", ColleagueRestriction.class)
				.setParameter("termCode", termCode)
				.list();
	}

	public static List<ColleagueRestriction> getAllColleagueRestrictionsForSession(Session hibSession, Long sessionId) {
		ColleagueSession cSession = ColleagueSession.findColleagueSessionForSession(sessionId, hibSession);
		if (cSession == null) return null;
		return ColleagueRestrictionDAO.getInstance().getSession()
				.createQuery("from ColleagueRestriction cr where cr.termCode = :termCode", ColleagueRestriction.class)
				.setParameter("termCode", cSession.getColleagueTermCode())
				.list();
	}

	public ColleagueRestriction clone() {
		ColleagueRestriction cr = new ColleagueRestriction();
		cr.setCode(this.getCode());
		cr.setName(this.getName());
		cr.setDescription(this.getDescription());
        return(cr);
	}
	
	@Transient
	public String getOptionLabel(){
		return(this.getCode() + " - " + this.getName());
	}
	
}
