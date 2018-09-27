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
 *   also some stored procedures are based on code submitted by James Marshall
 */

-- Create table
create table banner_session
(
  uniqueid               number(20) not null,
  session_id             number(20) not null,
  banner_campus          varchar2(20) not null,
  banner_term_code       varchar2(20) not null,
  store_data_for_banner  number(1) not null,
  send_data_to_banner    number(1) not null,
  loading_offerings_file number(1) not null
);
alter table banner_session
  add constraint pk_banner_session_uid primary key (uniqueid);
alter table banner_session
  add constraint fk_banner_session_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;


-- Create table
create table banner_course
(
  uniqueid            number(20) not null,
  course_offering_id  number(20) not null,
  gradable_itype_id   number(2),
  uid_rolled_fwd_from number(20)
);
alter table banner_course
  add constraint pk_bannner_course_id primary key (uniqueid);
alter table banner_course
  add constraint fk_bc_itype foreign key (gradable_itype_id)
  references itype_desc (itype) on delete set null;


-- Create table
create table banner_config
(
  uniqueid             number(20) not null,
  banner_course_id     number(20) not null,
  instr_offr_config_id number(20) not null,
  gradable_itype_id    number(2),
  uid_rolled_fwd_from  number(20)
);
alter table banner_config
  add constraint pk_banner_config_id primary key (uniqueid);
alter table banner_config
  add constraint fk_banner_cfg_banner_crs foreign key (banner_course_id)
  references banner_course (uniqueid);
alter table banner_config
  add constraint fk_bcfg_itype foreign key (gradable_itype_id)
  references itype_desc (itype) on delete set null;

-- Create table
create table banner_section
(
  uniqueid                 number(20) not null,
  crn                      number(10),
  section_index            varchar2(10),
  banner_config_id         number(20),
  cross_list_identifier    varchar2(10),
  link_identifier          varchar2(10),
  link_connector           varchar2(10),
  uid_rolled_fwd_from      number(20),
  consent_type_id          number(20),
  session_id               number(20),
  parent_banner_section_id number(20),
  override_limit           number(4),
  override_course_credit   float
);
alter table banner_section
  add constraint pk_banner_section_id primary key (uniqueid);
alter table banner_section
  add constraint fk_banner_sec_banner_cfg foreign key (banner_config_id)
  references banner_config (uniqueid) on delete set null;
alter table banner_section
  add constraint fk_banner_sec_consent_type foreign key (consent_type_id)
  references offr_consent_type (uniqueid) on delete set null;
alter table banner_section
  add constraint fk_banner_sec_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;
alter table banner_section
  add constraint fk_banner_sec_to_parent_sec foreign key (parent_banner_section_id)
  references banner_section (uniqueid) on delete set null;


-- Create table
create table banner_section_join_class
(
  uniqueid          number(20) not null,
  banner_section_id number(20) not null,
  class_id          number(20) not null
);
alter table banner_section_join_class
  add constraint pk_bsc_uniqueid primary key (uniqueid);
alter table banner_section_join_class
  add constraint uk_banner_section_class unique (banner_section_id, class_id);
alter table banner_section_join_class
  add constraint fk_bsc_banner_section foreign key (banner_section_id)
  references banner_section (uniqueid);

-- Create table
create table banner_response
(
  uniqueid      number(20) not null,
  seqno         number not null,
  activity_date date not null,
  term_code     varchar2(6) not null,
  crn           varchar2(5),
  subj_code     varchar2(4),
  crse_numb     varchar2(5),
  seq_numb      varchar2(3),
  xlst_group    varchar2(2),
  external_id   varchar2(50),
  action        varchar2(50),
  type          varchar2(50),
  message       varchar2(4000) not null,
  packet_id     varchar2(500) not null,
  queue_id      number(20)
);
alter table banner_response
  add constraint pk_banner_message primary key (uniqueid);
create index banner_message_idx1 on banner_response (term_code, subj_code, crse_numb);
create index idx_banner_response_crn_xlst on banner_response (term_code||crn||xlst_group);

-- Create table
create table integrationqueueout
(
  uniqueid    number not null,
  xml         clob not null,
  status      varchar2(10) not null,
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
  status      varchar2(10),
  postdate    timestamp(6) not null,
  processdate timestamp(6)
);
alter table integrationqueuein
  add constraint pk_integrationqueuein primary key (uniqueid);

