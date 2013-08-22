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
 * Author:  Stephanie Schluttenhofer
 */
 
/*
 * Add indexes to the Banner Response table
 */

create index idx_response_term_code on banner_response(term_code);
create index idx_response_crn on banner_response(crn);
create index idx_response_xlst_group on banner_response(xlst_group);
 
/*
 * Update database version
 */

update application_config set value='2' where name='tmtbl.db.banner.version';

commit;
 