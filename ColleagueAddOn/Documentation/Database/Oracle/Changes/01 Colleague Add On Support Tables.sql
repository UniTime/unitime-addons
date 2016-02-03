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

-- Create table
create table colleague_session
(
  uniqueid               number(20) not null,
  session_id             number(20) not null,
  colleague_campus          varchar2(20 char) not null,
  colleague_term_code       varchar2(20 char) not null,
  store_data_for_colleague  number(1) not null,
  send_data_to_colleague    number(1) not null,
  loading_offerings_file number(1) not null
  uid_rolled_fwd_from      number(20),
);
alter table colleague_session
  add constraint pk_colleague_session_uid primary key (uniqueid);
alter table colleague_session
  add constraint uk_session unique (session_id);
alter table colleague_session
  add constraint fk_colleague_session_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;


-- Create table
create table colleague_section
(
  uniqueid                 number(20) not null,
  colleague_id             varchar2(20 char),
  course_offering_id       number(20) not null,
  subject_area_id          number(20) not null,
  colleague_crs_nbr        varchar2(5 char),
  section_index            varchar2(10 char),
  deleted            	   number(1),
  uid_rolled_fwd_from      number(20),
  session_id               number(20),
  parent_colleague_section_id number(20)
);
alter table colleague_section
  add constraint pk_colleague_section_id primary key (uniqueid);
alter table colleague_section
  add constraint fk_colleague_sec_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;
alter table colleague_section
  add constraint fk_coll_sec_to_prnt_sec foreign key (parent_colleague_section_id)
  references colleague_section (uniqueid) on delete set null;


-- Create table
create table colleague_section_join_class
(
  uniqueid          number(20) not null,
  colleague_section_id number(20) not null,
  class_id          number(20) not null
);
alter table colleague_section_join_class
  add constraint pk_csc_uniqueid primary key (uniqueid);
alter table colleague_section_join_class
  add constraint uk_colleague_section_class unique (colleague_section_id, class_id);
alter table colleague_section_join_class
  add constraint fk_csc_colleague_section foreign key (colleague_section_id)
  references colleague_section (uniqueid);

  
  
-- Create table
create table colleague_restriction (
  uniqueid      number(20) not null,
  term_code       varchar2(20 char) not null,
  code varchar2(8 char) not null,
  name varchar2(30 char) not null,
  description varchar2(500 char)
);
alter table colleague_restriction
  add constraint pk_colleague_restriction_uid primary key (uniqueid);
alter table colleague_restriction
  add constraint uk_term_code_code unique (term_code, code);

-- Create table
create table colleague_sect_join_restr (
  colleague_section_id number(20) default null,
  colleague_restriction_id number(20) default null
);
alter table colleague_sect_join_restr
  add constraint uk_sect_join_restr unique (colleague_section_id, colleague_restriction_id);
alter table colleague_sect_join_restr
  add constraint fk_sect_join_restr_restr foreign key (colleague_restriction_id)
  references colleague_restriction (uniqueid) on delete cascade;

  
-- Create table
create table colleague_response
(
  uniqueid      number(20) not null,
  seqno         number not null,
  activity_date date not null,
  term_code     varchar2(20 char) not null,
  colleague_id  varchar2(20 char),
  subj_code     varchar2(4 char),
  crse_numb     varchar2(5 char),
  seq_numb      varchar2(3 char),
  external_id   varchar2(50 char),
  action        varchar2(50 char),
  type          varchar2(50 char),
  message       varchar2(4000 char) not null,
  packet_id     varchar2(500 char) not null,
  queue_id      number(20)
);
alter table colleague_response
  add constraint pk_colleague_message primary key (uniqueid);
create index colleague_message_idx1 on colleague_response (term_code, subj_code, crse_numb);
create index idx_response_term_code on colleague_response(term_code);
create index idx_response_colleague_id on colleague_response(colleague_id);

-- Create table
create table integrationqueueout
(
  uniqueid    number not null,
  xml         clob not null,
  status      varchar2(10 char) not null,
  postdate    timestamp(6) not null,
  pickupdate  timestamp(6),
  processdate timestamp(6)
);
alter table integrationqueueout
  add constraint pk_integrationqueueout primary key (uniqueid);

-- Create table
create table integrationqueuein
(
  uniqueid    number not null,
  xml         clob,
  matchid     number,
  status      varchar2(10 char),
  postdate    timestamp(6) not null,
  processdate timestamp(6)
);
alter table integrationqueuein
  add constraint pk_integrationqueuein primary key (uniqueid);
create index idx_integrationqueuein_status on integrationqueuein(uniqueId, status);

-- Create table
create table integrationqueueerror
(
  queueid   number not null,
  errortype varchar2(2 char) not null,
  errordate timestamp(6) not null,
  errortext varchar2(255 char)
);
alter table integrationqueueerror
  add constraint pk_integrationqueueerror primary key (queueid, errortype, errordate);

-- Create sequence 
create sequence colleague_message_seq
minvalue 1
maxvalue 99999999999999999999
start with 1
increment by 1;


-- Create sequence 
create sequence colleague_response_seq
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1;


-- Create sequence 
create sequence queue_seq
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1;


-- Create table
create table colleague_course_suffix_def
(
  uniqueid    number(20) not null,
  term_code   varchar2(20 char) not null,
  itype_id    number(2),
  course_suffix varchar2(5 char),
  campus_code varchar2(20 char),
  min_section_num number(2) not null,
  max_section_num number(2) not null,
  itype_prefix varchar2(1 char),
  prefix varchar2(1 char),
  suffix varchar2(1 char),
  note varchar2(500 char)
  );
alter table colleague_course_suffix_def
  add constraint pk_colleague_crs_suf_def primary key (uniqueid);
alter table colleague_course_suffix_def
  add constraint uk_term_itype_course_suffix unique (term_code, itype_id, course_suffix);

   
  
/*
 * Update database version
 */

insert into application_config (name,value,description)
	values('tmtbl.db.colleague.version','1','Timetabling Colleague Add On DB version (do not change -- this is used by automatic database update)'); 


commit;
  