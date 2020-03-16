package freezer;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/***
 * Excel sheet data get/set methods
 * @author sdushenkov
 *
 */
public class ExcelData {
	private final Sheet sheet;
	private final FormulaEvaluator evaluator;
	
	public ExcelData(final Sheet sheet, final FormulaEvaluator evaluator) {
		this.sheet = sheet;
		this.evaluator = evaluator;
	}
	
	public double getDouble (
			final int row, 
			final int col, 
			final String namePrefix, 
			final String nameSuffix
			) throws InputDataException {
		Cell cell = sheet.getRow(row).getCell(col);
		if (cell == null || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.ERROR) {
			constructError(row, col, "double value expected");
		}
		CellValue cv = evaluator.evaluate(cell);
		if (cv.getCellType() == CellType.NUMERIC) {
			double val = cell.getNumericCellValue();
			if (!namePrefix.isEmpty()) {
				Main.LOG.println(namePrefix + " = " + val + " " + nameSuffix);
			}
			return val;
		} else {
			constructError(row, col, "double value expected");
		}
		return 0.;
	}
	public String getString (final int row, final int col) throws InputDataException {
		Cell cell = sheet.getRow(row).getCell(col);
		if (cell == null || cell.getCellType() == CellType.ERROR) {
			constructError(row, col, "string value expected");
		}
		CellValue cv = evaluator.evaluate(cell);
		switch (cv.getCellType()) {
		case STRING:
			return cv.getStringValue();
		case NUMERIC:
			return cv.toString();
		default:
			break;
		}
		return "";
	}
	
	public double getDouble (final int row, final int col) throws InputDataException {
		return getDouble(row, col, "", "");
	}
	
	public boolean cellExist (final int row, final int col) {
		Cell cell = sheet.getRow(row).getCell(col);
		if (cell == null || cell.getCellType() == CellType.BLANK) {
			return false;
		}
		return true;
	}
	
	private void constructError(
			final int row, 
			final int col, 
			final String whatsMissing) throws InputDataException {
		String str = "Error in reading excel input file\n" + 
				"Sheet " + sheet.getSheetName() +
				"\nCell [" + row + ", " + col + "] " + whatsMissing;
		throw new InputDataException(str);
	}
	
	public void setDouble(final int i, final int j, final double val) {
		Row row = sheet.getRow(i);
		if (sheet.getRow(i) == null) {
			row = sheet.createRow(j);
		}
		Cell cell = row.createCell(j, CellType.NUMERIC);
		cell.setCellValue(val);
	}
	public void setString(final int i, final int j, final String val) {
		Row row = sheet.getRow(i);
		if (sheet.getRow(i) == null) {
			row = sheet.createRow(j);
		}
		Cell cell = row.createCell(j, CellType.STRING);
		cell.setCellValue(val);
	}
	public void setString(final int i, final int j, final ArrayList<Object> val) {
		Row row = sheet.createRow(i);
		for (int k = 0; k < val.size(); k++) {
			Cell cell = row.createCell(k, CellType.STRING);
			Object x = val.get(k);
			if (x instanceof String) {
                cell.setCellValue((String) x);
            } else if (x instanceof Double) {
                cell.setCellValue((Double) x);
            } else if (x instanceof Boolean) {
                cell.setCellValue((Boolean) x);
            }
		}
	}
}
