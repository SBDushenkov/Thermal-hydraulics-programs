package freezer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/***
 * Simple logger
 * @author sdushenkov
 *
 */
class MathLogger {
	private FileWriter pw = null;
	public MathLogger() {
		try {
			pw = new FileWriter(new File("log.txt"));
			printlnCons("Log creaded");
		} catch (IOException e1) {
			printlnCons("Can't create or access to \"log.txt\" file");
			printlnCons(e1.getMessage());
		}
	}
	public void printCons(final String str) {
		System.out.print(str);
	}
	public void printlnCons(final String str) {
		System.out.println(str);
	}
	public void print(final String str) {
		if (!Main.DEBUG) {
			printCons(str);
		}
		printLog(str);
	}
	public void println(final String str) {
		if (!Main.DEBUG) {
			printlnCons(str);
		}
		printlnLog(str);
	}
	public void printLog(final String str) {
		if (Main.DEBUG) {
			printCons(str);
		}
		if ( !isAlive() ) return;
		try {
			pw.write(str);
		} catch (IOException e) {
			try {
				pw.close();
			} catch (IOException e1) {

			}
			pw = null;
			printlnCons("can' print to log file");
			e.printStackTrace();
		}
	}
	public void printlnLog(final String str) {
		printLog(str + "\n");
	}
	
	public boolean isAlive() {
		return pw != null;
	}
	public void close() {
		if ( !isAlive() ) return;
		try {
			pw.close();
		} catch (IOException e) {
			printlnCons("can' finish to log file");
			e.printStackTrace();
		}
	}
}