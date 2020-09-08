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

alter table banner_session add banner_term_crn_prop_id number(20);
alter table banner_session add use_subj_area_prfx_as_campus number(1);
alter table banner_session add subj_area_prfx_delim varchar2(5 char);
alter table banner_session add constraint fk_ban_trm_to_crn_prop foreign key (banner_term_crn_prop_id)
			references banner_crn_provider (uniqueid) on delete set null;
update banner_session
set banner_session.banner_term_crn_prop_id = (select banner_crn_provider.uniqueid from banner_crn_provider where banner_crn_provider.term_code = banner_session.banner_term_code);


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
create or replace PACKAGE BODY crn_processor IS

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
            next_crn       := get_min_crn(term_code, crn_min);
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
    v_crn_prov_id  banner_crn_provider.uniqueid%TYPE;
    v_next_crn  banner_crn_provider.last_crn%TYPE;
    v_min_crn   banner_crn_provider.min_crn%TYPE;
    v_max_crn   banner_crn_provider.max_crn%TYPE;
    v_search    banner_crn_provider.search_flag%TYPE;
    v_new_crn_prov_id  banner_crn_provider.uniqueid%TYPE;

  BEGIN

    v_term_code := section_processor.get_term_code(sesn_id);

    BEGIN
      SELECT banner_crn_provider.uniqueid,
             banner_crn_provider.last_crn,
             banner_crn_provider.min_crn,
             banner_crn_provider.max_crn,
             banner_crn_provider.search_flag
        INTO v_crn_prov_id,
	           v_next_crn,
             v_min_crn,
             v_max_crn,
             v_search
       FROM banner_session
       INNER JOIN banner_crn_provider on banner_crn_provider.uniqueid = banner_session.banner_term_crn_prop_id
       WHERE banner_session.session_id = sesn_id;
    EXCEPTION
      WHEN no_data_found THEN
        v_min_crn  := 10000;
        v_max_crn  := 69999;
        v_next_crn := get_max_crn(v_term_code, v_min_crn, v_max_crn);
    END;

    IF (v_search IS NOT NULL AND (v_search = 'Y' or v_search = '1')) THEN
      v_next_crn := get_next_unused_crn(v_term_code, v_next_crn, v_min_crn, v_max_crn);
    ELSE
      v_next_crn := v_next_crn + 1;
    END IF;

    IF (v_next_crn > v_max_crn) THEN
      v_next_crn := get_min_crn(v_term_code, v_min_crn);
      v_next_crn := get_next_unused_crn(v_term_code, v_next_crn, v_min_crn, v_max_crn);
      v_search   := '1';
    END IF;

    IF (v_crn_prov_id IS NULL) THEN
      BEGIN
	      SELECT pref_group_seq.nextval
	      INTO v_new_crn_prov_id
	      FROM dual;
      END;
      INSERT INTO banner_crn_provider
      VALUES
        (v_new_crn_prov_id, v_term_code,
         v_next_crn,
         '1',
         v_min_crn,
         v_max_crn);
      UPDATE banner_session
      SET banner_term_crn_prop_id = v_new_crn_prov_id
      WHERE session_id = sesn_id;
    ELSE
      UPDATE banner_crn_provider
         SET last_crn    = v_next_crn,
             search_flag = v_search
      WHERE uniqueid = v_crn_prov_id;
    END IF;
    COMMIT;

    RETURN v_next_crn;

  END get_crn;

END crn_processor;
/
update application_config set value='11' where name='tmtbl.db.banner.version';

commit;