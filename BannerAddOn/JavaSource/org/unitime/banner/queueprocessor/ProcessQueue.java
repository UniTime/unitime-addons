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

package org.unitime.banner.queueprocessor;

import java.util.Date;
import java.util.List;

import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class ProcessQueue {

	QueueOutDAO qod;
	QueueInDAO qid;

	private static long sleep_interval = 10; // in seconds
	private static long loop_times = -1;
	private static long error_sleep_interval = 300; // in seconds

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
		
		String logfilename = ApplicationProperties.getProperty("queueprocessor.logfilename", "queueprocessor.log"); 
		SolverServerImplementation.configureLogging(
				logfilename,
				ApplicationProperties.getProperties()
				);
		
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
			boolean error = false;
			
			List<QueueOut> outList = null;
			
			do {
			
				try {
					outList = QueueOut.findByStatus(QueueOut.STATUS_POSTED);
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
				Debug.info("Processing ID:" + outList.get(i).getUniqueId().toString());

				(new QueuedItem(outList.get(i))).processItem();
				
				
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
