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
			
alter table banner_campus_override add first_banner_term varchar(8)
alter table banner_campus_override add last_banner_term varchar(8)
alter table banner_campus_override add used_default_calc decimal(1,0)
alter table banner_campus_override add replace_campus_code decimal(1,0)
alter table banner_campus_override add acad_init_regex varchar(100)
alter table banner_campus_override add mng_dept_code_regex varchar(100)
alter table banner_campus_override add campus_code_regex varchar(100)
alter table banner_campus_override add sequence_order decimal(10,0);


/*
 * Update database version
 */
  
update application_config set value='17' where name='tmtbl.db.banner.version';
  
commit;