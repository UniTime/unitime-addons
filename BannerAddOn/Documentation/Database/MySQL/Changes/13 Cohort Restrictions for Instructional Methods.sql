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

create table banner_inst_method_cohort_rstrct (
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	instr_method_id decimal(20,0) not null,
	cohort_id decimal(20,0) not null,
	restriction_action varchar(1) not null,
	removed decimal(1,0) not null
);
create unique index uk_bimcr_sess_method_cohort on banner_inst_method_cohort_rstrct(session_id, instr_method_id, cohort_id);

alter table banner_inst_method_cohort_rstrct 
	add constraint fk_bimcr_session_id foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table banner_inst_method_cohort_rstrct 
	add constraint fk_bimcr_instr_method_id foreign key (instr_method_id)
	references instructional_method (uniqueid) on delete cascade;

alter table banner_inst_method_cohort_rstrct 
	add constraint fk_bimcr_cohort_id foreign key (cohort_id)
	references student_group (uniqueid) on delete cascade;
	
create table banner_last_sent_sect_restr (
	uniqueid decimal(20,0) primary key not null,
	restriction_type decimal(10,0) not null,
	banner_section_id decimal(20,0) not null,
	restriction_action varchar(1) not null,
	removed decimal(1,0) not null,
	cohort_id decimal(20,0) not null
);

create unique index uk_blssr_ban_sec_rest_type_cohrt on banner_last_sent_sect_restr(banner_section_id, restriction_type, cohort_id);

alter table banner_last_sent_sect_restr 
	add constraint fk_blssr_banner_sec_id foreign key (banner_section_id)
	references banner_section (uniqueid) on delete cascade;

alter table banner_last_sent_sect_restr 
	add constraint fk_blssr_cohort_id foreign key (cohort_id)
	references student_group (uniqueid) on delete cascade;
	
update application_config set value='13' where name='tmtbl.db.banner.version';

commit;