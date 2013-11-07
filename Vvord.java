import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Enumeration;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.awt.FileDialog;
import javax.swing.JFrame;
import tdm.tool.TreeDiffMerge;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import javax.xml.stream.XMLStreamException;

public class Vvord{
	//TODO: updateRevisionHistory, update Content_Type.xml, grab base from revision-history.xml, replace /s with File.separators, tons of error checking, documentation
	
	static JFrame frame;
	static FileDialog browser;
	static long startTime, endTime;
	static String docxId;

	public static void main(String[] args){
	startTime = System.nanoTime();
	frame = new JFrame();
	
	String base = getDocx("base");
	String branch1 = getDocx("branch1");
	String branch2 = getDocx("branch2");
	
	extractXml(base, "base.xml", "word/document.xml");
	extractXml(base, "baseRevisionHistory.xml", "history/revision-history.xml");
	
	extractXml(branch1, "branch1.xml", "word/document.xml");
	extractXml(branch1, "branch1RevisionHistory.xml", "history/revision-history.xml");
	
	extractXml(branch2, "branch2.xml", "word/document.xml");
	extractXml(branch2, "branch2RevisionHistory.xml", "history/revision-history.xml");
	
	String[] arguments = {"-m", "base.xml", "branch1.xml", "branch2.xml", "document.xml"};
	merge3dm(arguments);
	String[] arguments2 = {"-m", "baseRevisionHistory.xml", "branch1RevisionHistory.xml", "branch2RevisionHistory.xml", "revision-history.xml"};
	merge3dm(arguments2);
	
	createDocx(base, branch1, branch2);
	endTime = System.nanoTime();
	System.out.println("Completion time: " + ((endTime-startTime)/1000000000.0) + " seconds.");
	
	System.exit(0);
	}
	
	static String getDocx(String type){
		
		FileDialog browser = new FileDialog(frame, "Select the " + type + " .docx file");
		browser.setFilenameFilter(new DocxFilter());
		browser.show();
		
		return (browser.getDirectory()+browser.getFile());
		
		
	}
	
