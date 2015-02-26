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
 *  Search for existence of duplicate CRNs within a session
 */
SELECT crn,
       COUNT(*)
  FROM timetable.banner_section t
 WHERE session_id = (SELECT s.uniqueid
                       FROM timetable.sessions s
                      WHERE s.academic_initiative = '&initiative' 
                        AND s.academic_year = '&year' 
                        AND s.academic_term = '&term') 
 GROUP BY t.crn
HAVING COUNT(*) > 1;
