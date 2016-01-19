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

package org.unitime.colleague.queueprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Clob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.colleague.queueprocessor.https.HttpsConnector;
import org.unitime.colleague.queueprocessor.oracle.OracleConnector;
import org.unitime.colleague.queueprocessor.util.ClobTools;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author says
 *
 */
public abstract class ColleagueCaller {

	public enum CONNECTION_TYPES { ORACLE, HTTPS, FILE };
	public enum FILE_PROCESS_STATUS { SUCCESS, FAILED };
	public enum FILE_TYPES { SECTION, STUDENT };
	
	public ColleagueCaller() {
		super();
	}

	public static String getColleagueHost() throws Exception{
		String colleagueHost = ApplicationProperties.getProperty("colleague.host");
        if (colleagueHost == null || colleagueHost.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.host', this property must be set to the host machine for the colleague database.");
        }
		return colleagueHost;
	}

	public static String getColleaguePort() throws Exception{
		String colleaguePort = ApplicationProperties.getProperty("colleague.port");
        if (colleaguePort == null || colleaguePort.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.port', this property must be set to the port number used to connect to the colleague database.");
        }
		return colleaguePort;
	}

	public static String getCollegueDatabase() throws Exception{
		String  colleagueDatabase = ApplicationProperties.getProperty("colleague.database");
        if (colleagueDatabase == null || colleagueDatabase.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.database', this property must be set to the name of the database that hosts the colleague schema.");
        }
		return colleagueDatabase;
	}

	public static String getColleagueUser() throws Exception{
		String colleagueUser = ApplicationProperties.getProperty("colleague.user");
        if (colleagueUser == null || colleagueUser.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.user', this property must be set to the name of the user used to access the colleague schema.");
        }
		return colleagueUser;
	}

	public static boolean getColleagueUseSelfSigned() throws Exception{
		String selfSigned = ApplicationProperties.getProperty("colleague.https.useSelfSigned", "false");
        if (selfSigned.equalsIgnoreCase("true")){
        	return(true);
        }
		return(false);
	}
	public static String getColleaguePassword() throws Exception{
		String colleaguePassword = ApplicationProperties.getProperty("colleague.password");
        if (colleaguePassword == null || colleaguePassword.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.password', this property must be set to the password of the user used to access the colleague schema.");
        }
		return colleaguePassword;
	}

	public static String getColleagueSectionHttpsSite() throws Exception {
		String colleagueHttpsSite = ApplicationProperties.getProperty("colleague.section.https.site");
        if (colleagueHttpsSite == null || colleagueHttpsSite.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.section.https.site', this property must be set to the colleague section interface url.");
        }
		return colleagueHttpsSite;
	}
	
	public static String getColleagueSectionHttpsUser() throws Exception {
		String colleagueHttpsUser = ApplicationProperties.getProperty("colleague.section.https.user");
		return colleagueHttpsUser;
	}
	
	public static String getColleagueSectionHttpsPassword() throws Exception {
		String colleagueHttpsPassword = ApplicationProperties.getProperty("colleague.section.https.password");
		return colleagueHttpsPassword;
	}
	
	public static String getColleagueStudentHttpsSite() throws Exception {
		String colleagueHttpsSite = ApplicationProperties.getProperty("colleague.student.https.site");
        if (colleagueHttpsSite == null || colleagueHttpsSite.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.student.https.site', this property must be set to the colleague student interface url.");
        }
		return colleagueHttpsSite;
	}
	
	public static String getColleagueStudentHttpsUser() throws Exception {
		String colleagueHttpsUser = ApplicationProperties.getProperty("colleague.student.https.user");
		return colleagueHttpsUser;
	}
	
	public static String getColleagueStudentHttpsPassword() throws Exception {
		String colleagueHttpsPassword = ApplicationProperties.getProperty("colleague.student.https.password");
		return colleagueHttpsPassword;
	}

	public static String getColleagueSectionInterfaceConnectionType() throws Exception {
		String colleagueInterfaceConnectionType = ApplicationProperties.getProperty("colleague.interface.section.connection.type");
        if (colleagueInterfaceConnectionType == null || colleagueInterfaceConnectionType.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'colleague.interface.section.connection.type', this property must be set to either ORACLE or HTTPS.  It defines how to transfer the section data to Colleague.");
        }
        if (!CONNECTION_TYPES.HTTPS.equals(colleagueInterfaceConnectionType) && !CONNECTION_TYPES.ORACLE.equals(colleagueInterfaceConnectionType)){
        	throw new Exception("Required custom application property:  'colleague.interface.section.connection.type', this property must be set to either ORACLE or HTTPS.  It defines how to transfer the section data to Colleague.");
        }
		return colleagueInterfaceConnectionType;
	}
	
