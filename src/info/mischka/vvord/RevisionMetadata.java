package info.mischka.vvord;

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
	
	final static String[] CONTENT_TYPES = {"application/vnd.openxmlformats-package.relationships+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml", "application/vnd.openxmlformats-package.relationships+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml", "application/vnd.openxmlformats-officedocument.extended-properties+xml", "application/vnd.openxmlformats-package.core-properties+xml", "application/xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml", "application/vnd.openxmlformats-officedocument.theme+xml", "application/vnd.ms-word.stylesWithEffects+xml"};
	final static String[] PART_NAMES = {"/_rels/.rels", "/word/settings.xml", "/word/_rels/document.xml.rels", "/word/fontTable.xml", "/word/styles.xml", "/word/document.xml", "/docProps/app.xml", "/docProps/core.xml", "/word/stylesWithEffects.xml"};
	final static String APP_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";
	final static String CORE_TYPE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
	final static String DOCUMENT_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
	final static String REVISION_HISTORY_TYPE = "http://www.cs.uwm.edu/molhado/revision-history";
	final static int RELS = 0;
	final static int SETTINGS = 1;
	final static int DOCUMENT_XML_RELS = 2;
	final static int FONT_TABLE = 3;
	final static int STYLES = 4;
	final static int DOCUMENT = 5;
	final static int APP = 6;
	final static int CORE = 7;
	final static int CONTENT_TYPE = 8;
	final static int WEB_SETTINGS = 9;
	final static int THEME_1 = 10;
	final static int STYLES_WITH_EFFECTS = 11;
	
	ArrayList<Override> overrides = new ArrayList<Override>();
	ArrayList<Relationship> relationships = new ArrayList<Relationship>();
	ArrayList<Default> defaults = new ArrayList<Default>();
	
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
		defaults.add(new Default("rels", CONTENT_TYPES[RELS]));
		defaults.add(new Default("xml", CONTENT_TYPES[CONTENT_TYPE]));
		defaults.add(new Default("xml~", CONTENT_TYPES[STYLES]));
		defaults.add(new Default("rels~", CONTENT_TYPES[RELS]));
		
		FileOutputStream fos = new FileOutputStream(file);
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(fos);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent endln = eventFactory.createDTD("\n");
		
		eventWriter.add(eventFactory.createStartDocument());
		eventWriter.add(endln);
		
		eventWriter.add(eventFactory.createStartElement("", "", "Types"));
		eventWriter.add(eventFactory.createAttribute("xmlns", "http://schemas.openxmlformats.org/package/2006/content-types"));
		eventWriter.add(endln);
		
		for(Default d:defaults){
			eventWriter.add(eventFactory.createStartElement("", "", "Default"));
			eventWriter.add(eventFactory.createAttribute("Extension", d.extension));
			eventWriter.add(eventFactory.createAttribute("ContentType", d.contentType));
			eventWriter.add(eventFactory.createEndElement("", "", "Default"));
			eventWriter.add(endln);
		}
		
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
	
	void addOverride(String partName){
		int type;
		
		if(partName.contains("rels"))
			type = RELS;
		else if(partName.contains("fontTable.xml"))
			type = FONT_TABLE;
		else if(partName.contains("document.xml"))
			type = DOCUMENT;
		else if(partName.contains("styles.xml"))
			type = STYLES;
		else if(partName.contains("settings.xml"))
			type = SETTINGS;
		else if(partName.contains("webSettings.xml"))
			type = WEB_SETTINGS;
		else if(partName.contains("theme1.xml"))
			type = THEME_1;
		else if(partName.contains("core.xml"))
			type = CORE;
		else if(partName.contains("app.xml"))
			type = APP;
		else if(partName.contains("stylesWithEffects.xml"))
			type = STYLES_WITH_EFFECTS;
		else
			type = CONTENT_TYPE;
		overrides.add(new Override(partName, CONTENT_TYPES[type]));
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
	
	
	void addRelationship(String id, String type, String target){
		relationships.add(new Relationship(id, type, target));
	}
	
	void addRelationship(String id, String target){
		String type = "http://www.cs.uwm.edu/molhado/backup-file";
		relationships.add(new Relationship(id, type, target));
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

class Default{
	String extension, contentType;
	
	public Default(String extension, String contentType){
		this.extension = extension;
		this.contentType = contentType;
	}
}
