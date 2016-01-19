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
 *  Author:  Stephanie Schluttenhofer
 */
/*
 *  The purpose of this sql it to provide a list of offerings that have
 *    multiple instructional offering configurations so a user can
 *    validate the configurations are as expected.
 */
SELECT sa.subject_area_abbreviation,
       co.course_nbr
  FROM sessions               s,
       subject_area           sa,
       course_offering        co,
       instructional_offering io
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND sa.session_id = s.uniqueid
   AND co.subject_area_id = sa.uniqueid
   AND co.is_control = 1
   AND io.uniqueid = co.instr_offr_id
   AND io.not_offered = 0
   AND 1 < (SELECT COUNT(1)
              FROM instr_offering_config ioc
             WHERE ioc.instr_offr_id = io.uniqueid)
 ORDER BY sa.subject_area_abbreviation,
          co.course_nbr
