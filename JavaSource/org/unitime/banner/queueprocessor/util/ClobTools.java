/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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

package org.unitime.banner.queueprocessor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import oracle.sql.CLOB;
/*
 * based on code contributed by Aaron Tyler
 */
public class ClobTools {

	public static String clobToString(Clob clob) throws SQLException, IOException {

		String returnStr = "";
		
		String aux;
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while ((aux = br.readLine()) != null) {
			returnStr += aux;
		}
		
		return returnStr;
	}
	

	public static CLOB documentToCLOB(Document document, Connection conn) throws IOException, SQLException {
		
		//Set up Output CLOB
		CLOB tempClob = null;
		tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);
		tempClob.open(CLOB.MODE_READWRITE);
		Writer tempClobWriter = tempClob.getCharacterOutputStream();

		//Write document text to Output CLOB

		tempClobWriter.write(document.asXML());

		//Finalize the Output CLOB and return it
		tempClobWriter.flush();
		tempClobWriter.close();
		tempClob.close();

		return tempClob;
		
		
	}
	
	public static Document clobToDocument(Clob clob) throws DocumentException, SQLException{
		SAXReader reader = new SAXReader();
		Document document = reader.read(clob.getCharacterStream());
		return document;
	}
	
	public static CLOB clobToCLOB(Clob inClob, Connection conn) throws IOException, SQLException {
		
		//Set up Output CLOB
		CLOB tempClob = null;
		tempClob = CLOB.createTemporary(conn, true, CLOB.DURATION_SESSION);
		tempClob.open(CLOB.MODE_READWRITE);
		Writer tempClobWriter = tempClob.getCharacterOutputStream();

		//Traverse through Input Clob and write contents to Output CLOB
		String aux;
		BufferedReader br = new BufferedReader(inClob.getCharacterStream());
		while ((aux = br.readLine()) != null) {
			tempClobWriter.write(aux);
		}

		//Finalize the Output CLOB and return it
		tempClobWriter.flush();
		tempClobWriter.close();
		tempClob.close();

		return tempClob;
	}

}
