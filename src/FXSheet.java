import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class FXSheet {
	static final int cExitErrorInvalidConfig=-1;
	
	static final String cKeyOutputPath="outputPath";
	static final String cKeyOutputExt="extension";
	static final String cKeyOutputFormat="format";
	static final String cKeyName="name";
	static final String splitString="\\|";//"|"
	static final Boolean c_defaultBooleanValue=Boolean.TRUE;
	
	public static int Line_Description=0;
	public static int Line_Name=1;
	public static int Line_Type=2;
	public static int Line_ValueBegin=3;
	
	
	public FXSourceFile mHostFile;
	public String mOutputPath;
	public String mOutputExt;
	public TypeOutputFormat mOutputFormat;
	public String mSheetName;
	public int mBufferColumn;
	public int mBufferRow;
	
	public FXSheet(String pSheetName, TypeOutputFormat pOutputFormat, String pOutputPath,String pOutputExt ,FXSourceFile pSource ){
		mHostFile=pSource;
		mSheetName=pSheetName;
		mOutputPath=pOutputPath;
		mOutputExt=pOutputExt;
		mOutputFormat=pOutputFormat;
		
	}
	
	public FXSheet(Element pConfig,FXSourceFile pSource) {
		mHostFile=pSource;
		
		//Read Configuration
		mSheetName=pConfig.getAttribute(cKeyName);
		
		//Format
		if (pConfig.hasAttribute(cKeyOutputFormat)) {
			mOutputFormat=FXTools.retrieveFormat(pConfig.getAttribute(cKeyOutputFormat)) ;
		}
		else{
			mOutputFormat=pSource.defaultOutputFormat;
		}
		//Output path
		if (pConfig.hasAttribute(cKeyOutputPath)) {
			mOutputPath=pConfig.getAttribute(cKeyOutputPath);
		}
		else{
			mOutputPath=pSource.defaultOutputPathString;
		}
		mOutputPath=FXTools.formatPath(mOutputPath);
		File file=new File(mOutputPath);
		if (!file.exists()) {
			if (file.mkdir()) {
				FXTools.LOGGER.fine("Successfully created the output path: '"+ mOutputPath+"'.");
			}
			else{
				FXTools.LOGGER.severe("Failed to create the output path: '"+ mOutputPath+"'");
			}
		}
		
		//Extension
		if (pConfig.hasAttribute(cKeyOutputExt)) {
			mOutputExt=pConfig.getAttribute(cKeyOutputExt);
		}
		else{
			switch (mOutputFormat) {
			case FORMAT_JSON:
				mOutputExt=pSource.defaultOutputExtString;
				break;
			case FORMAT_XML:
				mOutputExt=pSource.defaultOutputExtXMLString;
				break;
			default:
				mOutputExt=pSource.defaultOutputExtString;
				break;
			}

		}
		mOutputExt=FXTools.formatExt(mOutputExt);
	}

	
	public String getCellValueString(Cell cell){
		if (cell==null) {
			return "";
		}
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue()?"t":"f";
		case Cell.CELL_TYPE_NUMERIC://number
		
			double l_value=cell.getNumericCellValue();
			return String.valueOf(l_value);
		case Cell.CELL_TYPE_STRING://String
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_FORMULA:{
			switch (cell.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_NUMERIC:
					return String.valueOf(cell.getNumericCellValue());
				case Cell.CELL_TYPE_STRING:
					return cell.getStringCellValue();
				default:
					break;
			}
		}
		case Cell.CELL_TYPE_BLANK:
			return "";
		default:
			break;
		}
		return "";
		
	}

	
	public Boolean convert(Workbook pWorkbook,TypeInputFile typeInputFile){
		
		Sheet sheetXls=pWorkbook.getSheet(mSheetName);
		if (sheetXls==null) {
			FXTools.LOGGER.warning("Failed to retrieve the sheet: '"+mSheetName+"' in file '"+mHostFile.mFileNameString+"'.");
			return false;
		}
		else{
			JSONArray sheetArray=new JSONArray();
			Row rowDesp=sheetXls.getRow(Line_Description);
			Row rowName=sheetXls.getRow(Line_Name);
			Row rowType=sheetXls.getRow(Line_Type);
			
			int numColumns=rowName.getPhysicalNumberOfCells();
			FXTools.LOGGER.fine("Processing sheet:"+sheetXls.getSheetName()+"...");
			
			for (int i = Line_ValueBegin; i <= sheetXls.getLastRowNum(); i++) {
				mBufferRow = i+1;
				Row thisRow=sheetXls.getRow(i);
				if (thisRow!=null) {
					FXTools.LOGGER.fine("----Line:"+(i+1)+"----");
					JSONObject lineJsonObject=new JSONObject();
					for(int j=0;j<numColumns;j++){
						
						String l_columnString=FXTools.xlsColumnStringFromIndex(j);
						String l_namePropString=getCellValueString(rowName.getCell(j));
						String l_valuePropString=getCellValueString(thisRow.getCell(j));
						mBufferColumn = j;
						l_valuePropString=l_valuePropString.replaceAll("\n", "");
						FXTools.LOGGER.finer("Column:"+l_columnString+"..."+getCellValueString(rowDesp.getCell(j))+":"+getCellValueString(rowName.getCell(j)));
						
						char thisType='N';
						Cell typeCell=rowType.getCell(j);
						if (typeCell==null || typeCell.getCellType()==Cell.CELL_TYPE_BLANK) {
							FXTools.LOGGER.warning("The value type of column "+l_columnString+" is not assigned. Use the default type 'string'");
							thisType='s';
						}
						else{
							String typeString=typeCell.getStringCellValue();
							thisType=typeString.charAt(0);
						}
						
						lineJsonObject=_convert(thisType, l_namePropString, l_valuePropString, lineJsonObject);
					}
					//
					sheetArray.put(lineJsonObject);
				}
			}
			String l_fullPathString=mOutputPath+mSheetName+mOutputExt;
			writeFile(sheetArray, l_fullPathString);
			FXTools.LOGGER.fine("======================================");
			FXTools.LOGGER.fine("Output Json Succeed!! Check the file at:"+l_fullPathString);
			FXTools.LOGGER.fine("======================================");
			return true;
		}
	}
	
	protected JSONObject _convert(char type,String l_namePropString,String l_valuePropString,JSONObject lineJsonObject) {
		switch (type) {
		case 's':{
			lineJsonObject.put(l_namePropString,l_valuePropString ); 
			break;
		}
		case 'i':
			if (l_valuePropString=="") {
				l_valuePropString="0";
			}
			try {
				Float parsedValue = Float.parseFloat(l_valuePropString);
				lineJsonObject.put(l_namePropString,Integer.valueOf(Math.round(parsedValue) )); 
			} catch (Exception e) { 
				// TODO: handle exception
				FXTools.LOGGER.warning("Cannot cast '"+l_valuePropString+"' to integer value. [sheet:"+mSheetName+", row:"+mBufferRow+", column:"+FXTools.xlsColumnStringFromIndex(mBufferColumn)+"]");
				System.exit(cExitErrorInvalidConfig);
			}
			break;
		case 'b':{
			if (l_valuePropString=="") {
				lineJsonObject.put(l_namePropString, c_defaultBooleanValue);
				//l_valuePropString="true";
			}
			else{
				char l_valueChar=l_valuePropString.charAt(0);
				if (FXTools.findCharInString(l_valueChar, "tTyY")!=-1) {
					lineJsonObject.put(l_namePropString, c_defaultBooleanValue);
				}
				else{
					lineJsonObject.put(l_namePropString, !c_defaultBooleanValue);
				}
			}
			break;
		}
		case 'f':{
			if (l_valuePropString=="") {
				l_valuePropString="0.0";
			}
			try{
				Float parseValue = Float.parseFloat(l_valuePropString);
				lineJsonObject.put(l_namePropString,parseValue); 
			}catch (Exception e){
				FXTools.LOGGER.warning("Cannot cast '"+l_valuePropString+"' to float value. [sheet:"+mSheetName+", row:"+mBufferRow+", column:"+FXTools.xlsColumnStringFromIndex(mBufferColumn)+"]");
				System.exit(cExitErrorInvalidConfig);
			}
			
			break;
		}
		case 'B':{
			JSONArray cellArray=new JSONArray();
			String[] arr=l_valuePropString.split(splitString);
			for (int k = 0; k < arr.length; k++) {
				Boolean l_destBoolean;
				if (arr[k]=="") {
					l_destBoolean=c_defaultBooleanValue;	
				}
				else{
					char l_valueChar=arr[k].charAt(0);
					if (FXTools.findCharInString(l_valueChar, "tTyY")!=-1) {
						l_destBoolean=c_defaultBooleanValue;
					}
					else{
						l_destBoolean=!c_defaultBooleanValue;
					}
				}
				
				cellArray.put(l_destBoolean);
			}
			lineJsonObject.put(l_namePropString,cellArray);
			break;
		}
		//----The arrays of type float---
		case 'a':
		case 'F':{
			JSONArray cellArray=new JSONArray();
			String[] arr=l_valuePropString.split(splitString);
			for (int k = 0; k < arr.length; k++) {
				if (arr[k]!="") {
					try{
						Float valueFloat = Float.parseFloat(arr[k]);
						cellArray.put(valueFloat);
					}
					catch(Exception e){
						FXTools.LOGGER.warning("Cannot cast '"+l_valuePropString+"' to float array. [sheet:"+mSheetName+", row:"+mBufferRow+", column:"+FXTools.xlsColumnStringFromIndex(mBufferColumn)+"]");
						System.exit(cExitErrorInvalidConfig);
					}
				}
			}
			lineJsonObject.put(l_namePropString,cellArray);
			break;
		}
		//----The arrays of type Int---
		case 'I':{
			JSONArray cellArray=new JSONArray();
			String[] arr=l_valuePropString.split(splitString);
			for (int k = 0; k < arr.length; k++) {
				if (arr[k]!="") {
					try {
						cellArray.put(Integer.valueOf((int) Float.parseFloat(arr[k])));
					} catch (Exception e) {
						FXTools.LOGGER.warning("Cannot cast '"+l_valuePropString+"' to int array. [sheet:"+mSheetName+", row:"+mBufferRow+", column:"+FXTools.xlsColumnStringFromIndex(mBufferColumn)+"]");
						System.exit(cExitErrorInvalidConfig);
					}
					
				}
			}
			lineJsonObject.put(l_namePropString,cellArray);
			break;
		}
		//----The arrays of type String---
		case 'S':
		case 'A':{
			JSONArray cellArray=new JSONArray();
			
			String[] arr=l_valuePropString.split(splitString);
			for (int k = 0; k < arr.length; k++) {
				if (arr[k]!="") {
					cellArray.put(arr[k]);
				}
			}
			lineJsonObject.put(l_namePropString,cellArray);
			break;
		}
		//----Not to convert columns---
		case 'N':	
		case 'n':
		case 'C':
		case 'c':
			break;
		default:
			break;
		}
		return lineJsonObject;
	}
	
	public void writeFile(JSONArray sheetArray,String outputName){
		String convertString;
		
		if (mOutputFormat==TypeOutputFormat.FORMAT_XML) {//XML
			convertString=XML.toString(sheetArray,"object");
			StringBuilder builder=new StringBuilder();
			builder
				.append("<?xml version=\"1.0\"?>\n")
				.append("<"+mSheetName+">")
				.append(convertString)
				.append("</"+mSheetName+">");
			convertString=builder.toString();
			try {
				Document document;
				try {
					document = DocumentBuilderFactory.newInstance()
					        .newDocumentBuilder()
					        .parse(new InputSource(new ByteArrayInputStream(convertString.getBytes("utf-8"))));
					XPath xPath = XPathFactory.newInstance().newXPath();
				    NodeList nodeList;
					try {
						nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
						                                              document,
						                                              XPathConstants.NODESET);
						for (int i = 0; i < nodeList.getLength(); ++i) {
					        Node node = nodeList.item(i);
					        node.getParentNode().removeChild(node);
					    }
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
					    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
					    StringWriter stringWriter = new StringWriter();
					    StreamResult streamResult = new StreamResult(stringWriter);
					 
					    try {
							transformer.transform(new DOMSource(document), streamResult);
							convertString=stringWriter.toString();
						} catch (TransformerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 
					    
					    
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
				    
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  				
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else{
			convertString=sheetArray.toString(2);
		}
		try {
			File f=new File(outputName);
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter outputBufferedWriter=new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8") );
			outputBufferedWriter.write(convertString);
			outputBufferedWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
}
