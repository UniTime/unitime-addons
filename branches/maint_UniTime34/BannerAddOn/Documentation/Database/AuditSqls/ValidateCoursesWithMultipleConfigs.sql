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
 *  The purpose of this sql it to provide a list of offerings that have
 *    multiple instructional offering configurations so a user can
 *    validate the configurations are as expected.
 */
SELECT sa.subject_area_abbreviation,
       co.course_nbr
  FROM sessions               s,
       subject_area           sa,
       course_offering        co,
       instructional_offering io
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND sa.session_id = s.uniqueid
   AND co.subject_area_id = sa.uniqueid
   AND co.is_control = 1
   AND io.uniqueid = co.instr_offr_id
   AND io.not_offered = 0
   AND 1 < (SELECT COUNT(1)
              FROM instr_offering_config ioc
             WHERE ioc.instr_offr_id = io.uniqueid)
 ORDER BY sa.subject_area_abbreviation,
          co.course_nbr
