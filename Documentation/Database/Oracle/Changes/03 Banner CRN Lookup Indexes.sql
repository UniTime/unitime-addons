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
 * Author:  Stephanie Schluttenhofer
 */
 
/*
 * Add indexes to the Banner Section and Banner Session tables
 */

create index idx_banner_section_sess_crn on banner_section(session_id, crn);
create index idx_banner_session_term_code on banner_session(banner_term_code);
 
 
/*
 * Update database version
 */

update application_config set value='3' where name='tmtbl.db.banner.version';

commit;
 