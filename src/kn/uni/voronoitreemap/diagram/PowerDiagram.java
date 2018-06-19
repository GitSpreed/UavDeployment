/*******************************************************************************
 * Copyright (c) 2013 Arlind Nocaj, University of Konstanz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * For distributors of proprietary software, other licensing is possible on request: arlind.nocaj@gmail.com
 * 
 * This work is based on the publication below, please cite on usage, e.g.,  when publishing an article.
 * Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864
 ******************************************************************************/
package kn.uni.voronoitreemap.diagram;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import kn.uni.voronoitreemap.Function.Function;
import kn.uni.voronoitreemap.Function.Test1;
import kn.uni.voronoitreemap.convexHull.HEdge;
import kn.uni.voronoitreemap.convexHull.JConvexHull;
import kn.uni.voronoitreemap.convexHull.JFace;
import kn.uni.voronoitreemap.convexHull.JVertex;
import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.debuge.ImageFrame;
import kn.uni.voronoitreemap.j2d.Point2D;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;
import kn.uni.voronoitreemap.uav.Role;
import kn.uni.voronoitreemap.uav.Integral;
import kn.uni.voronoitreemap.uav.Region;



/**
 * Computes the PowerDiagram by using the convex hull of the transformed half
 * planes of the Sites.
 * 
 * @author Arlind Nocaj
 * 
 */
public class PowerDiagram {

	public static Random rand = new Random(99);
	public static final int halfLineScalingFactor = 10000;
	private static final double numericError = 1E-10;
	public static boolean debug = true;
	public static ImageFrame frame;
	public static Graphics2D graphics;
	private static int numOfUav = 50;

	protected JConvexHull hull = null;
	protected OpenList Sites;
	protected PolygonSimple clipPoly;
	private int amountPolygons;
	private Rectangle2D bb;
	protected List<JFace> facets = null;

	// set of Sites which forms a rectangle that is big enough to bound a
	// diagram with creating a bisector in the clipping polygon
	Site s1;
	Site s2;
	Site s3;
	Site s4;

	public PowerDiagram() {
		Sites = null;
		clipPoly = null;
	}

