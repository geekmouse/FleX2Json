import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.*;

public class FXSourceFile {
	private static final String cKeyOutputPath = "outputPath";
	private static final String cKeyOutputExt = "extension";
	public String defaultOutputPathString;
	public String defaultOutputExtString;
	
	public String mFileNameString;
	
	public ArrayList<FXSheet> mSheetsList;
	
	public FXSourceFile(Element pConfig){
		mFileNameString=pConfig.getAttribute("name");
		String xlsExtString=(String) mFileNameString.subSequence(4, mFileNameString.length());
		if (xlsExtString!=".xls") {
			mFileNameString+=".xls";
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
			POIFSFileSystem fs;
			try {
				fs = new POIFSFileSystem(new FileInputStream(mFileNameString));
				HSSFWorkbook wb=new HSSFWorkbook(fs);
				for (FXSheet sheet : mSheetsList) {
					Boolean resultBoolean = sheet.convert(wb);
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
		else{
			FXTools.LOGGER.warning("File: "+ mFileNameString+ " does not existed");
			return false;
		}
	}
}
