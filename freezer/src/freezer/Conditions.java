package freezer;

import org.apache.poi.util.NotImplemented;

/***
 * External thermophysical conditions, electrical power supply condition and calculation times
 * @author sdushenkov
 */
public class Conditions {
	@NotImplemented
	private final double Ginner;
	/***
	 * Inner fluid initial temperature, 'C<br>
	 */
	public final double Tinner;
	/***
	 * Inner fluid thermal capacity for mass calculation, J/kg
	 */
	public final double CpInner;
	/***
	 * Inner wall heat transfer coefficient, W/m2/K
	 */
	public final double alphaInner;
	/***
	 * Inner fluid area for mass calculation, m2
	 */
	public final double areaInner;
	/***
	 * Inner fluid density for mass calculation, kg/m3
	 */
	public final double rhoInner;

	@NotImplemented
	private final double Gouter;
	/***
	 * Outer fluid constant temperature, 'C<br>
	 */
	public final double Touter;
	
	@NotImplemented
	private final double CpOuter;
	/***
	 * Outer wall heat transfer coefficient, W/m2/K
	 */
	public final double alphaOuter;
	
	@NotImplemented
	private final double areaOuter;

	/***
	 * Given current, A<br>
	 * If zero then voltage used
	 */
	public final double I;
	/***
	 * Given voltage per module, V<br>
	 * Used only if current set to zero
	 */
	public final double V;
	/***
	 * Solid layers initial temperature, 'C
	 */
	public final double Ttem_init;
	
	/***
	 * Calculation end time step, s
	 */
	public final double timeEnd;
	/***
	 * Calculation time step, s
	 */
	public final double timeStep;
	/***
	 * Print every n-th steps, s
	 */
	public final int nToPrint;
	
	public Conditions(final ExcelData env) throws InputDataException{
		Main.LOG.println("Conditions");
		
		Ginner = env.getDouble(2, 2, "Ginner", "kg/s");
		Tinner = env.getDouble(3, 2, "Tinner", "'C");
		CpInner = env.getDouble(4, 2, "CpInner", "J/kg/K");
		alphaInner = env.getDouble(5, 2, "alphaInner", "W/m2/K");
		areaInner = env.getDouble(6, 2, "areaInner", "m2");
		rhoInner = env.getDouble(7, 2, "areaInner", "kg/m3");
		
		Gouter = env.getDouble(11, 2, "Gouter", "kg/s");
		Touter = env.getDouble(12, 2, "Touter", "'C");
		CpOuter = env.getDouble(13, 2, "CpOuter", "J/kg/K");
		alphaOuter = env.getDouble(14, 2, "alphaouter", "W/m2/K");
		areaOuter = env.getDouble(15, 2, "areaOuter", "m2");
		
		I = env.getDouble(20, 2, "I", "A");
		V = env.getDouble(21, 2, "V", "V");
		Ttem_init = env.getDouble(22, 2, "Ttem_init", "'C");
		
		timeEnd = env.getDouble(25, 2, "Tend", "s");
		timeStep = env.getDouble(26, 2, "delta T", "s");
		nToPrint = (int) env.getDouble(27, 2, "delta T", "s");
	}
}
