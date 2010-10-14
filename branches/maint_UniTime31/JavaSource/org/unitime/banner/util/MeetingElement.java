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

package org.unitime.banner.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.TimeLocation.IntEnumeration;

import org.dom4j.Element;
import org.unitime.banner.dataexchange.BannerMessage;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;


/**
 * @author says
 *
 */
public class MeetingElement implements Comparable<MeetingElement> {

	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private Date startDate;
	private Date endDate;
	private String beginTime;
	private String endTime;
	private String bldgCode;
	private String roomCode;
	private String hoursToArrange;
	private String meetingId;
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	

	/**
	 * 
	 */
	public MeetingElement() {
		super();
		monday = false;
		tuesday = false;
		wednesday = false;
		thursday = false;
		friday = false;
		saturday = false;
		sunday = false;
	}
	
	public MeetingElement(Date startDate, Date endDate, String beginTime, String endTime, IntEnumeration days,
						String bldgCode, String roomCode, String hoursToArrange, BannerSection bannerSection, Class_ clazz) {
		this.monday = false;
		this.tuesday = false;
		this.wednesday = false;
		this.thursday = false;
		this.friday = false;
		this.saturday = false;
		this.sunday = false;
		this.startDate = startDate;
		this.endDate = endDate;
		this.beginTime = beginTime;
		this.endTime = endTime;
		setDaysOfWeek(days);
		this.bldgCode = bldgCode;
		this.roomCode = roomCode;
		this.hoursToArrange = hoursToArrange;
		this.meetingId = bannerSection.getUniqueId().toString() + "-" + clazz.getUniqueId().toString();
	}
	
    private static String getStartTimeForAssignment(Assignment a) {
    	int min = a.getStartSlot().intValue() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int h = min/60;
        int m = min%60;
        return (h<10?"0":"")+h+(m<10?"0":"")+m;
    }
 
    private static String getEndTimeForAssignment(Assignment a) { 	
    	int min = (a.getStartSlot().intValue() + a.getSlotPerMtg()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - a.getBreakTime();
        int m = min % 60;
        int h = min / 60;
        return (h<10?"0":"")+h+(m<10?"0":"")+m;
    }


	@SuppressWarnings({ "unchecked", "unchecked" })
	public static Vector<MeetingElement> createMeetingElementsFor(BannerSection bannerSection, Class_ clazz, org.hibernate.Session hibSession, BannerMessage bannerMessage){
		Vector<MeetingElement> elements = new Vector<MeetingElement>();
		
		Assignment a = clazz.getCommittedAssignment();
		if (a != null){
			TreeMap<Date, Date> dates = bannerMessage.findDatesFor(a.getDatePattern());
			String beginTime = getStartTimeForAssignment(a);
			String endTime = getEndTimeForAssignment(a);
			String bldgAbbv = null;
			String roomNbr = null;
			int roomCount = 0;
			for(Iterator locationIt = a.getRooms().iterator(); locationIt.hasNext();){
				Location loc = (Location) locationIt.next();
				if (loc instanceof NonUniversityLocation) {
					NonUniversityLocation nonUnivLoc = (NonUniversityLocation) loc;
					bldgAbbv = "OFFCMP";
					roomNbr = nonUnivLoc.getName();
				}
				if (loc instanceof Room) {
					Room room = (Room) loc;
					bldgAbbv = room.getBuildingAbbv();
					roomNbr = room.getRoomNumber();
				}
				roomCount++;
				for(Iterator<Date> dateIt = dates.keySet().iterator(); dateIt.hasNext();){
					Date startDate = dateIt.next();
					Date endDate = dates.get(startDate);				
					MeetingElement me = new MeetingElement(startDate, endDate, beginTime, endTime, a.getTimeLocation().getDays(), bldgAbbv, roomNbr, null, bannerSection, clazz);
					elements.add(me);
				}
			}
			if (roomCount == 0) {
				for(Iterator<Date> dateIt = dates.keySet().iterator(); dateIt.hasNext();){
					Date startDate = dateIt.next();
					Date endDate = dates.get(startDate);				
					MeetingElement me = new MeetingElement(startDate, endDate, beginTime, endTime, a.getTimeLocation().getDays(), bldgAbbv, roomNbr, null, bannerSection, clazz);
					elements.add(me);
				}
			}
		} else {
			if (clazz.getEffectiveTimePreferences().isEmpty()){
				double hours = clazz.getSchedulingSubpart().getMinutesPerWk()/50;
				DatePattern dp = clazz.getDatePattern();
				TreeMap<Date, Date> dates = null;
				if (dp == null){
					dp = clazz.getSchedulingSubpart().getDatePattern();
				}
				
				if (dp != null) {
					dates = bannerMessage.findDatesFor(dp);
				} else {
					dates = bannerMessage.findDatesFor(bannerMessage.findDefaultDatePatternFor(bannerSection.getSession()));
				}
				boolean createdMeetingElement = false;				
				
				for(Iterator rmPrefIt = clazz.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
					RoomPref rp = (RoomPref) rmPrefIt.next();
					if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
						createdMeetingElement = true;
						String bldgAbbv = null;
						String roomNbr = null;
						if (rp.getRoom() instanceof NonUniversityLocation) {
							NonUniversityLocation nonUnivLoc = (NonUniversityLocation) rp.getRoom();
							bldgAbbv = "OFFCMP";
							roomNbr = nonUnivLoc.getName();			
						}
						if (rp.getRoom() instanceof Room) {
							Room room = (Room) rp.getRoom();
							bldgAbbv = room.getBuildingAbbv();
							roomNbr = room.getRoomNumber();								
						}
						for(Iterator<Date> dateIt = dates.keySet().iterator(); dateIt.hasNext();){
							Date startDate = dateIt.next();
							Date endDate = dates.get(startDate);				
							MeetingElement me = new MeetingElement(startDate, endDate, null, null, null, bldgAbbv, roomNbr, new Double(hours).toString(), bannerSection, clazz);
							elements.add(me);
						}
					}
				}
				if (!createdMeetingElement){
					for(Iterator<Date> dateIt = dates.keySet().iterator(); dateIt.hasNext();){
						Date startDate = dateIt.next();
						Date endDate = dates.get(startDate);				
						MeetingElement me = new MeetingElement(startDate, endDate, null, null, null, null, null, new Double(hours).toString(), bannerSection, clazz);
						elements.add(me);
					}
				}
			}
		}
		return(elements);
	}
	
