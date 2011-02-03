/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
/*
 *  Author:  Stephanie Schluttenhofer
 */
/*
 *  This provides a list of all course offerings that are crosslisted
 *    i.e. All instances where an instructional offering has multiple
 *        course offerings associated with it.
 */
SELECT sa1.subject_area_abbreviation AS ctrl_subj,
       co1.course_nbr                AS ctrl_crs,
       sa2.subject_area_abbreviation AS subj,
       co2.course_nbr                AS crs
  FROM instructional_offering io,
       course_offering        co1,
       course_offering        co2,
       sessions               s,
       subject_area           sa1,
       subject_area           sa2
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND io.session_id = s.uniqueid
   AND io.not_offered = '0'
   AND co1.instr_offr_id = io.uniqueid
   AND co1.is_control = 1
   AND co2.instr_offr_id = io.uniqueid
   AND co2.is_control = 0
   AND sa1.uniqueid = co1.subject_area_id
   AND sa2.uniqueid = co2.subject_area_id
 ORDER BY sa1.subject_area_abbreviation,
          co1.subject_area_id
