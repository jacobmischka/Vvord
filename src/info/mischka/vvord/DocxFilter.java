package info.mischka.vvord;

import java.io.FilenameFilter;
import java.io.File;

class DocxFilter implements FilenameFilter{
	
	public boolean accept(File dir, String name){
		if(name.matches(".+docx"))
			return true;
		else return false;
	}
}
