package freezer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import freezer.TECalcRes.NormalizationType;

public class Main {
	public static boolean DEBUG;
	public static MathLogger LOG;
	
	public static final String sheetNameThermoelectric = "Thermoelectric";
	public static final String sheetNameConditions = "Conditions";
	
	private TEProps teProps;
	private Geom geom;
	private Conditions cond;

	
	private final ArrayList<Object> vals = new ArrayList<Object>();

	private File getFile() {
		JFileChooser fc = new JFileChooser(".");
		fc.setFileFilter(new FileNameExtensionFilter("Microsoft Excel (*.xlsx)", "xlsx"));
		int res = fc.showOpenDialog(null);
		File fileIn = null;
		if (res == JFileChooser.APPROVE_OPTION) {
			fileIn = fc.getSelectedFile();
			return fileIn;
		} else {
			LOG.println("No file to work with");
			System.exit(0);
		}
		return null;
	}
	
	
	
	public Main() {

		File fileIn = getFile();
        
        String fileOutPath = fileIn.getAbsolutePath().substring(0, fileIn.getAbsolutePath().length() - 4) + "_RESULT.xlsx";
		try( 
				FileInputStream file = new FileInputStream(fileIn); 
				FileOutputStream outputStream = new FileOutputStream(fileOutPath);
				Workbook workbookIn = new XSSFWorkbook(file);
				XSSFWorkbook workbookOut = new XSSFWorkbook()) {
			
			LOG.println("File \"" + fileIn.getAbsolutePath() + "\" is found");
			LOG.println("Workbook is found");
			
	        XSSFSheet sheetOutMeter = workbookOut.createSheet("Per meter");
	        XSSFSheet sheetOutModule = workbookOut.createSheet("Module");
	        XSSFSheet sheetOutBattery = workbookOut.createSheet("Battery");
	        XSSFSheet sheetOutRing = workbookOut.createSheet("Ring");
			
			Sheet sheet = workbookIn.getSheet(sheetNameThermoelectric);
			if (sheet == null) {
				throw new InputDataException("Sheet \"" + sheetNameThermoelectric + "\" not found");
			} else {
				Main.LOG.println("Sheet \"" + sheetNameThermoelectric + "\" is found");
			}

			LOG.println("\n====reading data====");
			FormulaEvaluator evaluator = workbookIn.getCreationHelper().createFormulaEvaluator();
			ExcelData thermoelectricIn = new ExcelData(sheet, evaluator);
			teProps = new TEProps(thermoelectricIn);
			Main.LOG.print(teProps.toString());
			geom = new Geom(thermoelectricIn);
			
			sheet = workbookIn.getSheet(sheetNameConditions);
			if (sheet == null) {
				throw new InputDataException("Sheet \"" + sheetNameConditions + "\" not found");
			} else {
				Main.LOG.println("Sheet \"" + sheetNameConditions + "\" is found");
			}
			
			ExcelData conditionsIn = new ExcelData(sheet, evaluator);
			cond = new Conditions(conditionsIn);
			geom.updateMargins(cond);
			
			
			LOG.println("\n====calculations====");
 
			ExcelData resultsEnvMeter = new ExcelData(sheetOutMeter, evaluator);
			ExcelData resultsModule = new ExcelData(sheetOutModule, evaluator);
			ExcelData resultsBattery = new ExcelData(sheetOutBattery, evaluator);
			ExcelData resultsRing = new ExcelData(sheetOutRing, evaluator);
			
			long nano = System.nanoTime();
			calcDynamic(resultsEnvMeter, resultsModule, resultsBattery, resultsRing);
			LOG.println("calcDynamic finished in " + ((System.nanoTime() - nano)/1_000_000L) + " ms");
			
			workbookOut.write(outputStream);
			
		} catch (FileNotFoundException e) {
			LOG.println(e.getMessage());
		} catch (IOException e) {
			LOG.println(e.getMessage());
		} catch(InputDataException e) {
			LOG.println(e.getMessage());
		}finally {

		}

	}
	
	void excelPrintHeaders(
			final ExcelData resultsMeter, 
			final ExcelData resultsModule, 
			final ExcelData resultsBattery,
			final ExcelData resultsRing,
			final TECalcRes cr,
			final ThermalState ts) throws InputDataException{
		ArrayList<Object> headers = new ArrayList<Object>();
		headers.add("time, s");
		headers.addAll(cr.getHeaderArray(NormalizationType.METER));
		headers.addAll(ts.getHeaderArray());
		resultsMeter.setString(0, 0, headers);
		
		headers.clear();
		headers.add("time, s");
		headers.addAll(cr.getHeaderArray(NormalizationType.MODULE));
		resultsModule.setString(0, 0, headers);
		
		headers.clear();
		headers.add("time, s");
		headers.addAll(cr.getHeaderArray(NormalizationType.BATTERY));
		resultsBattery.setString(0, 0, headers);
		
		headers.clear();
		headers.add("time, s");
		headers.addAll(cr.getHeaderArray(NormalizationType.RING));
		resultsRing.setString(0, 0, headers);
		
		Main.LOG.printlnLog("time,s  " + cr.getHeader() + ts.getHeader());
	}

