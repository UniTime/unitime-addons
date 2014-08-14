/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC
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
 * Author:  Stephanie Schluttenhofer
 */
 
/*
 * Populate Class External UniqueIds with CRN
 */

use timetable;

update class_ c
set c.external_uid = substring_index(c.class_suffix, '-', 1)
where c.external_uid is null
  and c.class_suffix like '%-%';  
  
/*
 * Update database version
 */

update application_config set value='5' where name='tmtbl.db.banner.version';


commit;