package kn.uni.voronoitreemap.uav;

import kn.uni.voronoitreemap.Function.Function;
import kn.uni.voronoitreemap.j2d.PolygonSimple;

public class Integral {	
	
	public static int count_s = 0;
	public static int count_d = 0;
	
	protected Integral() {}
	
	private static double simpsonX(double left, double right, double pos, Function f, Region r) {
		double mid = (left + right) / 2.0;
		return (f.ValueInRegion(left, pos, r) + 4.0 * f.ValueInRegion(mid, pos, r) + f.ValueInRegion(right, pos, r)) * (right - left) / 6.0;
	}
	
	private static double simpsonY(double left, double right, double pos, Function f, Region r) {
		double mid = (left + right) / 2.0;
		return (f.ValueInRegion(pos, left, r) + 4.0 * f.ValueInRegion(pos, mid, r) + f.ValueInRegion(pos, right, r)) * (right - left) / 6.0;
	}
	
	private static double simpson(double left, double right, double mid, double len) {
		return (left + 4.0 * mid + right) * len / 6.0;
	}

	public static double simpleIntegralX(double left, double right, double pos, double eps, Function f, Region r) {
		double mid = (left + right) / 2.0;
		double ST = simpsonX(left, right, pos, f, r);
		double SL = simpsonX(left, mid, pos, f, r);
		double SR = simpsonX(mid, right, pos, f, r);
		
		if (Math.abs(SL + SR - ST) <= 15.0 * eps) {
			return SL + SR + (SL + SR - ST) / 15.0;
		}
		//System.out.println("s" + (count_s++) + (15.0 * eps - Math.abs(SL + SR - ST)));
		return simpleIntegralX(left, mid, pos, eps, f, r) + simpleIntegralX(mid, right, pos, eps, f, r);
	}

	public static double simpleIntegralY(double left, double right, double pos, double eps, Function f, Region r) {
		double mid = (left + right) / 2.0;
		double ST = simpsonY(left, right, pos, f, r);
		double SL = simpsonY(left, mid, pos, f, r);
		double SR = simpsonY(mid, right, pos, f, r);
		
		if (Math.abs(SL + SR - ST) <= 15.0 * eps) {
			return SL + SR + (SL + SR - ST) / 15.0;
		}
		//System.out.println("s" + (count_s++) + ((15.0 * eps - Math.abs(SL + SR - ST))));
		return simpleIntegralY(left, mid, pos, eps, f, r) + simpleIntegralY(mid, right, pos, eps, f, r);
	}

	public static double doubleIntegral(double left, double right, double bottom, double top, double eps, Function f, Region r) {	
		double mid  = (left + right) / 2.0;
		double midL = (left + mid) / 2.0;
		double midR = (mid + right) / 2.0;
		
		double M = simpleIntegralY(bottom, top, mid, eps, f, r);
		double L = simpleIntegralY(bottom, top, left, eps, f, r);
		double ML = simpleIntegralY(bottom, top, midL, eps, f, r);
		double R = simpleIntegralY(bottom, top, right, eps, f, r);
		double MR = simpleIntegralY(bottom, top, midR, eps, f, r);
		
		double ST = simpson(L, R, M, right - left);
		double SL = simpson(L, M, ML, mid - left);
		double SR = simpson(M, R, MR, right - mid);
		
		if (Math.abs(SL + SR - ST) <= 15.0 * eps) {
			return SL + SR + (SL + SR - ST) / 15.0;
		}
		//System.out.println("d" + (count_d++) + ((15.0 * eps - Math.abs(SL + SR - ST))));
		return doubleIntegral(left, mid, bottom, top, eps , f, r) + doubleIntegral(mid, right, bottom, top, eps, f, r);
	}
	
	public static double regionIntergral(Region region, double eps, Function f) {
		double left = region.getLeft();
		double right = region.getRight();
		double top = region.getTop();
		double bottom = region.getBottom();
		
		return doubleIntegral(left, right, bottom, top, eps, f, region);
	}
	
//	public static void main(String []arg) {
//		Function f = Testf.getInstance();
//		System.out.println(doubleIntegral(0, 1, 0, 1, 1E-10, f));
//
//	}
}

class Testf implements Function {
	
	private static Function _instance = null;
	
	protected Testf() {}
	
	@Override
	public double Value(double x, double y) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public double ValueInRegion(double x, double y, Region region) {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public static Function getInstance() {
		if (_instance == null) {
			_instance = new Testf();
		}
		return _instance;
	}
	
}
