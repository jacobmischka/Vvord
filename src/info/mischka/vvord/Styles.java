package info.mischka.vvord;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Attribute;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

class Styles{
	static ArrayList<String> styleIds = new ArrayList<String>();
	
	static void mergeStyles(File infile1, File infile2, File outfile) throws XMLStreamException, FileNotFoundException{
		InputStream is = new FileInputStream(infile1);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
		String characters = null;
		String styleId = null;
		
		FileOutputStream fos = new FileOutputStream(outfile);
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(fos);
		
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			
			if(event.isStartElement()){
				StartElement startElement = event.asStartElement();
				String name = startElement.getName().getLocalPart();
				if(name.equals("style")){
					Iterator<Attribute> itr = startElement.getAttributes();
					while(itr.hasNext()){
						Attribute attribute = itr.next();
						if(attribute.getName().getLocalPart().equals("styleId")){
							styleId = attribute.toString();
							
							if(styleIds.contains(styleId)){
								System.out.println("styles.xml already contains styleId " + styleId );
							}
							else{
								styleIds.add(styleId);
								boolean endNotFound = true;
								while(endNotFound){
									eventWriter.add(event);
									event = eventReader.nextEvent();
									if(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("style")){
										eventWriter.add(event);
										endNotFound = false;
									}
								}
							}
						}
					}
				}
				else if(name.equals("styles")){
					eventWriter.add(event);
				}
			}
			else if(event.isStartDocument()){
				eventWriter.add(event);
			}
		}
		
		is = new FileInputStream(infile2);
		inputFactory = XMLInputFactory.newInstance();
		eventReader = inputFactory.createXMLEventReader(is);
		
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			
			if(event.isStartElement()){
				StartElement startElement = event.asStartElement();
				String name = startElement.getName().getLocalPart();
				if(name.equals("style")){
					Iterator<Attribute> itr = startElement.getAttributes();
					while(itr.hasNext()){
						Attribute attribute = itr.next();
						if(attribute.getName().getLocalPart().equals("styleId")){
							styleId = attribute.toString();
							
							if(styleIds.contains(styleId)){
								System.out.println("styles.xml already contains styleId " + styleId );
							}
							else{
								styleIds.add(styleId);
								boolean endNotFound = true;
								while(endNotFound){
									eventWriter.add(event);
									event = eventReader.nextEvent();
									if(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("style")){
										eventWriter.add(event);
										endNotFound = false;
									}
								}
							}
						}
					}
				}
			}
			else if(event.isEndElement()){
				EndElement endElement = event.asEndElement();
				String name = endElement.getName().getLocalPart();
				System.out.println(name);
				
				if(name.equals("styles"))
					eventWriter.add(event);
			}
			else if(event.isEndDocument()){
				eventWriter.add(event);
			}
		}
		
	}
}
