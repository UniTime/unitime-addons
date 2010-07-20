/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
/*
 * Author:  Stephanie Schluttenhofer
 */

use timetable;

CREATE TABLE banner_session (
  uniqueid DECIMAL(20, 0) NOT NULL,
  session_id DECIMAL(20, 0) NOT NULL,
  banner_campus VARCHAR(20) BINARY NOT NULL,
  banner_term_code VARCHAR(20) BINARY NOT NULL,
  store_data_for_banner INT(1) NOT NULL,
  send_data_to_banner INT(1) NOT NULL,
  loading_offerings_file INT(1) NOT NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_banner_session_session FOREIGN KEY fk_banner_session_session (session_id)
    REFERENCES sessions (uniqueid)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

CREATE TABLE banner_course (
  uniqueid DECIMAL(20, 0) NOT NULL,
  course_offering_id DECIMAL(20, 0) NOT NULL,
  gradable_itype_id INT(2) NULL,
  uid_rolled_fwd_from DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_bc_itype FOREIGN KEY fk_bc_itype (gradable_itype_id)
    REFERENCES itype_desc (itype)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

CREATE TABLE banner_config (
  uniqueid DECIMAL(20, 0) NOT NULL,
  banner_course_id DECIMAL(20, 0) NOT NULL,
  instr_offr_config_id DECIMAL(20, 0) NOT NULL,
  gradable_itype_id INT(2) NULL,
  uid_rolled_fwd_from DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_banner_cfg_banner_crs FOREIGN KEY fk_banner_cfg_banner_crs (banner_course_id)
    REFERENCES banner_course (uniqueid)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_bcfg_itype FOREIGN KEY fk_bcfg_itype (gradable_itype_id)
    REFERENCES itype_desc (itype)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

CREATE TABLE banner_section (
  uniqueid DECIMAL(20, 0) NOT NULL,
  crn BIGINT(10) NULL,
  section_index VARCHAR(10) BINARY NULL,
  banner_config_id DECIMAL(20, 0) NULL,
  cross_list_identifier VARCHAR(10) BINARY NULL,
  link_identifier VARCHAR(10) BINARY NULL,
  link_connector VARCHAR(10) BINARY NULL,
  uid_rolled_fwd_from DECIMAL(20, 0) NULL,
  consent_type_id DECIMAL(20, 0) NULL,
  session_id DECIMAL(20, 0) NULL,
  parent_banner_section_id DECIMAL(20, 0) NULL,
  override_limit INT(4) NULL,
  override_course_credit DOUBLE NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_banner_sec_banner_cfg FOREIGN KEY fk_banner_sec_banner_cfg (banner_config_id)
    REFERENCES banner_config (uniqueid)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT fk_banner_sec_consent_type FOREIGN KEY fk_banner_sec_consent_type (consent_type_id)
    REFERENCES offr_consent_type (uniqueid)
    ON DELETE SET NULL
    ON UPDATE NO ACTION,
  CONSTRAINT fk_banner_sec_session FOREIGN KEY fk_banner_sec_session (session_id)
    REFERENCES sessions (uniqueid)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT fk_banner_sec_to_parent_sec FOREIGN KEY fk_banner_sec_to_parent_sec (parent_banner_section_id)
    REFERENCES banner_section (uniqueid)
    ON DELETE SET NULL
    ON UPDATE NO ACTION
)
ENGINE = INNODB;
create index idx_bnr_sectn_cross_lst_id on banner_section(cross_list_identifier);
create index idx_bnr_sectn_link_id on banner_section(link_identifier);


CREATE TABLE banner_section_join_class (
  uniqueid DECIMAL(20, 0) NOT NULL,
  banner_section_id DECIMAL(20, 0) NOT NULL,
  class_id DECIMAL(20, 0) NOT NULL,
  PRIMARY KEY (uniqueid),
  CONSTRAINT fk_bsc_banner_section FOREIGN KEY fk_bsc_banner_section (banner_section_id)
    REFERENCES banner_section (uniqueid)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
ENGINE = INNODB;

CREATE TABLE banner_response (
  uniqueid DECIMAL(20, 0) NOT NULL,
  seqno DECIMAL(22, 0) NOT NULL,
  activity_date DATETIME NOT NULL,
  term_code VARCHAR(6) BINARY NOT NULL,
  crn VARCHAR(5) BINARY NULL,
  subj_code VARCHAR(4) BINARY NULL,
  crse_numb VARCHAR(5) BINARY NULL,
  seq_numb VARCHAR(3) BINARY NULL,
  xlst_group VARCHAR(2) BINARY NULL,
  external_id VARCHAR(50) BINARY NULL,
  action VARCHAR(50) BINARY NULL,
  type VARCHAR(50) BINARY NULL,
  message VARCHAR(4000) BINARY NOT NULL,
  packet_id VARCHAR(500) BINARY NOT NULL,
  queue_id DECIMAL(20, 0) NULL,
  PRIMARY KEY (uniqueid),
  INDEX banner_message_idx1 (term_code(6), subj_code(4), crse_numb(5))
)
ENGINE = INNODB;

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

CREATE TABLE integrationqueueerror (
  queueid DECIMAL(22, 0) NOT NULL,
  errortype VARCHAR(2) BINARY NOT NULL,
  errordate DATETIME NOT NULL,
  errortext VARCHAR(255) BINARY NULL,
  PRIMARY KEY (queueid, errortype, errordate)
)
ENGINE = INNODB;

CREATE TABLE banner_cross_list_provider (
  cross_list_identifier VARCHAR(2) BINARY NOT NULL
)
ENGINE = INNODB;

CREATE TABLE banner_crn_provider (
  uniqueid DECIMAL(20, 0) NOT NULL,
  term_code VARCHAR(20) BINARY NOT NULL,
  last_crn BIGINT(10) NOT NULL,
  search_flag CHAR(1) BINARY NOT NULL,
  min_crn BIGINT(10) NOT NULL,
  max_crn BIGINT(10) NOT NULL,
  PRIMARY KEY (uniqueid)
)
ENGINE = INNODB;

DROP FUNCTION IF EXISTS get_term_code;
delimiter //
CREATE FUNCTION get_term_code(sesn_id DECIMAL(20, 0))
    RETURNS VARCHAR(20)

  BEGIN
    declare term_code VARCHAR(20);


    BEGIN
      SELECT banner_session.banner_term_code
        INTO term_code
        FROM banner_session
       WHERE banner_session.session_id = sesn_id;
    END;

    RETURN term_code;

  END //
delimiter ;

DROP FUNCTION IF EXISTS is_numeric;
delimiter //
CREATE FUNCTION is_numeric(section varchar(10))
    RETURNS boolean

  BEGIN
    declare RESULT boolean;
    set RESULT = FALSE;

    IF (select trim(section) regexp '^[0-9][0-9]*$') THEN
      set RESULT = TRUE;
    END IF;

    RETURN RESULT;

  END //
delimiter ;

DROP FUNCTION IF EXISTS set_next_999;
delimiter //

CREATE FUNCTION set_next_999(section VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN
	DECLARE sect VARCHAR(10);
	DECLARE tmp_sect DECIMAL(3);
      IF (cast(section as DECIMAL(3)) = 999) THEN
        set sect = NULL;
      ELSE
        set sect = lpad((cast((cast(section as DECIMAL(3)) + 1) as CHAR(10))), 3, '0');
      END IF;
      
    RETURN sect;

  END //
 delimiter ;
 

DROP FUNCTION IF EXISTS form_99a;
delimiter //

CREATE FUNCTION form_99a(section VARCHAR(10))
    RETURNS BOOLEAN

  BEGIN

    DECLARE RESULT BOOLEAN;

    SET RESULT = FALSE;

    IF (select trim(section) regexp '^[0-9][0-9][A-Z]$') THEN
      set RESULT = TRUE;
    END IF;

    RETURN RESULT;
  END //
  
delimiter ;

DROP FUNCTION IF EXISTS set_next_99a;
delimiter //

CREATE FUNCTION set_next_99a(section VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN

    DECLARE num_val DECIMAL(2,0);
    DECLARE last_char DECIMAL(10,0);
    DECLARE sect VARCHAR(10);

    set num_val = cast(substr(section,
                                  1,
                                  2) as DECIMAL(2));
    set last_char = ascii(substr(section,
                              3,
                              1)) + 1;

    IF (last_char > 90) THEN
      set last_char = 65;
      set num_val = num_val + 1;
    END IF;

    IF (num_val > 99) THEN
      set sect = NULL;
    ELSE
      set sect = CONCAT(TRIM(lpad(cast(num_val as CHAR(2)), 2, '0'
                           )) , TRIM(char(last_char)));
    END IF;

    RETURN sect;

  END //
delimiter ;

DROP FUNCTION IF EXISTS form_a99;
delimiter //

CREATE FUNCTION form_a99(section VARCHAR(10))
    RETURNS BOOLEAN

  BEGIN

   DECLARE RESULT BOOLEAN;

    SET RESULT = FALSE;

    IF (select trim(section) regexp '^[A-Z][0-9][0-9]$') THEN
      set RESULT = TRUE;
    END IF;

    RETURN RESULT;


  END //

delimiter ;

DROP FUNCTION IF EXISTS set_next_a99;
delimiter //

CREATE FUNCTION set_next_a99(section VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN
    DECLARE num_val    DECIMAL(2);
    DECLARE first_char DECIMAL(10);
    DECLARE sect VARCHAR(10);

    set num_val = cast(substr(section,
                                   2) as
                            DECIMAL(2)) + 1;
    set first_char = ascii(substr(section,
                               1,
                               1));

    IF (num_val > 99) THEN
      set num_val = 1;
      set first_char = first_char + 1;
    END IF;

    IF (first_char > 90) THEN
      set sect = NULL;
    ELSE
      set sect = concat(TRIM(char(first_char)) ,
              TRIM(lpad(cast(num_val as char(2)), 2, '0')));
    END IF;

    RETURN sect;

  END //
delimiter ;

DROP FUNCTION IF EXISTS find_section_nbr;
delimiter //

CREATE FUNCTION find_section_nbr(sesn_id DECIMAL(20, 0),
                            subject VARCHAR(10),
                            crs_nbr VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN

    DECLARE section_nbr VARCHAR(10);
    DECLARE section_num DECIMAL(3);

    set section_num = 1;
    set section_nbr = TRIM(lpad(cast(section_num as char(3)), 3, '0'));
    set section_num = section_num + 1;

	WHILE (section_exists(sesn_id, subject, crs_nbr, section_nbr) and section_num <= 999)
     DO
      set section_nbr = TRIM(lpad(cast(section_num as char(3)), 3, '0'));
      set section_num = section_num + 1;

    END WHILE;
    IF (section_exists(sesn_id, subject, crs_nbr, section_nbr) and section_num > 999)
      THEN
      	set section_nbr = null;
    END IF;

    RETURN section_nbr;

  END //
  
delimiter ;

DROP FUNCTION IF EXISTS check_exists;
delimiter //

CREATE FUNCTION check_exists(sesn_id DECIMAL(20,0),
                          subject VARCHAR(10),
                          crs_nbr VARCHAR(10),
                          section VARCHAR(10))
    RETURNS VARCHAR(10)

  BEGIN

    DECLARE section_nbr VARCHAR(10);
    set section_nbr = section;

    WHILE (section_exists(sesn_id,
                          subject,
                          crs_nbr,
                          section_nbr)) DO
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

    END WHILE;

    RETURN section_nbr;

  END //
  delimiter ;

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
             subject_area
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND course_offering.uniqueid = banner_course.course_offering_id
         AND subject_area.uniqueid = course_offering.subject_area_id
         AND rtrim(subject_area.subject_area_abbreviation) = rtrim(subject)
         AND is_numeric(banner_section.section_index)
         AND rtrim(substr(course_offering.course_nbr,
                          1,
                          5)) = rtrim(substr(crs_nbr,
                                             1,
                                             5))
         AND banner_section.session_id IN
             (SELECT banner_session.session_id
                FROM banner_session
               WHERE banner_session.banner_term_code = rtrim(term_code));
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
             subject_area
       WHERE banner_config.uniqueid = banner_section.banner_config_id
         AND banner_course.uniqueid = banner_config.banner_course_id
         AND banner_section.link_identifier = banner_cross_list_provider.cross_list_identifier
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
               WHERE banner_session.banner_term_code = rtrim(term_code)))
    order by cross_list_identifier;
 
 
    set term_code = get_term_code(sesn_id);
    set link_id = NULL;

    OPEN l_id;

    FETCH l_id
      INTO link_id;
    CLOSE l_id;

    RETURN link_id;

  END //
  
delimiter ;
DROP FUNCTION IF EXISTS get_max_crn;
delimiter //
CREATE FUNCTION get_max_crn(term_code VARCHAR(20),
                       crn_min   BIGINT(10),
                       crn_max   BIGINT(10))
    RETURNS BIGINT(10)

  BEGIN
    DECLARE max_crn BIGINT(10);

    BEGIN
      SELECT MAX(crn)
        INTO max_crn
        FROM banner_section
       WHERE banner_section.session_id IN
             (SELECT session_id
                FROM banner_session
               WHERE banner_term_code = term_code);

    END;
    IF (max_crn is null) THEN
       set max_crn = crn_min;
    END IF;
    IF (max_crn > crn_max) THEN
      set max_crn = crn_max;
    END IF;
    IF (max_crn < crn_min) THEN
      set max_crn = crn_min;
    END IF;

    RETURN max_crn;

  END //
delimiter ;

DROP FUNCTION IF EXISTS get_min_crn;
delimiter //
CREATE FUNCTION get_min_crn(term_code VARCHAR(20),
                       crn_min   BIGINT(10))
    RETURNS BIGINT(10)

  BEGIN
    DECLARE min_crn BIGINT(10);

    BEGIN
      SELECT MIN(crn)
        INTO min_crn
        FROM banner_section
       WHERE banner_section.session_id IN
             (SELECT session_id
                FROM banner_session
               WHERE banner_term_code = term_code);
    END;

    IF (min_crn is null) THEN
       SET min_crn = crn_min;
    END IF;
    IF (min_crn < crn_min) THEN
      SET min_crn = crn_min;
    END IF;

    RETURN min_crn;

  END //
delimiter ;

DROP FUNCTION IF EXISTS get_next_unused_crn;
delimiter //
CREATE FUNCTION get_next_unused_crn(term_code VARCHAR(20),
                               start_crn BIGINT(10),
                               crn_min   BIGINT(10),
                               crn_max   BIGINT(10))
    RETURNS BIGINT(10)

  BEGIN
    DECLARE next_crn BIGINT(10);
    DECLARE restart_search BOOLEAN;
    DECLARE answer         BOOLEAN;
    DECLARE tmp_crn BIGINT(10);

    set next_crn       = start_crn + 1;
    set restart_search = FALSE;

    SET answer = false;
    WHILE (NOT answer and next_crn is not null) DO
      SELECT banner_section.crn INTO tmp_crn FROM banner_section WHERE session_id IN (SELECT session_id FROM banner_session WHERE banner_term_code = term_code) and banner_section.crn = next_crn; 
      set answer = tmp_crn is null;
      IF (not answer) THEN
        set tmp_crn = null;
        set next_crn = next_crn + 1;

        IF (next_crn > crn_max) THEN
          IF (restart_search) THEN
            set next_crn = null;
          ELSE
            set next_crn = get_min_crn(term_code, crn_min);
            set restart_search = TRUE;
          END IF;
        END IF;
      END IF;
    END WHILE;

    RETURN next_crn;

  END //
 delimiter ;

DROP PROCEDURE IF EXISTS insert_crn_provider_rec;
delimiter //
CREATE PROCEDURE insert_crn_provider_rec(v_term_code VARCHAR(20),
       v_next_crn  BIGINT(10),
       v_min_crn   BIGINT(10),
       v_max_crn   BIGINT(10)) 

BEGIN
      DECLARE next_id DECIMAL(20,0);
      start transaction;
      select 32767 * next_hi
      into next_id 
      from hibernate_unique_key;
      
      INSERT INTO banner_crn_provider
      VALUES
        (next_id, v_term_code,
         v_next_crn,
         'N',
         v_min_crn,
         v_max_crn);
      update hibernate_unique_key set next_hi = next_hi+1;
      commit;

END //
delimiter ;


DROP PROCEDURE IF EXISTS update_crn_provider_rec;
delimiter //
CREATE PROCEDURE update_crn_provider_rec(v_term_code VARCHAR(20),
       v_next_crn  BIGINT(10),
       v_search    CHAR(1)) 

BEGIN
      start transaction;
      UPDATE banner_crn_provider
         SET last_crn    = v_next_crn,
             search_flag = v_search
      WHERE term_code = v_term_code;
      COMMIT;
END //
delimiter ;

DROP PROCEDURE IF EXISTS get_crn;
delimiter //
CREATE PROCEDURE get_crn(OUT out_crn BIGINT(10), IN sesn_id LONG)

  BEGIN
    DECLARE v_term_code VARCHAR(20);
    DECLARE v_next_crn  BIGINT(10);
    DECLARE v_min_crn   BIGINT(10);
    DECLARE v_max_crn   BIGINT(10);
    DECLARE v_search    CHAR(1);

    SET v_term_code = get_term_code(sesn_id);

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
    END;
    
    IF (v_min_crn IS NULL) THEN
        SET v_min_crn  = 10000;
        SET v_max_crn  = 69999;
        SET v_next_crn = get_max_crn(v_term_code,
                                v_min_crn,
                                v_max_crn);
    END IF;

    IF (v_search IS NOT NULL AND (v_search = 'Y' or v_search = '1')) THEN
      SET v_next_crn = get_next_unused_crn(v_term_code,
                                      v_next_crn,
                                      v_min_crn,
                                      v_max_crn);
    ELSE
      SET v_next_crn = v_next_crn + 1;
    END IF;

    IF (v_next_crn > v_max_crn) THEN
      SET v_next_crn = get_min_crn(v_term_code,
                              v_min_crn);
      SET v_next_crn = get_next_unused_crn(v_term_code,
                                      v_next_crn,
                                      v_min_crn,
                                      v_max_crn);
      SET v_search   = 'Y';
    END IF;

    IF (v_search IS NULL) THEN
	    call insert_crn_provider_rec(v_term_code,
	       v_next_crn,
	       v_min_crn,
	       v_max_crn); 
    ELSE
      call update_crn_provider_rec(v_term_code, v_next_crn, v_search);
    END IF;
    set out_crn = v_next_crn;
  END //
  delimiter ;

DROP PROCEDURE IF EXISTS init_ban_xlist_prov_table;
delimiter //
CREATE PROCEDURE init_ban_xlist_prov_table()
  
  BEGIN
  	declare char1 varchar(35);
    declare char2 varchar(36);
    declare idnt VARCHAR(2);
    declare i int;
    declare j int;

    set char1 = '123456789ABCDEFGHIJKLMNOPQRSTUVW';
    set char2 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    set i = 1;
  
    DELETE FROM banner_cross_list_provider;
    COMMIT;
  
    while i <= length(char1) do
      set j = 1;
      while j <= length(char2) do
        set idnt = concat(substr(char1,
                            i,
                            1), substr(char2,
                                         j,
                                         1));
        INSERT INTO banner_cross_list_provider
        VALUES
          (idnt);
        set j = j + 1;
      END while;
      set i = i + 1;    
    end while;
    COMMIT;
END//
delimiter ;

DROP FUNCTION IF EXISTS get_cross_list_id;
delimiter //
CREATE FUNCTION get_cross_list_id (sesn_id  LONG)
    RETURNS VARCHAR(10)
   
  BEGIN
  
    declare cross_list_id VARCHAR(10);
    declare term_code VARCHAR(20);
  
    declare c_cl CURSOR for
      SELECT banner_cross_list_provider.cross_list_identifier
        FROM banner_cross_list_provider
        WHERE
           0 = (select count(1) 
                from banner_section left join banner_session on banner_section.session_id = banner_session.session_id
                where banner_section.cross_list_identifier = banner_cross_list_provider.cross_list_identifier 
                  and banner_session.banner_term_code = term_code
        )
        limit 1;
    set term_code = get_term_code(sesn_id);
  
    OPEN c_cl;
  
    FETCH c_cl
      INTO cross_list_id;
    
    CLOSE c_cl;
  
    RETURN cross_list_id;
  
  END//
delimiter ;

call init_ban_xlist_prov_table();

/* 
 *make sure all course number are at least 5 characters 
*/
update course_offering co
set co.course_nbr = rpad(co.course_nbr, 5, '0')
where length(co.course_nbr) < 5;

/*
 * Update database version
 */

insert into application_config (name,value,description)
	values('tmtbl.db.banner.version','1','Timetabling Banner Add On DB version (do not change -- this is used by automatic database update)'); 

commit;