	public static String getColleagueStudentInterfaceConnectionType() throws Exception {

		String colleagueInterfaceConnectionType = ApplicationProperties.getProperty("colleague.interface.student.connection.type");
			if (colleagueInterfaceConnectionType == null || colleagueInterfaceConnectionType.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.student.connection.type', this property must be set to either ORACLE or FILE.  It defines how to transfer the student data to Colleague.");
	        }
		return colleagueInterfaceConnectionType;
	}

	public static String getColleagueStudentInterfaceConnectionFileDirectory() throws Exception {
		String colleagueInterfaceConnectionDirectory = ApplicationProperties.getProperty("colleague.interface.student.connection.file.directory");
        if (CONNECTION_TYPES.FILE.equals(getColleagueStudentInterfaceConnectionType())) {
	        if (colleagueInterfaceConnectionDirectory == null || colleagueInterfaceConnectionDirectory.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.student.connection.file.directory', this property must be set when the property 'colleague.interface.student.connection.type' is set to file FILE.  It defines where to look for the student data from Colleague.");
	        }
        }
		return colleagueInterfaceConnectionDirectory;
	}

	public static String getColleagueStudentInterfaceConnectionIncomingFileBaseFilename() throws Exception {
		String colleagueInterfaceConnectionIncomingBaseFilename = ApplicationProperties.getProperty("colleague.interface.student.connection.file.incomingBaseFilename");
        if (CONNECTION_TYPES.FILE.equals(getColleagueStudentInterfaceConnectionType())) {
	        if (colleagueInterfaceConnectionIncomingBaseFilename == null || colleagueInterfaceConnectionIncomingBaseFilename.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.student.connection.file.incomingBaseFilename', this property must be set when the property 'colleague.interface.student.connection.type' is set to file FILE.  It defines the base file name used for the student data from Colleague.");
	        }
        }
		return colleagueInterfaceConnectionIncomingBaseFilename;
	}

	public static String getColleagueStudentInterfaceConnectionOutgoingFileBaseFilename() throws Exception {
		String colleagueInterfaceOutgoingBaseFilename = ApplicationProperties.getProperty("colleague.interface.student.connection.file.outgoingBaseFilename");
        if (CONNECTION_TYPES.FILE.equals(getColleagueStudentInterfaceConnectionType())) {
	        if (colleagueInterfaceOutgoingBaseFilename == null || colleagueInterfaceOutgoingBaseFilename.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.student.connection.file.outgoingBaseFilename', this property must be set when the property 'colleague.interface.student.connection.type' is set to file FILE.  It defines the base file name used for the student data to Colleague.");
	        }
        }
		return colleagueInterfaceOutgoingBaseFilename;
	}

	public static String getColleagueSectionInterfaceConnectionFileDirectory() throws Exception {
		String colleagueInterfaceConnectionDirectory = ApplicationProperties.getProperty("colleague.interface.section.connection.file.directory");
        if (CONNECTION_TYPES.FILE.equals(getColleagueSectionInterfaceConnectionType())) {
	        if (colleagueInterfaceConnectionDirectory == null || colleagueInterfaceConnectionDirectory.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.section.connection.file.directory', this property must be set when the property 'colleague.interface.section.connection.type' is set to file FILE.  It defines where to look for the section data from Colleague.");
	        }
        }
		return colleagueInterfaceConnectionDirectory;
	}

	public static String getColleagueSectionInterfaceConnectionIncomingFileBaseFilename() throws Exception {
		String colleagueInterfaceConnectionIncomingBaseFilename = ApplicationProperties.getProperty("colleague.interface.section.connection.file.incomingBaseFilename");
        if (CONNECTION_TYPES.FILE.equals(getColleagueSectionInterfaceConnectionType())) {
	        if (colleagueInterfaceConnectionIncomingBaseFilename == null || colleagueInterfaceConnectionIncomingBaseFilename.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.section.connection.file.incomingBaseFilename', this property must be set when the property 'colleague.interface.section.connection.type' is set to file FILE.  It defines the base file name used for the section data from Colleague.");
	        }
        }
		return colleagueInterfaceConnectionIncomingBaseFilename;
	}

	public static String getColleagueSectionInterfaceConnectionOutgoingFileBaseFilename() throws Exception {
		String colleagueInterfaceOutgoingBaseFilename = ApplicationProperties.getProperty("colleague.interface.section.connection.file.outgoingBaseFilename");
        if (CONNECTION_TYPES.FILE.equals(getColleagueSectionInterfaceConnectionType())) {
	        if (colleagueInterfaceOutgoingBaseFilename == null || colleagueInterfaceOutgoingBaseFilename.trim().length() == 0){
	        	throw new Exception("Missing required custom application property:  'colleague.interface.section.connection.file.outgoingBaseFilename', this property must be set when the property 'colleague.interface.section.connection.type' is set to file FILE.  It defines the base file name used for the section data to Colleague.");
	        }
        }
		return colleagueInterfaceOutgoingBaseFilename;
	}

