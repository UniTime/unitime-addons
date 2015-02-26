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

package org.unitime.banner.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * 
 * @author Tomas Muller
 *
 */
public class MergeXml extends Task {
    private String iSource, iTarget; 
    
    public void setSource(String source) {
        iSource = source;
    }
    
    public void setTarget(String target) {
        iTarget = target;
    }
    
    private void merge(Element target, Element source) {
        for (Iterator i=source.attributeIterator();i.hasNext();) {
            Attribute attribute = (Attribute)i.next();
            target.addAttribute(attribute.getName(),attribute.getValue());
        }
        for (Iterator i=source.nodeIterator();i.hasNext();) {
            Node node = (Node)i.next();
            if (node instanceof Element) {
                Element element = (Element)node;
                if ("property".equals(element.getName())) {
                    String name = element.attributeValue("name","noname");
                    Element targetProperty = null;
                    for (Iterator j=target.elementIterator("property");j.hasNext();) {
                        Element property = (Element)j.next();
                        if (name.equals(property.attributeValue("name"))) {
                            targetProperty = property; break;
                        }
                    }
                    if (targetProperty!=null) {
                        target.remove(targetProperty);
                    }
                    if (element.getText()!=null && element.getText().trim().length()>0) {
                        target.addElement("property").addAttribute("name", name).setText(element.getText());
                    }
                } else {
                    if (target.elements(element.getName()).size()==1 && source.elements(element.getName()).size()==1)
                        merge(target.element(element.getName()),element);
                    else
                        merge(target.addElement(element.getName()),element);
                }
            } else if (node instanceof Comment) {
                Comment comment = (Comment)node;
                target.addComment(comment.getText());
            } else if (node instanceof CDATA) {
                CDATA data = (CDATA)node;
                target.add((CDATA)data.clone());
            } else if (node instanceof Text) {
                Text text = (Text)node;
                if (text.getText()!=null && text.getText().trim().length()>0)
                    target.addText(text.getText());
            } else if (node instanceof Namespace) {
            } else {
                log("Unknown node "+node);
            }
        }
    }

    public void execute() throws BuildException {
        try {
            log("Merging "+iTarget+" with "+iSource);
            SAXReader sax = new SAXReader();
            sax.setEntityResolver( new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            		if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
            			return new InputSource(getClass().getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
            		} else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
            			return new InputSource(getClass().getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
            		}
            		return null;
            	}
            });
            sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document targetDoc = sax.read(new File(iTarget));
            Document sourceDoc = sax.read(new File(iSource));
            
            merge(targetDoc.getRootElement(), sourceDoc.getRootElement());
            
            if (new File(iTarget).getName().equals("hibernate.cfg.xml")) {
                targetDoc.setDocType(sourceDoc.getDocType()); // Remove DOCTYPE
                Element sessionFactoryElement = targetDoc.getRootElement().element("session-factory");
                Vector<Element> mappings = new Vector<Element>();
                for (Iterator i=sessionFactoryElement.elementIterator("mapping");i.hasNext();) {
                    Element mappingElement = (Element)i.next();
                    mappings.add(mappingElement);
                    sessionFactoryElement.remove(mappingElement);
                }
                for (Iterator i=mappings.iterator();i.hasNext();) {
                    Element mappingElement = (Element)i.next();
                    sessionFactoryElement.add(mappingElement);
                }
            }
            
            FileOutputStream fos = new FileOutputStream(iTarget);
            (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(targetDoc);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
}