	void excelPrintString(
			final ExcelData resultsMeter, 
			final ExcelData resultsModule, 
			final ExcelData resultsBattery,
			final ExcelData resultsRing,
			final TECalcRes cr,
			final ThermalState ts,
			final double time,
			final int rowN) throws InputDataException {
		Main.LOG.printLog(
				String.format("%8.2f",  time)
				+ cr 
				+ ts);
		vals.clear();
		vals.add(time);
		vals.addAll(cr.toStringArray(NormalizationType.METER));
		vals.addAll(ts.toStringArray());
		Main.LOG.printlnLog(" | " + cr.pc.propsVals.getMessage());
		vals.add(cr.pc.propsVals.getMessage());
		
		resultsMeter.setString(rowN, 0, vals);
		vals.clear();
		vals.add(time);
		vals.addAll(cr.toStringArray(NormalizationType.MODULE));
		resultsModule.setString(rowN, 0, vals);
		
		vals.clear();
		vals.add(time);
		vals.addAll(cr.toStringArray(NormalizationType.BATTERY));
		resultsBattery.setString(rowN, 0, vals);
		
		vals.clear();
		vals.add(time);
		vals.addAll(cr.toStringArray(NormalizationType.RING));
		resultsRing.setString(rowN, 0, vals);
	}
	
	void calcDynamic(
			final ExcelData resultsMeter, 
			final ExcelData resultsModule, 
			final ExcelData resultsBattery,
			final ExcelData resultsRing) throws InputDataException{
		final double I = getI(null);
		
		final int Nlayers_mod = geom.Nlayers - 1; 
		final int xLayerInner = geom.Xlayer;
		final int xLayerOuter = geom.Xlayer + 1;
		final double[] layerT_mod = new double[Nlayers_mod];
		
		layerT_mod[0] = cond.Tinner;
		for (int i = 1; i < Nlayers_mod; i++) {
			layerT_mod[i] = cond.Ttem_init;
		}
		
		TECalcRes cr = new TECalcRes(
				layerT_mod[xLayerInner], layerT_mod[xLayerOuter], 
				I, geom, teProps);
		ThermalState ts = new ThermalState(geom, cond, layerT_mod, cr, 1./10000.);

		excelPrintHeaders(resultsMeter, resultsModule, resultsBattery, resultsRing, cr, ts);
		excelPrintString(resultsMeter, resultsModule, resultsBattery, resultsRing, cr, ts, 0., 1);
		
		int i = 0;
		int j = 2;
		for (double time = 0; time < cond.timeEnd; time += cond.timeStep, i++) {
			double Inew = getI(cr); 
			cr = new TECalcRes(
					ts.layerT_mod[xLayerInner], ts.layerT_mod[xLayerOuter], 
					Inew, geom, teProps);
			ts = new ThermalState(geom, cond, ts.layerT_mod, cr, cond.timeStep);
			
			if (i % cond.nToPrint == 0) {
				excelPrintString(resultsMeter, resultsModule, resultsBattery, resultsRing, cr, ts, time, j);
				j++;
			}
		}

	}
	
	double getI(final TECalcRes cr) {
		if (Math.abs(cond.I) < 1e-6) {
			double v = cond.V /geom.nRingsInBattery/ geom.nBatteries;
			if (cr == null) {
				PrepareConsts temp_pc = new PrepareConsts(cond.Ttem_init, cond.Ttem_init, geom, teProps);
				return  v / (temp_pc.Re_semi + temp_pc.Re_inner + temp_pc.Re_outer);
			} else {
				return (v - cr.pc.propsVals.alpha * (cr.TouterJunct - cr.TinnerJunct) *  (geom.nSegmentsInRing / 2.)) / 
						(cr.pc.Re_semi + cr.pc.Re_inner + cr.pc.Re_outer);
			}
		} else {
			return cond.I;
		}
	}

	static double CtoK(final double C) {
		return C + 273.15;
	}
	
	public static void main(String[] args) {
		for (String str : args) {
			if (str.contentEquals("-debug")) {
				Main.DEBUG = true;
			}
		}
		LOG = new MathLogger();
		new Main();
		
	}
	


}
