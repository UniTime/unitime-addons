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

use timetable;

CREATE TABLE colleague_session (
  uniqueid DECIMAL(20, 0) NOT NULL,
  session_id DECIMAL(20, 0) NOT NULL,
  colleague_campus VARCHAR(20) BINARY NOT NULL,
  colleague_term_code VARCHAR(20) BINARY NOT NULL,
  store_data_for_colleague INT(1) NOT NULL,
  send_data_to_colleague INT(1) NOT NULL,
  loading_offerings_file INT(1) NOT NULL,
  uid_rolled_fwd_from DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  UNIQUE key uk_session (session_id),
  CONSTRAINT fk_colleague_session_session FOREIGN KEY fk_colleague_session_session (session_id)
    REFERENCES sessions (uniqueid)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;


CREATE TABLE colleague_section (
  uniqueid DECIMAL(20, 0) NOT NULL,
  colleague_id VARCHAR(20) BINARY NULL,
  course_offering_id DECIMAL(20, 0) NOT NULL,
  subject_area_id DECIMAL(20, 0) NOT NULL,
  colleague_crs_nbr VARCHAR(5) BINARY NOT NULL,
  section_index VARCHAR(10) BINARY NULL,
  deleted int(1) default null,
  uid_rolled_fwd_from DECIMAL(20, 0) NULL,
  session_id DECIMAL(20, 0) NULL,
  parent_coll_section_id DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_colleague_sec_session FOREIGN KEY fk_colleague_sec_session (session_id)
    REFERENCES sessions (uniqueid)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT fk_coll_sec_to_prnt_sec FOREIGN KEY fk_coll_sec_to_prnt_sec (parent_coll_section_id)
    REFERENCES colleague_section (uniqueid)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;


CREATE TABLE colleague_section_join_class (
  uniqueid DECIMAL(20, 0) NOT NULL,
  colleague_section_id DECIMAL(20, 0) NOT NULL,
  class_id DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_csc_colleague_section FOREIGN KEY fk_csc_collegaue_section (colleague_section_id)
    REFERENCES colleague_section (uniqueid)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

CREATE TABLE colleague_restriction (
  uniqueid DECIMAL(20, 0) NOT NULL,
  term_code VARCHAR(20) BINARY NOT NULL,
  code VARCHAR(8) BINARY NOT NULL,
  name VARCHAR(30) BINARY NOT NULL,
  description VARCHAR(500) BINARY NULL,
  PRIMARY KEY (uniqueid),
  UNIQUE key uk_term_code_code (term_code, code)
)
ENGINE = INNODB;


create table colleague_sect_join_restr (
  colleague_section_id decimal(20,0) default null,
  colleague_restriction_id decimal(20,0) default null,
  UNIQUE key uk_sect_join_restr (colleague_section_id, colleague_restriction_id),
  key fk_sect_join_restr_restr (colleague_restriction_id),
  constraint fk_sect_join_restr_restr foreign key (colleague_restriction_id) references colleague_restriction (uniqueid) on delete cascade
) ENGINE=InnoDB;



CREATE TABLE colleague_response (
  uniqueid DECIMAL(20, 0) NOT NULL,
  seqno DECIMAL(22, 0) NOT NULL,
  activity_date DATETIME NOT NULL,
  term_code VARCHAR(20) BINARY NOT NULL,
  colleague_id VARCHAR(20) BINARY NULL,
  subj_code VARCHAR(10) BINARY NULL,
  crse_numb VARCHAR(5) BINARY NULL,
  sec_numb VARCHAR(3) BINARY NULL,
  external_id VARCHAR(50) BINARY NULL,
  action VARCHAR(50) BINARY NULL,
  type VARCHAR(50) BINARY NULL,
  message VARCHAR(4000) BINARY NOT NULL,
  packet_id VARCHAR(500) BINARY NOT NULL,
  queue_id DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  INDEX colleague_message_idx1 (term_code(6), subj_code(10), crse_numb(5))
)
ENGINE = INNODB;

create index idx_response_term_code on colleague_response(term_code);
create index idx_response_colleag_id on colleague_response(colleague_id);

CREATE TABLE integrationqueueout (
  uniqueid DECIMAL(22, 0) NOT NULL,
  xml LONGTEXT BINARY NOT NULL,
  status VARCHAR(10) BINARY NOT NULL,
  postdate DATETIME NOT NULL,
  pickupdate DATETIME NULL,
  processdate DATETIME NULL,
  PRIMARY KEY (uniqueid)
)
ENGINE = INNODB;

CREATE TABLE integrationqueuein (
  uniqueid DECIMAL(22, 0) NOT NULL,
  xml LONGTEXT BINARY NULL,
  matchid DECIMAL(22, 0) NULL,
  status VARCHAR(10) BINARY NULL,
  postdate DATETIME NOT NULL,
  processdate DATETIME NULL,
  PRIMARY KEY (uniqueid)
)
ENGINE = INNODB;

create index idx_integrationqueuein_status on integrationqueuein(uniqueId, status);


CREATE TABLE integrationqueueerror (
  queueid DECIMAL(22, 0) NOT NULL,
  errortype VARCHAR(2) BINARY NOT NULL,
  errordate DATETIME NOT NULL,
  errortext VARCHAR(255) BINARY NULL,
  PRIMARY KEY (queueid, errortype, errordate)
)
ENGINE = INNODB;


CREATE TABLE colleague_course_suffix_def (
  uniqueid DECIMAL(20, 0) NOT NULL,
  term_code VARCHAR(20) BINARY NOT NULL,
  subject_area_id DECIMAL(20, 0) NULL,
  itype_id INT(2) NULL,
  course_suffix VARCHAR(5) BINARY NULL,
  campus_code VARCHAR(20) BINARY NULL,
  min_section_num INT(2) NOT NULL,
  max_section_num INT(2) NOT NULL,
  itype_prefix VARCHAR(1) BINARY NULL,
  prefix VARCHAR(1) BINARY NULL,
  suffix VARCHAR(1) BINARY NULL,
  note VARCHAR(500) BINARY NULL,
  PRIMARY KEY (uniqueid),
  UNIQUE key uk_term_itype_course_suffix (term_code, itype_id, course_suffix)
)
ENGINE = INNODB;



/*
 * Update database version
 */

insert into application_config (name,value,description)
	values('tmtbl.db.colleague.version','1','Timetabling Colleague Add On DB version (do not change -- this is used by automatic database update)'); 

commit;
