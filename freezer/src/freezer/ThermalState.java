package freezer;

import java.util.ArrayList;
import Jama.Matrix;

/***
 * energy balance dynamic solver
 */
class ThermalState {
	/**
	 * for printing purposes block
	 */
	private final int Nlayers_mod; 
	private final int xLayerInner;
	private final int xLayerOuter;

	private final boolean leftBalanceOk;
	private final boolean rightBalanceOk;
	
	/***
	 * temperatures array<br>
	 * 0 - inner fluid<br> 
	 * 1 - inner wall<br>
	 * ...<br>
	 * xLayerInner - inner junction<br>
	 * xLayerOuter - outer junction<br>
	 * ...<br>
	 * Nlayers-2 - outer wall<br>
	 * (non used) Nlayers-1 - outer fluid <br>
	 */
	public final double[] layerT_mod;
		
	public ThermalState(
			final Geom geom,
			final Conditions cond,
			final double[] layerT_mod, 
			final TECalcRes cr0, 
			final double dt) {
		
		Nlayers_mod = geom.Nlayers - 1; 
		xLayerInner = geom.Xlayer;
		xLayerOuter = geom.Xlayer + 1;
		
		
		double[] a = new double[Nlayers_mod];
		double[] b = new double[Nlayers_mod];
		double[] c = new double[Nlayers_mod];
		double[] d = new double[Nlayers_mod];
		
		for (int i = 0; i < Nlayers_mod ; i++) {
			d[i] = layerT_mod[i] * geom.layerCpM_mod[i];
		}
		if (cr0.I > 0.) {
			d[xLayerInner] -= dt * cr0.Qc;
			d[xLayerOuter] += dt * cr0.Qh;
		} else {
			d[xLayerInner] += dt * cr0.Qh;
			d[xLayerOuter] -= dt * cr0.Qc;
		}
		d[Nlayers_mod - 1] += dt * cond.Touter / geom.layerRh[Nlayers_mod - 1];

		b[0] = 0. + dt / geom.layerRh[0] + 1. * geom.layerCpM_mod[0];
		b[1] = dt / geom.layerRh[0] + dt / 2. / geom.layerRh[1] + 1. * geom.layerCpM_mod[1];
		for (int i = 2; i < xLayerInner; i++) {
			b[i] = dt / 2. / geom.layerRh[i-1] + dt / 2. / geom.layerRh[i] + 1. * geom.layerCpM_mod[i];
		}
		b[xLayerInner] = dt / 2. / geom.layerRh[xLayerInner - 1] + 0. + 1. * geom.layerCpM_mod[xLayerInner];
		
		
		for (int i = xLayerOuter + 1 ; i < Nlayers_mod - 1; i++) {
			b[i] = dt / 2. / geom.layerRh[i-1] + dt / 2. / geom.layerRh[i] + 1. * geom.layerCpM_mod[i];
		}
		b[xLayerOuter] = 0. + dt / 2. / geom.layerRh[xLayerOuter] + 1. * geom.layerCpM_mod[xLayerOuter];
		b[Nlayers_mod - 1] = dt / 2. / geom.layerRh[Nlayers_mod - 1 - 1] + dt / geom.layerRh[Nlayers_mod - 1] + 1. * geom.layerCpM_mod[Nlayers_mod - 1];
		
		a[0] = 0.;
		a[1] =  - dt / geom.layerRh[0];
		for (int i = 2; i < Nlayers_mod; i++) {
			a[i] = - dt / 2. / geom.layerRh[i-1];
		}
		a[xLayerOuter] = 0;
		
		c[0] = - dt / geom.layerRh[0];
		for (int i = 1; i < Nlayers_mod - 1; i++) {
			c[i] = - dt / 2. / geom.layerRh[i];
		}
		c[xLayerInner] = 0.;

			
			
		double[][] arrALeft = new double[xLayerInner + 1][xLayerInner + 1];
		arrALeft[0][0] = b[0];
		arrALeft[0][1] = c[0];
		for (int i = 1; i <= xLayerInner; i++) {
			arrALeft[i][i-1] = a[i];
			arrALeft[i][i] = b[i];
			if (i != xLayerInner) {
				arrALeft[i][i+1] = c[i];
			}
		}
		
		double[][] arrARight = new double[Nlayers_mod - xLayerOuter][Nlayers_mod - xLayerOuter];

		for (int i = 0; i < Nlayers_mod - xLayerOuter - 1; i++) {
			if (i != 0) {
				arrARight[i][i-1] = a[i + xLayerOuter];
			}
			arrARight[i][i] = b[i + xLayerOuter];
			arrARight[i][i+1] = c[i + xLayerOuter];
		}
		
		arrARight[Nlayers_mod - xLayerOuter - 1][Nlayers_mod - xLayerOuter - 1 - 1] = a[Nlayers_mod - 1];
		arrARight[Nlayers_mod - xLayerOuter - 1][Nlayers_mod - xLayerOuter -  1] = b[Nlayers_mod - 1];
		Matrix Aleft = new Matrix(arrALeft);
		Matrix Aright = new Matrix(arrARight);
		Matrix Bleft = new Matrix(xLayerInner + 1, 1);
		Matrix Bright = new Matrix(Nlayers_mod - xLayerOuter , 1);
		for (int i = 0; i < xLayerInner + 1; i++) {
			Bleft.set(i, 0, d[i]);
		}
		for (int i = 0; i < Nlayers_mod - xLayerOuter; i++) {
			Bright.set(i, 0, d[i + xLayerOuter]);
		}
		
		Matrix Tleft = Aleft.solve(Bleft);
		Matrix Tright = Aright.solve(Bright);
		
		this.layerT_mod = new double[Nlayers_mod];
		for (int i = 0; i < xLayerInner + 1; i++) {
			this.layerT_mod[i] = Tleft.get(i, 0);
		}
		for (int i = 0; i < Nlayers_mod - xLayerOuter; i++) {
			this.layerT_mod[i + xLayerOuter] = Tright.get(i, 0);
		}
		
		double balanceDQleft = 0.;
		for (int i = 0; i <= xLayerInner; i++) {
			balanceDQleft += (this.layerT_mod[i] - layerT_mod[i]) * geom.layerCpM_mod[i];
		}
		
		double balanceFlowLeftJunction;
		double balanceFlowRightJunction;
		if (cr0.I > 0.) {
			balanceFlowLeftJunction = - dt * cr0.Qc;
			balanceFlowRightJunction = dt * cr0.Qh;
		} else {
			balanceFlowLeftJunction = dt * cr0.Qh;
			balanceFlowRightJunction = - dt * cr0.Qc;
		}
		double balanceDQRight = 0.;
		for (int i = xLayerOuter; i < Nlayers_mod; i++) {
			balanceDQRight += (this.layerT_mod[i] - layerT_mod[i]) * geom.layerCpM_mod[i];
		}
		double balanceFlowRight = (cond.Touter - this.layerT_mod[Nlayers_mod - 1]) / geom.layerRh[Nlayers_mod - 1] * dt;
		
		
		leftBalanceOk = Math.abs(balanceDQleft - ( balanceFlowLeftJunction)) < 
				Math.abs( balanceFlowLeftJunction) * 1e-6;
		rightBalanceOk = Math.abs(balanceDQRight - (balanceFlowRightJunction + balanceFlowRight)) < 
				Math.abs(balanceFlowRightJunction + balanceFlowRight) * 1e-6;
	}
		
