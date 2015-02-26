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
 */
 
/*
 * Add table to store Banner Campus Overrides
 */

CREATE TABLE "BANNER_CAMPUS_OVERRIDE"
  (
    "UNIQUEID"               NUMBER(20,0) NOT NULL ENABLE,
    "BANNER_CAMPUS_CODE"          VARCHAR2(20 BYTE) NOT NULL ENABLE,
    "BANNER_CAMPUS_NAME"       VARCHAR2(100 BYTE) NOT NULL ENABLE,
    "VISIBLE"  NUMBER(1,0) NOT NULL ENABLE,
    CONSTRAINT "PK_BANNER_CAMPUS_OVERRIDE_UID" PRIMARY KEY ("UNIQUEID")) ;
    
/*
 * Add column to BannerSection to store banner campus override
 */
 
 ALTER TABLE "BANNER_SECTION" ADD BANNER_CAMPUS_OVERRIDE_ID NUMBER(20,0);
 ALTER TABLE "BANNER_SECTION" ADD CONSTRAINT FK_CAMPUS_OVERRIDE FOREIGN KEY ("BANNER_CAMPUS_OVERRIDE_ID") REFERENCES "BANNER_CAMPUS_OVERRIDE"("UNIQUEID") ON DELETE SET NULL;
 
/*
 * Update database version
 */

update application_config set value='4' where name='tmtbl.db.banner.version';

commit;
 