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
 *  Search for existence of duplicate CRNs within a session
 */
SELECT crn,
       COUNT(*)
  FROM timetable.banner_section t
 WHERE session_id = (SELECT s.uniqueid
                       FROM timetable.sessions s
                      WHERE s.academic_initiative = '&initiative' 
                        AND s.academic_year = '&year' 
                        AND s.academic_term = '&term') 
 GROUP BY t.crn
HAVING COUNT(*) > 1;
