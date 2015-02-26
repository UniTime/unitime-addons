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
 *  When using UniTime with Banner if the same itype is used it must 
 *    be nested and the classes underneath it must have the same
 *    expected capacity as their parent classes.  If the classes do
 *    not have the same expected capacity as their parent classes they
 *    need to be a different itype.  The purpose of this sql is to help
 *    find instances where a different itype is needed.
 */
SELECT DISTINCT sa.subject_area_abbreviation AS subj,
                co.course_nbr,
                ss.itype
  FROM sessions               s,
       subject_area           sa,
       course_offering        co,
       instructional_offering io,
       instr_offering_config  ioc,
       scheduling_subpart     ss,
       class_                 c,
       scheduling_subpart     ss2,
       class_                 c2
 WHERE s.academic_initiative = '&initiative'
   AND s.academic_year = '&year'
   AND s.academic_term = '&term'
   AND sa.session_id = s.uniqueid
   AND co.subject_area_id = sa.uniqueid
   AND co.is_control = 1
   AND io.uniqueid = co.instr_offr_id
   AND ioc.instr_offr_id = io.uniqueid
   AND ss.config_id = ioc.uniqueid
   AND ss2.config_id = ioc.uniqueid
   AND ss.uniqueid != ss2.uniqueid
   AND c.subpart_id = ss.uniqueid
   AND c2.subpart_id = ss2.uniqueid
   AND ss.itype = ss2.itype
   AND (ss.PARENT = ss2.uniqueid OR ss2.PARENT = ss.uniqueid)
   AND c2.parent_class_id = c.uniqueid
   AND c.expected_capacity != c2.expected_capacity
