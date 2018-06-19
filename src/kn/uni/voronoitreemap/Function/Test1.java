package kn.uni.voronoitreemap.Function;

import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.uav.Region;

public class Test1 implements Function {
	private static Test1 _instance = null;
	private static double alpha = -0.04;
	
	protected Test1() {};

	@Override
	public double Value(double x, double y) {
		return Math.exp(alpha * Math.sqrt((x - 500) * (x - 500) + (y - 500) * (y - 500)));
	}

	@Override
	public double ValueInRegion(double x, double y, Region region) {
		
		return (region.contains(x, y) ? this.Value(x, y) : 0);
	}


	public static Function getInstance() {
		if (_instance == null) {
			_instance = new Test1();
		}
		return _instance;
	}
	
//	public  static void main(String[] args) {
//		Function f = Test1.getInstance();
//		double[] x = new double[4];
//		double[] y = new double[4];
//		x[0] = 50;
//		x[1] = 100;
//		x[2] = 100;
//		x[3] = 50;
//		y[0] = 50;
//		y[1] = 50;
//		y[2] = 100;
//		y[3] = 100;
//		PolygonSimple p = new PolygonSimple(x, y, 4);
//		double xd = 70, yd = 70;
//		System.out.println("xd = " + xd + " " + "yd = " + yd + " " + f.ValueInRegion(xd, yd, p));
//		for (xd = 0; xd < 1000; xd += 1) {
//			for(yd = 0; yd < 1000; yd +=1) {
//				if (p.contains(new Point2D(xd, yd))) {
//					System.out.println("xd = " + xd + "yd = " + yd + " " + f.ValueInRegion(xd, yd, p));
//				}
//				
//				//System.out.println(p.contains(new Point2D(xd, yd)));
//			}
//		}
//		
//	}

}
