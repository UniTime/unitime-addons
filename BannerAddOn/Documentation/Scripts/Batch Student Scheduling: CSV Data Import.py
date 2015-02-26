# Licensed to The Apereo Foundation under one or more contributor license
# agreements. See the NOTICE file distributed with this work for
# additional information regarding copyright ownership.
#
# The Apereo Foundation licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.
"""
Name: Batch Student Scheduling: Data Import
Description:
    <h3>Import student course requests from the given CSV file</h3>
    <b>Format:</b><ul>
    <li>There is a line for each student
    <li>PUID, Name, PIN, Course1, Course2, .... 
    <li>PIN column is optional, make sure PIN Included toggle is set accordingly
    </ul>
    <b>Parameters:</b><ul>
    <li>Input File: CSV file in the above format
    <li>PIN Included: set to true if the input file contains PIN column
    <li>Status: student status to be set (if selected)
    <li>Trim Leading Zeros: trim leading zeros from the PUID
    <li>Clear Existing Demands: replace existing student course demands with the given demands
    <li>Create Limit Overrides: create limit override reservations for the imported students
    </ul>
Engine: python
Permission: Script Edit
Parameters:
    clear
        Clear Existing Demands
        boolean
        false
    override
        Create Limit Overrides
        boolean
        false
    file
        Input File (CSV)
        file
    pinIncluded
        PIN Included
        boolean
        false
    statusRef
        Status
        reference(StudentSectioningStatus)
    trimLeadingZeros
        Trim Leading Zeros
        boolean
        false
"""
import csv
from org.unitime.timetable.spring import SpringApplicationContextHolder
from org.unitime.timetable.model import UserData, StudentSectioningStatus, CourseDemand, CourseRequest, OverrideReservation, StudentSectioningQueue
from org.unitime.timetable.onlinesectioning.updates import ReloadStudent
from java.util import HashSet, Date
from java.lang import Long

def isOverrideNeeded(offering):
    for config in offering.getInstrOfferingConfigs():
        canEnroll = True
        for subpart in config.getSchedulingSubparts():
            hasEnabledClazz = False
            for clazz in subpart.getClasses():
                if clazz.isEnabledForStudentScheduling():
                    hasEnabledClazz = True
                    break
            if not hasEnabledClazz:
                canEnroll = False
                break
        if canEnroll: return False
    return True

def getStudent(puid, name, clear):
    student = hibSession.createQuery("from Student where session = :session and externalUniqueId = :puid"
        ).setLong("session", session.getUniqueId()).setString("puid", puid).uniqueResult()
    if not student:
        log.warn("Student %s (%s) does not exist." % (name, puid))
        return None
    log.info("%s %s" % (student.getExternalUniqueId(), student.getName("last-first-middle")))
    if clear:
        offerings = HashSet()
        for cd in student.getCourseDemands():
            if cd.getFreeTime():
                hibSession.delete(cd.getFreeTime())
            for cr in cd.getCourseRequests():
                offerings.add(cr.getCourseOffering().getInstructionalOffering())
                hibSession.delete(cr)
            hibSession.delete(cd)
        student.getCourseDemands().clear()
        for enrl in student.getClassEnrollments():
            hibSession.delete(enrl)
        student.getClassEnrollments().clear()
        if override:
            for offering in offerings:
                reservation = hibSession.createQuery(
                    "from OverrideReservation r where r.instructionalOffering = :offeringId and r.type = 1 and r.configurations is empty and r.classes is empty"
                    ).setLong("offeringId", offering.getUniqueId()).setMaxResults(1).uniqueResult()
                if reservation:
                    reservation.getStudents().remove(student)
                    if reservation.getStudents().isEmpty(): hibSession.delete(reservation)
                    else: hibSession.saveOrUpdate(reservation)
    return student
    

def setPin(student, pin):
    UserData.setProperty(student.getExternalUniqueId(), "PIN[%d]" % session.getUniqueId(), pin)

