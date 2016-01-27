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

import java.util.Collection;

import org.hibernate.Query;
import org.hibernate.Session;
import org.unitime.colleague.model.base.BaseColleagueSuffixDef;
import org.unitime.colleague.model.dao.ColleagueSuffixDefDAO;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.dao.ItypeDescDAO;


/**
 * 
 * @author says
 *
 */
public class ColleagueSuffixDef extends BaseColleagueSuffixDef {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ColleagueSuffixDef () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ColleagueSuffixDef (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public static String findCourseSuffix(CourseOffering courseOffering) {
		String courseSuffix = null;
		if (courseOffering.getCourseNbr().indexOf("-") > 0){
			courseSuffix = courseOffering.getCourseNbr().substring((courseOffering.getCourseNbr().indexOf('-') + 1));
		}
		return(courseSuffix);
	}

	public static String findColleagueCourseNumber(CourseOffering courseOffering) {
		String courseNumber = null;
		if (courseOffering.getCourseNbr().indexOf("-") > 0){
			courseNumber = courseOffering.getCourseNbr().substring(0, courseOffering.getCourseNbr().indexOf('-'));
		} else {
			courseNumber = courseOffering.getCourseNbr();
		}
		return(courseNumber);
	}

   
	public static ColleagueSuffixDef findColleagueSuffixDefForTermCodeItypeSuffix(String termCode, Integer itypeId, String suffix){
		StringBuilder sb = new StringBuilder();
		sb.append("from ColleagueSuffixDef csd where csd.termCode = :termCode");
		if (itypeId == null){
			sb.append(" and csd.itypeId is null");
		} else {
			sb.append(" and csd.itypeId = :itypeId");
		}
		if (suffix == null){
			sb.append(" and csd.courseSuffix is null");
		} else {
			sb.append(" and csd.courseSuffix = :suffix");
		}
		Query query = ColleagueSuffixDefDAO.getInstance().getQuery(sb.toString());
		query.setString("termCode", termCode);
		if (itypeId != null){
			query.setInteger("itypeId", itypeId.intValue());
		}
		if (suffix != null){
			query.setString("suffix", suffix);
		}
		return((ColleagueSuffixDef) query.uniqueResult());
		
	}
	
	public static ColleagueSuffixDef findColleagueSuffixDefForTermCode(ItypeDesc itype, CourseOffering courseOffering,
			String termCode, Session session) {

		String courseSuffix = findCourseSuffix(courseOffering);
		ColleagueSuffixDef csd = findColleagueSuffixDefForTermCodeItypeSuffix(termCode, (itype == null?null:itype.getItype()), courseSuffix);
		if (courseSuffix != null) {} else {}
		if (csd == null) {
			ColleagueSuffixDef csdItype = findColleagueSuffixDefForTermCodeItypeSuffix(termCode, (itype == null?null:itype.getItype()), null);
					
			ColleagueSuffixDef csdCourseSuffix = findColleagueSuffixDefForTermCodeItypeSuffix(termCode, null, courseSuffix);
			if (csdItype == null && csdCourseSuffix == null){
				csd = findColleagueSuffixDefForTermCodeItypeSuffix(termCode, null, null);
				if (csd == null){
					csd = new ColleagueSuffixDef();
					csd.setMinSectionNum(new Integer(1));
					csd.setMaxSectionNum(new Integer(99));
				}
			} else if (csdItype == null && csdCourseSuffix != null) {
				csd = csdCourseSuffix;
			} else if (csdItype != null && csdCourseSuffix == null) {
				csd = csdItype;
			} else {
				// Combine the two defs
				csd = new ColleagueSuffixDef();
				csd.setItypeId(csdItype.getItypeId());
				csd.setCourseSuffix(csdCourseSuffix.getCourseSuffix());
				csd.setItypePrefix(csdItype.getItypePrefix());
				csd.setPrefix(csdCourseSuffix.getPrefix());
				csd.setSuffix(csdCourseSuffix.getSuffix());
				if (csdItype.getCampusCode() != null && csdCourseSuffix.getCampusCode() == null) {
					csd.setCampusCode(csdItype.getCampusCode());
				} else if (csdItype.getCampusCode() == null && csdCourseSuffix.getCampusCode() != null) {
					csd.setCampusCode(csdCourseSuffix.getCampusCode());
				} else if (csdItype.getCampusCode() != null && csdCourseSuffix.getCampusCode() != null) {
					if(csdItype.getCampusCode() == csdCourseSuffix.getCampusCode()){
						csd.setCampusCode(csdItype.getCampusCode());
					} else {
						csd.setCampusCode(csdCourseSuffix.getCampusCode());
					}
				}
				csd.setMinSectionNum(csdCourseSuffix.getMinSectionNum());
				csd.setMaxSectionNum(csdCourseSuffix.getMaxSectionNum());
				if (csd.getItypePrefix() != null && (csd.getPrefix() != null || csd.getSuffix() != null)){
					// the section identifier can only be 3 characters, if two are taken with prefix or suffixes
					//  only one character is left to be used so the min/max cannot exceed 9
					if (csd.getMinSectionNum().intValue() > 10){
						csd.setMinSectionNum(new Integer(1));
					}
					if (csd.getMaxSectionNum().intValue() > 10){
						csd.setMaxSectionNum(new Integer(10));
					}
				}
			}
			
		}
		return(csd);
	}
	

	public static ColleagueSuffixDef getColleagueSuffixDefsById(Long id) {
		return(ColleagueSuffixDefDAO.getInstance().get(id));
	}

	@SuppressWarnings("unchecked")
	public static Collection<ColleagueSuffixDef> getAllColleagueSuffixDefs() {
		return((Collection <ColleagueSuffixDef>)ColleagueSuffixDefDAO.getInstance().getQuery("from ColleagueSuffixDef").list());
	}

	@SuppressWarnings("unchecked")
	public static Collection<ColleagueSuffixDef> getAllColleagueSuffixDefsForSession(Session hibSession, Long acadSessionId) {
		ColleagueSession cSession = ColleagueSession.findColleagueSessionForSession(acadSessionId, hibSession);
		return((Collection <ColleagueSuffixDef>)ColleagueSuffixDefDAO
				.getInstance()
				.getQuery("from ColleagueSuffixDef csd where csd.termCode = :termCode")
				.setString("termCode", cSession.getColleagueTermCode()).list());
	}

	public String sectionIdSearchString() {
    	StringBuilder sb = new StringBuilder();
    	if (this.getItypePrefix() != null) {
    		sb.append(this.getItypePrefix());
    	}
    	if (this.getPrefix() != null){
    		sb.append(this.getPrefix());
    	}
    	sb.append("%");
    	if (this.getSuffix() != null){
    		sb.append(this.getSuffix());
    	}
    	return(sb.toString());
    }
    
    public String sectionIdRegexSearchString() {
    	StringBuilder sb = new StringBuilder();
    	if (this.getItypePrefix() != null) {
    		sb.append(this.getItypePrefix());
    		if (this.getPrefix() == null){
    			sb.append("[0-9]");
    			if (this.getSuffix() != null){
        			sb.append("?");
        		}
    		}  
    		if (this.getSuffix() == null){
    			sb.append("[0-9]");
    			if (this.getPrefix() != null){
        			sb.append("?");
        		} 			
    		}
    	}
    	if (this.getPrefix() != null){
    		sb.append(this.getPrefix());
			sb.append("[0-9]");
   		if (this.getItypePrefix() == null) {
			sb.append("+");    			
    		}
    	}
    	if (this.getSuffix() != null){
			sb.append("[0-9]");
       		if (this.getItypePrefix() == null) {
    			sb.append("+");    			
        	} else {
        		sb.append(".");
        	}
    		sb.append(this.getSuffix());
    	}
    	if (this.isAllNumbers()){
    		sb.append("[0-9]+");
    	}
    	return(sb.toString());
	
    }
    
    public boolean isAllNumbers() {
    	return((this.getItypePrefix() == null || this.getItypePrefix().isEmpty()) 
    			&& (this.getPrefix() == null || this.getPrefix().isEmpty())
    			&& (this.getSuffix() == null || this.getSuffix().isEmpty()));
    }
    
    public boolean isValidColleagueSuffixDef() {
    	if (this.getMinSectionNum() == null) {
    		return(false);
    	}
    	if (this.getMaxSectionNum() == null){
    		return(false);
    	}
    	if (this.getPrefix() != null && this.getSuffix() != null){
    		return(false);
    	}
    	if (this.getItypePrefix() != null && (this.getPrefix() != null || this.getSuffix() != null)){
    		if (this.getMinSectionNum().intValue() > 9){
    			return(false);
    		}
    		if (this.getMaxSectionNum().intValue() > 9){
    			return(false);
    		}
    	}
    	return(true);
    }
    
    private String nextNum(String indexString) throws Exception{
		int num = Integer.parseInt(indexString);
		num++;
		if (num > this.getMaxSectionNum().intValue()){
			
			ItypeDesc itype = null;
			if (this.getItypeId() != null) {
				ItypeDescDAO.getInstance().get(this.getItypeId());
			}
			throw new Exception("All possible section numbers have been used for this combiniation of instructional type and course suffix:  " + (itype == null? "Any Instr Type" : itype.getDesc()) + " "+ (this.getCourseSuffix() == null? "No Matching Course": this.getCourseSuffix()));
		}
		return(Integer.toString(num));
		
    }
    public String findNextSectionIndex(String sectionIndex) throws Exception {
    	String next = "";
    	if (sectionIndex == null) {
    		if (this.isAllNumbers()){
    			int num = this.getMinSectionNum().intValue();
    			next = Integer.toString(num);
        		if (next.length() == 1){
        			next = "0" + next;
        		}
    		} else if ((this.getItypePrefix() != null && !this.getItypePrefix().isEmpty()) 
    				&& ((this.getPrefix() != null && !this.getPrefix().isEmpty()) || (this.getSuffix() != null && !this.getSuffix().isEmpty()))){
    			next = this.getItypePrefix();
    			if (this.getPrefix() != null && !this.getPrefix().isEmpty()){
    				next += this.getPrefix();
    			} else {
    				next += this.getSuffix();
    			}
    		} else if (this.getItypePrefix() != null && !this.getItypePrefix().isEmpty()){
    			next = this.getItypePrefix();
    			next += this.getMinSectionNum().toString();
    		} else if (this.getPrefix() != null && !this.getPrefix().isEmpty()){
    			next = this.getPrefix();
    			next += this.getMinSectionNum().toString();
    		} else {
    			next = this.getMinSectionNum().toString();
    			next += this.getSuffix();
    		}
    	} else if (this.isAllNumbers()){
    		next = nextNum(sectionIndex);
    		if (next.length() == 1){
    			next = "0" + next;
    		}
    	} else if (this.getItypePrefix() != null && !this.getItypePrefix().isEmpty()) {
    		String sec = sectionIndex.substring(1);
    		next = this.getItypePrefix();
    		if (this.getPrefix() != null && !this.getPrefix().isEmpty()){
    			next += this.getPrefix();
    			if (sec.length() == 1){
    				next += this.getMinSectionNum().toString();
    			} else {
    				next += nextNum(sec.substring(1));
    			}
    		} else if (this.getSuffix() != null && !this.getSuffix().isEmpty()){
    			if (sec.length() == 1){
    				next += this.getMinSectionNum().toString();
    			} else {
    				next += nextNum(sec.substring(0,1));
    			}
    			next += this.getSuffix();
    		} else {
				next += nextNum(sec);
    		}
    	} else if (this.getPrefix() != null && !this.getPrefix().isEmpty()) {
    		next = this.getPrefix();
    		next += nextNum(sectionIndex.substring(1));
    	} else {
    		next += nextNum(sectionIndex.substring(0, sectionIndex.indexOf(this.getSuffix())));
    		next += this.getSuffix();
    	}
    	if (next.isEmpty()){
    		next=null;
    	}
    	return(next);
    }
    
    public ColleagueSuffixDef clone() {
    	ColleagueSuffixDef csd = new ColleagueSuffixDef();
    	csd.setItypeId(this.getItypeId());
    	csd.setCourseSuffix(this.getCourseSuffix());
    	csd.setCampusCode(this.getCampusCode());
    	csd.setMinSectionNum(this.getMinSectionNum());
    	csd.setMaxSectionNum(this.getMaxSectionNum());
    	csd.setItypePrefix(this.getItypePrefix());
    	csd.setPrefix(this.getPrefix());
    	csd.setSuffix(this.getSuffix());
    	csd.setNote(this.getNote());
    	return(csd);
    }
}