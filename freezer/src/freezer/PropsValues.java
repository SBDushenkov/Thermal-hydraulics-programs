package freezer;

public class PropsValues {
	/**
	 * Limits
	 */
	private final double T0;
	private final double T1;
	/***
	 * Seebek coefficient, V/K
	 */
	private final double alpha_n;
	/***
	 * Seebek coefficient, V/K
	 */
	private final double alpha_p;
	/***
	 * Seebek coefficient, V/K
	 */
	public final double alpha;
	/***
	 * Electrical conductivity 1/Ohm/m
	 */
	private final double sigma_n;
	/***
	 * Electrical conductivity 1/Ohm/m
	 */
	private final double sigma_p;
	/***
	 * Electrical conductivity 1/Ohm/m
	 */
	public final double sigma;
	/***
	 * Thermal conductivity W/m/K
	 */
	private final double lambda_n;
	/***
	 * Thermal conductivity W/m/K
	 */
	private final double lambda_p;
	/***
	 * Thermal conductivity W/m/K
	 */
	public final double lambda;
	/***
	 * Figure of merit 1/K
	 */
	public final double z;
	private final boolean intervalCheck;
	private String message = "";
	public PropsValues(TEProps props, double T0, double T1) {
		this.T0 = T0;
		this.T1 = T1;
		message = "TE properties calculation for interval [" + T0 + ", " + T1 + "]";
		
		boolean intervalCheck = true;
		double aveableDataLowerBound = Math.max(props.T_n[0], props.T_p[0]);
		double aveableDataUpperBound = Math.min(props.T_n[props.T_n.length-1], props.T_p[props.T_n.length-1]);
		if (T1 < T0) {
			message += "Warning Temperatures are suspicious. Getting wrong props!!!!"; 
			intervalCheck = false;
		}
		if (T0 < aveableDataLowerBound) {
			message += "Warning lower bound is less then material data (" +	aveableDataLowerBound + "'C)";
			intervalCheck = false;
		} 
		if (T1 > aveableDataUpperBound) {
			message += "Warning upper bound is greater then material data (" + aveableDataUpperBound + "'C)";
			intervalCheck = false;
		} 
		if ( T1 < aveableDataLowerBound || T0 > aveableDataUpperBound) {
			message += "TE data interval [" + aveableDataLowerBound + ", " 
					+ aveableDataUpperBound + "] and target interval are not intersecting";
			intervalCheck = false;
		}
			
		
		this.alpha_n = TEProps.getIntAve(props.T_n, props.alpha_n, T0, T1);
		this.alpha_p = TEProps.getIntAve(props.T_p, props.alpha_p, T0, T1);
		this.alpha = this.alpha_n + this.alpha_p;
		this.sigma_n = TEProps.getIntAve(props.T_n, props.sigma_n, T0, T1);
		this.sigma_p = TEProps.getIntAve(props.T_p, props.sigma_p, T0, T1);
		this.sigma = (this.sigma_n + this.sigma_p) / 2.;
		this.lambda_n = TEProps.getIntAve(props.T_n, props.lambda_n, T0, T1);
		this.lambda_p = TEProps.getIntAve(props.T_p, props.lambda_p, T0, T1);
		this.lambda = (this.lambda_n + this.lambda_p) / 2.;
		this.z = this.alpha * this.alpha 
				/ Math.pow(Math.sqrt(this.lambda_n/this.sigma_n) + 
				Math.sqrt(this.lambda_p/this.sigma_p), 2);
		
		this.intervalCheck = intervalCheck;
	}
	@Override
	public String toString() {
		String str = "" +
				"aveInt [" + T0 + ", " + T1 + "]\n" +
				"                     |" +
				"n               |" +
				"p               |" +
				"aveInt           " +
				"\n";
		str += "alpha, V/K           |";
		str += String.format("%16.5e|", alpha_n);
		str += String.format("%16.5e|", alpha_p);
		str += String.format("%16.5e\n", alpha);
		
		str += "sigma, S*m (1/Ohm/m) |";
		str += String.format("%16.5e|", sigma_n);
		str += String.format("%16.5e|", sigma_p);
		str += String.format("%16.5e\n", sigma);
		
		str += "lambda, W/m/K        |";
		str += String.format("%16.5e|", lambda_n);
		str += String.format("%16.5e|", lambda_p);
		str += String.format("%16.5e\n", lambda);
				
		str += "z = " + z + "\n";
		str += "Interval is Ok = " + intervalCheck; 
		return str;
	}
	
	public String getMessage() {
		return intervalCheck ? "" : message;
	}
}
