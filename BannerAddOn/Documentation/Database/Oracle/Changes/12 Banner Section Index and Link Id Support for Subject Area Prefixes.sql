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

create or replace PACKAGE section_processor IS

--Author : JRM
--Created : 5/12/2009 11:25:47 AM
--Purpose : Provide Section Number for Banner sections
--Check for existance of a Banner Section Number
--Provide Link Identifier for Banner sectons


--Public function and procedure declarations
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
create or replace PACKAGE BODY section_processor IS

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

    IF ((length(TRIM(translate(section, '0123456789',
                                        ' ')))) IS NULL) THEN
      RESULT := TRUE;
    END IF;
    RETURN RESULT;
  END is_numeric;


  FUNCTION set_next_999(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    sect banner_section.section_index%TYPE;

  BEGIN

    IF (to_number(section, '099') = 998) THEN
      sect := NULL;
    ELSE
      sect := TRIM(to_char((to_number(section, '099') + 1), '099'));
    END IF;
    RETURN sect;
  END set_next_999;


  FUNCTION form_99a(section IN banner_section.section_index%TYPE)
    RETURN BOOLEAN IS

    RESULT BOOLEAN;
    first_chars VARCHAR2(10);
    last_char INTEGER;

  BEGIN
    RESULT := FALSE;
    first_chars := substr(section, 1, 2);
    last_char := ascii(substr(section, 3, 1));

    IF (last_char >= 65 AND last_char <= 90 AND is_numeric(first_chars)) THEN
      RESULT := TRUE;
    END IF;
    RETURN RESULT;
  END form_99a;


  FUNCTION set_next_99a(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    num_val INTEGER;
    last_char INTEGER;
    sect banner_section.section_index%TYPE;

  BEGIN
    num_val := to_number(substr(section, 1, 2), '99');
    last_char := ascii(substr(section, 3, 1)) + 1;

    IF (last_char > 90) THEN
      last_char := 65;
      num_val := num_val + 1;
    END IF;

    IF (num_val > 99) THEN
      sect := NULL;
    ELSE
      sect := TRIM(to_char(num_val, '09')) || TRIM(chr(last_char));
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
    first_char := ascii(substr(section, 1, 1));
    last_chars := substr(section, 2);

    IF (first_char >= 65 AND first_char <= 90 AND is_numeric(last_chars)) THEN
      RESULT := TRUE;
    END IF;
    RETURN RESULT;
  END form_a99;


  FUNCTION set_next_a99(section IN banner_section.section_index%TYPE)
    RETURN banner_section.section_index%TYPE IS

    num_val INTEGER;
    first_char INTEGER;
    sect banner_section.section_index%TYPE;

  BEGIN
    num_val := to_number(substr(section, 2), '99') + 1;
    first_char := ascii(substr(section, 1, 1));

    IF (num_val > 99) THEN
      num_val := 1;
      first_char := first_char + 1;
    END IF;

    IF (first_char > 90) THEN
      sect := NULL;
    ELSE
      sect := TRIM(chr(first_char)) ||
              TRIM(to_char(num_val, '09'));
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
      section_nbr := TRIM(to_char(section_num, '099'));
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
             subject_area,
             banner_session
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(case 
    when banner_session.use_subj_area_prfx_as_campus is null then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 0 then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, ' - ') + 3) 
    else substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim) + length(banner_session.subj_area_prfx_delim)) 
    end
         ) = rtrim(subject)
         AND (length(TRIM(translate(banner_section.section_index, '0123456789',
                                                                  ' ')))) IS NULL

         AND rtrim(substr(course_offering.course_nbr, 1, 5)) = rtrim(substr(crs_nbr, 1, 5))
         AND banner_session.session_id = banner_section.session_id 
         AND banner_session.banner_term_code = rtrim(term_code);
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
    rec INTEGER;

  BEGIN
    sect_exists := FALSE;
    term_code := get_term_code(sesn_id);
    SELECT COUNT(*)
      INTO rec
      FROM banner_section,
           banner_config,
           banner_course,
           course_offering,
           subject_area,
           banner_session
     WHERE banner_config.uniqueid = banner_section.banner_config_id
       AND banner_course.uniqueid = banner_config.banner_course_id
       AND course_offering.uniqueid = banner_course.course_offering_id
       AND subject_area.uniqueid = course_offering.subject_area_id
       AND rtrim(case 
    when banner_session.use_subj_area_prfx_as_campus is null then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 0 then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, ' - ') + 3) 
    else substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim) + length(banner_session.subj_area_prfx_delim)) 
    end) = rtrim(subject)
       AND rtrim(substr(course_offering.course_nbr, 1, 5)) = rtrim(substr(crs_nbr, 1, 5))
       AND rtrim(banner_section.section_index) = rtrim(section)
       AND banner_session.session_id = banner_section.session_id 
       AND banner_session.banner_term_code = rtrim(term_code);
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
             subject_area,
             banner_session
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(case 
    when banner_session.use_subj_area_prfx_as_campus is null then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 0 then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, ' - ') + 3) 
    else substr(subject_area.subject_area_abbreviation, instr(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim) + length(banner_session.subj_area_prfx_delim)) 
    end) = rtrim(subject)
         AND rtrim(substr(course_offering.course_nbr, 1, 5)) = rtrim(substr(crs_nbr, 1, 5))
         AND banner_session.session_id = banner_section.session_id 
         AND banner_session.banner_term_code = rtrim(term_code)
         AND banner_section.session_id IN (SELECT banner_session.session_id
                                             FROM banner_session
                                            WHERE banner_session.banner_term_code = rtrim(term_code))
                                           MINUS
                                           SELECT cross_list_identifier
                                             FROM banner_cross_list_provider
                                            WHERE ascii(substr(cross_list_identifier, 1, 1)) BETWEEN 48 AND 57
                                               OR ascii(substr(cross_list_identifier, 2, 1)) = 48;

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
ALTER PACKAGE cross_list_processor COMPILE BODY;

update application_config set value='12' where name='tmtbl.db.banner.version';

commit;