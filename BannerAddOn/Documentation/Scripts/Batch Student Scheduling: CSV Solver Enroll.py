# UniTime 3.5 (University Timetabling Application)
# Copyright (C) 2015, UniTime LLC
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along
# with this program.  If not, see <http://www.gnu.org/licenses/>.
"""
Name: Batch Student Scheduling: Solver Enroll
Description:
    <h3>Used the batch student scheduling solver solution to enroll students on the online student scheduling server.</h3>
    For each student that is loaded in the solver, the appropriate Enroll action is created and executed on the online student scheduling server.<br>
    <b>Output is returned in the following CSV format:</b><ul>
    <li>There is a line for each student class enrollment
    <li>PUID, Name, PIN, Course, CRN, Result, Error Message
    </ul>
    <b>Parameters:</b><ul>
    <li>Default PIN: default PIN number to be used in the enrollment
    </ul>
Engine: python
Permission: Script Edit
Parameters:
    defaultPin
        Default PIN
        string
"""
import csv
from java.util import ArrayList, HashSet
from java.lang import Long
from org.unitime.timetable.model import UserData
from org.unitime.timetable.spring import SpringApplicationContextHolder
from org.unitime.timetable.onlinesectioning.match import AnyStudentMatcher
from org.unitime.timetable.onlinesectioning.model import XCourseRequest
from org.unitime.timetable.onlinesectioning.basic import GetRequest, GetAssignment
from org.unitime.timetable.onlinesectioning.updates import EnrollStudent
from org.unitime.timetable.onlinesectioning.custom import RequestStudentUpdates
from org.unitime.timetable.onlinesectioning import OnlineSectioningLog
from org.unitime.timetable.gwt.shared import SectioningException

def getBatchServer():
    service = SpringApplicationContextHolder.getBean("solverServerService")
    container = service.getStudentSolverContainer()
    server = container.getSolver(log.getOwnerId())
    return server

def getOnlineServer():
    service = SpringApplicationContextHolder.getBean("solverServerService")
    container = service.getOnlineStudentSchedulingContainer()
    server = container.getSolver(str(session.getUniqueId()))
    return server

def getStudents(server):
    m = AnyStudentMatcher()
    m.setServer(server)
    return server.findStudents(m)

def getPin(student):
    pin = UserData.getProperty(student.getExternalId(), "PIN[%d]" % session.getUniqueId())
    if pin: return pin
    return defaultPin

def getUser(student):
    user = OnlineSectioningLog.Entity.newBuilder()
    user.setExternalId(log.getOwnerId())
    user.setName(log.getOwnerName())
    user.setType(OnlineSectioningLog.Entity.EntityType.MANAGER)
    if student:
        pin = getPin(student)
        if pin: user.addParameterBuilder().setKey("pin").setValue(pin)
    return user.build()

def generateAction(server, student):
    log.info("%s %s" % (student.getExternalId(), student.getName()))
    action = EnrollStudent()
    action.forStudent(student.getStudentId())
    
    request = server.execute(GetRequest().forStudent(student.getStudentId()), getUser(student))
    action.withRequest(request)

    enrollment = server.execute(GetAssignment().forStudent(student.getStudentId()), getUser(student))
    assignments = ArrayList()
    for course in enrollment.getCourseAssignments():
        if course.isFreeTime(): continue
        assignments.addAll(course.getClassAssignments())
    action.withAssignment(assignments)
    
    return action

def failStudent(server, action, student, writer, message):
    for assignment in action.getAssignment():
        course = server.getCourse(assignment.getCourseId())
        offering = server.getOffering(course.getOfferingId())
        section = offering.getSection(assignment.getClassId())
        writer.writerow([
            student.getExternalId(), student.getName(), getPin(student),
            course.getCourseName(), section.getExternalId(assignment.getCourseId()),
            'Failed', message
            ])

def executeAction(server, action, student, writer):
    if not action: return False
    enrollment = None
    try:
        enrollment = server.execute(action, getUser(student))
    except SectioningException, e:
        log.error('Enrollment failed: %s' % e.getMessage())
        failStudent(server, action, student, writer, e.getMessage())
        # writer.writerow([ student.getExternalId(), student.getName(), getPin(student), None, None, 'Failed', e.getMessage() ])
        return False
    for course in enrollment.getCourseAssignments():
        if course.isFreeTime(): continue
        sections = HashSet()
        for clazz in course.getClassAssignments():
            if not sections.add(clazz.getSection()): continue
            line = [ student.getExternalId(), student.getName(), getPin(student), '%s %s' % (course.getSubject(), course.getCourseNbr()), clazz.getSection() ]
            if clazz.isSaved(): line.append('Enrolled')
            else: line.append('Not Enrolled')
            if clazz.hasError():
                line.append(clazz.getError())
            if clazz.hasError():
                log.warn('%s %s %s: %s (%s)' % (course.getSubject(), course.getCourseNbr(), clazz.getSection(), clazz.getError(), line[5]))
            else:
                log.debug('%s %s %s' % (course.getSubject(), course.getCourseNbr(), clazz.getSection()))
            writer.writerow(line)
    return True

def execute(batch, online):
    file = open(log.createOutput('enrollments','csv').getAbsolutePath(), 'w')
    writer = csv.writer(file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    writer.writerow(['PUID','Name','PIN','Course','CRN','Status','Message'])
    failed = ArrayList()
    for student in getStudents(batch):
        if not executeAction(online, generateAction(batch, student), student, writer):
            failed.add(Long(student.getStudentId()))
    writer.writerow(['EOF'])
    file.close()
    if failed.size() > 0:
        req = RequestStudentUpdates()
        req.forStudents(failed)
        online.execute(req, getUser(None))

batch = getBatchServer()
online = getOnlineServer()
if not batch:
    log.error("Batch student scheduling solver is not loaded in.")
elif batch.isRunning():
    log.error("Batch student scheduling solver is running.")
elif batch.isWorking():
    log.error("Batch student scheduling solver is working.")
elif not online:
    log.error("Online student scheduling solver is not running.")
else:
    execute(batch, online)