import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;



/**Static tool functions*/
public class FXTools extends Formatter{
	public static final String  cExtExcel97=".xls";
	public static final String  cExtExcel2007=".xlsx";
	
	static public char pathSymbol;
	static FXTools tools;
	static final public Logger LOGGER=Logger.getGlobal();
	static public void init(){
		pathSymbol=getPathSymbol();
		Handler consoleHandler=new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		tools=new FXTools();
		consoleHandler.setFormatter(tools);
		LOGGER.addHandler(consoleHandler);
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);
		
	}
	static public int findCharInString(char destChar,String referString){
		int length=referString.length();
		for (int i = 0; i < length; i++) {
			if (destChar==referString.charAt(i)){
				return i;
			}
		}
		return -1;
	}
	
	static public String formatPath(String pOrigPathString){
		int lengthOutputPathString=pOrigPathString.length();
		if (lengthOutputPathString>0){
			if(pOrigPathString.charAt(lengthOutputPathString-1)!=pathSymbol) {
				pOrigPathString+=FXTools.pathSymbol;
			}
		}
		return pOrigPathString;
	}
	
	static public String formatExt(String pOrigExt){
		int length=pOrigExt.length();
		if (length>0) {
			if (pOrigExt.charAt(0)!='.') {
				pOrigExt='.'+pOrigExt;
			}
		}
		return pOrigExt;
	}
	
	static public String xlsColumnStringFromIndex(int columnIndex) {
		//[Pending]Temporary solution, will use radix 26 later to make xls column indexes readable
		String s="";
		int n=columnIndex+1;
	    while (n > 0){
	        int m = n % 26;
	        if (m == 0) m = 26;
	        s = (char)(m + 64) + s;
	        n = (n - m) / 26;
	    }
	    return s;
	}
	
	static public String getOS(){
		String osString=System.getProperties().getProperty("os.name");
		LOGGER.finer("os:"+osString);
		return osString;
	}
	
	static public char getPathSymbol(){
		if (getOS().charAt(0)=='w') {
			return '\\';
		}
		else{
			return '/';
		}
	}
	static public TypeOutputFormat retrieveFormat(String formatString){
		switch (formatString.charAt(0)) {
		case 'j':
		case 'J':
			return TypeOutputFormat.FORMAT_JSON;
		case 'x':
		case 'X':
			return TypeOutputFormat.FORMAT_XML;
		case '.':
		case ' ':{
			return retrieveFormat(formatString.substring(1));
		}
		
		default:
			FXTools.LOGGER.warning("Unretrievable parameter for default output format '"+formatString +"'. Use Json for default");
			return TypeOutputFormat.FORMAT_JSON;
		}
	}
	
	static public String stringFromFormatType(TypeOutputFormat pType){
		switch (pType) {
		case FORMAT_JSON:
			return "json";
		case FORMAT_XML:
			return "xml";
		default:
			return "unknown";
		}
	}
	
	static public TypeInputFile retrieveInputFormat(String pPath){
		int length=pPath.length();
		String xlsExtString=(String) pPath.subSequence( Math.max(length-cExtExcel97.length(),0), length);
		String xlsxExtString=(String) pPath.subSequence(Math.max(length-cExtExcel2007.length(),0), length);
		if (FXTools.cExtExcel97.equals(xlsExtString) ) {
			return TypeInputFile.INPUT_TYPE_EXCEL97;
		}
		else if (xlsxExtString.equals(FXTools.cExtExcel2007) ) {
			return TypeInputFile.INPUT_TYPE_EXCEL07;
		}
		else{
			return TypeInputFile.INPUT_TYPE_Unkown;
		}
	}
	
	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
	    String message = formatMessage(record);
	    
	    // Level
	    Level lvLevel=record.getLevel();
	    if (lvLevel==Level.WARNING ||lvLevel==Level.SEVERE) {
	    	sb.append(record.getLevel().getLocalizedName());
		    sb.append(": ");
		 // Indent - the more serious, the more indented.
		    //sb.append( String.format("% ""s") );
		    int iOffset = (1000 - record.getLevel().intValue()) / 100;
		    for( int i = 0; i < iOffset;  i++ ){
		      sb.append(" ");
		    }
		}
	    
	    sb.append(message+"\r\n");
	    if (record.getThrown() != null) {
	      try {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        record.getThrown().printStackTrace(pw);
	        pw.close();
	        sb.append(sw.toString());
	      } catch (Exception ex) {
	    	  return null;
	      }
	    }
	    return sb.toString();
	}
}