	@Override
	public String toString() {
		String str = "|";
		for (int i = 0; i < Nlayers_mod; i++) {
			str += String.format("%8.3f|", layerT_mod[i]);
		}

		str += leftBalanceOk ? "Ok          |" : "Error       |";
		str += rightBalanceOk ? "Ok          |" : "Error       |";

		return str;
	}
	public String getHeader() {
		String str = "|";
		for (int i = 0; i < Nlayers_mod; i++) {
			if (i == xLayerInner) {
				str += String.format("TinJunct|", i);
			} else if(i == xLayerOuter) {
				str += String.format("ToutJunc|", i);
			} else {
				str += String.format("T%2d     |", i);
			}
		}
		str += "LeftBalance |";
		str += "RightBalance|";
		return str;
	}
	public ArrayList<Object> getHeaderArray() {
		ArrayList<Object> strs = new  ArrayList<Object>();
		for (int i = 0; i < Nlayers_mod; i++) {
			if (i == xLayerInner) {
				strs.add(String.format("TinnerJunction", i));
			} else if(i == xLayerOuter) {
				strs.add(String.format("TouterJunction", i));
			} else {
				strs.add(String.format("T%2d", i));
			}
		}
		strs.add("LeftBalanceOK");
		strs.add("RightBalanceOK");
		return strs;
	}
	public ArrayList<Object> toStringArray() {
		ArrayList<Object> strs = new  ArrayList<Object>();
		for (int i = 0; i < Nlayers_mod; i++) {
			strs.add(layerT_mod[i]);
		}

		strs.add(leftBalanceOk);
		strs.add(rightBalanceOk);

		return strs;
	}
}