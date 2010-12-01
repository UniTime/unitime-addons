/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
package org.unitime.banner.util;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
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
