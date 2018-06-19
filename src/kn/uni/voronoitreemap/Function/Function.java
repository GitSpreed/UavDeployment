package kn.uni.voronoitreemap.Function;

import kn.uni.voronoitreemap.uav.Region;

public interface Function {
	
	double Value(double x, double y);
	
	double ValueInRegion(double x, double y, Region region);
	
	static Function getInstance() {
		return null;
	}
}
