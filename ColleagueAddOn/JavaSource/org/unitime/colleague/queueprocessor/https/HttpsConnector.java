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

package org.unitime.colleague.queueprocessor.https;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */

class TrustPrivatelySignedCertificates implements X509TrustManager {
    static final TrustPrivatelySignedCertificates INSTANCE = new TrustPrivatelySignedCertificates();

	private final static X509Certificate[] x509Certificates = new X509Certificate[0];

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// Do nothing
		Debug.info("Using TrustPrivatelySignedCertificates checkClientTrusted");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// Do nothing
		Debug.info("Using TrustPrivatelySignedCertificates checkServerTrusted");
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		Debug.info("Using TrustPrivatelySignedCertificates getAcceptedIssuers");
		return x509Certificates;
	}

}

class TrustPrivatelySignedCertificatesSslContextFactory extends DefaultSslContextFactory {
    @Override
    public SSLContext createSslContext() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { TrustPrivatelySignedCertificates.INSTANCE }, null);
        return createWrapper(sslContext);
    }
}

class PrivatelySignedCertificateHostNameVerifier implements HostnameVerifier {

	@Override
	public boolean verify(String arg0, SSLSession arg1) {
		// return true no matter what
		Debug.info("Using PrivatelySignedCertificateHostNameVerifier verify");
		return true;
	}
}

public class HttpsConnector {
    final static SslContextFactory sslContextFactory = new TrustPrivatelySignedCertificatesSslContextFactory();
	private Client iClient;

	public HttpsConnector() throws ClassNotFoundException, SQLException {

		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
	
	}
	
	protected Document readResponse(Representation response) throws IOException, DocumentException {
//		if (response == null) return null;
		
		
//		Reader r = response.getReader();
		Document d = null;
//		try {
//			d = (new SAXReader()).read(r);
//		} finally {
//			r.close();
//			response.release();
//		}
//		return(d);
		
		try {
			if (response == null) { 
				Debug.info("Response is null");
				return null;
			}
			if (response.isEmpty()) {
				Debug.info("Response is empty");
	            d = null;
	        } else if (response.isAvailable()) {
				Debug.info("Response is available");
	        	String result;
	            java.io.StringWriter sw = new java.io.StringWriter();
	            response.write(sw);
	            sw.flush();
	            result = sw.toString();
	            StringReader sr = new StringReader(result);
	            d = (new SAXReader()).read(sr);
	        }			
			return (d);
		} finally {
			response.release();
		}
	}
	
	protected String readResponse2(Representation response) throws IOException, DocumentException {
		String result = null;
		try {
			if (response == null) {
				Debug.info("Response is null");
				return null;
			}
			if (response.isEmpty()) {
				Debug.info("Response is empty");
	            result = "";
	        } else if (response.isAvailable()) {
				Debug.info("Response is available");
	            java.io.StringWriter sw = new java.io.StringWriter();
	            response.write(sw);
	            sw.flush();
	            result = sw.toString();
	        }			
			return (result);
		} finally {
			response.release();
		}
		
	}

	public Document processUniTimePacket(String site, String user,
			String password, boolean siteWithSelfSignedCertificate, Document xml) throws Exception{

		ClientResource resource = null;
		Document response = null;
		try {
			// First, check student registration status
			if(siteWithSelfSignedCertificate){
		        Context clientContext = new Context();
		        clientContext.getAttributes().put("sslContextFactory", sslContextFactory);
		        clientContext.getAttributes().put("hostnameVerifier", new PrivatelySignedCertificateHostNameVerifier());
		        clientContext.getParameters().add("readTimeout", ApplicationProperties.getProperty("colleague.listener.readTimeout", "300000"));
		        resource = new ClientResource(clientContext, site);
		        Debug.info("Using custom context factory.");
			} else {
		        Debug.info("Not using custom context factory.");
				resource = new ClientResource(site);
			}
			resource.setNext(iClient);				
			if (user != null){
				resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user, password);
			}
			try {
				Debug.info("\tBefore post call to Colleague...");		
				resource.setResponseEntityBuffering(true);
				Representation result = resource.post(new StringRepresentation((xml == null? null : xmlToString(xml)), MediaType.APPLICATION_XML));
				Debug.info("\tAfter post call to Colleague...");
				response = readResponse(result);
			} catch (ResourceException exception) {
				exception.printStackTrace();
				try {
					Debug.info("response entity: " + resource.getResponse().getEntityAsText());
					readResponse(resource.getResponse().getEntity());
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				} catch (Throwable t) {
					t.printStackTrace();
 					throw exception;
				}
			} 		

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
		return(response);
	}
	
	private String xmlToString(Document xml) throws IOException{
		StringWriter w = new StringWriter();
		new XMLWriter(w, OutputFormat.createPrettyPrint()).write(xml);
		w.flush(); w.close();
		return(w.toString());
	}


}
