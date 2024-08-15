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

create index IDX_BANNER_CONFIG_BANNER_COURSE on BANNER_CONFIG(BANNER_COURSE_ID);
create index IDX_BANNER_CONFIG_GRADABLE_ITYPE on BANNER_CONFIG(GRADABLE_ITYPE_ID);
create index IDX_BANNER_COURSE_GRADABLE_ITYPE on BANNER_COURSE(GRADABLE_ITYPE_ID);
create index IDX_BANNER_INST_METHOD_COHORT_RSTRCT_COHORT on BANNER_INST_METHOD_COHORT_RSTRCT(COHORT_ID);
create index IDX_BANNER_INST_METHOD_COHORT_RSTRCT_INSTR_METHOD on BANNER_INST_METHOD_COHORT_RSTRCT(INSTR_METHOD_ID);
create index IDX_BANNER_LAST_SENT_SECT_RESTR_COHORT on BANNER_LAST_SENT_SECT_RESTR(COHORT_ID);
create index IDX_BANNER_RESPONSE_BANNER_SECTION on BANNER_RESPONSE(BANNER_SECTION_ID);
create index IDX_BANNER_RESPONSE_SUBJECT_AREA on BANNER_RESPONSE(SUBJECT_AREA_ID);
create index IDX_BANNER_RESPONSE_SESSION on BANNER_RESPONSE(SESSION_ID);
create index IDX_BANNER_SECTION_BANNER_CONFIG on BANNER_SECTION(BANNER_CONFIG_ID);
create index IDX_BANNER_SECTION_CONSENT_TYPE on BANNER_SECTION(CONSENT_TYPE_ID);
create index IDX_BANNER_SECTION_PARENT_BANNER_SECTION on BANNER_SECTION(PARENT_BANNER_SECTION_ID);
create index IDX_BANNER_SECTION_BANNER_CAMPUS_OVERRIDE on BANNER_SECTION(BANNER_CAMPUS_OVERRIDE_ID);
create index IDX_BANNER_SESSION_FUTURE on BANNER_SESSION(FUTURE_ID);
create index IDX_BANNER_SESSION_SESSION on BANNER_SESSION(SESSION_ID);
create index IDX_BANNER_SESSION_BANNER_TERM_CRN_PROP on BANNER_SESSION(BANNER_TERM_CRN_PROP_ID);

/*
 * Update database version
 */
  
update application_config set value='16' where name='tmtbl.db.banner.version';

commit;
