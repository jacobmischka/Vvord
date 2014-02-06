import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
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
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import tdm.tool.TreeDiffMerge;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.lang.ClassNotFoundException;

public class Vvord{
	//TODO: grab base from history if existing, try to get working on osx, get rid of temp files or put them somewhere better, kill when cancel is selected, test a lot using molhado tool
	// extractHistory from branch files if needed or else get rid of that method
	
	static JFrame frame;
	static FileDialog browser;
	static long startTime, endTime;
	static String docxId, baseId, branch1Id, branch2Id;
	static RevisionMetadata contentTypes;
	static String authorName;
	static String baseLocation;

	public static void main(String[] args){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){
			System.err.println("System look and feel not found or identified.");
		}
		frame = new JFrame();		

		String branch1 = getDocx("branch1"); //get branch1
		if(branch1 == null){
			JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		String branch2 = getDocx("branch2"); //get branch2
		if(branch2 == null){
			JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		try{ //extract branch1 files
			extractXml(branch1, "branch1.xml", "word/document.xml");
		}
		catch(IOException e){
			System.err.println("Error reading document.xml in file " + branch1);
			e.printStackTrace();
			System.exit(1);		
		}
		try{
			extractXml(branch1, "branch1RevisionHistory.xml", "history/revision-history.xml");
		}
		catch(IOException e){
			System.out.println("No revision-history.xml found in file " + branch1);
		}
		catch(NullPointerException e){
			System.out.println("No revision-history.xml found in file " + branch1);
		}
			
		try{ //extract branch2 files
			extractXml(branch2, "branch2.xml", "word/document.xml");
		}
		catch(IOException e){
			System.err.println("Error reading document.xml in file " + branch2);
			e.printStackTrace();
			System.exit(1);		
		}
		try{
			extractXml(branch2, "branch2RevisionHistory.xml", "history/revision-history.xml");
		}
		catch(IOException e){
			System.out.println("No revision-history.xml found in file " + branch2);
		}
		catch(NullPointerException e){
			System.out.println("No revision-history.xml found in file " + branch2);
		}
		
		
		
		Revision baseRevision = findSharedBase("branch1RevisionHistory.xml", "branch2RevisionHistory.xml"); //attempts to find a base within the histories of the branches
		baseLocation = "";
		String base;
		if(baseRevision == null){ //no shared base found
			//make a dialog saying no base was found and to select one or try a straight blank one maybe
			JOptionPane.showMessageDialog(null, "No common base revision found, please select one", "No base found", JOptionPane.WARNING_MESSAGE);
			base = getDocx("base");
			if(base == null){
				JOptionPane.showMessageDialog(null, "Please select a docx file", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}	
		}
		else{ //shared base found
			base = branch1; //uses the xml files found in branch1's history
			baseLocation = baseRevision.location;
		}
		
		
		
		String outputName = JOptionPane.showInputDialog("Enter the filename for the merged document"); //get input for merged document's filename
		if(outputName.trim().equals("") || outputName == null){
			JOptionPane.showMessageDialog(null, "Please enter a filename for the merged document.", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if(!outputName.endsWith(".docx"))
			outputName += ".docx";
			
		if(args.length > 0) //uses first argument as author name if specified
			authorName = args[0];
		else
			authorName = JOptionPane.showInputDialog("Please enter an author name."); //if no arguments found, prompts user for author name
			
		if(authorName == null || authorName.trim().equals("")) //if user does not enter author name, defaults to "Author Name" for now
			authorName = "Author Name";
			
		String comments = "";
		if(args.length > 1) //uses second argument as comments if specified
			comments = args[1];
		else
			comments = JOptionPane.showInputDialog("Would you like to enter a comment for this merge?"); //if no arguments found, prompts user for comments
		
		
		
		startTime = System.currentTimeMillis();
		
		try{
			extractXml(base, "base.xml", baseLocation+"word/document.xml");
		}
		catch(IOException e){
			System.err.println("Error reading document.xml in file " + base);
			e.printStackTrace();
			System.exit(1);
		}
		try{
			extractXml(base, "baseRevisionHistory.xml", baseLocation+"history/revision-history.xml");
		}
		catch(IOException e){
			System.out.println("No revision-history.xml found in file " + base);
		}
		catch(NullPointerException e){
			System.out.println("No revision-history.xml found in file " + base);
		}
		
		
		
		String[] arguments = {"-m", "base.xml", "branch1.xml", "branch2.xml", "document.xml"};
		merge3dm(arguments); //calls 3dm to merge
		
		updateRevisionHistory("revision-history.xml", "baseRevisionHistory.xml", "branch1RevisionHistory.xml", "branch2RevisionHistory.xml", comments);
		createDocx(base, branch1, branch2, outputName);
		endTime = System.currentTimeMillis();
		System.out.println("Completion time: " + ((endTime-startTime)/1000.0) + " seconds.");
		
		System.exit(0);
	}
	
	static String getDocx(String type){
		//Presents file browser that prompts user for a .docx file
		
		FileDialog browser = new FileDialog(frame, "Select the " + type + " .docx file");
		browser.setFilenameFilter(new DocxFilter());
		browser.setVisible(true);
		
		if(browser.getFile() == null)
			return null;
		else
			return (browser.getDirectory()+browser.getFile());
		
		
	}
	
	static File extractXml(String name, String outputName, String entryName) throws IOException{
		//extracts an XML file "entryName" in the docx file "name" and writes it to XML file "outputName" in current directory as of now
		
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
	
	static void merge3dm(String[] args){
		//calls 3DM TreeDiffMerge to merge the XML files specified in args
		
		try{
			TreeDiffMerge.main(args);
		}
		catch(IOException e){
			System.err.println("Error running 3dm");
		}
	}
	
	static void updateRevisionHistory(String revisionHistoryXml, String base, String branch1, String branch2, String comments){
		//Creates an XML revision history file revisionHistoryXml that documents the 
		
		RevisionHistory revisionHistory = new RevisionHistory();
		String id = UUID.randomUUID().toString();
		Revision currentRevision = new Revision(id);
		
		String computerName;
		
		try{
			computerName = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e){
			e.printStackTrace();
			computerName = "Unknown";
		}
		
		currentRevision.author = authorName+"@"+computerName;
		currentRevision.location = "/history/"+currentRevision.id; 
		currentRevision.comments = comments;
		Calendar cal = Calendar.getInstance();
		currentRevision.timestamp = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DATE)+"T"+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
		
		
		try{	
			RevisionHistory baseRevisionHistory = new RevisionHistory();
			baseRevisionHistory.readXML(base);
			baseId = baseRevisionHistory.current;
			baseRevisionHistory.writeXML("testBaseRevisionHistory.xml");
			
			for(int i = 0; i < baseRevisionHistory.revisions.size(); i++){
				if(!revisionHistory.revisions.contains(baseRevisionHistory.revisions.get(i))){
					revisionHistory.add(baseRevisionHistory.revisions.get(i));
				}
			}
		}
		catch(FileNotFoundException e){
			System.err.println("Existing revision-history.xml file not found in file " + base);
			baseId = UUID.randomUUID().toString();
		}
		catch(XMLStreamException e){
			e.printStackTrace();
		}
		
		currentRevision.parents.add(baseId);
		
		
		try{
			RevisionHistory branch1RevisionHistory = new RevisionHistory();
			branch1RevisionHistory.readXML(branch1);
			branch1Id = branch1RevisionHistory.current;
			for(int i = 0; i < branch1RevisionHistory.revisions.size(); i++){
				if(!revisionHistory.revisions.contains(branch1RevisionHistory.revisions.get(i))){
					revisionHistory.add(branch1RevisionHistory.revisions.get(i));
				}
			}
		}
		catch(FileNotFoundException e){
			System.err.println("Existing revision-history.xml file not found in file " + branch1);
			branch1Id = UUID.randomUUID().toString();
		}
		catch(XMLStreamException e){
			e.printStackTrace();
		}
		
		currentRevision.parents.add(branch1Id);
		
		
		
		try{
			RevisionHistory branch2RevisionHistory = new RevisionHistory();
			branch2RevisionHistory.readXML(branch2);
			branch2Id = branch2RevisionHistory.current;
			for(int i = 0; i < branch2RevisionHistory.revisions.size(); i++){
				if(!revisionHistory.revisions.contains(branch2RevisionHistory.revisions.get(i))){
					revisionHistory.add(branch2RevisionHistory.revisions.get(i));
				}
			}
		}
		catch(FileNotFoundException e){
			System.err.println("Existing revision-history.xml file not found in file " + branch2);
			branch2Id = UUID.randomUUID().toString();
		}
		catch(XMLStreamException e){
			e.printStackTrace();
		}
		
		currentRevision.parents.add(branch2Id);
		
		
		revisionHistory.add(currentRevision);
		revisionHistory.current = id;
		
		try{
			revisionHistory.writeXML(revisionHistoryXml);
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(XMLStreamException e){
			e.printStackTrace();
		}
	}
	
	static void extractHistory(String docxName, String outputName, String historyName){
		try{
			ZipFile docx = new ZipFile(docxName);
			ZipEntry entry;
			Enumeration<?> enu = docx.entries();
			InputStream is;
			File revisionFolder = new File(outputName);
			File revisionHistory = new File(historyName);
			FileOutputStream fos;
			byte[] buffer = new byte[1024];
			int length;
			
			while(enu.hasMoreElements()){
				ZipEntry oldEntry = (ZipEntry)enu.nextElement();
				entry = new ZipEntry(oldEntry.getName());
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
	
	static void writeEntry(ZipEntry entry, InputStream is, ZipOutputStream zos) throws FileNotFoundException, IOException{
		byte[] buffer = new byte[1024];
		int length;
		System.out.println(entry);
		if(!entry.getName().equals("[Content_Types].xml"))
			contentTypes.addOverride("/"+entry.getName());
		if(entry.getName().startsWith("history"))
			contentTypes.addRelationship(UUID.randomUUID().toString(), entry.getName().substring(entry.getName().indexOf("/")+1));
						
		while((length = is.read(buffer)) >= 0){
			zos.write(buffer, 0, length);
		}
		is.close();
		zos.closeEntry();
	}
	
	
	static void createDocx(String base, String branch1, String branch2, String outputFile){
		//creates the new docx file 
		try{
			ZipFile docxFile = new ZipFile(base);
			ZipFile branch1Docx = new ZipFile(branch1);
			ZipFile branch2Docx = new ZipFile(branch2);
			Enumeration<?> enu = docxFile.entries();
			Enumeration<?> branch1enu = branch1Docx.entries();
			Enumeration<?> branch2enu = branch2Docx.entries();
			InputStream is;
			contentTypes = new RevisionMetadata();
			ZipEntry entry;
			ZipEntry oldEntry;
			
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));
			
			while(enu.hasMoreElements()){
				
				oldEntry = (ZipEntry)enu.nextElement();
				String entryName = oldEntry.getName();
				if(baseLocation != "")
					entryName = entryName.replace(baseLocation, "");
					
				entry = new ZipEntry(entryName);
				
				if(!entry.getName().equals("history/revision-history.xml")){
				
					
					//zos.setMethod(oldEntry.getMethod());
					
					if(entry.getName().equals("word/document.xml")){
						zos.putNextEntry(new ZipEntry(entry.getName()));
						is = new FileInputStream("document.xml"); 
						writeEntry(entry, is, zos);
					}
					
					else if(entry.getName().startsWith("history")){
						try{ //check to see if already exists in history
							zos.putNextEntry(entry);
							is = docxFile.getInputStream(entry);
							writeEntry(entry, is, zos);
						}
						catch(ZipException e){
							e.printStackTrace();
						}
					}
					else if(entry.getName().equals("_rels/.rels")){
						entry = new ZipEntry("_rels/.rels");
						RevisionMetadata rels = new RevisionMetadata();
						rels.addRelationship("rId3", RevisionMetadata.APP_TYPE, "docProps/app.xml");
						rels.addRelationship("rId2", RevisionMetadata.CORE_TYPE, "docProps/core.xml");
						rels.addRelationship("rId1", RevisionMetadata.DOCUMENT_TYPE, "word/document.xml");
						rels.addRelationship("revisionHistory", RevisionMetadata.REVISION_HISTORY_TYPE, "history/revision-history.xml");
						rels.writeRels(".rels");
						is = new FileInputStream(".rels");
						zos.putNextEntry(entry);
						writeEntry(entry, is, zos);
					}
					else if(entry.getName().equals("[Content_Types].xml")){

					}
					else{
						zos.putNextEntry(entry);
						is = docxFile.getInputStream(entry);
						writeEntry(entry, is, zos);
					}

					
					
					if(!entry.getName().startsWith("history")){
						is = docxFile.getInputStream(entry);
						entry = new ZipEntry("history/"+baseId+"/"+entry.getName()+"~");
						if(entry.getName().contains("[")){
							entry = new ZipEntry(entry.getName().replace("[", "%5B").replace("]", "%5D"));
						}
						zos.putNextEntry(entry);
						writeEntry(entry, is, zos);
					}
				}
			}

			while(branch1enu.hasMoreElements()){
				oldEntry = (ZipEntry)branch1enu.nextElement();
				entry = new ZipEntry(oldEntry.getName());
				//zos.setMethod(oldEntry.getMethod());
				is = branch1Docx.getInputStream(entry);
				if(!entry.getName().startsWith("history"))
					entry = new ZipEntry("history/"+branch1Id+"/"+entry.getName()+"~");
				if(entry.getName().contains("[")){
					entry = new ZipEntry(entry.getName().replace("[", "%5B").replace("]", "%5D"));
				}
				try{
					zos.putNextEntry(entry);
					writeEntry(entry, is, zos);
				}
				catch(ZipException e){
					e.printStackTrace();
				}
			}
			
			while(branch2enu.hasMoreElements()){
				oldEntry = (ZipEntry)branch2enu.nextElement();
				entry = new ZipEntry(oldEntry.getName());
				//zos.setMethod(oldEntry.getMethod());
				is = branch2Docx.getInputStream(entry);
				if(!entry.getName().startsWith("history"))
					entry = new ZipEntry("history/"+branch2Id+"/"+entry.getName()+"~");
				if(entry.getName().contains("[") || entry.getName().contains("]")){
					entry = new ZipEntry(entry.getName().replace("[", "%5B").replace("]", "%5D"));
				}
				try{
					zos.putNextEntry(entry);
					writeEntry(entry, is, zos);
				}
				catch(ZipException e){
					e.printStackTrace();
				}
			}
			
			entry = new ZipEntry("history/revision-history.xml");
			is = new FileInputStream("revision-history.xml");
			zos.putNextEntry(entry);
			writeEntry(entry, is, zos);
			
			entry = new ZipEntry("history/_rels/revision-history.xml.rels");
			zos.putNextEntry(entry);
			contentTypes.writeRels("revision-history.xml.rels");
			is = new FileInputStream("revision-history.xml.rels");
			writeEntry(entry, is, zos);
			
			entry = new ZipEntry("[Content_Types].xml");
			zos.putNextEntry(entry);
			contentTypes.writeContentTypes("content_types.xml");
			is = new FileInputStream("content_types.xml");
			writeEntry(entry, is, zos);
			
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
		catch(XMLStreamException e){
			e.printStackTrace();
			System.exit(1);
		}
				
	}
	
	static Revision findSharedBase(String branch1, String branch2){
		
		try{
			RevisionHistory branch1RevisionHistory = new RevisionHistory();
			branch1RevisionHistory.readXML(branch1);

			RevisionHistory branch2RevisionHistory = new RevisionHistory();
			branch2RevisionHistory.readXML(branch2);
						
				
			if(!branch1RevisionHistory.revisions.isEmpty() && !branch2RevisionHistory.revisions.isEmpty()){
				ArrayList<Revision> sharedRevisions = new ArrayList<Revision>();
				
				for(int i = 0; i < branch1RevisionHistory.revisions.size(); i++){
					for(int j = 0; j < branch2RevisionHistory.revisions.size(); j++){
						if(branch1RevisionHistory.revisions.get(i).id.equals(branch2RevisionHistory.revisions.get(j).id))
							sharedRevisions.add(branch1RevisionHistory.revisions.get(i)); //gonna need to actually look at parents
					}
				}
				
				if(!sharedRevisions.isEmpty()){
					Revision mostRecent = sharedRevisions.get(0);
					
					for(int i = 0; i < sharedRevisions.size(); i++){
						if(sharedRevisions.get(i).timestamp.compareTo(mostRecent.timestamp) > 0)
							mostRecent = sharedRevisions.get(i);
					}
				
					return mostRecent;
				}
				
				return null;
			}
		}
		catch(FileNotFoundException e){
			System.err.println("Existing revision-history.xml file not found in both branches");
			return null;
		}
		catch(XMLStreamException e){
			e.printStackTrace();
			return null;
		}
		
		return null;
		
	}
	
}