def addCourse(student, cn, ts, offeringIds):
    course = hibSession.createQuery("from CourseOffering where subjectArea.session = :session and subjectAreaAbbv || ' ' || courseNbr = :name"
        ).setLong("session", session.getUniqueId()).setString("name", cn).uniqueResult()
    if not course:
        log.warn("Course %s does not exist." % cn)
        return
    log.debug(course.getCourseNameWithTitle())
    priority = 0
    for cd in student.getCourseDemands():
        if priority <= cd.getPriority() and not cd.isAlternative(): priority = cd.getPriority() + 1
        for cr in cd.getCourseRequests():
            if course == cr.getCourseOffering():
                log.warn("-- course is already requested")
                return False
    cd = CourseDemand()
    cd.setCourseRequests(HashSet())
    cd.setStudent(student)
    student.getCourseDemands().add(cd)
    cd.setAlternative(False)
    cd.setPriority(priority)
    cd.setWaitlist(False)
    cd.setTimestamp(ts)
    cr = CourseRequest()
    cd.getCourseRequests().add(cr)
    cr.setCourseDemand(cd)
    cr.setAllowOverlap(False)
    cr.setCredit(0)
    cr.setOrder(0)
    cr.setCourseOffering(course)
    if override and isOverrideNeeded(course.getInstructionalOffering()):
        log.debug("-- creating override for %s" % course.getCourseName())
        reservation = hibSession.createQuery(
            "from OverrideReservation r where r.instructionalOffering = :offeringId and r.type = 1 and r.configurations is empty and r.classes is empty"
            ).setLong("offeringId", course.getInstructionalOffering().getUniqueId()).setMaxResults(1).uniqueResult()
        if not reservation:
            reservation = OverrideReservation()
            reservation.setType(1)
            reservation.setInstructionalOffering(course.getInstructionalOffering())
            reservation.setClasses(HashSet())
            reservation.setConfigurations(HashSet())
            reservation.setStudents(HashSet())
            offeringIds.add(Long(course.getInstructionalOffering().getUniqueId()))
        reservation.getStudents().add(student)
        hibSession.saveOrUpdate(reservation)
    return True

def setStatus(student, status):
    student.setSectioningStatus(status)

def getOnlineServer():
    service = SpringApplicationContextHolder.getBean("solverServerService")
    container = service.getOnlineStudentSchedulingContainer()
    server = container.getSolver(str(session.getUniqueId()))
    return server

def execute():
    lines = csv.reader(file.getString('utf-8').split('\n'), delimiter=",", quotechar='"')
    status = None
    if statusRef:
        status = StudentSectioningStatus.getStatus(statusRef, session.getUniqueId(), hibSession)
        log.info("Using status %s - %s" % (status.getReference(), status.getLabel()))
    ts = Date()
    studentIds = HashSet()
    offeringIds = HashSet()
    for line in lines:
        if not line: continue
        if line[0].lower() == 'puid': continue
        puid = line[0]
        while trimLeadingZeros and puid[0] == '0': puid = puid[1:]
        name = line[1].strip()
        pin = None
        cstart = 2
        if pinIncluded:
            pin = line[2]
            cstart = 3
        student = getStudent(puid, name, clear)
        if not student: continue
        if pin: setPin(student, pin)
        for course in line[cstart:]:
            addCourse(student, course, ts, offeringIds)
        if status:
            setStatus(student, status)
        studentIds.add(Long(student.getUniqueId()))
        hibSession.saveOrUpdate(student)
    
    StudentSectioningQueue.studentChanged(hibSession, None, session.getUniqueId(), studentIds)
    
    if offeringIds.size() > 0:
        StudentSectioningQueue.offeringChanged(hibSession, None, session.getUniqueId(), offeringIds)
    
    offeringIds

if not file:
    log.error("No input file was provided.")
else:
    execute()