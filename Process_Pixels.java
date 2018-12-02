/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.mycompany.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.Queue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Process_Pixels implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;

	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();

		if (showDialog()) {
			process(ip);
			image.updateAndDraw();
		}
	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Process pixels");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addNumericField("value", 0.00, 2);
		gd.addStringField("name", "John");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		value = gd.getNextNumber();
		name = gd.getNextString();

		return true;
	}

	/**
	 * Process an image.
	 * <p>
	 * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does
	 * require it; the method
	 * {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return
	 * the results and change the {@link #setup(java.lang.String, ij.ImagePlus)}
	 * method to return also the <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image the image (possible multi-dimensional)
	 */
	public void process(ImagePlus image) {
		// slice numbers start with 1 for historical reasons
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i));
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		int type = image.getType();
		if (type == ImagePlus.GRAY8)
			process((byte[]) ip.getPixels());
		else if (type == ImagePlus.GRAY16)
			process((short[]) ip.getPixels());
		else if (type == ImagePlus.GRAY32)
			process((float[]) ip.getPixels());
		else if (type == ImagePlus.COLOR_RGB)
			process((int[]) ip.getPixels());
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public void process(byte[] pixels) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (byte) value;
			}
		}
	}

	// processing of GRAY16 images
	public void process(short[] pixels) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (short) value;
			}
		}
	}

	// processing of GRAY32 images
	public void process(float[] pixels) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (float) value;
			}
		}
	}

	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (int) value;
			}
		}
	}

	public void showAbout() {
		IJ.showMessage("ProcessPixels", "a template for processing each pixel of an image");
	}

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads an
	 * image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Process_Pixels.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(),
				url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the Clown sample
		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
		image.show();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}

	public ArrayList<int[]> flood(byte[] image, boolean[] filled, int x, int y, int height, int width) {
		Queue<int[]> fillQueue = new LinkedList<int[]>();
		ArrayList<int[]> filledPixels = new ArrayList<int[]>();
		int[][] conn = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };

		int[] pixel = { x, y };
		fillQueue.add(pixel);
		while (fillQueue.size() > 0) {
			pixel = fillQueue.remove();

			if (image[x + y * width] > 0 && !filled[x + y * width]) {
				filled[x + y * width] = true;
				int[] coords = new int[2];
				coords[0] = x;
				coords[1] = y;
				filledPixels.add(coords);
				for (int i = 0; i < conn.length; ++i) {
					int xnew = +conn[i][0];
					int ynew = y + conn[i][1];
					if (xnew >= 0 && xnew < width && ynew >= 0 && ynew < height) {
						int[] newCoords = new int[2];
						newCoords[0] = xnew;
						newCoords[1] = ynew;
						fillQueue.add(newCoords);
					}
				}
			}
		}
		return filledPixels;
	}

	class SortBySize implements Comparator<ArrayList<int[]>>{

		@Override
		public int compare(ArrayList<int[]> a, ArrayList<int[]> b) {
			return a.size() - b.size();
		}
	}
	
	public byte[] GCC(byte[] image, int num, int width, int height) {
		ArrayList<ArrayList<int[]>> connectedComponents = new ArrayList<ArrayList<int[]>>(); 

		boolean[] filled = new boolean[image.length];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (image[x + y * width] > 0 && filled[x + y * width]) {
					connectedComponents.add(flood(image, filled, x, y, width, height));
				}
			}
		}

		byte[] newImage = new byte[image.length];

		Collections.sort(connectedComponents,new SortBySize());

		for(int i = 0; i < num && i < connectedComponents.size(); ++i) {
			ArrayList<int[]> gcc = connectedComponents.get(i);
			for (int[] pixel : gcc) {
				int x = pixel[0];
				int y = pixel[1];
				newImage[x + y * width] = 1;
			}
		}
		return newImage;
	}

	public void checkConn(byte[] image, int x, int y, int width, int height, ij.gui.Roi[] rois) {
		int numSurrounding = 0;
		
		for(int i = 0; i < rois.length; ++i) {
			if (rois[i].contains(x, y)) {
				return;
			}
		}
		
		int[][] conn = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};

		for(int i = 0; i < conn.length; ++i) {
			int xnew = x + conn[i][0];
			int ynew = y + conn[i][1];
			if(xnew >= 0 && xnew < width && ynew >= 0 && ynew < height  && image[xnew + ynew*width] > 0) {
				++numSurrounding;
			}
		}
		if (numSurrounding == 1) {
			image[x + y * height] = 0;
			for(int i = 0; i < conn.length; ++i) {
				int xnew = x + conn[i][0];
				int ynew = y + conn[i][1];
				if(xnew >= 0 && xnew < width && ynew >= 0 && ynew < height  && image[xnew + ynew*width] > 0) {
					checkConn(image, xnew, ynew, width, height, rois);
					return;
				}
			}
		}
		return;
	}
	
	public byte[] trimSperm(byte[] image, int width, int height, ij.gui.Roi[] rois) {
		byte[] nextImage = image.clone();
				
		for(int x = 0; x < width; ++x) {
			for(int y = 0; y < height; ++y) {
				if(image[x+y*height] > 1) {
					checkConn(nextImage, x, y, width, height, rois);
				}
			}
		}
		
		return nextImage;
	}
	
	ArrayList<Point> path;
	
	
	public void createCurve(ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		
		byte[] pixels = (byte[])ip.getPixels();
		int[] pixelToPointMap = new int[pixels.length];
		Arrays.fill(pixelToPointMap, -1);
		ArrayList<Point> points = new ArrayList<Point>();
		ArrayList<ArrayList<Integer>> adjList;
		
		int iter = 0;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if(pixels[x+y*width] > 0) {
					pixelToPointMap[x+y*width] = iter++;
					points.add(new Point(x,y));
				}
			}
		}
		
		int[][] conn= {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
		adjList = new ArrayList<ArrayList<Integer>>(points.size());
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for(int i = 0; i < conn.length; ++i) {
					int pointNum = pixelToPointMap[x+y*width];
					int newx = x + conn[i][0];
					int newy = y + conn[i][1];
					
					if(pixelToPointMap[newx + newy*width] >= 0) {
						int adjPointNum = pixelToPointMap[newx + newy*width];
						adjList.get(pointNum).add(adjPointNum);
					}
				}
			}
		}
	}
	
	public void updateNextPath(Point nextPoint) {
		
	}
	
	public class MouseMove implements PlugIn, MouseMotionListener{

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			updateNextPath(arg0.getPoint());
		}

		@Override
		public void run(String arg) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
