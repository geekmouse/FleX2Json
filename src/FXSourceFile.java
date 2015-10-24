import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FXSourceFile {
	private static final String cKeyOutputPath = "outputPath";
	private static final String cKeyOutputExt = "extension";
	private static final String cKeyOutputExtXML = "extension_xml";
	private static final String cKeyOutputFormat = "format";
	
	
	
	public String defaultOutputPathString;
	public String defaultOutputExtString;
	public String defaultOutputExtXMLString;
	public TypeOutputFormat defaultOutputFormat;
	
	public TypeInputFile mTypeInputFile;
	public String mFileNameString;
	
	public ArrayList<FXSheet> mSheetsList;
	
	public FXSourceFile(Element pConfig){
		mFileNameString=pConfig.getAttribute("name");
		mTypeInputFile=FXTools.retrieveInputFormat(mFileNameString);
		
		if (mTypeInputFile==TypeInputFile.INPUT_TYPE_Unkown) {
			
			String l_tempName07=mFileNameString+FXTools.cExtExcel2007;//2007 has higher priority
			String l_tempName97=mFileNameString+FXTools.cExtExcel97;
			
			File file =new File(l_tempName07);
			if (file.exists()) {
				mFileNameString=l_tempName07;
				mTypeInputFile=TypeInputFile.INPUT_TYPE_EXCEL07;
			}
			file=new File(l_tempName97);
			if (file.exists()) {
				if (mTypeInputFile==TypeInputFile.INPUT_TYPE_EXCEL07) {
					FXTools.LOGGER.warning("You have not mentioned the extension of input file '"+ mFileNameString+ "', while both .xls and .xlsx exists under the path. Will use .xlsx as default");
				}
				else{
					mTypeInputFile=TypeInputFile.INPUT_TYPE_EXCEL97;
					mFileNameString=l_tempName97;
				}
			}
			if (mTypeInputFile==TypeInputFile.INPUT_TYPE_Unkown) {
				FXTools.LOGGER.warning("File '"+mFileNameString +"' does not exist.");
			}
		}
		
		if (pConfig.hasAttribute(cKeyOutputPath)) {
			defaultOutputPathString=pConfig.getAttribute(cKeyOutputPath);
		}
		else{
			defaultOutputPathString=entrance.mDefaultOutputPath;
		}
		
		if (pConfig.hasAttribute(cKeyOutputExt)) {
			defaultOutputExtString=pConfig.getAttribute(cKeyOutputExt);
		}
		else{
			defaultOutputExtString=entrance.mDefaultOutputExt;
		}
		
		if (pConfig.hasAttribute(cKeyOutputExtXML)) {
			defaultOutputExtXMLString=pConfig.getAttribute(cKeyOutputExtXML);
		}
		else{
			defaultOutputExtXMLString=entrance.mDefaultOutputExtXML;
		}
		
		if(pConfig.hasAttribute(cKeyOutputFormat)){
			defaultOutputFormat=FXTools.retrieveFormat(pConfig.getAttribute(cKeyOutputFormat)) ;
		}
		else{
			defaultOutputFormat=entrance.mDefaultOutputFormat;
		}
		
		
		
		mSheetsList=new ArrayList<FXSheet>();
		NodeList sheetsList=pConfig.getElementsByTagName("sheet");
		for (int i = 0; i < sheetsList.getLength(); i++) {
			Element sheetElement=(Element) sheetsList.item(i);
			FXSheet sheet=new FXSheet(sheetElement, this);
			mSheetsList.add(sheet);
		}
	}
	
	public Boolean convert(){
		File file=new File(mFileNameString);
		if (file.exists()) {
			switch (mTypeInputFile) {
			case INPUT_TYPE_EXCEL97:{
				POIFSFileSystem fs;
				try {
					fs = new POIFSFileSystem(new FileInputStream(mFileNameString));
					HSSFWorkbook wb=new HSSFWorkbook(fs);
					for (FXSheet sheet : mSheetsList) {
						Boolean resultBoolean = sheet.convert(wb,mTypeInputFile);
						if (!resultBoolean) {
							//return false;
						}
					}
					return true;
					
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			case INPUT_TYPE_EXCEL07:{
				try {
					File excelFile=new File(mFileNameString);
					if (excelFile.exists()) {
						FileInputStream thisFileInputStream=new FileInputStream(excelFile);
						XSSFWorkbook wb=new XSSFWorkbook(thisFileInputStream);
						for (FXSheet sheet : mSheetsList) {
							sheet.convert(wb,mTypeInputFile);
						}
					}
					
					return true;
					
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			default:
				return false;
			}
			
		}
		else{
			FXTools.LOGGER.warning("File: "+ mFileNameString+ " does not existed");
			return false;
		}
	}
}
