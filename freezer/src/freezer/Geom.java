package freezer;

/***
 * construction and thermal properties data
 * @author sdushenkov
 *
 */
public class Geom {
	/***
	 * Layers count in calculations<br>
	 * Nlayers = solid layers count + <br>
	 * + 1 inner fluid + 1 outer wall + 1 outer fluid 
	 */
	public final int Nlayers;
	/***
	 * Semiconductor layer number
	 */
	public final int Xlayer;
	/***
	 * Volume filling for semiconductor (takes into account gaps between segments and rings)
	 * Applies for conductors too   
	 */
	public final double Xvolume_factor;
	/**
	 * self explanatory
	 * in SI
	 */
	public final double[] layerLambda;
	public final double[] layerCp;
	public final double[] layerRho;
	public final double[] layerRin;
	public final double[] layerThickness;
	/**
	 * Layer CpM mod for calculations, J/kg <br>
	 * <code>
	 * layerCpM_mod[1] = 0. + tempCpM[1] / 2.<br>
	 * layerCpM_mod[2] = tempCpM[1] / 2. + tempCpM[2] / 2.<br>
	 * ...<br>
	 * layerCpM_mod[Nlayers - 3] = tempCpM[Nlayers - 4] / 2. + tempCpM[3] / 2.<br>
	 * layerCpM_mod[Nlayers - 2] = tempCpM[Nlayers - 3] / 2. + 0.
	 * </code>
	 */	
	public final double[] layerCpM_mod;
	/**
	 * Layer thermal resistance (per ring), K/W
	 */
	public final double[] layerRh;
	/**
	 * Layer pure CpM (per ring), J/kg
	 */
	private final double[] tempCpM;
	/***
	 * Additional thermal resistance for inner layers (per meter), K*m/W<br>
	 * <i> Applies to inner conductor layer</i>
	 */	
	public final double RhColdSide;
	/***
	 * Additional thermal resistance for outer layers (per meter), K*m/W<br>
	 * <i> Applies to outer conductor layer</i>
	 */	
	public final double RhHotSide;
	/**
	 * TEM structure in numbers
	 * self explanatory
	 */
	public final double ringThickness;
	public final double nSegmentsInRing;
	public final double nRingsInBattery;
	public final double nBatteries;

	/***
	 * Conductor material for electrical resistance calculations
	 */
	public enum Conductor {
		/***
		 * Nickel
		 */
		Ni("Nickel"), 
		/***
		 * Cuprum
		 */		
		Cu("Cuprum"), 
		/***
		 * Zero resistance
		 */	
		NONE("none");
		private final String string;
		static Conductor get(String str) {
			if (str == null) return NONE;
			switch (str) {
			case "Ni":
				return Ni;
			case "Cu":
				return Cu;
			default:
				return NONE;
			}
		}
		private Conductor(String string ) {
			this.string = string;
		}
		@Override
		public String toString() {
			return string;
		}
		/***
		 * Calculate conductor electrical resistance for given temperature
		 * <a href="http://chemanalytica.com/book/novyy_spravochnik_khimika_i_tekhnologa/12_obshchie_svedeniya/6101">
		 * Справочник химика технолога</a>
		 * @param T temperature
		 * @return electrical resistance, Ohm
		 */
		double getConductorRho(double T) {
			//http://chemanalytica.com/book/novyy_spravochnik_khimika_i_tekhnologa/12_obshchie_svedeniya/6101
			double[] arrT = {-78, 100, 200, 300};
			double[] arrRho = new double[4];
			double rho0, rho;
			switch (this) {
			case Cu:
				rho0 = 1.55e-8;
				arrRho[0] = 0.649;
				arrRho[1] = 1.433;
				arrRho[2] = 1.866;
				arrRho[3] = 2.308;
				break;
			case Ni:
				rho0 = 6.05e-8;
				arrRho[0] = 0.615;
				arrRho[1] = 1.672;
				arrRho[2] = 2.532;
				arrRho[3] = 3.660;
				break;
			default:
				rho0 = 0.;
				arrRho[0] = 0.;
				arrRho[1] = 0.;
				arrRho[2] = 0.;
				arrRho[3] = 0.;
			}
			if (T < arrT[0]) {
				rho = rho0 * arrRho[0];
			} else if (T < arrT[1]) {
				rho = rho0 * TEProps.getLinear(arrT[0], arrT[1], arrRho[0], arrRho[1], T);
			} else if (T < arrT[2]) {
				rho = rho0 * TEProps.getLinear(arrT[1], arrT[2], arrRho[1], arrRho[2], T);
			} else if (T < arrT[3]) {
				rho = rho0 * TEProps.getLinear(arrT[2], arrT[3], arrRho[2], arrRho[3], T);
			} else {
				rho = rho0 * arrRho[3];
			}
			return rho;
		}
	}
	/***
	 * Inner conductor material
	 */
	public final Conductor innerConductor;
	/***
	 * Outer conductor material
	 */
	public final Conductor outerConductor;
	/***
	 * Electrical resistance multiplier for inner conductor
	 */
	public final double ReMultInner;
	/***
	 * Electrical resistance multiplier for outer conductor
	 */
	public final double ReMultOuter;
	/***
	 * Additional electrical resistance in Ohm for single semiconductor branch inner junction (counts in conductor)
	 */	
	public final double ReInJunction;
	/***
	 * Additional electrical resistance in Ohm for single semiconductor branch outer junction (counts in conductor)
	 */		
	public final double ReOutJunction;
	