	public boolean canBeMerged(MeetingElement me){
		if (me.getStartDate() != null && getStartDate() != null && me.getStartDate().getTime() == getStartDate().getTime()){
			if (me.getEndDate() != null && getEndDate() != null && me.getEndDate().getTime() == getEndDate().getTime()){
				if(me.getBeginTime() != null && getBeginTime() != null && me.getBeginTime().equals(getBeginTime())){
					if (me.getEndTime() != null && getEndTime() != null && me.getEndTime().equals(getEndTime())){
						if (me.getBldgCode() != null && getBldgCode() != null && me.getBldgCode().equals(getBldgCode())){
							if (me.getRoomCode() != null && getRoomCode() != null && me.getRoomCode().equals(getRoomCode())){
								return(true);
							} else {
								return(false);
							}
						} else {
							return(false);
						}
					} else {
						return(false);
					}
				} else {
					return(false);
				}
			} else {
				return(false);
			}
		} else {
			return(false);
		}
	}
	
	public void merge(MeetingElement me){
		if (me.isMonday()){
			setMonday(true);
		}
		if (me.isTuesday()){
			setTuesday(true);
		}
		if (me.isWednesday()){
			setWednesday(true);
		}
		if (me.isThursday()){
			setThursday(true);
		}
		if (me.isFriday()){
			setFriday(true);
		}
		if (me.isSaturday()){
			setSaturday(true);
		}
		if (me.isSunday()){
			setSunday(true);
		}
	}

	private void setDaysOfWeek(IntEnumeration e){
		if (e == null){
			return;
		}
		while (e.hasMoreElements()){
			int day = (int)e.nextInt();
			if (day == Constants.DAY_MON){
				monday = true;
			} else if (day == Constants.DAY_TUE){
				tuesday = true;
			} else if (day == Constants.DAY_WED){
				wednesday = true;
			} else if (day == Constants.DAY_THU){
				thursday = true;
			} else if (day == Constants.DAY_FRI){
				friday = true;
			} else if (day == Constants.DAY_SAT){
				saturday = true;
			} else if (day == Constants.DAY_SUN){
				sunday = true;
			}
		}	
	}
	
