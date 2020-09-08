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

alter table banner_session add banner_term_crn_prop_id decimal(20,0);
alter table banner_session add use_subj_area_prfx_as_campus decimal(1,0);
alter table banner_session add subj_area_prfx_delim varchar(5);

alter table banner_session add constraint fk_ban_trm_to_crn_prop foreign key (banner_term_crn_prop_id)
			references banner_crn_provider (uniqueid) on delete set null;
update banner_session
set banner_session.banner_term_crn_prop_id = (select banner_crn_provider.uniqueid from banner_crn_provider where banner_crn_provider.term_code = banner_session.banner_term_code);
 

DROP FUNCTION IF EXISTS get_crn_provider_id;
delimiter //
CREATE FUNCTION get_crn_provider_id(sesn_id DECIMAL(20, 0))
    RETURNS DECIMAL(20, 0)

  BEGIN
    declare crn_provider_id DECIMAL(20, 0);


    BEGIN
      SELECT banner_session.banner_term_crn_prop_id
        INTO crn_provider_id
        FROM banner_session
       WHERE banner_session.session_id = sesn_id;
    END;

    RETURN crn_provider_id;

  END //
  delimiter ;
  DROP PROCEDURE IF EXISTS insert_crn_provider_rec;
  delimiter //
  CREATE PROCEDURE insert_crn_provider_rec(v_term_code VARCHAR(20),
         v_next_crn  BIGINT(10),
         v_min_crn   BIGINT(10),
         v_max_crn   BIGINT(10),
         v_sesn_id DECIMAL(20, 0)) 
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
         '1',
         v_min_crn,
         v_max_crn);
         
      update banner_session
      set banner_term_crn_prop_id = next_id
      where session_id = v_sesn_id;
      
      update hibernate_unique_key set next_hi = next_hi+1;
      commit;

  END //
  delimiter ;
  DROP PROCEDURE IF EXISTS update_crn_provider_rec;
  delimiter //
  CREATE PROCEDURE update_crn_provider_rec(v_banner_crn_provider_id DECIMAL(20, 0),
       v_next_crn  BIGINT(10),
       v_search    CHAR(1)) 

  BEGIN
        start transaction;
        UPDATE banner_crn_provider
           SET last_crn    = v_next_crn,
               search_flag = v_search
        WHERE uniqueid = v_banner_crn_provider_id;
        COMMIT;
  END //
  delimiter ;
DROP PROCEDURE IF EXISTS get_crn;
delimiter //
CREATE PROCEDURE get_crn(OUT out_crn BIGINT(10), IN sesn_id LONG)

BEGIN
    DECLARE v_term_code VARCHAR(20);
    DECLARE v_banner_crn_provider_id DECIMAL(20, 0);
    DECLARE v_next_crn  BIGINT(10);
    DECLARE v_min_crn   BIGINT(10);
    DECLARE v_max_crn   BIGINT(10);
    DECLARE v_search    CHAR(1);

    SET v_term_code = get_term_code(sesn_id);
    SET v_banner_crn_provider_id = get_crn_provider_id(sesn_id);

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
       WHERE banner_crn_provider.uniqueid = v_banner_crn_provider_id;
    END;
    
    IF (v_min_crn IS NULL) THEN
        SET v_min_crn  = 10000;
        SET v_max_crn  = 69999;
        SET v_next_crn = get_max_crn(v_term_code,
                                v_min_crn,
                                v_max_crn);
    END IF;

    IF (v_search IS NULL OR v_search = 'Y' or v_search = '1') THEN
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
      SET v_search   = '1';
    END IF;

    IF (v_banner_crn_provider_id IS NULL) THEN
	    call insert_crn_provider_rec(v_term_code,
	       v_next_crn,
	       v_min_crn,
	       v_max_crn,
         sesn_id); 
    ELSE
      call update_crn_provider_rec(v_banner_crn_provider_id, v_next_crn, v_search);
    END IF;
    set out_crn = v_next_crn;
  END //
  delimiter ;

update application_config set value='11' where name='tmtbl.db.banner.version';
  
commit;