import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class ShowDifferences{
	
	
	
	public static void main(String[] args){
		String file1 = Vvord.getDocx("file1");
		String file2 = Vvord.getDocx("file2");
		
		try{
			Vvord.extractXml(file1, "file1.xml", "word"+File.separator+"document.xml");
		}
		catch(IOException e){
			System.err.println("Error reading document.xml in file " + file1);
			e.printStackTrace();
			System.exit(1);
		}
		try{
			Vvord.extractXml(file2, "file2.xml", "word"+File.separator+"document.xml");
		}
		catch(IOException e){
			System.err.println("Error reading document.xml in file " + file2);
			e.printStackTrace();
			System.exit(1);
		}
		
		String[] arguments = {"-d", "file1.xml", "file2.xml", "diffed.xml"};
		Vvord.merge3dm(arguments);
		
		//readXML("diffed.xml");
		
		//createDocx(file1, "diffed.xml", "diffed.docx");
	}
	
	static void createDocx(String base, String diffedXml, String outputFile){
		ZipFile zipFile = new ZipFile(base);
		Enumeration<?> enu = zipFile.entries();
		InputStream is;
		byte[] buffer = new byte[1024];
		int length;
		
		ZipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
		
		while(enu.hasMoreElements()){
			ZipEntry entry = (ZipEntry)enu.nextElement();
			zos.setMethod(entry.getMethod());
			
			if(entry.getName().equals("word"+File.separator+"document.xml")){
				zos.putNextEntry(new ZipEntry(entry.getName()));
				is = new FileInputStream(diffedXml);
			}
			
		}
	}
	
	static void readXML(String file) throws FileNotFoundException, XMLStreamException{
		InputStream is = new FileInputStream(file);
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
		String characters = null
		
		while(eventReader.hasNext()){
			XMLEvent event = eventReader.nextEvent();
			
			if(event.isStartElement()){
				StartElement startElement = event.asStartElement();
				String name = startElement.getName().getLocalPart();
				if(name.equals("diff:insert")){
					
				}
				else if(name.equals("diff:
			}
		}
	}
	
}