	public static TreeMap<Date, Date> datePatternDates(DatePattern datePattern){
		TreeMap<Date, Date> tm = new TreeMap<Date, Date>();
		Calendar aCalendarDate = Calendar.getInstance();
		aCalendarDate.setTime(datePattern.getSession().getSessionBeginDateTime());
		int offset = DateUtils.getDayOfYear(aCalendarDate.get(Calendar.DAY_OF_MONTH), aCalendarDate.get(Calendar.MONTH), datePattern.getSession().getSessionStartYear())- DateUtils.getDayOfYear(1, datePattern.getSession().getStartMonth(), datePattern.getSession().getSessionStartYear());
		int sessionOffset = offset + (datePattern.getOffset().intValue() * -1);
		aCalendarDate.add(Calendar.DAY_OF_MONTH, (datePattern.getOffset().intValue() * -1));
		java.util.Date meetingStartDate = aCalendarDate.getTime();
		java.util.Date meetingEndDate;
		if (!datePattern.getPattern().contains("0")){
			//If the date pattern does not have any days off return dates that cover the full period.
			aCalendarDate.add(Calendar.DAY_OF_MONTH, (datePattern.getPattern().length() - 1));
			meetingEndDate = aCalendarDate.getTime();
			tm.put(meetingStartDate, meetingEndDate);
			return(tm);
		}
		boolean haveMeetingBeginDate = true;
		int notMeetingIndex = 0;
		int previousNotMeetingIndex = 0;
		int i = 0;
		while (i < datePattern.getPattern().length()){
			if (datePattern.getPattern().charAt(i) == '0'){
				previousNotMeetingIndex++;
				aCalendarDate.add(Calendar.DAY_OF_MONTH, 1);
			} else {
				break;
			}
			i++;
		}
		meetingStartDate = aCalendarDate.getTime();
		while (notMeetingIndex != -1){
			notMeetingIndex = datePattern.getPattern().indexOf("0", previousNotMeetingIndex + 1);
			if (haveMeetingBeginDate){
				if (notMeetingIndex == -1){
					aCalendarDate.add(Calendar.DAY_OF_MONTH, ((datePattern.getPattern().length() - 1) - previousNotMeetingIndex));
					meetingEndDate = aCalendarDate.getTime();
					tm.put(meetingStartDate, meetingEndDate);
					haveMeetingBeginDate = false;
				} else if ((sessionOffset+notMeetingIndex) > 0 && datePattern.getSession().getHolidays().charAt(sessionOffset+notMeetingIndex) == '0'){
						int j = 1;
						while (datePattern.getSession().getHolidays().charAt(sessionOffset+notMeetingIndex - j) != '0' && (datePattern.getPattern().charAt(notMeetingIndex - j) == '0')){
							j++;
						}
						aCalendarDate.add(Calendar.DAY_OF_MONTH, (notMeetingIndex - previousNotMeetingIndex - j));
						meetingEndDate = aCalendarDate.getTime();
						tm.put(meetingStartDate, meetingEndDate);
						haveMeetingBeginDate = false;
						aCalendarDate.add(Calendar.DAY_OF_MONTH, j);
				} else {
					aCalendarDate.add(Calendar.DAY_OF_MONTH, (notMeetingIndex - previousNotMeetingIndex));
				}
			} else {
				if (notMeetingIndex != -1){
					if (datePattern.getPattern().toCharArray()[notMeetingIndex + 1] != '0'){
						aCalendarDate.add(Calendar.DAY_OF_MONTH, (notMeetingIndex - previousNotMeetingIndex +1));
						meetingStartDate = aCalendarDate.getTime();
						haveMeetingBeginDate = true;
						notMeetingIndex++;
					} else {
						aCalendarDate.add(Calendar.DAY_OF_MONTH, (notMeetingIndex - previousNotMeetingIndex));
					}
				}
			}
			previousNotMeetingIndex = notMeetingIndex;
		}
		return(tm);
	}
	
