
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
enum TypeInputFile{
	INPUT_TYPE_EXCEL97,
	INPUT_TYPE_EXCEL07,
	INPUT_TYPE_CSV,
	INPUT_TYPE_Unkown
}

enum TypeOutputFormat{
	FORMAT_JSON,
	FORMAT_XML,
	FORMAT_Unkown
}

public class entrance {
//	public static final int FORMAT_JSON=0;
//	public static final int FORMAT_XML=1;
//	
//	public static final int INPUT_TYPE_EXCEL97=0;
//	public static final int INPUT_TYPE_EXCEL07=1;
//	public static final int INPUT_TYPE_CSV=2;
	
	public static String mDefaultOutputPath;
	public static String mDefaultOutputExt;
	public static String mDefaultOutputExtXML;
	public static TypeOutputFormat mDefaultOutputFormat;
	
	public static String version(){return "0.91";}
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
				mDefaultOutputExt=defaultOutputNode.getElementsByTagName("extension").item(0).getTextContent();
				if (mDefaultOutputExt.charAt(0)!='.') {
					mDefaultOutputExt="."+mDefaultOutputExt;
				}
				
				mDefaultOutputExtXML=defaultOutputNode.getElementsByTagName("extension_xml").item(0).getTextContent();
				if (mDefaultOutputExtXML.charAt(0)!='.') {
					mDefaultOutputExtXML="."+mDefaultOutputExtXML;
				}
				
				
				
				String formatString=defaultOutputNode.getElementsByTagName("format").item(0).getTextContent();
				mDefaultOutputFormat=FXTools.retrieveFormat(formatString);
						
				mDefaultOutputPath=defaultOutputNode.getElementsByTagName("path").item(0).getTextContent();
				
				//Read Files
				Element sourceNode=(Element) doc.getElementsByTagName("source").item(0);
				NodeList fileList=sourceNode.getElementsByTagName("file");
				for (int i=0;i<fileList.getLength();i++){
					Element fileNode=(Element) fileList.item(i);
					FXSourceFile sourceFile=new FXSourceFile(fileNode);
					sourceFile.convert();
//					if (false/*!result*/) {
//						FXTools.LOGGER.warning("converting abort in file:"+fileNode.getAttribute("name"));
//						return;
//					}
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
