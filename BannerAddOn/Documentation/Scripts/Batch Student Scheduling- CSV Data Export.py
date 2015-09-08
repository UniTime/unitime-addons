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