/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
