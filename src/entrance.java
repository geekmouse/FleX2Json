
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
	public static String mDefaultOutputPath;
	public static String mDefaultOutputExt=".json";
	public static String mDefaultOutputExtXML=".xml";
	public static TypeOutputFormat mDefaultOutputFormat=TypeOutputFormat.FORMAT_JSON;
	
	public static String version(){return "0.92";}
	public static String CopyrightString(){return "Copyright (c) 2013-2015 GeekMouse Game\nCopyright (c) 2015 Liqing Pan\n";}
	
	//Parameter Related
	static String mParaInputFileString;
	static String mParaSheetString;
	static boolean bParaInput=false;
	static boolean bParaSheet=false;
	static boolean bParaOutput=false;
	
	public static void main(String[] args){
		FXTools.init();
		FXTools.LOGGER.fine("=====FleX2Json ver "+version()+"=====");
		FXTools.LOGGER.fine(CopyrightString());
		FXTools.LOGGER.fine("Current path:"+System.getProperty("user.dir"));
		
		if (args.length==0) {// Read From configure file
			FXTools.LOGGER.fine("Read configuration from config.xml...");
			processConvert();
		}
		if (args.length<0) {//Invalid input parameters
			
			FXTools.LOGGER.severe("Number of input parameters cannot be less than 3:\n1st:\tInput file name\n 2ed:\tInput sheet name\n 3rd:\tOutput path\nAbort...\n");
			return;
		}
		else{
			FXTools.LOGGER.fine("Read configuration from parameters...");


			//Read parameters:
			for (int i = 0; i < args.length; i++) {
				if(!processParameter(args[i])){
					return;
				}
			}
			
			if (!bParaInput) {
				FXTools.LOGGER.severe("Missing input file (eg. -i=your/path/source.xlsx).\nAbort");
				return;
			}
			if (!bParaSheet) {
				FXTools.LOGGER.severe("Missing input sheet name (eg. -s=sheetName).\nAbort");
				return;
			}
			if (!bParaOutput) {
				FXTools.LOGGER.severe("Missing output path (eg. -o=your/output/path).\nAbort");
				return;
			}
			
			FXSourceFile sourceFile=new FXSourceFile(mParaInputFileString,mParaSheetString,mDefaultOutputPath,mDefaultOutputFormat,mDefaultOutputExt);
			sourceFile.convert();
		}
	}
	
	public static boolean processParameter(String pParameter){
		//InputPath
		int indexInput=pParameter.indexOf("-i=");
		if (indexInput!=-1) {
			mParaInputFileString=pParameter.substring(3);
			FXTools.LOGGER.fine("Input file:\t"+mParaInputFileString);
			bParaInput=true;
			return true;
		}
		//InputSheet
		int indexSheet=pParameter.indexOf("-s=");
		if (indexSheet!=-1) {
			mParaSheetString=pParameter.substring(3);
			FXTools.LOGGER.fine("Input sheet:\t"+mParaInputFileString);
			bParaSheet=true;
			return true;
		}
		//Output Path
		int indexOutput=pParameter.indexOf("-o=");
		if (indexOutput!=-1) {
			mDefaultOutputPath=FXTools.formatPath(pParameter.substring(3));
			FXTools.LOGGER.fine("Output path:\t"+mDefaultOutputPath);
			bParaOutput=true;
			return true;
		}
		
		//Format
		int indexFormat=pParameter.indexOf("-f=");
		if (indexFormat!=-1) {
			mDefaultOutputFormat=FXTools.retrieveFormat(pParameter.substring(3));
			FXTools.LOGGER.fine("Output format:\t"+FXTools.stringFromFormatType(mDefaultOutputFormat));
			return true;
		}
		//Extension
		int indexExt=pParameter.indexOf("-x=");
		if (indexExt!=-1) {
			mDefaultOutputExt=FXTools.formatExt(pParameter.substring(3));
			FXTools.LOGGER.fine("Output extension:\t"+mDefaultOutputExt);
			return true;
		}
		//Help
		int indexHelp=pParameter.indexOf("-h");
		if (indexHelp!=-1) {
			printHelpString();
			return false;
		}
		
		FXTools.LOGGER.warning("Parameter string '"+pParameter+"' is invalid and ignored.\nUse '-h' for help");
		return true;
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
				mDefaultOutputExt=FXTools.formatExt( defaultOutputNode.getElementsByTagName("extension").item(0).getTextContent());
				mDefaultOutputExtXML=FXTools.formatExt(defaultOutputNode.getElementsByTagName("extension_xml").item(0).getTextContent());

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
	
	public static void printHelpString(){
		 StringBuilder builder=new StringBuilder()
		.append("-i:\t")
		.append("Required. Input file path.\n")
		.append("-s:\t")
		.append("Required. Input sheet name.\n")
		.append("-o:\t")
		.append("Required. Output path.\n")
		.append("-f:\t")
		.append("Optional. Output file format. Use 'json' or 'xml'.\n")
		.append("-x:\t")
		.append("Optional. Output file extension.\n")
		.append("Example: -o=your/output/path -i=your/path/input.xls -x=.txt -s=sheet_name -f=xml\n")
		;

		FXTools.LOGGER.fine(builder.toString());
	}
	
	
	
}
