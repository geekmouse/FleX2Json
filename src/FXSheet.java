import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FXSheet {
	static final String cKeyOutputPath="outputPath";
	static final String cKeyOutputExt="extention";
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
	public String mSheetName;
	
	
	public FXSheet(Element pConfig,FXSourceFile pSource) {
		mHostFile=pSource;
		
		//Read Configuration
		mSheetName=pConfig.getAttribute(cKeyName);
		
		if (pConfig.hasAttribute(cKeyOutputPath)) {
			mOutputPath=pConfig.getAttribute(cKeyOutputPath);
		}
		else{
			mOutputPath=pSource.defaultOutputPathString;
		}
		
		int lengthOutputPathString=mOutputPath.length();
		if (lengthOutputPathString>0){
			if(mOutputPath.charAt(lengthOutputPathString-1)!=FXTools.pathSymbol) {
				mOutputPath+=FXTools.pathSymbol;
			}
		}
		
		
		
		if (pConfig.hasAttribute(cKeyOutputExt)) {
			mOutputExt=pConfig.getAttribute(cKeyOutputExt);
		}
		else{
			mOutputExt=pSource.defaultOutputExtString;
		}
		if (mOutputExt.charAt(0)!='.') {
			mOutputExt="."+mOutputExt;
		}
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

	
	public Boolean convert(HSSFWorkbook pWorkbook){
		HSSFSheet sheetXls=pWorkbook.getSheet(mSheetName);
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
				FXTools.LOGGER.fine("----Line:"+i+"----");
				Row thisRow=sheetXls.getRow(i);
				JSONObject lineJsonObject=new JSONObject();
				for(int j=0;j<numColumns;j++){
					String l_columnString=FXTools.xlsColumnStringFromIndex(j);
					String l_namePropString=getCellValueString(rowName.getCell(j));
					String l_valuePropString=getCellValueString(thisRow.getCell(j));
					
					l_valuePropString=l_valuePropString.replaceAll("\n", "");
					FXTools.LOGGER.finer("Column:"+l_columnString+"..."+getCellValueString(rowDesp.getCell(j))+":"+getCellValueString(rowName.getCell(j)));
					
					char thisType='N';
					String typeString=rowType.getCell(j).getStringCellValue();
					if (typeString.length()!=0) {
						thisType=typeString.charAt(0);
					}
					else{
						FXTools.LOGGER.warning("The value type of column "+l_columnString+" is not assigned.");
						
					}
					
					switch (thisType) {
					
					case 's':{
						lineJsonObject.put(l_namePropString,l_valuePropString ); 
						break;
					}
					case 'i':
						if (l_valuePropString=="") {
							l_valuePropString="0";
						}
						lineJsonObject.put(l_namePropString,Integer.valueOf((int) Float.parseFloat(l_valuePropString)) ); 
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
						lineJsonObject.put(l_namePropString,Float.parseFloat(l_valuePropString)); 
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
							
							cellArray.add(l_destBoolean);
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
								cellArray.add(Float.parseFloat(arr[k]));
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
								cellArray.add(Integer.valueOf((int) Float.parseFloat(arr[k])));
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
								cellArray.add(arr[k]);
							}
						}
						lineJsonObject.put(l_namePropString,cellArray);
						break;
					}
					//----Not to convert columns---
					case 'N':	
					case 'n':

					default:
						break;
					}			
				}
				//
				sheetArray.add(lineJsonObject);
			}
			String l_fullPathString=mOutputPath+mSheetName+mOutputExt;
			WriteJson(sheetArray, l_fullPathString);
			FXTools.LOGGER.fine("======================================");
			FXTools.LOGGER.fine("Output Json Succeed!! Check the file at:"+l_fullPathString);
			FXTools.LOGGER.fine("======================================");
			return true;
		}
		
		
	}
	
	public void WriteJson(JSONArray sheetArray,String outputName){
		
		String jsonString=JSONArray.toJSONString(sheetArray, true);
		try {
			File f=new File(outputName);
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter outputBufferedWriter=new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8") );
			outputBufferedWriter.write(jsonString);
			outputBufferedWriter.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
}
