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

drop index idx_banner_response_crn_xlst;

alter table banner_campus_override modify banner_campus_code varchar2(20 char);
alter table banner_campus_override modify banner_campus_name varchar2(100 char);
alter table banner_crn_provider modify term_code varchar2(20 char);
alter table banner_cross_list_provider modify cross_list_identifier varchar2(2 char);
alter table banner_response modify term_code varchar2(6 char);
alter table banner_response modify crn varchar2(5 char);
alter table banner_response modify subj_code varchar2(4 char);
alter table banner_response modify crse_numb varchar2(5 char);
alter table banner_response modify seq_numb varchar2(3 char);
alter table banner_response modify xlst_group varchar2(2 char);
alter table banner_response modify external_id varchar2(50 char);
alter table banner_response modify action varchar2(50 char);
alter table banner_response modify type varchar2(50 char);
alter table banner_response modify message varchar2(4000 char);
alter table banner_response modify packet_id varchar2(500 char);
alter table banner_section modify section_index varchar2(10 char);
alter table banner_section modify cross_list_identifier varchar2(10 char);
alter table banner_section modify link_identifier varchar2(10 char);
alter table banner_section modify link_connector varchar2(10 char);
alter table banner_session modify banner_campus varchar2(20 char);
alter table banner_session modify banner_term_code varchar2(20 char);
alter table integrationqueueerror modify errortype varchar2(2 char);
alter table integrationqueueerror modify errortext varchar2(255 char);
alter table integrationqueuein modify status varchar2(10 char);
alter table integrationqueueout modify status varchar2(10 char);

create index idx_banner_response_crn_xlst on banner_response (term_code||crn||xlst_group);

/*
 * Update database version
 */

update application_config set value='7' where name='tmtbl.db.banner.version';

commit;
