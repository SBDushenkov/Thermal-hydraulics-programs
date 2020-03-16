package freezer;

import java.util.ArrayList;

/***
 * for semiconductor layer thermal flows including Joule for conductors
 * @author sdushenkov
 */
class TECalcRes {
	/***
	 * for next I calculation, A
	 */
	public final double TinnerJunct;
	public final double TouterJunct;
	
	/***
	 * current, A
	 */
	public final double I;
	/**
	 * for printing purposes block
	 */
	private final double Tcold;
	private final double Thot;
		
	private final double xQpeltier;
	private final double xQthermCond;
	private final double xQjoule;
		
	private final double QjouleInnerJunct;
	private final double QjouleOuterJunct;
	
	private final Geom geom;
	
	private final ArrayList<Object> vals = new ArrayList<>();
	
	/***
	 * constants
	 */
	public final PrepareConsts pc;
	/***
	 *   the heat absorbed from the cold side per ring, W
	 *   <i>note: including conductor Joule</i>
	 */
	public final double Qc;
	/***
	 * electrical power consumption per ring, W<br>
	 * <i>note: including conductor Joule</i>
	 */
	public final double Pe;
	/***
	 * coefficient of performance
	 */
	public final double COP;
	/***
	 *  the heat released to the hot side per ring, W
	 *  <i>note: including conductor Joule</i>
	 */
	public final double Qh;
	/***
	 * actual voltage per ring (calculated from I, Re, alpha*dT) 
	 */
	private final double V;

	public TECalcRes(
			final double TinnerJunct, 
			final double TouterJunct, 
			final double I, 
			final Geom geom, 
			final TEProps props) {
		this.TinnerJunct = TinnerJunct;
		this.TouterJunct = TouterJunct;
		this.I = I;
		this.geom = geom;
		if (I > 0) {
			this.Tcold = TinnerJunct;
			this.Thot = TouterJunct;
		} else {
			this.Tcold = TouterJunct;
			this.Thot = TinnerJunct;
		}

		pc = new PrepareConsts(TinnerJunct, TouterJunct, geom, props);

		QjouleInnerJunct = pc.Re_inner * I * I;
		QjouleOuterJunct = pc.Re_outer * I * I;

		xQpeltier = pc.propsVals.alpha * (geom.nSegmentsInRing / 2.) * Main.CtoK(Tcold) * Math.abs(I);
		xQthermCond = (Thot - Tcold) / pc.Rh_semi;
		xQjoule = pc.Re_semi * I * I / 2.;
		Qc = (xQpeltier - xQthermCond - xQjoule) - QjouleInnerJunct;

		Pe = (pc.Re_semi * I * I + pc.propsVals.alpha * I * (TouterJunct - TinnerJunct) * (geom.nSegmentsInRing / 2.))
				+ QjouleInnerJunct + QjouleOuterJunct;

		COP = Qc / Pe;

		Qh = (xQpeltier - xQthermCond - xQjoule) + Pe - QjouleInnerJunct;

		V = I * (pc.Re_semi + pc.Re_inner + pc.Re_outer)
				+ pc.propsVals.alpha * (TouterJunct - TinnerJunct) * (geom.nSegmentsInRing / 2.);
	}