	public void addMeetingElements(Element sectionElement){
		
		Element meetingElement = sectionElement.addElement("MEETING");
		meetingElement.addAttribute("MEETING_ID", meetingId);
		
		if (monday){
			meetingElement.addAttribute("MONDAY", "M");
		} else {
			meetingElement.addAttribute("MONDAY", "");			
		}
		if (tuesday){
			meetingElement.addAttribute("TUESDAY", "T");
		} else {
			meetingElement.addAttribute("TUESDAY", "");			
		}
		if (wednesday){
			meetingElement.addAttribute("WEDNESDAY", "W");
		} else {
			meetingElement.addAttribute("WEDNESDAY", "");
		}
		if (thursday){
			meetingElement.addAttribute("THURSDAY", "R");
		} else {
			meetingElement.addAttribute("THURSDAY", "");			
		}
		if (friday){
			meetingElement.addAttribute("FRIDAY", "F");
		} else {
			meetingElement.addAttribute("FRIDAY", "");			
		}
		if (saturday){
			meetingElement.addAttribute("SATURDAY", "S");
		} else {
			meetingElement.addAttribute("SATURDAY", "");
		}
		if (sunday){
			meetingElement.addAttribute("SUNDAY", "U");
		} else {
			meetingElement.addAttribute("SUNDAY", "");
		}
		if (startDate != null){
			meetingElement.addAttribute("START_DATE", sDateFormat.format(startDate));
		} else {
			meetingElement.addAttribute("START_DATE", "");			
		}
		if (endDate != null){
			meetingElement.addAttribute("END_DATE", sDateFormat.format(endDate));
		} else {
			meetingElement.addAttribute("END_DATE", "");
		}
		if (beginTime != null){
			meetingElement.addAttribute("BEGIN_TIME", beginTime);
		} else {
			meetingElement.addAttribute("BEGIN_TIME", "");			
		}
		if (endTime != null){
			meetingElement.addAttribute("END_TIME", endTime);
		} else {
			meetingElement.addAttribute("END_TIME", "");			
		}
		if (bldgCode != null && bldgCode.trim().length() > 0 && roomCode != null && roomCode.trim().length() > 0){
			meetingElement.addAttribute("BLDG_CODE", bldgCode.trim());
			meetingElement.addAttribute("ROOM_CODE", roomCode.trim());
		} else {
			meetingElement.addAttribute("BLDG_CODE", "");
			meetingElement.addAttribute("ROOM_CODE", "");			
		}
		if (hoursToArrange != null && hoursToArrange.trim().length() > 0){
			meetingElement.addAttribute("ARRANGE_HOURS_WEEK", hoursToArrange);
		} else {
			meetingElement.addAttribute("ARRANGE_HOURS_WEEK", "");			
		}
	}


	public boolean isMonday() {
		return monday;
	}


	public void setMonday(boolean monday) {
		this.monday = monday;
	}


