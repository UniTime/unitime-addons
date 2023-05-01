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

package org.unitime.banner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;


/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "banner_crn_provider")
public class BannerTermCrnProperties extends BaseBannerTermCrnProperties {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerTermCrnProperties () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerTermCrnProperties (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public static BannerTermCrnProperties findBannerTermCrnPropertiesForTermCode(
			String bannerTermCode, Session session) {

		return session.createQuery(
				"from BannerTermCrnProperties btcp where btcp.bannerTermCode = :bannerTermCode", BannerTermCrnProperties.class)
				.setParameter("bannerTermCode", bannerTermCode).uniqueResult();
	}
	
	public static HashSet<BannerTermCrnProperties> findAllBannerTermCrnPropertiesForBannerSessions(ArrayList<Long> bannerSessionIds){
		HashSet<BannerTermCrnProperties> crnProps = new HashSet<BannerTermCrnProperties>();
		for (Long bsId : bannerSessionIds) {
			BannerSession bs = BannerSession.getBannerSessionById(bsId);
			if (bs.getBannerTermCrnProperties() != null) {
				crnProps.add(bs.getBannerTermCrnProperties());
			}
		}
		return(crnProps);
	}

	public static BannerTermCrnProperties getBannerTermCrnPropertiesById(Long id) {
		return(BannerTermCrnPropertiesDAO.getInstance().get(id));
	}

	@Transient
	public static List<BannerTermCrnProperties> getAllBannerTermCrnProperties() {
		return BannerTermCrnPropertiesDAO.getInstance().getSession().createQuery("from BannerTermCrnProperties", BannerTermCrnProperties.class).list();
	}
	
	@Transient
	public String getBannerSessionsLabel() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (BannerSession bs : this.getBannerSessions()) {
			if (first) {
				first = false;
			} else {
				sb.append(";");
			}
			sb.append(bs.getLabel());
		}
		return(sb.toString());
	}


}