	public PowerDiagram(OpenList Sites, PolygonSimple clipPoly) {
		setSites(Sites);
		setClipPoly(clipPoly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diagram.iPowerDiagram#setSites(datastructure.OpenList)
	 */
	public void setSites(OpenList Sites) {
		this.Sites = Sites;
		hull = null;
	}

	public void setClipPoly(PolygonSimple polygon) {
		clipPoly = polygon;
		bb = polygon.getBounds2D();
		// create Sites on a rectangle which is big enough to not create
		// bisectors which intersect the clippingPolygon
		double minX = bb.getMinX();
		double minY = bb.getMinY();

		double width = bb.getWidth();
		double height = bb.getHeight();

		s1 = new Site(minX - width, minY - height);
		s2 = new Site(minX + 2 * width, minY - height);
		s3 = new Site(minX + 2 * width, minY + 2 * height);
		s4 = new Site(minX - width, minY + 2 * height);

		s1.setAsDummy();
		s2.setAsDummy();
		s3.setAsDummy();
		s4.setAsDummy();

	}

	public PolygonSimple getClipPoly() {
		return clipPoly;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see diagram.iPowerDiagram#computeDiagram()
	 */
	public void computeDiagram() {

		if (Sites.size > 0) {
			//Sites.permutate();

			hull = new JConvexHull();
			Site[] array = Sites.array;
			int size = Sites.size;
			for (int z = 0; z < size; z++) {
				Site s = array[z];
				if (Double.isNaN(s.getWeight())){
			
//					s.setWeight(0.001);
					throw new RuntimeException(
							"Weight of a Site may not be NaN.");
				}
				hull.addPoint(s);
			}

			// reset the border Sites, otherwise they could have old data
			// cached.
			s1.clear();
			s2.clear();
			s3.clear();
			s4.clear();

			hull.addPoint(s1);
			hull.addPoint(s2);
			hull.addPoint(s3);
			hull.addPoint(s4);

			// long start = System.currentTimeMillis();
			facets = hull.compute();

			// long end = System.currentTimeMillis();
			// double seconds = end - start;
			// seconds = seconds/1000.0;
			// System.out.println("Hull needed seconds: " + seconds);

			computeData();
		}
	}

	public void writeHullTestCodeOut(Site s) {
		System.out.println("hull.addPoint(" + s.x + "," + s.y + "," + s.z
				+ ");");
	}


	/**
	 * For each Site the corresponding polygon and the corresponding neighbours
	 * are computed and stored in the Site.
	 */
	private void computeData() {

		// make all vertices visible. When we finished working on one we make
		// invisible to not do it several times
		int vertexCount = hull.getVertexCount();
		boolean[] verticesViSited = new boolean[vertexCount];
		// for (int i = 0; i < vertexCount; i++) {
		//
		// JVertex v = hull.getVertex(i);
		//
		// Site Site = (Site) v;
		// Site.setPolygon(null);
		// Site.setNeighbours(null);
		//
		// v.setHandled(true);
		// }

		int facetCount = facets.size();
		for (int i = 0; i < facetCount; i++) {
			JFace facet = facets.get(i);

			if (facet.isVisibleFromBelow()) {

				for (int e = 0; e < 3; e++) {
					// got through the edges and start to build the polygon by
					// going through the double connected edge list
					HEdge edge = facet.getEdge(e);
					JVertex destVertex = edge.getDest();
					Site Site = (Site) destVertex.originalObject;

					if (!verticesViSited[destVertex.getIndex()]) {

						verticesViSited[destVertex.getIndex()] = true;
						if (Site.isDummy) {
							continue;
						}

						// faces around the vertices which correspond to the
						// polygon corner points
						ArrayList<JFace> faces = getFacesOfDestVertex(edge);
						PolygonSimple poly = new PolygonSimple();
						double lastX = Double.NaN;
						double lastY = Double.NaN;
						double dx = 1;
						double dy = 1;
						for (JFace face : faces) {
							Point2D point = face
									.getDualPoint();
							double x1 = point.getX();
							double y1 = point.getY();
							if (!Double.isNaN(lastX)) {

								dx = lastX - x1;
								dy = lastY - y1;
								if (dx < 0) {
									dx = -dx;
								}
								if (dy < 0) {
									dy = -dy;
								}
							}
							if (dx > numericError || dy > numericError) {

								poly.add(x1, y1);
								lastX = x1;
								lastY = y1;
							}

						}
						Site.nonClippedPolyon = poly;

						if (!Site.isDummy) {
//							try {
								Site.setPolygon(clipPoly.convexClip(poly));

//							} catch (Exception ex) {
//
//								ex.printStackTrace();
//								
//								// TODO fallback for nonconvex clipping
//							}
						}

					}
				}
			}

		}
	}

	/**
	 * Return the faces which are visible from below
	 * 
	 * @param edge
	 * @return
	 */
	private ArrayList<JFace> getFacesOfDestVertex(HEdge edge) {
		ArrayList<JFace> faces = new ArrayList<JFace>();
		HEdge previous = edge;
		JVertex first = edge.getDest();

		Site Site = (Site) first.originalObject;
		ArrayList<Site> neighbours = new ArrayList<Site>();
		do {
			previous = previous.getTwin().getPrev();

			// add neighbour to the neighbourlist
			Site SiteOrigin = (Site) previous.getOrigin().originalObject;
			if (!SiteOrigin.isDummy) {
				neighbours.add(SiteOrigin);
			}
			JFace iFace = previous.getiFace();

			if (iFace.isVisibleFromBelow()) {

				faces.add(iFace);
			}
		} while (previous != edge);
		Site.setNeighbours(neighbours);
		return faces;
	}

	
	public void setAmountPolygons(int amountPolygons) {
		this.amountPolygons = amountPolygons;
	}

	public int getAmountPolygons() {
		return amountPolygons;
	}

	public static void initDebug() {
//		if (graphics == null) {
			BufferedImage image = new BufferedImage(2000, 2000,
					BufferedImage.TYPE_INT_RGB);

			frame = new ImageFrame(image);
			frame.setVisible(true);
			frame.setBounds(20, 20, 1600, 800);
			graphics = image.createGraphics();
			graphics.translate(200, 200);
//		}
	}

	public static void main(String[] args) {
		
		PowerDiagram diagram = new PowerDiagram();

		// normal list based on an array
		OpenList Sites = new OpenList();

		Random rand = new Random(100);
		// create a root polygon which limits the voronoi diagram.
		// here it is just a rectangle.

		PolygonSimple rootPolygon = new PolygonSimple();
		int width = 1000;
		int height = 1000;
		
		rootPolygon.add(0, 0);
		rootPolygon.add(width, 0);
		rootPolygon.add(width, height);
		rootPolygon.add(0, height);
		
		// create 100 points (Sites) and set random positions in the rectangle defined above.
		for (int i = 0; i < numOfUav; i++) {
			Site Site = new Site(rand.nextInt(width), rand.nextInt(width));
			// we could also set a different weighting to some Sites
			Site.setWeight(300);
			Sites.add(Site);
		}
		//System.out.println(Sites.size);
		
		// set the list of points (Sites), necessary for the power diagram
		diagram.setSites(Sites);
		// set the clipping polygon, which limits the power voronoi diagram
		diagram.setClipPoly(rootPolygon);
		
		// do the computation
		diagram.computeDiagram();
		
		// for each Site we can no get the resulting polygon of its cell. 
		// note that the cell can also be empty, in this case there is no polygon for the corresponding Site.
		/*PolygonSimple polygon = Sites.array[0].getPolygon();
		for (int i=0;i<Sites.size;i++){
			Site Site=Sites.array[i];
			PolygonSimple polygons=Site.getPolygon();
			Site.setPolygon(polygons);
			
		}*/
		
		System.out.println("test");
		diagram.showDiagram();
		diagram.printPoint();
		Function f = Test1.getInstance();
		System.out.println(Integral.regionIntergral(new Region(Sites.get(0)), 0.1, f));
		
		// iterate
		int count = 0;
		double pre = 0, v = 0;
		do {
			pre = v;
			v = diagram.ComputeValue();
			diagram.Update();
			count++;
			System.out.println("count" + count + ": " + v);
//			if (count % 50 == 0) {
//				diagram.showDiagram();
//			}
			
		} while(Math.abs(v - pre) > 0.1);
		//System.out.println(diagram.ComputeValue());
		diagram.printPoint();
		System.out.println("single:" + Integral.count_s);
		System.out.println("double:" + Integral.count_d);
		diagram.showDiagram();
	} 

	private void Update() {
		int index = 0;
		double maxValue = 0;
		OpenList temp = new OpenList();
		Function f = Test1.getInstance();
		for (int i = 0; i < Sites.size; i++) {
			Site s = Sites.get(i).clone();
			PolygonSimple polygon = Sites.get(i).getPolygon();
			int n = polygon.getNumPoints();
			double[] x = polygon.getXPoints();
			double[] y = polygon.getYPoints();
			double maxX = x[0], maxY = y[0];
			for (int j = 1; j < n; j++) {
				if (f.Value(x[j], y[j]) > f.Value(maxX, maxY)) {
					maxX = x[j];
					maxY = y[j];
				}
			}
			double radius = s.getWeight();
			double diffx = s.getX() - maxX;
			double diffy = s.getY() - maxY;
			double distance = diffx * diffx + diffy * diffy;
			double scale = Math.sqrt(radius / distance);
			//System.out.println("x: " + (maxX + diffx * scale) + "   y: " + (maxY + diffy * scale));
			Sites.get(i).setXY(maxX + diffx * scale, maxY + diffy * scale);
			computeDiagram();
			double v = ComputeValue();
			if (v > maxValue) {
				maxValue = v;
				index = i;
			}
			temp.add(Sites.get(i));
			Sites.set(i, s);
			//computeDiagram();
		}
		Sites.set(index, temp.get(index));
		computeDiagram();
	}

	private double ComputeValue() {
		double ret = 0;
		double eps = 0.001;
		double a = 1, b = 0.1;
		Function f = Test1.getInstance();
		for (int i = 0; i < Sites.size; i++) {
			Site site = Sites.get(i);
			Role role = site.getRole();
			if (role == Role.collect) {
				ret += a * Integral.regionIntergral(new Region(site), eps, f);
			} else if (role == Role.translate) {
				for (int j = 0; j < Sites.size; j++) {
					if (i == j) continue;
					Site s = Sites.get(j);
					ret += b * Integral.regionIntergral(new Region(s), eps, f) * Math.exp(site.getPoint().distance(s.getPoint()));
				}
			}
		}
		return ret;
	}
	
	private void printPoint() {
		for (int i = 0; i < numOfUav; i++) {
			System.out.println("x: "+Sites.get(i).getX() + "   y: "+Sites.get(i).getY());
		}
	}

	public void showDiagram() {
		initDebug();

		graphics.clearRect(0, 0, 1600, 800);
		graphics.setColor(Color.blue);
		graphics.scale(1/1.2, 1/1.2);

		Site[] array = Sites.array;
		int size = Sites.size;
		for (int z = 0; z < size; z++) {
			Site s = array[z];
			s.paint(graphics);

			PolygonSimple poly = s.getPolygon();
			if (poly != null) {
				graphics.draw(poly);
			} else {
				System.out.println("Poly null of:" + s);
			}
		}
		frame.repaint();
		 /*draw(s1);
		 draw(s2);
		 draw(s3);
		 draw(s4);

		 ArrayList<HLine> lines = this.getLines();
		 for (HLine line : lines) {
		 line.paint(graphics);
		 }
		 HashMap<Point2D.Double, HashSet<Site>> vertices = getVertices();
		 for (Point2D.Double p:vertices.keySet()){
		 System.out.println("Vertex:"+p);
		 graphics.setColor(Color.pink);
		 int radius = (int)Math.sqrt(3);
		 graphics.drawOval((int)p.getX()-20, (int)p.getY()-20, 2*20, 2*20);
		
		 }
*/
	}

	public void draw(Site s) {
		s.paint(graphics);

		PolygonSimple poly = s.getPolygon();
		if (poly != null) {
			graphics.draw(poly);
		} else {
			System.out.println("Poly null of:" + s);
		}
	}
}
