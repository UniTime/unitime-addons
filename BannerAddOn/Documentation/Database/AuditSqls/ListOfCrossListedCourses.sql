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
 *  This provides a list of all course offerings that are crosslisted
 *    i.e. All instances where an instructional offering has multiple
 *        course offerings associated with it.
 */
SELECT sa1.subject_area_abbreviation AS ctrl_subj,
       co1.course_nbr                AS ctrl_crs,
       sa2.subject_area_abbreviation AS subj,
       co2.course_nbr                AS crs
  FROM instructional_offering io,
       course_offering        co1,
       course_offering        co2,
       sessions               s,
       subject_area           sa1,
       subject_area           sa2
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND io.session_id = s.uniqueid
   AND io.not_offered = '0'
   AND co1.instr_offr_id = io.uniqueid
   AND co1.is_control = 1
   AND co2.instr_offr_id = io.uniqueid
   AND co2.is_control = 0
   AND sa1.uniqueid = co1.subject_area_id
   AND sa2.uniqueid = co2.subject_area_id
 ORDER BY sa1.subject_area_abbreviation,
          co1.subject_area_id
