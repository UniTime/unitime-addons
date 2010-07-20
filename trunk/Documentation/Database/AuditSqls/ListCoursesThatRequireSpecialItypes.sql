/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
/*
 *  Author:  Stephanie Schluttenhofer
 */
/*
 *  When using UniTime with Banner if the same itype is used it must 
 *    be nested and the classes underneath it must have the same
 *    expected capacity as their parent classes.  If the classes do
 *    not have the same expected capacity as their parent classes they
 *    need to be a different itype.  The purpose of this sql is to help
 *    find instances where a different itype is needed.
 */
SELECT DISTINCT sa.subject_area_abbreviation AS subj,
                co.course_nbr,
                ss.itype
  FROM sessions               s,
       subject_area           sa,
       course_offering        co,
       instructional_offering io,
       instr_offering_config  ioc,
       scheduling_subpart     ss,
       class_                 c,
       scheduling_subpart     ss2,
       class_                 c2
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND sa.session_id = s.uniqueid
   AND co.subject_area_id = sa.uniqueid
   AND co.is_control = 1
   AND io.uniqueid = co.instr_offr_id
   AND ioc.instr_offr_id = io.uniqueid
   AND ss.config_id = ioc.uniqueid
   AND ss2.config_id = ioc.uniqueid
   AND ss.uniqueid != ss2.uniqueid
   AND c.subpart_id = ss.uniqueid
   AND c2.subpart_id = ss2.uniqueid
   AND ss.itype = ss2.itype
   AND (ss.PARENT = ss2.uniqueid OR ss2.PARENT = ss.uniqueid)
   AND c2.parent_class_id = c.uniqueid
   AND c.expected_capacity != c2.expected_capacity
