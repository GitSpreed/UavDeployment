package kn.uni.voronoitreemap.uav;

import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

public class Region extends PolygonSimple {
	private double left, right, top, bottom;
	private double px, py, radius;
	
	public Region(Site simple) {
		super(simple.getPolygon());
		left = Double.MAX_VALUE;
		bottom = Double.MAX_VALUE;
		right = Double.MIN_VALUE;
		top = Double.MIN_VALUE;

		for (int i = 0; i < length; i++) {
			double x = this.x[i];
			double y = this.y[i];
			if (x < left)
				left = x;
			if (x > right)
				right = x;
			if (y < bottom)
				bottom = y;
			if (y > top)
				top = y;
		}
		
		px = simple.getX();
		py = simple.getY();
		radius = Math.sqrt(simple.getWeight());
	}
	
	public double getLeft() {
		return left;
	}
	
	public double getRight() {
		return right;
	}
	
	public double getTop() {
		return top;
	}
	
	public double getBottom() {
		return bottom;
	}
	
	public boolean contains(double a, double b) {
		if (!super.contains(a, b)) return false;
		double distance = Math.sqrt((a - px) * (a - px) + (b - py) * (b - py));
		if (distance > radius) return false;
		return true;
	}
}