	@Override
	public String toString() {
		String str = "|"
				+ String.format("%10.4f|", TinnerJunct) 
				+ String.format("%10.4f|", TouterJunct)
				+ String.format("%10.4f|", I) 
				+ String.format("%10.4f|", V / geom.ringThickness) 
				+ String.format("%10.4f|", Tcold) 
				+ String.format("%10.4f|", Thot)
				+ String.format("%10.4f|", pc.propsVals.lambda) 
				+ String.format("%10.6f|", pc.propsVals.z) 
				+ String.format("%10.4f|", xQpeltier / geom.ringThickness)
				+ String.format("%10.4f|", xQthermCond / geom.ringThickness)
				+ String.format("%10.4f|", xQjoule / geom.ringThickness)
				+ String.format("%10.4f|", QjouleInnerJunct / geom.ringThickness)
				+ String.format("%10.4f|", QjouleOuterJunct / geom.ringThickness)
				+ String.format("%10.4f|", Qc / geom.ringThickness)
				+ String.format("%10.4f|", Pe / geom.ringThickness) 
				+ String.format("%10.4f|", COP)
				+ String.format("%10.4f|", Qh / geom.ringThickness) 
				+ pc.toString();
		return str;
	}
	public String getHeader() {
		String str = "|" 
				+ "TinJunct  |" 
				+ "ToutJunct |" 
				+ "I         |"
				+ "V         |"
				+ "Tcold     |"
				+ "Thot      |" 
				+ "lambda    |"
				+ "z         |"
				+ "Qpeltier  |" 
				+ "QtCond    |" 
				+ "Qjoule    |" 
				+ "QInJunct  |" 
				+ "QOutJunct |"
				+ "Qc        |" 
				+ "Pe        |" 
				+ "COP       |" 
				+ "Qh        |" 
				+ pc.getHeader();
		return str;
	}

	enum NormalizationType {
		METER, MODULE, BATTERY, RING
	};

	public ArrayList<Object> toStringArray(final NormalizationType type) {
		double mult;
		switch (type) {
		case METER:
			mult = 1. / geom.ringThickness;
			break;
		case MODULE:
			mult = geom.nRingsInBattery * geom.nBatteries;
			break;
		case BATTERY:
			mult = geom.nRingsInBattery;
			break;
		case RING:
			mult = 1.;
			break;
		default:
			mult = 0.;
		}
		vals.clear();
		vals.add(TinnerJunct);
		vals.add(TouterJunct);

		vals.add(I);
		vals.add(V * mult);
		vals.add(Tcold);
		vals.add(Thot);
		vals.add(pc.propsVals.lambda);
		vals.add(pc.propsVals.z);
		vals.add(xQpeltier * mult);
		vals.add(xQthermCond * mult);
		vals.add(xQjoule * mult);
		vals.add(QjouleInnerJunct * mult);
		vals.add(QjouleOuterJunct * mult);
		vals.add(Qc * mult);
		vals.add(Pe * mult);
		vals.add(COP);
		vals.add(Qh * mult);
		vals.addAll(pc.toStringArray(type));
		return vals;
	}

	public ArrayList<Object> getHeaderArray(final NormalizationType type) {
		vals.clear();
		if (type == NormalizationType.METER) {
			vals.add("TinJunct, 'T");
			vals.add("ToutJunct, 'T");
			vals.add("I, A");
			vals.add("V, V / m");
			vals.add("Tcold, 'T");
			vals.add("Thot, 'T");
			vals.add("lambda, W/m/K");
			vals.add("z, 1/K");
			vals.add("Qpeltier, W/m");
			vals.add("QtCond, W/m");
			vals.add("Qjoule, W/m");
			vals.add("QInJunct, W/m");
			vals.add("QOutJunct, W/m");
			vals.add("Qc, W/m");
			vals.add("Pe, W/m");
			vals.add("COP");
			vals.add("Qh, W/m");
		} else {
			vals.add("TinJunct, 'T");
			vals.add("ToutJunct, 'T");
			vals.add("I, A");
			vals.add("V, V");
			vals.add("Tcold, 'T");
			vals.add("Thot, 'T");
			vals.add("lambda, W/m/K");
			vals.add("zT, 1/K");
			vals.add("Qpeltier, W");
			vals.add("QtCond, W");
			vals.add("Qjoule, W");
			vals.add("QInJunct, W");
			vals.add("QOutJunct, W");
			vals.add("Qc, W");
			vals.add("Pe, W");
			vals.add("COP");
			vals.add("Qh, W");
		}
		vals.addAll(pc.getHeaderArray(type));
		return vals;
	}
}