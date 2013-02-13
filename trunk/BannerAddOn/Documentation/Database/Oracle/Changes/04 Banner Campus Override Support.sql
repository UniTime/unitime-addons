/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 