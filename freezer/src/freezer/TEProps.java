package freezer;

/***
 * Thermoelectrical properties of semiconductor materials
 * @author sdushenkov
 *
 */
class TEProps {
	private static final int FirstRow = 42;
	private static final int FirstCol = 1;
	
	private final int NTpoints_n;
	private final int NTpoints_p;
	public final double[] T_n;
	public final double[] T_p;
	/**
	 * Seebek coefficient, V/K
	 */
	public final double[] alpha_n;
	public final double[] alpha_p;
	/**
	 * Electrical conductivity 1/Ohm/m
	 */
	public final double[] sigma_n;
	public final double[] sigma_p;
	/**
	 * Thermal conductivity W/m/K
	 */
	public final double[] lambda_n;
	public final double[] lambda_p;


	
	public TEProps(final ExcelData env) throws InputDataException{
		
		int i = TEProps.FirstRow;
		do {
			i++;
		} while(env.cellExist(i, TEProps.FirstCol));
		int NT_n = i - 1 - TEProps.FirstRow;
		
		i = TEProps.FirstRow;
		do {
			i++;
		} while(env.cellExist(i, TEProps.FirstCol + 1));
		int NT_p = i - 1 - TEProps.FirstRow;
		
		this.NTpoints_n = NT_n;
		this.NTpoints_p = NT_p;
		T_n = new double[NT_n];
		T_p = new double[NT_p];
		alpha_n = new double[NT_n];
		alpha_p = new double[NT_p];
		sigma_n = new double[NT_n];
		sigma_p = new double[NT_p];
		lambda_n = new double[NT_n];
		lambda_p = new double[NT_p];
		
		for (i = 0; i < this.NTpoints_n; i++) {
			T_n[i] = env.getDouble(i + FirstRow, FirstCol + 0);
			alpha_n[i] = env.getDouble(i + FirstRow, FirstCol + 2);
			sigma_n[i] = env.getDouble(i + FirstRow, FirstCol + 4);
			lambda_n[i] = env.getDouble(i + FirstRow, FirstCol + 6);
		}
		
		for (i = 0; i < this.NTpoints_p; i++) {
			T_p[i] = env.getDouble(i + FirstRow, FirstCol + 1);
			alpha_p[i] = env.getDouble(i + FirstRow, FirstCol + 3);
			sigma_p[i] = env.getDouble(i + FirstRow, FirstCol + 5);
			lambda_p[i] = env.getDouble(i + FirstRow, FirstCol + 7);
		}
	}
	
	@Override
	public String toString() {
		String str = "" +
				"i   |" +
				"#Tpoint T, 'C        |" + 
				"alpha, V/K           |" +
				"sigma, S*m (1/Ohm/m) |" +
				"lambda, W/m/K" + 
				"\n";
		str +=  
				"    |" + 
				"n         |" + 
				"p         |" +
				"n         |" + 
				"p         |" +
				"n         |" + 
				"p         |" +
				"n         |" + 
				"p" +
				"\n";
		boolean n;
		boolean p;
		for (int i = 0; i < Math.max(NTpoints_n, NTpoints_n); i++) {
			n = i < NTpoints_n;
			p = i < NTpoints_p;	
			str += String.format("%4d|", i);
			str += n ? String.format("%10.3f|", T_n[i]) : "          |";
			str += p ? String.format("%10.3f|", T_p[i]) : "          |";
			str += n ? String.format("%10.3e|", alpha_n[i]) : "          |";
			str += p ? String.format("%10.3e|", alpha_p[i]) : "          |";
			str += n ? String.format("%10.3e|", sigma_n[i]) : "          |";
			str += p ? String.format("%10.3e|", sigma_p[i]) : "          |";
			str += n ? String.format("%10.3f|", lambda_n[i]) : "          |";
			str += p ? String.format("%10.3f|", lambda_p[i]) : "";
			str += "\n";
		}
		return str;
	}

	
	static double getIntAve(
			final double[] T, 
			final double[] data, 
			final double T0, 
			final double T1) {
		int i = T.length - 1;
		while (T0 < T[i]) {
			i--;
			if (i < 0) {
				break;
			}
		}
		int a = i;
		i = 0;
		while (T1 > T[i]) {
			i++;
			if (i >= T.length) {
				break;
			}
		}
		int b = i - 1;
		
		if (a == -1 && b == -1) {
			return data[0];
		} else if ((a == T.length - 1) && (b == T.length -1) ) {
			return data[T.length - 1];
		}
		
		if ( a == b) {
			return (getLinear(T[a], T[a+1], data[a], data[a+1], T0) + 
					getLinear(T[b], T[b+1], data[b], data[b+1], T1) ) / 2.;
		}
		
		double left;
		if ( a < 0 ) {
			left = data[0] * (T[0] - T0);
		} else {
			left = (getLinear(T[a], T[a+1], data[a], data[a+1], T0) + data[a+1]) / 2 * (T[a + 1] - T0);
		}
		
		double center = 0.;
		for (i = a + 1; i < b; i++) {
			center += (data[i] + data[i+1]) / 2. * (T[i+1] - T[i]);
		}
		
		double right;
		if ( b >= T.length - 1 ) {
			right = data[T.length - 1] * (T1 - T[T.length - 1]);
		} else {
			right = (getLinear(T[b], T[b+1], data[b], data[b+1], T1) + data[b]) / 2 * (T1 - T[b]);
		}
		return (left + center + right) / (T1 - T0);
	}
	
	public static double getLinear(
			final double x1, 
			final double x2, 
			final double y1, 
			final double y2, 
			final double x) {
		return y1 + (y2 - y1) / (x2 - x1) * (x-x1);
	}

}