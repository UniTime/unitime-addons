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

import java.io.Serializable;

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;

public abstract class BaseBannerSectionToClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iClassId;

	private BannerSection iBannerSection;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CLASS_ID = "classId";

	public BaseBannerSectionToClass() {
		initialize();
	}

	public BaseBannerSectionToClass(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getClassId() { return iClassId; }
	public void setClassId(Long classId) { iClassId = classId; }

	public BannerSection getBannerSection() { return iBannerSection; }
	public void setBannerSection(BannerSection bannerSection) { iBannerSection = bannerSection; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSectionToClass)) return false;
		if (getUniqueId() == null || ((BannerSectionToClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSectionToClass)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerSectionToClass["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerSectionToClass[" +
			"\n	BannerSection: " + getBannerSection() +
			"\n	ClassId: " + getClassId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