	/***
	 * Total thermal resistance for inner solid layers (per ring), K/W 
	 */
	private final double Rh_innerWall;  
	/***
	 * Total thermal resistance for outer solid layers (per ring), K/W 
	 */
	private final double Rh_outerWall; 
	
	
	public Geom(final ExcelData env) throws InputDataException{
		Main.LOG.println("Geometry");

		Nlayers = (int) env.getDouble(0, 1, "Nlayers", "") + 3;
		layerRin = new double[Nlayers];
		layerThickness = new double[Nlayers];
		layerLambda = new double[Nlayers];
		layerCp = new double[Nlayers];
		layerRho = new double[Nlayers];
		layerCpM_mod = new double[Nlayers];
		layerRh = new double[Nlayers];
		tempCpM = new double[Nlayers];
		
		Xlayer = (int) env.getDouble(1, 1, "Xlayer", "");
		Xvolume_factor = env.getDouble(3, 1, "Xvolume_factor", "");
		RhColdSide = env.getDouble(0, 6, "Rcold", "K*m/W");
		RhHotSide = env.getDouble(1, 6, "Rhot", "K*m/W");
		layerRin[1] = env.getDouble(2, 1, "R[0]", "m");
		
		ringThickness = env.getDouble(2, 6, "Ring thickness", "m");

		for (int i = 1; i < Nlayers - 2; i++) {
			layerThickness[i] = env.getDouble(7 + i - 1, 1);
			layerRin[i+1] = layerRin[i] + layerThickness[i];
			layerLambda[i] = env.getDouble(7 + i - 1, 2);
			layerCp[i] = env.getDouble(7 + i - 1, 3);
			layerRho[i] = env.getDouble(7 + i - 1, 4);
			double area = Math.PI * (layerRin[i+1] * layerRin[i+1] - layerRin[i] * layerRin[i]);
			tempCpM[i] = area * layerRho[i] * layerCp[i] * ringThickness;
			layerRh[i] = 1. / 2. / Math.PI / layerLambda[i] * Math.log(layerRin[i + 1] / layerRin[i]) / ringThickness;
			
		}
		
		layerRh[Xlayer - 1] += RhHotSide / ringThickness;
		layerRh[Xlayer + 1] += RhColdSide / ringThickness;
		
		layerCpM_mod[1] = tempCpM[1] / 2.;
		for (int i = 2; i < Nlayers - 2; i++) {
			layerCpM_mod[i] = tempCpM[i - 1] / 2. + tempCpM[i] / 2.;
		}
		
		layerCpM_mod[Nlayers - 2] = tempCpM[Nlayers - 3] / 2.;
		
		nSegmentsInRing = env.getDouble(0, 9, "Segments in ring (total n + p)", "");
		nRingsInBattery = env.getDouble(1, 9, "Rings in battery", "");
		nBatteries = env.getDouble(2, 9, "Batteries count", "");
		innerConductor = Conductor.get(env.getString(7, 6));
		Main.LOG.println("Inner conductor = " + innerConductor);
		outerConductor = Conductor.get(env.getString(8, 6));
		Main.LOG.println("Outer conductor = " + outerConductor);
		
		ReMultInner = env.getDouble(9, 6, "Inner Re multipyer", "");
		ReMultOuter = env.getDouble(10, 6, "Outer junction Re per segment", "");
		
		ReInJunction = env.getDouble(11, 6, "Inner junction Re per segment", "Ohm");
		ReOutJunction = env.getDouble(12, 6, "Outer junction Re per segment", "Ohm");
		
		
		double Rh_innerWall = 0;
		for (int i = 1; i < Xlayer; i++) {
			Rh_innerWall += 1. / 2. / Math.PI / layerLambda[i] * Math.log(layerRin[i + 1]/layerRin[i]) ;
		}
		this.Rh_innerWall = (Rh_innerWall + RhHotSide);
		
		
		double Rh_outerWall = 0;
		for (int i = Xlayer + 1; i < Nlayers - 2; i++) {
			Rh_outerWall += 1. / 2. / Math.PI / layerLambda[i] * Math.log(layerRin[i + 1]/layerRin[i]) ;
		}
		this.Rh_outerWall = (Rh_outerWall + RhColdSide); 
		
	}
	/***
	 * Update layers properties for fluids (data from 'conditions') 
	 * @param cond
	 */
	public void updateMargins(final Conditions cond) {
		layerRh[0] = 1. / 2. / Math.PI / layerRin[1] / cond.alphaInner / ringThickness; 
		layerRh[Nlayers - 2] = 1. / 2. / Math.PI /layerRin[Nlayers - 2] / cond.alphaOuter / ringThickness;
		layerCpM_mod[0] = cond.areaInner * ringThickness * cond.rhoInner * cond.CpInner;
		
		Main.LOG.println("Constant properties calculation");
		Main.LOG.println( 
				"i   |" +
				"Rout, m   |" + 
				"delta, m  |" +
				"lambda, W/m/K|" +
				"cp, J/kg/K|" +
				"rho, kg/m3|" +
				"CpMPure,   K/W/m|" +
				"CpMCalc,   K/W/m|" +
				"RhForCalc, K/W/m|");
		for (int i = 0; i < Nlayers; i++) {
			Main.LOG.print( 
				String.format(""
						+ "%4d|"
						+ "%10.7f|"
						+ "%10.7f|"
						+ "%13.3f|"
						+ "%10.3f|"
						+ "%10.3f|"
						+ "%16.7f|"
						+ "%16.7f|"
						+ "%16.7f|", 
						i, 
						layerRin[i], 
						layerThickness[i], 
						layerLambda[i], 
						layerCp[i],
						layerRho[i],
						tempCpM[i] / ringThickness,
						layerCpM_mod[i] / ringThickness,
						Math.max( -1., layerRh[i] * ringThickness )
						));

			if (i == 0) {
				Main.LOG.println(" inside");
			} else if (i == Nlayers - 1) {
				Main.LOG.println(" outside (not used)");
			} else if (i == Xlayer) {
				Main.LOG.println(" Xlayer");
			} else {
				Main.LOG.println("");
			}
		}
		Main.LOG.println("Rh (with additional constants), m*W/K \n"
				+ "\tinner = " + Rh_innerWall + "\n"
				+ "\touter = " + Rh_outerWall );
	}
}