	protected OracleConnector getJDBCconnection(){
		OracleConnector jdbc = null;
		try {
			jdbc = new OracleConnector(
					QueuedItem.getColleagueHost(), 
					QueuedItem.getCollegueDatabase(),
					QueuedItem.getColleaguePort(),
					QueuedItem.getColleagueUser(),
					QueuedItem.getColleaguePassword());
		} catch (Exception e) {
			Debug.info("*********************************************************************");
			Debug.info("** Error setting up OracleConnector in in callOracleProcess *********");
			Debug.info("*********************************************************************");
			Debug.info(e.getMessage());
			e.printStackTrace();
			Debug.info("*********************************************************************");
		}
		return(jdbc);
	}
	
	protected HttpsConnector getHTTPSconnection(){
		HttpsConnector jdbc = null;
		try {
			jdbc = new HttpsConnector();
		} catch (Exception e) {
			Debug.info("*********************************************************************");
			Debug.info("** Error setting up HttpsConnector in in callHttps Process  *********");
			Debug.info("*********************************************************************");
			Debug.info(e.getMessage());
			e.printStackTrace();
			Debug.info("*********************************************************************");
		}
		return(jdbc);
	}

	protected Document convertClobToDocument(Clob clob){
		Document outDoc = null;
		try {
			outDoc = ClobTools.clobToDocument(clob);
			
		} catch (Exception ex) {
			Debug.info("***************************************");
			Debug.info("** Error in callOracleProcess *********");
			Debug.info("***************************************");
			ex.printStackTrace();
			Debug.info("***************************************");
		}	
		return(outDoc);
	}
	
	protected ArrayList<File> filesToProcess(String directoryName, String fileNameBase) throws Exception {
	
		File directory = new File(directoryName);
		if (!directory.isDirectory()){
			throw(new Exception("'" + directoryName + "' is not a valid directory."));
		}
		if (fileNameBase.isEmpty()){
			throw(new Exception("Missing base name for files to look for in directory: '" + directoryName + "'."));			
		}
		ArrayList<File> files = new ArrayList<File>();
		for(File f : directory.listFiles()){
			if (f.getName().contains(fileNameBase)){
				if (!f.getName().contains(FILE_PROCESS_STATUS.SUCCESS.toString()) && !f.getName().contains(FILE_PROCESS_STATUS.FAILED.toString())){
					files.add(f);
				}
			}
		}
				
		Collections.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return (f1.getName()).compareTo(f2.getName());
		    } });
		return(files);
	}
	
	protected void renameFile(File file, String namePrefix){
		String path = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(file.getName()));
		String newName = namePrefix + file.getName();
		String newFileName = path + File.separator + newName;
		
        File newFile = new File(newFileName);
         
        boolean status = file.renameTo(newFile) ;
         
        if (status)
            Debug.info("File:  " + newFileName + " - renamed successfully.");
        else
            Debug.info("File name:  " + newFileName + " - rename failed.");
		
	}
	
	protected void renameFileSuccess(File file) {
			renameFile(file, "success_");
	}
	
	protected void renameFileError(File file) {
		renameFile(file, "error_");
	}

	protected Document documentFromFile(File file) {
		Document document = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            document = (new SAXReader()).read(fis);
            renameFileSuccess(file);
        } catch (IOException e) {
            Debug.error("Unable to read file "+file.getName()+", reason:"+e.getMessage(),e);
            renameFileError(file);
        } catch (DocumentException e) {
            renameFileError(file);
            Debug.error("Unable to parse xml "+file.getName()+", reason:"+e.getMessage(),e);
		} finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException e) {}
            }
        }
		return(document);
	}
	
	protected File createNewFileOfType(FILE_TYPES fileType) throws Exception{
		StringBuilder sb = new StringBuilder();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		if (FILE_TYPES.SECTION.equals(fileType)){
			sb.append(getColleagueSectionInterfaceConnectionFileDirectory())
			  .append(File.separator)
			  .append(getColleagueSectionInterfaceConnectionOutgoingFileBaseFilename())
			  .append(df.format(new Date()))
			  .append(".xml");
		} else {
			sb.append(getColleagueStudentInterfaceConnectionFileDirectory())
			  .append(File.separator)
			  .append(getColleagueStudentInterfaceConnectionOutgoingFileBaseFilename())
			  .append(df.format(new Date()))
			  .append(".xml");			
		}
		
		return(new File(sb.toString()));
	}

	protected void documentToFile(File file, Document document) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
            fos.flush();fos.close();fos=null;
        } finally {
            try {
                if (fos!=null) fos.close();
            } catch (IOException e) {
                Debug.error("Unable to write file "+file.getName()+", reason:"+e.getMessage(),e);
                throw e;
            }
        }
	}
}
