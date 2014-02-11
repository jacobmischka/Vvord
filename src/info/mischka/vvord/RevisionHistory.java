package info.mischka.vvord;

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Attribute;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;



class RevisionHistory{
	String current;
	ArrayList<Revision> revisions;
	
	public RevisionHistory(){
		revisions = new ArrayList<Revision>();
	}
	
	

	String printXML(){
		return null;
	}
	
	void readXML(String file) throws FileNotFoundException, XMLStreamException{
		InputStream is = new FileInputStream(file);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
		String characters = null;
		Revision revision = null;
		
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			
			if(event.isStartElement()){
				StartElement startElement = event.asStartElement();
				String name = startElement.getName().getLocalPart();
				if(name.equals("revision")){
					revision = new Revision();
					Iterator<Attribute> itr = startElement.getAttributes();
					while(itr.hasNext()){
						Attribute attribute = itr.next();
						if(attribute.getName().toString().equals("id")){
							revision.id = attribute.getValue();
						}
						else if(attribute.getName().toString().equals("location"))
							revision.location = attribute.getValue();
						else if(attribute.getName().toString().equals("author"))
							revision.author = attribute.getValue();
						else if(attribute.getName().toString().equals("timestamp"))
							revision.timestamp = attribute.getValue();
					}
				}
				else if(name.equals("revision-history")){
					Iterator<Attribute> itr = startElement.getAttributes();
					while(itr.hasNext()){
						Attribute attribute = itr.next();
						if(attribute.getName().toString().equals("current"))
							current = attribute.getValue();
					}
				}
				else if(name.equals("parent")){
					Iterator<Attribute> itr = startElement.getAttributes();
					while(itr.hasNext()){
						Attribute attribute = itr.next();
						if(attribute.getName().toString().equals("id"))
							revision.parents.add(attribute.getValue());
					}
				}
			}
			else if(event.isEndElement()){
				EndElement element = event.asEndElement();
				String name = element.getName().getLocalPart();
				if(name.equals("revision"))
					add(revision);
				else if(name.equals("comments"))
					revision.comments = characters;
				
			}
			else if(event.isCharacters())
				characters = event.asCharacters().getData();
		}
	}
	
	void writeXML(String file) throws FileNotFoundException, XMLStreamException{
		FileOutputStream fos = new FileOutputStream(file);
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(fos);
		
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeDTD("\n");
		
		writer.writeStartElement("revision-history");
		writer.writeAttribute("current", current);
		writer.writeAttribute("xmlns", "http://www.cs.uwm.edu/molhado/revision-history");
		writer.writeDTD("\n");
		
		for(Revision r:revisions){
			writer.writeStartElement("revision"); //add attributes
			writer.writeAttribute("id", r.id);
			writer.writeAttribute("author", r.author);
			writer.writeAttribute("timestamp", r.timestamp);
			writer.writeAttribute("location", r.location);
			writer.writeDTD("\n");
			
			for(String p:r.parents){
				writer.writeEmptyElement("parent"); //add attributes
				
				writer.writeAttribute("id", p);;
				writer.writeDTD("\n");
			}
			
			writer.writeStartElement("comments");
			writer.writeCharacters(r.comments);
			writer.writeEndElement();
			writer.writeDTD("\n");
			writer.writeEndElement();
			writer.writeDTD("\n");
		}
		
		writer.writeEndElement();
		writer.writeDTD("\n");
		
		writer.writeEndDocument();
		writer.close();
	}
	
	Revision getRevision(String id){
		for(Revision r:revisions){
			if(r.id.equals(id))
				return r;
		}
		return null;
	}
	
	void add(Revision revision){
		revisions.add(revision);
	}
}

class Revision{
	String id, location, comments, author, timestamp;
	ArrayList<String> parents;
	
	Revision(){
		parents = new ArrayList<String>();
	}
	
	Revision(String id){
		this.id = id;
		parents = new ArrayList<String>();
	}
	
}
