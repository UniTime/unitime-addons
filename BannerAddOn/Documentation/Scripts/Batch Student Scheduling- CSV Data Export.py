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
Name: Batch Student Scheduling: Data Export
Description:
    <h3>Export student class enrollments into a CSV file</h3>
    <b>Format:</b><ul>
    <li>There is a line for each student
    <li>PUID, Name, PIN, CRN1, CRN2, .... 
    </ul>
    <b>Parameters:</b><ul>
    <li>Status: students of this status will be exported
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
    status
        Status
        reference(StudentSectioningStatus)
"""
import csv
from org.unitime.timetable.model import UserData

def getPin(student):
    pin = UserData.getProperty(student.getExternalId(), "PIN[%d]" % session.getUniqueId())
    if pin: return pin
    return defaultPin

def getStudent(student):
    puid = student.getExternalUniqueId()
    if zeros: puid = puid.zfill(9)
    log.info("%s %s" % (puid, student.getName("last-first-middle")))
    line = [puid, student.getName("last-first-middle"), getPin(student)]
    crns = {}
    for enrl in student.getClassEnrollments():
        crns[enrl.getClazz().getExternalId(enrl.getCourseOffering())] = True
    line.extend(sorted(crns.keys()))
    log.debug('-- %s' % ','.join(sorted(crns.keys())))
    return line

def execute():
    students = hibSession.createQuery("from Student where session = :session and sectioningStatus.reference = :status"
        ).setLong("session", session.getUniqueId()).setString("status", status).list()
    
    file = open(log.createOutput('%s_enrollments' % status,'csv').getAbsolutePath(), 'w')
    writer = csv.writer(file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    writer.writerow(['PUID','Name','PIN','CRN','CRN','CRN','CRN','CRN','CRN','CRN','CRN','CRN'])
    for student in students:
        writer.writerow(getStudent(student))
    writer.writerow(['EOF'])
    file.close()

if not status:
    log.error("No student status was provided.")
else:
    execute()