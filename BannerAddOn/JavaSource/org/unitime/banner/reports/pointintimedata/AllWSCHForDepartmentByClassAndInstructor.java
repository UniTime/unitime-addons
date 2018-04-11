package org.unitime.banner.reports.pointintimedata;

import java.util.ArrayList;
import java.util.HashSet;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.reports.pointintimedata.WSCHByDepartment;
import org.unitime.timetable.security.SessionContext;

public class AllWSCHForDepartmentByClassAndInstructor extends WSCHByDepartment {
	private ArrayList<Long> iDepartmentIds;
	private Long iSessionId;

	@Autowired 
	private SessionContext sessionContext;

	public AllWSCHForDepartmentByClassAndInstructor() {
		super();
		getParameters().add(Parameter.DEPARTMENTS);
		getParameters().add(Parameter.SESSION);
	}

	private boolean bannerDataExistsForSession() {
		if (sessionContext == null || sessionContext.getUser() == null || sessionContext.getUser().getCurrentAcademicSessionId() == null) {
			return(false);
		}
		 
		BannerSession bs = BannerSession.findBannerSessionForSession(sessionContext.getUser().getCurrentAcademicSessionId(), BannerSessionDAO.getInstance().getSession());
		return(bs != null);
	}
	
	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnInstructor());
		hdr.add(MSG.columnInstructorExternalId());
		hdr.add(MSG.columnSubjectArea());
		hdr.add(MSG.columnCourseNumber());
		hdr.add(MSG.columnItype());
		hdr.add(MSG.columnOrganized());
		hdr.add(MSG.columnSectionNumber());
		hdr.add(MSG.columnExternalId());
		hdr.add(MSG.columnNormalizedPercentShare());
		hdr.add(MSG.columnGradableItypeCredit());
		hdr.add(MSG.columnOrganizedWeeklyClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyClassHours());
		hdr.add(MSG.columnWeeklyClassHours());
		hdr.add(MSG.columnOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		setHeader(hdr);
	}

	@Override
	protected void parseParameters() {
		super.parseParameters();
		if (getParameterValues().get(Parameter.DEPARTMENTS).size() < 1){
			//TODO: error
		} else {
			setDepartmentIds(getParameterValues().get(Parameter.DEPARTMENTS));
		}
		if (getParameterValues().get(Parameter.SESSION).size() != 1){
			//TODO: error
		} else {
			setSessionId((Long)getParameterValues().get(Parameter.SESSION).get(0));
		}

	}
	
	@Override
	public String reportName() {
		return(MSG.deptWSCHReportAllHoursForDepartmentByClassAndInstructor());
	}

	@Override
	public String reportDescription() {
		return(MSG.deptWSCBReportAllHoursForDepartmentByClassAndInstructorNote());
	}

	@Override
	protected float weeklyClassHours(PitClass pitClass) {
		return(pitClass.getAllWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()));
	}

	@Override
	protected float weeklyStudentClassHours(PitClass pitClass) {
		return(pitClass.getAllWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()));
	}

	@Override
	public void createWeeklyStudentContactHoursByDepartmentReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		HashSet<Long> processedClasses = new HashSet<Long>();

		for(Long deptId : getDepartmentIds()) {
			Department d = (Department) hibSession.createQuery("from Department d where d.uniqueId = :id").setLong("id", deptId).setCacheable(true).uniqueResult();
			for (Long pioUid : findAllPitInstructionalOfferingUniqueIdsForDepartment(pointInTimeData, deptId, hibSession)) {
				for(PitClass pc : findAllPitClassesForPitInstructionalOfferingId(pointInTimeData, pioUid, hibSession)) {
					if (processedClasses.contains(pc.getUniqueId())){
						continue;
					}
					processedClasses.add(pc.getUniqueId());
					if (pc.getPitClassInstructors() != null && !pc.getPitClassInstructors().isEmpty()){
						for (PitClassInstructor pci : pc.getPitClassInstructors()){
							addClassInstructorRow(d, pc, pci, hibSession);
						}
					} else {
						addClassInstructorRow(d, pc, null, hibSession);
					}
				}
			}
		}
	}
	
	private void addClassInstructorRow(Department department, PitClass pitClass, PitClassInstructor pitClassInstructor, Session hibSession) {
		float normalizedRatio = 1.0f;
		if (pitClassInstructor != null) {
			normalizedRatio = pitClassInstructor.getNormalizedPercentShare().intValue() / 100.0f;
		}
		ArrayList<String> row = new ArrayList<String>();
		row.add(department.getDeptCode());
		row.add(department.getAbbreviation());
		row.add(department.getName());
		row.add((pitClassInstructor == null || pitClassInstructor.getPitDepartmentalInstructor() == null) ? MSG.labelUnknown() : pitClassInstructor.getPitDepartmentalInstructor().getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
		row.add((pitClassInstructor == null || pitClassInstructor.getPitDepartmentalInstructor() == null)? MSG.labelUnknown() : pitClassInstructor.getPitDepartmentalInstructor().getExternalUniqueId());
		row.add(pitClass.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
		row.add(pitClass.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().getCourseNbr());
		row.add(pitClass.getPitSchedulingSubpart().getItype().getAbbv());
		row.add(pitClass.getPitSchedulingSubpart().getItype().getOrganized().toString());
		row.add(pitClass.getSectionNumber().toString() + (pitClass.getPitSchedulingSubpart().getSchedulingSubpartSuffixCache().equals("-")?"":pitClass.getPitSchedulingSubpart().getSchedulingSubpartSuffixCache()));
		row.add(pitClass.getExternalUniqueId());
		row.add(pitClassInstructor == null ? "100" : pitClassInstructor.getNormalizedPercentShare().toString());
		if (bannerDataExistsForSession()) {
			if (pitClass.getClazz() == null) {
				row.add(MSG.labelUnknown());
			} else {
				BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOffering(pitClass.getClazz(), pitClass.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering(), hibSession);
				if (bs == null) {
					row.add(MSG.labelUnknown());
				} else {
					row.add(bs.bannerCourseCreditStr(pitClass.getClazz()));
				}
			}
		} else {
			row.add(MSG.labelUnknown());
		}
		row.add(Float.toString(!pitClass.isOrganized() ? 0.0f : (pitClass.getAllWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * normalizedRatio)));
		row.add(Float.toString(pitClass.isOrganized() ? 0.0f : (pitClass.getAllWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * normalizedRatio)));
		row.add(Float.toString(pitClass.getAllWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * normalizedRatio));
		row.add(Float.toString(!pitClass.isOrganized() ? 0.0f : (pitClass.getAllWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * normalizedRatio)));
		row.add(Float.toString(pitClass.isOrganized() ? 0.0f : (pitClass.getAllWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * normalizedRatio)));
		row.add(Float.toString(pitClass.getAllWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()) * (normalizedRatio)));
		addDataRow(row);
	
	}

	public ArrayList<Long> getDepartmentIds() {
		return iDepartmentIds;
	}

	public void setDepartmentIds(ArrayList<Object> departmentIds) {
		this.iDepartmentIds = new ArrayList<Long>();
		for(Object o : departmentIds) {
			this.iDepartmentIds.add((Long) o);
		}
	}

	public Long getSessionId() {
		return iSessionId;
	}

	public void setSessionId(Long sessionId) {
		this.iSessionId = sessionId;
	}

}
