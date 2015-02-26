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
