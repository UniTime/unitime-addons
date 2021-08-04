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

alter table banner_response add session_id decimal(20, 0);
alter table banner_response add subject_area_id decimal(20, 0);
alter table banner_response add banner_section_id decimal(20, 0);
alter table banner_response add campus varchar(5);
alter table banner_response add constraint fk_ban_rsp_to_sess foreign key (session_id)
			references sessions (uniqueid) on delete set null;
alter table banner_response add constraint fk_ban_rsp_to_sa foreign key (subject_area_id)
			references subject_area (uniqueid) on delete set null;
alter table banner_response add constraint fk_ban_rsp_to_bs foreign key (banner_section_id)
			references banner_section (uniqueid) on delete set null;

update application_config set value='14' where name='tmtbl.db.banner.version';
  
commit;