-- Create table
create table integrationqueueerror
(
  queueid   number not null,
  errortype varchar2(2) not null,
  errordate timestamp(6) not null,
  errortext varchar2(255)
);
alter table integrationqueueerror
  add constraint pk_integrationqueueerror primary key (queueid, errortype, errordate);

-- Create sequence 
create sequence banner_message_seq
minvalue 1
maxvalue 99999999999999999999
start with 1
increment by 1;


-- Create sequence 
create sequence banner_response_seq
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
create table banner_cross_list_provider
(
  cross_list_identifier varchar2(2) not null
);

-- Create table
create table banner_crn_provider
(
  uniqueid    number(20) not null,
  term_code   varchar2(20) not null,
  last_crn    number(10) not null,
  search_flag char(1) not null,
  min_crn number(10) not null,
  max_crn number(10) not null
);
alter table banner_crn_provider
  add constraint pk_banner_crn_prov primary key (uniqueid);

  
CREATE OR REPLACE PACKAGE section_processor IS

  -- Author  : JRM
  -- Created : 5/12/2009 11:25:47 AM
  -- Purpose : Provide Section Number for Banner sections
  --           Check for existance of a Banner Section Number
  --           Provide Link Identifier for Banner sectons

  -- Public function and procedure declarations
  FUNCTION get_term_code(sesn_id IN banner_session.session_id%TYPE)
    RETURN banner_session.banner_term_code%TYPE;

  FUNCTION get_section(sesn_id IN banner_section.session_id%TYPE,
                       subject IN subject_area.subject_area_abbreviation%TYPE,
                       crs_nbr IN course_offering.course_nbr%TYPE)
    RETURN banner_section.section_index%TYPE;

  FUNCTION section_exists(sesn_id IN banner_section.session_id%TYPE,
                          subject IN subject_area.subject_area_abbreviation%TYPE,
                          crs_nbr IN course_offering.course_nbr%TYPE,
                          section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN;

  FUNCTION get_link_identifier(sesn_id IN banner_section.session_id%TYPE,
                               subject IN subject_area.subject_area_abbreviation%TYPE,
                               crs_nbr IN course_offering.course_nbr%TYPE)
    RETURN banner_section.link_identifier%TYPE;

END section_processor;
/
CREATE OR REPLACE PACKAGE BODY section_processor IS

  FUNCTION get_term_code(sesn_id IN banner_session.session_id%TYPE)
    RETURN banner_session.banner_term_code%TYPE IS

    term_code banner_session.banner_term_code%TYPE;

  BEGIN

    BEGIN
      SELECT banner_term_code
        INTO term_code
        FROM banner_session
       WHERE banner_session.session_id = sesn_id;
    EXCEPTION
      WHEN no_data_found THEN
        raise_application_error(-20999,
                                'Unknown session id (' || sesn_id || ')');
    END;

    RETURN term_code;

  END get_term_code;

  FUNCTION is_numeric(section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN IS

    RESULT BOOLEAN;

  BEGIN

    RESULT := FALSE;

    IF ((length(TRIM(translate(section,
                               '0123456789',
                               ' ')))) IS NULL) THEN
      RESULT := TRUE;
    END IF;

    RETURN RESULT;

  END is_numeric;

  FUNCTION set_next_999(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    sect banner_section.section_index%TYPE;

  BEGIN

    IF (to_number(section,
                  '099') = 998) THEN
      sect := NULL;
    ELSE
      sect := TRIM(to_char((to_number(section,
                                      '099') + 1),
                           '099'));
    END IF;

    RETURN sect;

  END set_next_999;

  FUNCTION form_99a(section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN IS

    RESULT BOOLEAN;

    first_chars VARCHAR2(10);
    last_char   INTEGER;

  BEGIN

    RESULT := FALSE;

    first_chars := substr(section,
                          1,
                          2);
    last_char   := ascii(substr(section,
                                3,
                                1));

    IF (last_char >= 65 AND last_char <= 90 AND is_numeric(first_chars)) THEN
      RESULT := TRUE;
    END IF;

    RETURN RESULT;

  END form_99a;

  FUNCTION set_next_99a(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    num_val   INTEGER;
    last_char INTEGER;

    sect banner_section.section_index%TYPE;

  BEGIN

    num_val   := to_number(substr(section,
                                  1,
                                  2),
                           '99');
    last_char := ascii(substr(section,
                              3,
                              1)) + 1;

    IF (last_char > 90) THEN
      last_char := 65;
      num_val   := num_val + 1;
    END IF;

    IF (num_val > 99) THEN
      sect := NULL;
    ELSE
      sect := TRIM(to_char(num_val,
                           '09')) || TRIM(chr(last_char));
    END IF;

    RETURN sect;

  END set_next_99a;

  FUNCTION form_a99(section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN IS

    RESULT BOOLEAN;

    first_char INTEGER;
    last_chars VARCHAR2(10);

  BEGIN

    RESULT := FALSE;

    first_char := ascii(substr(section,
                               1,
                               1));
    last_chars := substr(section,
                         2);

    IF (first_char >= 65 AND first_char <= 90 AND is_numeric(last_chars)) THEN
      RESULT := TRUE;
    END IF;

    RETURN RESULT;

  END form_a99;

  FUNCTION set_next_a99(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    num_val    INTEGER;
    first_char INTEGER;

    sect banner_section.section_index%TYPE;

  BEGIN

    num_val    := to_number(substr(section,
                                   2),
                            '99') + 1;
    first_char := ascii(substr(section,
                               1,
                               1));

    IF (num_val > 99) THEN
      num_val    := 1;
      first_char := first_char + 1;
    END IF;

    IF (first_char > 90) THEN
      sect := NULL;
    ELSE
      sect := TRIM(chr(first_char)) ||
              TRIM(to_char(num_val,
                           '09'));
    END IF;

    RETURN sect;

  END set_next_a99;

  FUNCTION find_section_nbr(sesn_id IN banner_section.session_id%TYPE,
                            subject IN subject_area.subject_area_abbreviation%TYPE,
                            crs_nbr IN course_offering.course_nbr%TYPE)
    RETURN banner_section.section_index%TYPE IS

    section_nbr banner_section.section_index%TYPE;
    section_num INTEGER;

  BEGIN

    section_num := 1;

    LOOP
      section_nbr := TRIM(to_char(section_num,
                                  '099'));
      IF (NOT section_exists(sesn_id,
                             subject,
                             crs_nbr,
                             section_nbr)) THEN
        EXIT;
      END IF;

      section_num := section_num + 1;
      IF (section_num > 999) THEN
        raise_application_error(-20999,
                                'No section number is available (' ||
                                rtrim(subject) || rtrim(crs_nbr) || ')');
      END IF;

    END LOOP;

    RETURN section_nbr;

  END find_section_nbr;

  FUNCTION check_exists(sesn_id IN banner_section.session_id%TYPE,
                        subject IN subject_area.subject_area_abbreviation%TYPE,
                        crs_nbr IN course_offering.course_nbr%TYPE,
                        section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    section_nbr banner_section.section_index%TYPE;

  BEGIN

    section_nbr := section;

    WHILE (section_exists(sesn_id,
                          subject,
                          crs_nbr,
                          section_nbr)) LOOP
      IF (is_numeric(section_nbr)) THEN
        section_nbr := set_next_999(section_nbr);
      ELSIF (form_a99(section_nbr)) THEN
        section_nbr := set_next_a99(section_nbr);
      ELSIF (form_99a(section_nbr)) THEN
        section_nbr := set_next_99a(section_nbr);
      ELSE
        section_nbr := find_section_nbr(sesn_id,
                                        subject,
                                        crs_nbr);
      END IF;

      IF (section_nbr IS NULL) THEN
        section_nbr := find_section_nbr(sesn_id,
                                        subject,
                                        crs_nbr);
      END IF;

    END LOOP;

    RETURN section_nbr;

  END check_exists;

  FUNCTION get_section(sesn_id IN banner_section.session_id%TYPE,
                       subject IN subject_area.subject_area_abbreviation%TYPE,
                       crs_nbr IN course_offering.course_nbr%TYPE)
    RETURN banner_section.section_index%TYPE IS

    section_nbr banner_section.section_index%TYPE;

    term_code banner_session.banner_term_code%TYPE;

  BEGIN

    term_code := get_term_code(sesn_id);

    BEGIN
      SELECT MAX(banner_section.section_index)
        INTO section_nbr
        FROM banner_section,
             banner_config,
             banner_course,
             course_offering,
             subject_area
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(subject_area.subject_area_abbreviation) = rtrim(subject)
         AND (length(TRIM(translate(banner_section.section_index,
                               '0123456789',
                               ' ')))) IS NULL
         AND rtrim(substr(course_offering.course_nbr,
                          1,
                          5)) = rtrim(substr(crs_nbr,
                                             1,
                                             5))
         AND banner_section.session_id IN
             (SELECT banner_session.session_id
                FROM banner_session
               WHERE banner_session.banner_term_code = rtrim(term_code));
    EXCEPTION
      WHEN no_data_found THEN
        section_nbr := '000';
    END;

    IF (section_nbr IS NULL) THEN
      section_nbr := '000';
    END IF;

    IF (is_numeric(section_nbr)) THEN
      section_nbr := set_next_999(section_nbr);
    ELSIF (form_a99(section_nbr)) THEN
      section_nbr := set_next_a99(section_nbr);
    ELSIF (form_99a(section_nbr)) THEN
      section_nbr := set_next_99a(section_nbr);
    ELSE
      section_nbr := find_section_nbr(sesn_id,
                                      subject,
                                      crs_nbr);
    END IF;

    IF (section_nbr IS NULL) THEN
      section_nbr := find_section_nbr(sesn_id,
                                      subject,
                                      crs_nbr);
    END IF;

    section_nbr := check_exists(sesn_id,
                                subject,
                                crs_nbr,
                                section_nbr);

    RETURN section_nbr;

  END get_section;

  FUNCTION section_exists(sesn_id IN banner_section.session_id%TYPE,
                          subject IN subject_area.subject_area_abbreviation%TYPE,
                          crs_nbr IN course_offering.course_nbr%TYPE,
                          section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN IS

    sect_exists BOOLEAN;

    term_code banner_session.banner_term_code%TYPE;
    rec       INTEGER;

  BEGIN

    sect_exists := FALSE;

    term_code := get_term_code(sesn_id);

    SELECT COUNT(*)
      INTO rec
      FROM banner_section,
           banner_config,
           banner_course,
           course_offering,
           subject_area
     WHERE banner_config.uniqueid = banner_section.banner_config_id
       AND banner_course.uniqueid = banner_config.banner_course_id
       AND course_offering.uniqueid = banner_course.course_offering_id
       AND subject_area.uniqueid = course_offering.subject_area_id
       AND rtrim(subject_area.subject_area_abbreviation) = rtrim(subject)
       AND rtrim(substr(course_offering.course_nbr,
                        1,
                        5)) = rtrim(substr(crs_nbr,
                                           1,
                                           5))
       AND rtrim(banner_section.section_index) = rtrim(section)
       AND banner_section.session_id IN
           (SELECT banner_session.session_id
              FROM banner_session
             WHERE rtrim(banner_session.banner_term_code) = rtrim(term_code));

    IF (rec > 0) THEN
      sect_exists := TRUE;
    END IF;

    RETURN sect_exists;

  END section_exists;

  FUNCTION get_link_identifier(sesn_id IN banner_section.session_id%TYPE,
                               subject IN subject_area.subject_area_abbreviation%TYPE,
                               crs_nbr IN course_offering.course_nbr%TYPE)
    RETURN banner_section.link_identifier%TYPE IS

    link_id banner_section.link_identifier%TYPE;

    term_code banner_session.banner_term_code%TYPE;

    CURSOR l_id IS
      SELECT cross_list_identifier
        FROM banner_cross_list_provider
      MINUS
      SELECT banner_section.link_identifier
        FROM banner_section,
             banner_config,
             banner_course,
             course_offering,
             subject_area
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(subject_area.subject_area_abbreviation) = rtrim(subject)
         AND rtrim(substr(course_offering.course_nbr,
                          1,
                          5)) = rtrim(substr(crs_nbr,
                                             1,
                                             5))
         AND banner_section.session_id IN
             (SELECT banner_session.session_id
                FROM banner_session
               WHERE banner_session.banner_term_code = rtrim(term_code))
      MINUS
      SELECT cross_list_identifier
        FROM banner_cross_list_provider
       WHERE ascii(substr(cross_list_identifier,
                          1,
                          1)) BETWEEN 48 AND 57
          OR ascii(substr(cross_list_identifier,
                          2,
                          1)) = 48;

  BEGIN

    term_code := get_term_code(sesn_id);

    link_id := NULL;

    OPEN l_id;

    FETCH l_id
      INTO link_id;

    IF (link_id IS NULL) THEN
      raise_application_error(-20999,
                              'No unused link identifier is available (' ||
                              rtrim(subject) || rtrim(crs_nbr) || ')');
    END IF;

    CLOSE l_id;

    RETURN link_id;

  END get_link_identifier;

END section_processor;
/
  
CREATE OR REPLACE PACKAGE crn_processor IS

  -- Author  : JRM
  -- Created : 4/22/2009 11:19:43 AM
  -- Purpose : Provide CRN for Banner sections

  -- banner_crn_provider table
  --   create table banner_crn_provider
  --   (
  --  		term_code   varchar2(20) not null,
  --		last_crn    number(10) not null,
  --		search_flag char(1) not null,
  --		min_crn number(10) not null,
  --		max_crn number(10) not null
  --   );

  -- Public function and procedure declarations
  FUNCTION get_crn(sesn_id IN banner_section.session_id%TYPE)
    RETURN banner_section.crn%TYPE;

END crn_processor;


 
/
CREATE OR REPLACE PACKAGE BODY crn_processor IS

  FUNCTION get_max_crn(term_code IN banner_session.banner_term_code%TYPE,
                       crn_min   IN banner_crn_provider.min_crn%TYPE,
                       crn_max   IN banner_crn_provider.max_crn%TYPE)
    RETURN banner_section.crn%TYPE IS

    max_crn banner_section.crn%TYPE;

  BEGIN

    BEGIN
      SELECT MAX(crn)
        INTO max_crn
        FROM banner_section
       WHERE banner_section.session_id IN
             (SELECT session_id
                FROM banner_session
               WHERE banner_term_code = term_code);
    EXCEPTION
      WHEN no_data_found THEN
        max_crn := crn_min;
    END;

    IF (max_crn > crn_max) THEN
      max_crn := crn_max;
    END IF;

    IF (max_crn < crn_min) THEN
      max_crn := crn_min;
    END IF;

    RETURN max_crn;

  END get_max_crn;

  FUNCTION get_min_crn(term_code IN banner_session.banner_term_code%TYPE,
                       crn_min   IN banner_crn_provider.min_crn%TYPE)
    RETURN banner_section.crn%TYPE IS

    min_crn banner_section.crn%TYPE;

  BEGIN

    BEGIN
      SELECT MIN(crn)
        INTO min_crn
        FROM banner_section
       WHERE banner_section.session_id IN
             (SELECT session_id
                FROM banner_session
               WHERE banner_term_code = term_code);
    EXCEPTION
      WHEN no_data_found THEN
        min_crn := crn_min;
    END;

    IF (min_crn < crn_min) THEN
      min_crn := crn_min;
    END IF;

    RETURN min_crn;

  END get_min_crn;

  FUNCTION get_next_unused_crn(term_code IN banner_session.banner_term_code%TYPE,
                               start_crn IN banner_section.crn%TYPE,
                               crn_min   IN banner_crn_provider.min_crn%TYPE,
                               crn_max   IN banner_crn_provider.max_crn%TYPE)
    RETURN banner_section.crn%TYPE IS

    TYPE crn_type IS TABLE OF banner_section.crn%TYPE;
    crn_table crn_type;

    next_crn banner_section.crn%TYPE;

    restart_search BOOLEAN;
    answer         BOOLEAN;

  BEGIN
    next_crn       := start_crn + 1;
    restart_search := FALSE;

    SELECT banner_section.crn BULK COLLECT
      INTO crn_table
      FROM banner_section
     WHERE banner_section.session_id IN
           (SELECT session_id
              FROM banner_session
             WHERE banner_term_code = term_code);

    LOOP
      answer := next_crn NOT MEMBER OF crn_table;
      IF (answer IS NULL OR answer) THEN
        EXIT;
      ELSE
        next_crn := next_crn + 1;

        IF (next_crn > crn_max) THEN
          IF (restart_search) THEN
            raise_application_error(-20999,
                                    'No unused CRN is available (' ||
                                    term_code || ')');
          ELSE
            next_crn       := get_min_crn(term_code,
                                          crn_min);
            restart_search := TRUE;
          END IF;
        END IF;
      END IF;
    END LOOP;

    RETURN next_crn;

  END get_next_unused_crn;

  FUNCTION get_crn(sesn_id IN banner_section.session_id%TYPE)
    RETURN banner_section.crn%TYPE IS

    v_term_code banner_session.banner_term_code%TYPE;
    v_next_crn  banner_crn_provider.last_crn%TYPE;
    v_min_crn   banner_crn_provider.min_crn%TYPE;
    v_max_crn   banner_crn_provider.max_crn%TYPE;
    v_search    banner_crn_provider.search_flag%TYPE;

  BEGIN

    v_term_code := section_processor.get_term_code(sesn_id);

    BEGIN
      SELECT banner_crn_provider.last_crn,
             banner_crn_provider.min_crn,
             banner_crn_provider.max_crn,
             banner_crn_provider.search_flag
        INTO v_next_crn,
             v_min_crn,
             v_max_crn,
             v_search
        FROM banner_crn_provider
       WHERE banner_crn_provider.term_code = v_term_code;
    EXCEPTION
      WHEN no_data_found THEN
        v_min_crn  := 10000;
        v_max_crn  := 69999;
        v_next_crn := get_max_crn(v_term_code,
                                v_min_crn,
                                v_max_crn);
    END;

    IF (v_search IS NOT NULL AND (v_search = 'Y' or v_search = '1')) THEN
      v_next_crn := get_next_unused_crn(v_term_code,
                                      v_next_crn,
                                      v_min_crn,
                                      v_max_crn);
    ELSE
      v_next_crn := v_next_crn + 1;
    END IF;

    IF (v_next_crn > v_max_crn) THEN
      v_next_crn := get_min_crn(v_term_code,
                              v_min_crn);
      v_next_crn := get_next_unused_crn(v_term_code,
                                      v_next_crn,
                                      v_min_crn,
                                      v_max_crn);
      v_search   := '1';
    END IF;

    IF (v_search IS NULL) THEN
      INSERT INTO banner_crn_provider
      VALUES
        (pref_group_seq.nextval, v_term_code,
         v_next_crn,
         '0',
         v_min_crn,
         v_max_crn);
    ELSE
      UPDATE banner_crn_provider
         SET last_crn    = v_next_crn,
             search_flag = v_search
      WHERE term_code = v_term_code;
    END IF;
    COMMIT;

    RETURN v_next_crn;

  END get_crn;

END crn_processor;
/
  
CREATE OR REPLACE PACKAGE cross_list_processor IS

  -- Author  : JRM
  -- Created : 5/13/2009 11:08:06 AM
  -- Purpose : Provide Cross List identifier for Banner sections

  -- Public function and procedure declarations
  PROCEDURE init_ban_xlist_prov_table;

  FUNCTION get_cross_list_id(sesn_id IN banner_section.session_id%TYPE)
    RETURN banner_section.cross_list_identifier%TYPE;

END cross_list_processor;
/

CREATE OR REPLACE PACKAGE BODY cross_list_processor IS

  PROCEDURE init_ban_xlist_prov_table IS
  
    char1 VARCHAR2(35) := '123456789ABCDEFGHIJKLMNOPQRSTUVW';
    char2 VARCHAR2(36) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  
    idnt VARCHAR2(2);
  
  BEGIN
    EXECUTE IMMEDIATE 'DELETE FROM banner_cross_list_provider';
    COMMIT;
  
    FOR i IN 1 .. length(char1) LOOP
      FOR j IN 1 .. length(char2) LOOP
        idnt := TRIM(substr(char1,
                            i,
                            1) || substr(char2,
                                         j,
                                         1));
        INSERT INTO banner_cross_list_provider
        VALUES
          (idnt);
      END LOOP;
    END LOOP;
  
    COMMIT;
  
  END init_ban_xlist_prov_table;

  FUNCTION get_cross_list_id(sesn_id IN banner_section.session_id%TYPE)
    RETURN banner_section.cross_list_identifier%TYPE IS
  
    cross_list_id banner_section.cross_list_identifier%TYPE;
  
    term_code banner_session.banner_term_code%TYPE;
  
    CURSOR c_cl IS
      SELECT banner_cross_list_provider.cross_list_identifier
        FROM banner_cross_list_provider
      MINUS
      SELECT banner_section.cross_list_identifier
        FROM banner_section
       WHERE banner_section.session_id IN
             (SELECT session_id
                FROM banner_session
               WHERE banner_term_code = term_code);
  
  BEGIN
  
    term_code := section_processor.get_term_code(sesn_id);
  
    OPEN c_cl;
  
    FETCH c_cl
      INTO cross_list_id;
  
    IF (NOT c_cl%FOUND) THEN
      raise_application_error(-20999,
                              'No unused cross list identifier is available (' ||
                              term_code || ')');
    END IF;
  
    CLOSE c_cl;
  
    RETURN cross_list_id;
  
  END get_cross_list_id;

END cross_list_processor;
/
call cross_list_processor.init_ban_xlist_prov_table();

/

/* 
 *make sure all course number are at least 5 characters 
*/

update course_offering co
set co.course_nbr = rpad(co.course_nbr, 5, '0')
where length(co.course_nbr) < 5;
 /
  
  
/*
 * Update database version
 */

insert into application_config (name,value,description)
	values('tmtbl.db.banner.version','1','Timetabling Banner Add On DB version (do not change -- this is used by automatic database update)'); 


commit;
  