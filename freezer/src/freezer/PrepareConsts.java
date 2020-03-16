package freezer;

import java.util.ArrayList;

import freezer.TECalcRes.NormalizationType;

/***
 * for resistances calculation (thermal and electrical for semiconductor layer, electrical for conductors)
 * @author sdushenkov
 */
public class PrepareConsts {
	/**
	 * for debug purposes only block
	 */
	private final double Tcold;
	private final double Thot;

	/***
	 * Thermal resistance of semiconductor layer (per ring), K/W 
	 */
	public final double Rh_semi;
	/***
	 * Electrical resistance of semiconductor layer (per ring), Ohm 
	 */
	public final double Re_semi;
	/***
	 * Electrical resistance of semiconductor layer (per ring), Ohm 
	 */
	/***
	 * Electrical resistance of inner conductor (per ring), Ohm 
	 */
	public final double Re_inner;
	/***
	 * Electrical resistance of outer conductor (per ring), Ohm 
	 */
	public final double Re_outer;
	/***
	 * averaged thermoelectric properties 
	 */
	public final PropsValues propsVals;
	/**
	 * for printing purposes block
	 */
	private final Geom geom;
	private final ArrayList<Object> vals = new ArrayList<>();
		
	public PrepareConsts (double TinnerJunct, double TouterJunct, Geom geom, TEProps props) {
		Tcold = Math.min(TinnerJunct, TouterJunct);
		Thot = Math.max(TinnerJunct, TouterJunct);
		this.propsVals = new PropsValues(props, Tcold, Thot);
		this.geom = geom;
		
		double Re_mat = 1. / 2. / Math.PI / propsVals.sigma  
				* Math.log(geom.layerRin[geom.Xlayer + 1] / geom.layerRin[geom.Xlayer])
				/ geom.ringThickness / geom.Xvolume_factor;
		double Rh_mat = 1. / 2. / Math.PI / propsVals.lambda 
				* Math.log(geom.layerRin[geom.Xlayer + 1] / geom.layerRin[geom.Xlayer])
				 / geom.ringThickness / geom.Xvolume_factor;
		
		Re_semi = Re_mat * geom.nSegmentsInRing * geom.nSegmentsInRing;
		Rh_semi = Rh_mat;
		
		Re_inner = 
				( 
					geom.innerConductor.getConductorRho(TinnerJunct) 
					* 2. * Math.PI * geom.layerRin[geom.Xlayer]
					/ geom.layerThickness[geom.Xlayer - 1] / geom.ringThickness / geom.Xvolume_factor
				) * geom.ReMultInner 
				+ geom.ReInJunction * geom.nSegmentsInRing;
		Re_outer = 
				(
					geom.outerConductor.getConductorRho(TouterJunct) 
					* 2. * Math.PI * geom.layerRin[geom.Xlayer + 1] 
					/ geom.layerThickness[geom.Xlayer + 1] / geom.ringThickness / geom.Xvolume_factor
				) * geom.ReMultOuter +
				geom.ReOutJunction * geom.nSegmentsInRing;
	}
		
	@Override
	public String toString() {
		String str = "|"
				+ String.format("%10.7f|", Rh_semi * geom.ringThickness)
				+ String.format("%10.7f|", Re_semi / geom.ringThickness)
				+ String.format("%10.7f|", Re_inner / geom.ringThickness)
				+ String.format("%10.7f|", Re_outer / geom.ringThickness)
				;
		return str;
	}
	public String getHeader() {
		String str = "|"
				+ "Rh_semi   |"
				+ "Re_semi   |"
				+ "Re_inner  |"
				+ "Re_outer  |"
				;			
		return str;
	}
		
	public ArrayList<Object> toStringArray(NormalizationType type) {
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
		vals.add(Rh_semi / mult);
		vals.add(Re_semi * mult);
		vals.add(Re_inner * mult);
		vals.add(Re_outer * mult);
		return vals;
	}
		
		
	public ArrayList<Object> getHeaderArray(NormalizationType type) {
		vals.clear();
		if (type == NormalizationType.METER) {
			vals.add("Rh_semi, K*m/W");
			vals.add("Re_semi, Ohm/m");
			vals.add("Re_inner, Ohm/m");
			vals.add("Re_outer, Ohm/m");
		} else {
			vals.add("Rh_semi, K/W");
			vals.add("Re_semi, Ohm");
			vals.add("Re_inner, Ohm");
			vals.add("Re_outer, Ohm");
		}
		return vals;
	}
}