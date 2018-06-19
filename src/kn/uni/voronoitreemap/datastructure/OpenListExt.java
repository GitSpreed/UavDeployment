package kn.uni.voronoitreemap.datastructure;


import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.uav.Uav;



/**
 * Direct accessable array list for faster iteration.
 * @author Arlind Nocaj	
 *
 * @param <E> type of Elements
 */
public class OpenListExt implements Iterable<Uav>{

	public Uav[] array;
	public int size=0;
	
	private static Random rand=new Random(1985);
	public OpenListExt(){
		this(10);
	}
	@SuppressWarnings("unchecked")
	public OpenListExt(int capacity){
		
		array=new Uav[capacity];

	}
	
	private void increaseCapacity(){
			int newCapacity = (array.length * 3)/2 + 1;
			array = Arrays.copyOf(array, newCapacity);
	}
	
	public boolean add(Uav e) {
		if (size>(array.length-1)){
			increaseCapacity();
		}
		array[size]=e;
		size++;
		return true;
	}


//	public void add(int index, E element) {
//		// TODO Auto-generated method stub
//		
//	}


	public void clear() {
		size=0;		
	}



	public Uav get(int index) {
		if (index<size){
			return array[index];
		}
		return null;
	}

	public boolean isEmpty() {
		return size==0;
	}

	public Uav set(int index, Uav element) {
		if (index<size){
		array[index]=element;
		}
		return element;
	}

	
	/**
	 * Clones the list of Uavs but with zero weights
	 */
	public OpenListExt cloneWithZeroWeights()  {
		OpenListExt neu=new OpenListExt(size+1);
		neu.size=size;
		for (int i=0;i<size;i++){
			Uav s=array[i];
			neu.array[i]= s.cloneZeroWeight();
		}
		return neu;
		
	}
	
	public OpenListExt clone()  {
		OpenListExt neu=new OpenListExt(size+1);
		neu.size=size;
		for (int i=0;i<size;i++){
			Uav s=array[i];
			neu.array[i]=s.clone();
		}
		return neu;
		
	}
	
	public void permutate(){
		
		for(int i = 0; i < size; ++i){
			int ra = rand.nextInt(size);
			Uav temp = array[ra];
			array[ra]=array[i];
			array[i]=temp;
		}
	}
	
	
	@Override
	public Iterator<Uav> iterator() {
		return new Iterator<Uav>() {
			
			int i=0;
			@Override
			public boolean hasNext() {
			return i<size;			
			}

			@Override
			public Uav next() {				
				return array[i++];				
			}

			@Override
			public void remove() {
				
			}
		};
	}
	
	public PolygonSimple getBoundsPolygon(double offset){
		if(offset<0) return null;
		
		Rectangle2D rect = getBounds();
		
		double x=rect.getMinX();
		double y=rect.getMinY();
		double w=rect.getWidth();
		double h=rect.getHeight();
		x-=offset;
		y-=offset;
		w+=offset;
		h+=offset;
		
		PolygonSimple poly=new PolygonSimple(4);
		poly.add(x,y);
		poly.add(x+w,y);
		poly.add(x+w,y+h);
		poly.add(x,y+h);
			return poly;
	}
	
	public Rectangle2D getBounds(){
		PolygonSimple simple=new PolygonSimple(this.size);
		for(Uav s:this)
			simple.add(s.x,s.y);
		
		return simple.getBounds2D();		
	}

}
