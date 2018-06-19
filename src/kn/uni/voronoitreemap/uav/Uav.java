package kn.uni.voronoitreemap.uav;

import kn.uni.voronoitreemap.j2d.Site;

public class Uav extends Site {
	
	private Role role;
	
	public Uav(double x, double y) {
		super(x, y);
		role = Role.collect;
	}
	
	public Uav(double x, double y, double weight){
		super(x,y,weight);
		role = Role.collect;
	 }
	 
	 public Uav(double x, double y, double weight, double percentage){
		 super(x,y,weight,percentage); 
		 role = Role.collect;
	 }

	public Role getRole() {
		return role;
	}
	
	public void setRole(Role r) {
		role = r;
	}

	public Uav cloneZeroWeight(){
		Uav uav = new Uav(x, y, 0);
		return uav;
	}
	
	
	public Uav clone(){
		 Uav uav=new Uav(x, y, weight);
		 uav.isDummy=this.isDummy;
		 uav.originalObject=this.originalObject;
		 uav.percentage=this.percentage;
		 
		 return uav;
	 }
}
