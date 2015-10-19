
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*****************************************************************************
 * 
 * Copyright (c) 2013-2015	GeekMouse Game
 * Copyright (c) 2015		Liqing Pan
 *
 *****************************************************************************/

public class entrance {
	public static String mDefaultOutputPath;
	public static String mDefaultOutputExt;
	
	
	public static String version(){return "0.9";}
	public static String CopyrightString(){return "Copyright (c) 2013-2015 GeekMouse Game\nCopyright (c) 2015 Liqing Pan\n";}
	
	
	public static void main(String[] args){
		FXTools.init();
		FXTools.LOGGER.fine("=====FleX2Json ver "+version()+"=====");
		FXTools.LOGGER.fine(CopyrightString());
		FXTools.LOGGER.fine("Current path:"+System.getProperty("user.dir"));
		processConvert();
	}

	
	public static void processConvert(){
		//----Read configurations----
		try {
			File file=new File("config.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			try {
				Document doc;
				doc = db.parse(file);
				doc.getDocumentElement().normalize();
				//Read default settings
				Element defaultNode=(Element) doc.getElementsByTagName("defaults").item(0);
				Element defaultOutputNode=(Element) defaultNode.getElementsByTagName("output").item(0);
				mDefaultOutputExt=defaultOutputNode.getElementsByTagName("extention").item(0).getTextContent();
				if (mDefaultOutputExt.charAt(0)!='.') {
					mDefaultOutputExt="."+mDefaultOutputExt;
				}
				
				mDefaultOutputPath=defaultOutputNode.getElementsByTagName("path").item(0).getTextContent();
				
				//Read Files
				Element sourceNode=(Element) doc.getElementsByTagName("source").item(0);
				NodeList fileList=sourceNode.getElementsByTagName("file");
				for (int i=0;i<fileList.getLength();i++){
					Element fileNode=(Element) fileList.item(i);
					FXSourceFile sourceFile=new FXSourceFile(fileNode);
					Boolean result=sourceFile.convert();
					if (false/*!result*/) {
						FXTools.LOGGER.warning("converting abort in file:"+fileNode.getAttribute("name"));
						return;
					}
				}
				
				
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
	
	
	
	
	
}
