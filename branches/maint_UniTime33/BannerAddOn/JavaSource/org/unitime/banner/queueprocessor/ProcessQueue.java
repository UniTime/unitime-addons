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

package org.unitime.banner.queueprocessor;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class ProcessQueue {

	QueueOutDAO qod;
	QueueInDAO qid;

	private static long sleep_interval = 10; // in seconds
	private static long loop_times = -1;
	private static long error_sleep_interval = 300; // in seconds
	private static String logfilename = "queueprocessor.log";

	public static void main(String[] args) {

		ProcessQueue processQueue = new ProcessQueue();
		PollStudentUpdates pollStudentUpdates = new PollStudentUpdates();

		Date lastRunTime = new Date();

		if (ApplicationProperties.getProperty("queueprocessor.sleepinterval") != null) {
			sleep_interval = Integer.parseInt(ApplicationProperties.getProperty("queueprocessor.sleepinterval"));
		}
		
		if (ApplicationProperties.getProperty("queueprocessor.errorsleepinterval") != null) {
			error_sleep_interval = Integer.parseInt(ApplicationProperties.getProperty("queueprocessor.errorsleepinterval"));
		}

		if (ApplicationProperties.getProperty("queueprocessor.looptimes") != null) {
			loop_times = Integer.parseInt(ApplicationProperties.getProperty("queueprocessor.looptimes"));
		}
		
		if (ApplicationProperties.getProperty("queueprocessor.logfilename") != null) {
			logfilename = ApplicationProperties.getProperty("queueprocessor.logfilename");
		}

		// Use a daily rolling log file
		Properties logProps = new Properties();
        logProps.setProperty("log4j.rootLogger", "info, LogFile");
    	logProps.setProperty("log4j.appender.LogFile","org.apache.log4j.DailyRollingFileAppender");
    	logProps.setProperty("log4j.appender.LogFile.DatePattern","'.'yyyy-MM-dd");
        logProps.setProperty("log4j.appender.LogFile.File",logfilename);
        logProps.setProperty("log4j.appender.LogFile.layout","org.apache.log4j.PatternLayout");
        logProps.setProperty("log4j.appender.LogFile.layout.ConversionPattern","%d{dd-MMM-yy HH:mm:ss.SSS} [%t] %-5p %c{2}> %m%n");
        logProps.setProperty("log4j.logger.org.unitime.commons.hibernate.connection.DBCPConnectionProvider","INFO");
        ToolBox.configureLogging("logs",logProps);
        System.out.println("Queue Processor Log File:"+logfilename);
        
		try {
			HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}

		Debug.info("Error Sleep Interval: " + error_sleep_interval);
		Debug.info("Sleep Interval: " + sleep_interval);
		Debug.info("Loop Count    : " + loop_times);

		// Convert sleep_intervals to milliseconds
		error_sleep_interval = error_sleep_interval * 1000;
		sleep_interval = sleep_interval * 1000;

		int count = 1;
		boolean done = false;

		while (!done) {

			Debug.info("Iteration: " + count);

			if (count == 1
					|| (new Date()).getTime() - lastRunTime.getTime() >= sleep_interval) {
				lastRunTime = new Date();
				processQueue.process();
				pollStudentUpdates.poll();
			} else {
				try {
					Thread.sleep(sleep_interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			count++;

			if (count >= Long.MAX_VALUE) {
				count = 1;
			}

			if (loop_times >= 0 && count >= loop_times) {
				Debug.info("Iteration count completed.  Exiting QueueProcessor.");
				done = true;
			}
		}
	}

	public static void printUsage() {
		System.out
				.println("java -jar queueProcessor.jar [connect string] [sleep_interval_in_seconds] [loop count]");
	}

	public void process() {

		try {
			qod = new QueueOutDAO();

			boolean error = false;
			
			List outList = null;
			
			do {
			
				try {
					outList = qod.findByStatus(QueueOut.STATUS_POSTED);
					error = false;
				} catch(Exception ex) {
					//Sleep for the error_sleep_interval and try again
					Debug.error(ex);
					Thread.sleep(error_sleep_interval);
					error = true;
				}
			} while (error); //Try again if an error occurred

			if (outList.size() == 0) {
				Debug.info("*** No items in Queue to be processed.");
				return;
			}

			Debug.info("*** Processing " + outList.size()
					+ " queue entries. ");

			for (int i = 0; i < outList.size(); i++) {
				try {
				Debug.info("Processing ID:"
						+ ((QueueOut) outList.get(i)).getUniqueId().toString());

				(new QueuedItem((QueueOut) outList.get(i))).processItem();
				
				
				} catch(Exception ex) {
					//Sleep for the error_sleep_interval and try again
					Debug.error(ex);
					Thread.sleep(error_sleep_interval);
					i--;
				}
			}

			Debug.info("*** Processing complete.");

		} catch (Exception ex) {
			LoggableException le = new LoggableException(ex);
			le.logError();

			le.printStackTrace();
		}
	}
}
