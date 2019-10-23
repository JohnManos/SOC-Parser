package socparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelParser {
	public void parseFile(String fileName, DBRenderer dbRenderer) {
		System.out.println("Parsing file...");
		try {		
		    FileInputStream file = new FileInputStream(new File(fileName));
		    XSSFWorkbook workbook = new XSSFWorkbook(file);
		    XSSFSheet sheet = workbook.getSheetAt(0);
		    //Iterator<Row> rowIterator = sheet.iterator();
		    //rowIterator.next();
		    for (Row row : sheet) {
		    	if (!isRowEmpty(row)) {
		    		Class rowObject = new Class();
			    	String[] rowItem = new String[row.getLastCellNum()];
			    	int cellIndex = 0;
			        for (Cell cell : row) {
			        	cell.setCellType(CellType.STRING);
			        	rowItem[cellIndex++] = cell.getStringCellValue();	
			        }
			        for (String s : rowItem) {
			        	System.out.println(s);
			        }
			        rowObject.course = rowItem[0];
			        rowObject.gordonRule = rowItem[1];
			        rowObject.genEd = rowItem[2];
			        rowObject.section = rowItem[3];
			        rowObject.classNum = rowItem[4];
			        rowObject.minMaxCred = rowItem[5];
			        rowObject.days = rowItem[6];
			        rowObject.time = rowItem[7];
			        rowObject.meetingPattern = rowItem[8];
			        rowObject.spec = rowItem[9];
			        rowObject.soc =rowItem[10];
			        rowObject.courseTitle = rowItem[11];
			        rowObject.instructor = rowItem[12];
			        rowObject.enrCap = rowItem[13];
			        rowObject.schedCodes = rowItem[14];
	/*		        rowItem.course = row.getCell(0).getStringCellValue();
			        rowItem.gordonRule = row.getCell(1).getStringCellValue();
			        rowItem.genEd = row.getCell(2).getStringCellValue();
			        rowItem.section = row.getCell(3).getStringCellValue();
			        rowItem.classNum = row.getCell(4).getStringCellValue();
			        rowItem.minMaxCred = row.getCell(5).getStringCellValue();
			        rowItem.days = row.getCell(6).getStringCellValue();
			        rowItem.time = row.getCell(7).getStringCellValue();
			        rowItem.meetingPattern = row.getCell(8).getStringCellValue();
			        rowItem.spec = row.getCell(9).getStringCellValue();
			        rowItem.soc = row.getCell(10).getStringCellValue();
			        rowItem.courseTitle = row.getCell(11).getStringCellValue();
			        rowItem.instructor = row.getCell(12).getStringCellValue();
			        rowItem.enrCap = row.getCell(13).getStringCellValue();
			        rowItem.schedCodes = row.getCell(14).getStringCellValue();
			        */
			        System.out.println();
			        dbRenderer.insertRowInDB(rowObject);
		    	}
		        System.out.println("");	
		    }
		    file.close();
		    workbook.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	public static boolean isRowEmpty(Row row) {
	    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
	        Cell cell = row.getCell(c);
	        if (cell != null && cell.getCellType() != CellType.BLANK)
	            return false;
	    }
	    return true;
	}
}