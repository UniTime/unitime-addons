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

USE timetable;

DROP FUNCTION IF EXISTS get_section;
delimiter //

CREATE FUNCTION get_section(sesn_id DECIMAL(20,0),
                            subject VARCHAR(10),
                            crs_nbr VARCHAR(10))
    RETURNS VARCHAR(10)
    
  BEGIN
    DECLARE section_nbr VARCHAR(10);
    DECLARE term_code VARCHAR(20);

    set term_code = get_term_code(sesn_id);

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
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim = '' then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
    else substring_index(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim, -1) 
    end) = rtrim(subject)
         AND is_numeric(banner_section.section_index)
         AND rtrim(substr(course_offering.course_nbr,
                          1,
                          5)) = rtrim(substr(crs_nbr,
                                             1,
                                             5))
         AND banner_session.session_id = banner_section.session_id 
         AND banner_session.banner_term_code = rtrim(term_code);
    END;

    IF (section_nbr IS NULL) THEN
      set section_nbr = '000';
    END IF;

    IF (is_numeric(section_nbr)) THEN
      set section_nbr = set_next_999(section_nbr);
    ELSEIF (form_a99(section_nbr)) THEN
      set section_nbr = set_next_a99(section_nbr);
    ELSEIF (form_99a(section_nbr)) THEN
      set section_nbr = set_next_99a(section_nbr);
    ELSE
      set section_nbr = find_section_nbr(sesn_id,
                                      subject,
                                      crs_nbr);
    END IF;

    IF (section_nbr IS NULL) THEN
      set section_nbr = find_section_nbr(sesn_id,
                                      subject,
                                      crs_nbr);
    END IF;

    set section_nbr = check_exists(sesn_id,
                                subject,
                                crs_nbr,
                                section_nbr);

    RETURN section_nbr;

  END //
delimiter ;

DROP FUNCTION IF EXISTS section_exists;
delimiter //

CREATE FUNCTION section_exists(sesn_id DECIMAL(20,0),
                          subject VARCHAR(10),
                          crs_nbr VARCHAR(10),
                          section VARCHAR(10))
    RETURNS BOOLEAN

  BEGIN
    DECLARE sect_exists BOOLEAN;
    DECLARE term_code VARCHAR(20);
    DECLARE rec       DECIMAL(10);

    set sect_exists = FALSE;
    set term_code = get_term_code(sesn_id);

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
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim = '' then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
    else substring_index(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim, -1) 
    end) = rtrim(subject)
       AND rtrim(substr(course_offering.course_nbr,
                        1,
                        5)) = rtrim(substr(crs_nbr,
                                           1,
                                           5))
       AND rtrim(banner_section.section_index) = rtrim(section)
       AND banner_session.session_id = banner_section.session_id 
       AND banner_session.banner_term_code = rtrim(term_code);
       
    IF (rec > 0) THEN
      set sect_exists = TRUE;
    END IF;

    RETURN sect_exists;

  END //
  
delimiter ;

DROP FUNCTION IF EXISTS get_link_identifier;
delimiter //

CREATE FUNCTION get_link_identifier(sesn_id DECIMAL(20,0),
                          subject VARCHAR(10),
                          crs_nbr VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN

    DECLARE link_id VARCHAR(10);
    DECLARE term_code VARCHAR(20);
    DECLARE l_id CURSOR FOR
       SELECT cross_list_identifier
        FROM banner_cross_list_provider
      where cross_list_identifier regexp '^[A-Z][A-Z1-9]$'
        and cross_list_identifier not in (
      SELECT banner_section.link_identifier
        FROM banner_section,
             banner_config,
             banner_course,
             course_offering,
             subject_area,
             banner_session
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND banner_section.link_identifier = banner_cross_list_provider.cross_list_identifier
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(case 
    when banner_session.use_subj_area_prfx_as_campus is null then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 0 then subject_area.subject_area_abbreviation
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim is null then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
    when banner_session.use_subj_area_prfx_as_campus = 1 and banner_session.subj_area_prfx_delim = '' then substring_index(subject_area.subject_area_abbreviation, ' - ', -1) 
   else substring_index(subject_area.subject_area_abbreviation, banner_session.subj_area_prfx_delim, -1) 
    end) = rtrim(subject)
         AND rtrim(substr(course_offering.course_nbr,
                          1,
                          5)) = rtrim(substr(crs_nbr,
                                             1,
                                             5))
       AND banner_session.session_id = banner_section.session_id 
       AND banner_session.banner_term_code = rtrim(term_code))
    order by cross_list_identifier;
 
    set term_code = get_term_code(sesn_id);
    set link_id = NULL;

    OPEN l_id;

    FETCH l_id
      INTO link_id;
    CLOSE l_id;

    RETURN link_id;

  END //	
update application_config set value='12' where name='tmtbl.db.banner.version';

commit;