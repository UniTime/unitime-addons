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
package org.unitime.banner.util;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.unitime.banner.dataexchange.BannerMessage;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao._RootDAO;


/**
 * 
 * @author Stephanie Schluttenhofer
 *
 */
public class BannerMessageIdGenerator {
    private static IdentifierGenerator sGenerator = null;
    
    protected static String sSequence = "banner_message_seq";
    
    public static IdentifierGenerator getGenerator() throws HibernateException {
        try {
            if (sGenerator!=null) return sGenerator;
            UniqueIdGenerator idGen = new UniqueIdGenerator();
            Dialect dialect = (Dialect)Class.forName(LocationDAO.getConfiguration().getProperty("hibernate.dialect")).getConstructor(new Class[]{}).newInstance(new Object[]{});
            Type type = new LongType();
            Properties params = new Properties();
            params.put(SequenceGenerator.SEQUENCE, sSequence);
            params.put(PersistentIdentifierGenerator.SCHEMA, _RootDAO.getConfiguration().getProperty("default_schema"));
            idGen.configure(type, params, dialect);
            sGenerator = idGen;
            return sGenerator;
        } catch (HibernateException e) {
            throw e;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }
    
    public static void setMessageId(BannerMessage message) {
        message.setMessageId(((Number)getGenerator().generate((SessionImplementor)new _RootDAO().getSession(), message)).longValue());
    }
}
