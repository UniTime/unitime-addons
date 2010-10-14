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
