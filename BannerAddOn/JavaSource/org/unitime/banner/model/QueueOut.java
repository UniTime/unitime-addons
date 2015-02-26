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

package org.unitime.banner.model;

import org.unitime.banner.model.base.BaseQueueOut;


/**
 * 
 * @author says
 *
 */
public class QueueOut extends BaseQueueOut {
	private static final long serialVersionUID = 1L;

	private static final String QUEUE_TYPE = "OT";
	public static final String STATUS_PICKED_UP = "PICKED_UP";

	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public QueueOut () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public QueueOut (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String getQueueType() {
		return QUEUE_TYPE;
	}

}