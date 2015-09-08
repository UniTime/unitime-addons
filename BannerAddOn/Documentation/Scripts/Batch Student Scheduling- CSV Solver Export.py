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
Name: Batch Student Scheduling: Solver Export
Description:
    <h3>Export batch student scheduling solver student class enrollments data into a CSV file</h3>
    <b>Format:</b><ul>
    <li>There is a line for each student
    <li>PUID, Name, PIN, CRN1, CRN2, .... 
    </ul>
    <b>Parameters:</b><ul>
    <li>Add Leading Zeros: add leading zeros to the PUID
    <li>Default PIN: default PIN number to be used in the enrollment
    </ul>
Engine: python
Permission: Script Edit
Parameters:
    zeros
        Add Leading Zeros
        boolean
        false
    defaultPin
        Default PIN
        string
"""
import csv
from org.unitime.timetable.model import UserData
from org.unitime.timetable.model.dao import Class_DAO, CourseOfferingDAO
from org.unitime.timetable.spring import SpringApplicationContextHolder
from org.unitime.timetable.onlinesectioning.match import AnyStudentMatcher
from org.unitime.timetable.onlinesectioning.model import XCourseRequest

def getServer():
    service = SpringApplicationContextHolder.getBean("solverServerService")
    container = service.getStudentSolverContainer()
    server = container.getSolver(log.getOwnerId())
    return server

def getStudents(server):
    m = AnyStudentMatcher()
    m.setServer(server)
    return server.findStudents(m)

def getPin(student):
    pin = UserData.getProperty(student.getExternalId(), "PIN[%d]" % session.getUniqueId())
    if pin: return pin
    return defaultPin

def generateRow(server, student):
    puid = student.getExternalId()
    if zeros: puid = puid.zfill(9)
    name = student.getName()
    log.info("%s %s" % (puid, name))
    line = [puid, name, getPin(student)]
    crns = {}
    for req in server.getStudent(student.getStudentId()).getRequests():
        if not isinstance(req, XCourseRequest): continue
        enrl = req.getEnrollment()
        if not enrl: continue
        course = hibSession.createQuery("from CourseOffering where uniqueId = :id").setLong("id", enrl.getCourseId()).uniqueResult()
        for id in enrl.getSectionIds():
            clazz = hibSession.createQuery("from Class_ where uniqueId = :id").setLong("id", id).uniqueResult()
            crns[clazz.getExternalId(course)] = True
    line.extend(sorted(crns.keys()))
    log.debug('-- %s' % ','.join(sorted(crns.keys())))
    return line

def execute(server):
    file = open(log.createOutput('enrollments','csv').getAbsolutePath(), 'w')
    writer = csv.writer(file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    writer.writerow(['PUID','Name','PIN','CRN','CRN','CRN','CRN','CRN','CRN','CRN','CRN','CRN'])
    for student in getStudents(server):
        writer.writerow(generateRow(server, student))
    writer.writerow(['EOF'])
    file.close()

server = getServer()
if not server:
    log.error("Batch student scheduling solver is not loaded in.")
elif server.isRunning():
    log.error("Batch student scheduling solver is running.")
elif server.isWorking():
    log.error("Batch student scheduling solver is working.")
else:
    execute(server)