	public boolean isTuesday() {
		return tuesday;
	}


	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}


	public boolean isWednesday() {
		return wednesday;
	}


	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}


	public boolean isThursday() {
		return thursday;
	}


	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}


	public boolean isFriday() {
		return friday;
	}


	public void setFriday(boolean friday) {
		this.friday = friday;
	}


	public boolean isSaturday() {
		return saturday;
	}


	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}


	public boolean isSunday() {
		return sunday;
	}


	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}


	public Date getStartDate() {
		return startDate;
	}


	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}


	public Date getEndDate() {
		return endDate;
	}


	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


	public String getBeginTime() {
		return beginTime;
	}


	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}


	public String getEndTime() {
		return endTime;
	}


	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}


	public String getBldgCode() {
		return bldgCode;
	}


	public void setBldgCode(String bldgCode) {
		this.bldgCode = bldgCode;
	}


	public String getRoomCode() {
		return roomCode;
	}


	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}


	public String getHoursToArrange() {
		return hoursToArrange;
	}


	public void setHoursToArrange(String hoursToArrange) {
		this.hoursToArrange = hoursToArrange;
	}


	public int compareTo(MeetingElement me) {
		if (me.getStartDate() != null && getStartDate() != null && me.getStartDate().getTime() == getStartDate().getTime()){
			if (me.getEndDate() != null && getEndDate() != null && me.getEndDate().getTime() == getEndDate().getTime()){
				if(me.getBeginTime() != null && getBeginTime() != null && me.getBeginTime().equals(getBeginTime())){
					if (me.getEndTime() != null && getEndTime() != null && me.getEndTime().equals(getEndTime())){
						if (me.getBldgCode() != null && getBldgCode() != null && me.getBldgCode().equals(getBldgCode())){
							if (me.getRoomCode() != null && getRoomCode() != null && me.getRoomCode().equals(getRoomCode())){
								if(me.isMonday() == true && isMonday() == false){
									return(1);
								} else if (me.isMonday() == false && isMonday() == true){
									return(-1);
								} else if (me.isTuesday() == true && isTuesday() == false){
									return(1);
								} else if (me.isTuesday() == false && isTuesday() == true){
									return(-1);
								} else if (me.isWednesday() == true && isWednesday() == false){
									return(1);
								} else if (me.isWednesday() == false && isWednesday() == true){
									return(-1);
								} else if (me.isThursday() == true && isThursday() == false){
									return(1);
								} else if (me.isThursday() == false && isThursday() == true){
									return(-1);
								} else if (me.isFriday() == true && isFriday() == false){
									return(1);
								} else if (me.isFriday() == false && isFriday() == true){
									return(-1);
								} else if (me.isSaturday() == true && isSaturday() == false){
									return(1);
								} else if (me.isSaturday() == false && isSaturday() == true){
									return(-1);
								} else if (me.isSunday() == true && isSunday() == false){
									return(1);
								} else if (me.isSunday() == false && isSunday() == true){
									return(-1);
								} else {
									if (me.getHoursToArrange() == null && getHoursToArrange() != null){
										return(1);
									} else if (me.getHoursToArrange() != null && getHoursToArrange() == null){
										return(-1);
									} else if (me.getHoursToArrange() == null && getHoursToArrange() == null){
										return(0);
									} else {
										return(new Float(getHoursToArrange()).compareTo(new Float(me.getHoursToArrange())));
									}
								}
							} else {
								if (me.getRoomCode() == null && getRoomCode() == null){
									return(0);
								} else if(me.getRoomCode() != null && getRoomCode() == null){
									return(-1);
								} else if (me.getRoomCode() == null && getRoomCode() != null){
									return(1);
								} else {
									return(getRoomCode().compareTo(me.getRoomCode()));
								}
							}
						} else {
							if (me.getBldgCode() == null && getBldgCode() == null){
								return(0);
							} else if(me.getBldgCode() != null && getBldgCode() == null){
								return(-1);
							} else if (me.getBldgCode() == null && getBldgCode() != null){
								return(1);
							} else {
								return(getBldgCode().compareTo(me.getBldgCode()));
							}
						}
					} else {
						if (me.getEndTime() == null && getEndTime() == null){
							return(0);
						} else if(me.getEndTime() != null && getEndTime() == null){
							return(-1);
						} else if (me.getEndTime() == null && getEndTime() != null){
							return(1);
						} else {
							return(getEndTime().compareTo(me.getEndTime()));
						}
					}
				} else {
					if (me.getBeginTime() == null && getBeginTime() == null){
						return(0);
					} else if(me.getBeginTime() != null && getBeginTime() == null){
						return(-1);
					} else if (me.getBeginTime() == null && getBeginTime() != null){
						return(1);
					} else {
						return(getBeginTime().compareTo(me.getBeginTime()));
					}
				}
			} else {
				if (me.getEndDate() == null && getEndDate() == null){
					return(0);
				} else if(me.getEndDate() != null && getEndDate() == null){
					return(-1);
				} else if (me.getEndDate() == null && getEndDate() != null){
					return(1);
				} else {
					return(getEndDate().compareTo(me.getEndDate()));
				}
			}
		} else {
			if (me.getStartDate() == null && getStartDate() == null){
				return(0);
			} else if(me.getStartDate() != null && getStartDate() == null){
				return(-1);
			} else if (me.getStartDate() == null && getStartDate() != null){
				return(1);
			} else {
				return(getStartDate().compareTo(me.getStartDate()));
			}
		}
	}

	public String getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

}
