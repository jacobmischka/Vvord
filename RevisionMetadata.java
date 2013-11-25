import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Attribute;


class RevisionMetadata{
	
	final String[] contentTypes = {"application/vnd.openxmlformats-package.relationships+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml", "application/vnd.openxmlformats-package.relationships+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml", "application/vnd.openxmlformats-officedocument.extended-properties+xml", "application/vnd.openxmlformats-package.core-properties+xml"}
	final String[] partNames = {"/_rels/.rels", "/word/settings.xml", "/word/_rels/document.xml.rels", "/word/fontTable.xml", "/word/styles.xml", "/word/document.xml", "/docProps/app.xml", "/docProps/core.xml"}
	final int rels = 0;
	final int settings = 1;
	final int documentXmlRels = 2;
	final int fontTable = 3;
	final int styles = 4;
	final int document = 5;
	final int app = 6;
	final int core = 7;
	
	ArrayList<Override> overrides = new ArrayList<Override>();
	ArrayList<Relationship> relationships = new ArrayList<Relationship>();
	/*
	void readContentTypes(String file){
		InputStream is = new FileInputStream(file);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
		String characters = null;
		
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			
			if(event.isStartElement()){
				
			}
		}
	}
	*/
	
	void writeContentTypes(String file) throws FileNotFoundException, XMLStreamException{
		FileOutputStream fos = new FileOutputStream(file);
		
		XMLOutputFactory outputFactory = XMOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(fos);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent endln = eventFactory.createDTD("\n");
		
		eventWriter.add(eventFactory.createStartDocument());
		eventWriter.add(endln);
		
		eventWriter.add(eventFactory.createStartElement("", "", "Types"));
		eventWriter.add(eventFactory.createAttribute("xmlns", "http://schemas.openxmlformats.org/package/2006/content-types"));
		eventWriter.add(endln);
		
		for(Override o:overrides){
			eventWriter.add(eventFactory.createStartElement("", "", "Override"));
			eventWriter.add(eventFactory.createAttribute("PartName", o.partName));
			eventWriter.add(eventFactory.createAttribute("ContentType", o.contentType));
			eventWriter.add(eventFactory.createEndElement("", "", "Override"));
			eventWriter.add(endln);
			
		}
		eventWriter.add(eventFactory.createEndElement("", "", "Types"));
		eventWriter.add(endln);
		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
	}
	
	void addOverride(String partName, int type){
		overrides.add(new Override(partName, contentTypes[type]));
	}
	
	void writeRels(String file) throws FileNotFoundException, XMLStreamException{
		FileOutputStream fos = new FileOutputStream(file);
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(fos);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent endln = eventFactory.createDTD("\n");
		
		eventWriter.add(eventFactory.createStartDocument());
		eventWriter.add(endln);
		
		eventWriter.add(eventFactory.createStartElement("", "", "Relationships"));
		eventWriter.add(eventFactory.createAttribute("xmlns", "http://schemas.openxmlformats.org/package/2006/relationships"));
		eventWriter.add(endln);
		
		for(Relationship r:relationships){
			eventWriter.add(eventFactory.createStartElement("", "", "Relationship"));
			eventWriter.add(eventFactory.createAttribute("Id", r.id));
			eventWriter.add(eventFactory.createAttribute("Type", r.type));
			eventWriter.add(eventFactory.createAttribute("Target", r.target));
			eventWriter.add(eventFactory.createEndElement("", "", "Relationship"));
			eventWriter.add(endln);
		}
		eventWriter.add(eventFactory.createEndElement("", "", "Relationships"));
		eventWriter.add(endln);
		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
		
		
	}
	
}

class Override{
	String partName;
	String contentType;
	
	public Override(String partName, String contentType){
		this.partName = partName;
		this.contentType = contentType;
	}
}

class Relationship{
	String id;
	String type;
	String target;
	
	public Relationship(String id, String type, String target){
		this.id = id;
		this.type = type;
		this.target = target;
	}
}
