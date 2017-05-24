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
/*
 * Author:  Stephanie Schluttenhofer
 */
 
/*
 * Add table to store Banner Campus Overrides
 */
use timetable;

create table banner_campus_override (
				uniqueid decimal(20,0) primary key not null,
				banner_campus_code varchar(20) not null,
				banner_campus_name varchar(100) not null,
				visible decimal(1,0) not null) engine = INNODB;
    
/*
 * Add column to BannerSection to store banner campus override
 */
 
alter table banner_section add banner_campus_override_id decimal(20,0);
alter table banner_section add constraint fk_campus_override foreign key (banner_campus_override_id) references banner_campus_override(uniqueid) on delete set null;
 
 
/*
 * Update database version
 */

update application_config set value='4' where name='tmtbl.db.banner.version';


commit;