	static File extractXml(String name, String outputName, String entryName){
		
		try{
			ZipFile docx = new ZipFile(name);
			ZipEntry document = docx.getEntry(entryName);
			File file = new File(outputName);
			
			InputStream is = docx.getInputStream(document);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while((length = is.read(buffer)) >= 0)
				fos.write(buffer, 0, length);
			is.close();
			fos.close();

			return file;
		}
		catch(IOException e){
			System.err.println("Error extracting " + name);
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
	
	static void merge3dm(String[] args){
		
		try{
			TreeDiffMerge.main(args);
		}
		catch(IOException e){
			System.err.println("Error running 3dm");
		}
	}
	
	static void updateRevisionHistory(String revisionHistoryXml, String base, String branch1, String branch2){
		//need to: change current, add revision, combine history of branches
		//might need to manually combine histories
		try{
			InputStream is = new FileInputStream(revisionHistoryXml);
			RevisionHistory revisionHistory = new RevisionHistory();
			revisionHistory.readXML(revisionHistoryXml);
			String id = UUID.randomUUID().toString();
			Revision currentRevision = new Revision(id);
			
			String computerName = InetAddress.getLocalHost().getHostName();
			currentRevision.author = "Author Name@"+computerName; //get passed as argument
			Calendar cal = Calendar.getInstance();
			currentRevision.timestamp = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DATE)+"T"+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
			
			RevisionHistory baseRevisionHistory = new RevisionHistory();
			baseRevisionHistory.readXML(base);
			currentRevision.parents.add(baseRevisionHistory.current);
			RevisionHistory branch1RevisionHistory = new RevisionHistory();
			branch1RevisionHistory.readXML(branch1);
			currentRevision.parents.add(branch1RevisionHistory.current);
			RevisionHistory branch2RevisionHistory = new RevisionHistory();
			branch2RevisionHistory.readXML(branch2);
			currentRevision.parents.add(branch2RevisionHistory.current);
			
			revisionHistory.add(currentRevision);
			revisionHistory.current = id;
			
			revisionHistory.writeXML(revisionHistoryXml);
			
		}
		catch(FileNotFoundException e){
			System.err.println("Existing revision-history.xml file not found.");
		}
		catch(XMLStreamException e){
			e.printStackTrace();
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	static void createVersionDetails(){ //deprecated, can remove
		try{
			String computerName = InetAddress.getLocalHost().getHostName();
			Calendar cal = Calendar.getInstance();
			FileWriter writer = new FileWriter("version-details.xml");
			writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><version computername=\""+computerName+"\" timestamp=\""+cal.getTimeInMillis()+"\" />");
		}
		catch(UnknownHostException e){
			e.printStackTrace();
			System.exit(1);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		

	}
	
	static void extractHistory(String docxName, String outputName, String historyName){
		try{
			ZipFile docx = new ZipFile(docxName);
			Enumeration<?> enu = docx.entries();
			InputStream is;
			File revisionFolder = new File(outputName);
			File revisionHistory = new File(historyName);
			FileOutputStream fos;
			byte[] buffer = new byte[1024];
			int length;
			
			while(enu.hasMoreElements()){
				ZipEntry entry = (ZipEntry)enu.nextElement();
				is = docx.getInputStream(entry);
				String name = entry.getName();
				File file = new File(name);
				if(name.endsWith("/"))
					file.mkdirs();
				
				File parent = file.getParentFile();
				if(parent != null){
					parent.mkdirs();
				}
				
				if(entry.getName().startsWith("history")){
					fos = new FileOutputStream(docxName.substring(0, docxName.indexOf("."))+file.getPath()+"history");
					
				}
				else{
					fos = new FileOutputStream(docxName.substring(0, docxName.indexOf("."))+file.getPath());
				}

				while((length = is.read(buffer)) >= 0){
					fos.write(buffer, 0, length);
				}
				is.close();
				fos.close();
			}
			
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	static void createDocx(String base, String branch1, String branch2){
		try{
			ZipFile docxFile = new ZipFile(base);
			ZipFile branch1Docx = new ZipFile(branch1);
			ZipFile branch2Docx = new ZipFile(branch2);
			Enumeration<?> enu = docxFile.entries();
			Enumeration<?> branch1enu = branch1Docx.entries();
			Enumeration<?> branch2enu = branch2Docx.entries();
			InputStream is;
			byte[] buffer = new byte[1024];
			int length;
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("merged.docx"));
			
			while(enu.hasMoreElements()){
				
				ZipEntry entry = (ZipEntry)enu.nextElement();
				zos.setMethod(entry.getMethod());
				
				if(entry.getName().equals("word/document.xml")){
					zos.putNextEntry(new ZipEntry(entry.getName()));
					is = new FileInputStream("document.xml"); 
					System.out.println(entry);
				}
				else if(entry.getName().equals("history/revision-history.xml")){
					zos.putNextEntry(new ZipEntry(entry.getName()));
					updateRevisionHistory("revision-history.xml", "baseRevisionHistory.xml", "branch1RevisionHistory.xml", "branch2RevisionHistory.xml");
					is = new FileInputStream("revision-history.xml");
					System.out.println(entry);
				}
				else if(entry.getName().startsWith("history")){
					is = docxFile.getInputStream(entry);
				}
				else{
					zos.putNextEntry(entry);
					is = docxFile.getInputStream(entry);
					System.out.println(entry);
				}

				while((length = is.read(buffer)) >= 0){
					zos.write(buffer, 0, length);
				}
				is.close();
				zos.closeEntry();
			}
			// Deprecated, can remove
			//zos.putNextEntry(new ZipEntry("revision-details.xml"));
			//is = new FileInputStream("revision-details.xml");
			//while((length = is.read(buffer)) >= 0){
			//	zos.write(buffer, 0, length);
			//}
			zos.closeEntry();
			zos.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			System.exit(1);
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
				
	}
	
}
