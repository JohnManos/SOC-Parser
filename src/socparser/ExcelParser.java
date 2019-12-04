package socparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelParser {
	public String[][] parseFile(String filePath) {
		System.out.println("Parsing file...");
		String[][] data = null;
		int numEmptyRows = 0;
		try {		
		    FileInputStream file = new FileInputStream(new File(filePath));
		    XSSFWorkbook workbook = new XSSFWorkbook(file);
		    XSSFSheet sheet = workbook.getSheetAt(0);
		    // Scan for empty rows to size array appropriately
		    for (Row row : sheet) {
		    	if (isRowEmpty(row)) { 
		    		++numEmptyRows;
		    	}
		    }
		    data = new String[sheet.getLastRowNum() - numEmptyRows + 1][sheet.getRow(0).getLastCellNum()];
		    System.out.println(sheet.getLastRowNum());
		    int rowIndex = 0;
		    for (Row row : sheet) {
		    	if (!isRowEmpty(row)) { // Skip empty rows
		    		RowObject rowObject = new RowObject();
			    	String[] rowItem = new String[row.getLastCellNum()];
			    	int cellIndex = 0;
			        for (Cell cell : row) {
			        	cell.setCellType(CellType.STRING); // Change all cell types to String for simplicity's sake
			        	rowItem[cellIndex++] = cell.getStringCellValue();	
			        }
			        for (String s : rowItem) {
			        	System.out.println(s);
			        }
			        data[rowIndex++] = rowItem;
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
		return data;
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