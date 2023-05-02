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

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Transient;

import java.io.StringReader;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.unitime.banner.model.base.BaseQueue;
import org.unitime.commons.Debug;



/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Queue extends BaseQueue {
	private static final long serialVersionUID = 1L;

	public static final String STATUS_POSTED = "POSTED";
	public static final String STATUS_PROCESSED = "PROCESSED";
	public static final String STATUS_INVALID = "INVALID";
	public static final String STATUS_STALE = "STALE";
	// posted message is ready to be processed (outside of the queue processor)
	public static final String STATUS_READY = "READY";
	// posted message failed to be processed (outside of the queue processor)
	public static final String STATUS_FAILED = "FAILED";
	// posted message is being processed
	public static final String STATUS_PROCESSING = "PROCESSING";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Queue () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Queue (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	@Transient
	public abstract String getQueueType();
	
	@Transient
	public Document getDocument() {
		if (getXml() == null) return null;
		try {
			return new SAXReader().read(new StringReader(getXml()));
		} catch (Exception e) {
			Debug.error("Failed to parse XML document: " + e.getMessage(), e);
			return null;
		}
	}
	
	public void setDocument(Document document) {
		try {
			if (document == null) {
				setXml(null);
			} else {
				StringWriter string = new StringWriter();
				XMLWriter writer = new XMLWriter(string, OutputFormat.createCompactFormat());
				writer.write(document);
				writer.flush(); writer.close();
				setXml(string.toString());
			}
		} catch (Exception e) {
			Debug.error("Failed to store XML document: " + e.getMessage(), e);
		}
	}
}
