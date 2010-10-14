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

package org.unitime.banner.ant;

import java.io.File;
import java.io.FileOutputStream;
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
            Document targetDoc = (new SAXReader()).read(new File(iTarget));
            Document sourceDoc = (new SAXReader()).read(new File(iSource));
            
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
