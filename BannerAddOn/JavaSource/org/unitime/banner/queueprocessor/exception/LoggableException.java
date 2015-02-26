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

package org.unitime.banner.queueprocessor.exception;


import org.unitime.banner.model.Queue;
import org.unitime.commons.Debug;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class LoggableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4788030534015762772L;
	private Queue queuedItem;
	private boolean errorLogged = false;

	public Queue getQueuedItem() {
		return queuedItem;
	}
	public void setQueuedItem(Queue queuedItem) {
		this.queuedItem = queuedItem;
	}
	
	/* ***************************************************** */

	public void logError() {
		
		if(errorLogged) return;
		
		//Log the error in the log file
		Debug.error(this);
		
		/* Removed so the error is logged in the log file instead
		 * of the database

		this.printStackTrace();
		
		try {

			//DAOs
			QueueErrorDAO dao = new QueueErrorDAO();

			//QueueIn
			QueueError qe = new QueueError();

			//Set the QueueError Data
			qe.setId(queuedItem);
			
			String msg = super.getMessage();
			if(msg == null) msg = "";
			if(msg.length() > 255) {
				msg = msg.substring(0, 254) + '*';
			}
			qe.setErrorText(msg);
			
			//Save Queue
			dao.save(qe);
			
			errorLogged = true;

		} catch (Throwable e) {

			System.err.println("Error Logging Exception: " 
					+ e.toString() + "\n\n");

			for(int i = 0; i < e.getStackTrace().length; i++) {
				System.err.println(e.getStackTrace()[i].toString());
			}
		}
		
		*/
	}
	
	
// Constructors
	public LoggableException(Queue queueEntry) {
		super();
		this.queuedItem = queueEntry;
	}

	public LoggableException(String message, Queue queueEntry) {
		super(message);
		this.queuedItem = queueEntry;
	}
	
	public LoggableException(String message) {
		super(message);
	}

	public LoggableException(Throwable throwable) {
		super(throwable);
	}

	public LoggableException(Throwable throwable, Queue queueEntry) {
		super(throwable);
		this.queuedItem = queueEntry;		
	}
}
