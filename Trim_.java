
/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ByteProcessor;
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

public class Trim_ implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;
	
	// parameters from IJ
	Double lowX1;
	Double hiX1;
	Double lowY1;
	Double hiY1;

	Double lowX2;
	Double hiX2;
	Double lowY2;
	Double hiY2;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		arg = Macro.getOptions();
		String[] args = arg.split(",");
		lowX1 = Double.parseDouble(args[0]);
		lowY1 = Double.parseDouble(args[1]);
		hiX1 = Double.parseDouble(args[2]);
		hiY1 = Double.parseDouble(args[3]);
		lowX2 = Double.parseDouble(args[4]);
		lowY2 = Double.parseDouble(args[5]);
		hiX2 = Double.parseDouble(args[6]);
		hiY2 = Double.parseDouble(args[7]);
		
		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();

		byte[] pixels = (byte[]) ip.getPixels();
		ij.gui.Roi[] rois = {new ij.gui.Roi(lowX1, lowY1, hiX1-lowX1, hiY1-lowY1), new ij.gui.Roi(lowX2, lowY2, hiX2-lowX2, hiY2-lowY2)};
		byte[] output = trimSperm(pixels, width, height, rois);

		ByteProcessor bp = new ByteProcessor(width, height, output);
		ip.copyBits(bp, 0, 0, Blitter.COPY);
		image.show();
		image.updateAndDraw();
	}

	public byte[] trimSperm(byte[] image, int width, int height, ij.gui.Roi[] rois) {
		Queue<int[]> fillQueue = new LinkedList<int[]>();
		
		byte[] newImage =  image.clone();
		
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (newImage[x + y * height] != 1) {
					int[] pixel = new int[2];
					pixel[0] = x;
					pixel[1] = y;
					fillQueue.add(pixel);
				}
			}
		}
		
		int[][] conn = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };

		while(!fillQueue.isEmpty()) {
			int numSurrounding = 0;
			int[] pixel = fillQueue.remove();
			int x = pixel[0];
			int y = pixel[1];
			
			if(newImage[x + y * width] == 0) {
				continue;
			}
			
			int cont = 0;
			for (int i = 0; i < rois.length; ++i) {
				if (rois[i].contains(x, y)) {
					cont = 1;
				}
			}
			
			if(cont == 1) {
				continue;
			}

			for (int i = 0; i < conn.length; ++i) {
				int xnew = x + conn[i][0];
				int ynew = y + conn[i][1];
				if (xnew >= 0 && xnew < width && ynew >= 0 && ynew < height && newImage[xnew + ynew * width] != 0) {
					++numSurrounding;
				}
			}
			if (numSurrounding == 1) {
				newImage[x + y * width] = 0;
				for (int i = 0; i < conn.length; ++i) {
					int xnew = x + conn[i][0];
					int ynew = y + conn[i][1];
					if (xnew >= 0 && xnew < width && ynew >= 0 && ynew < height) {
						int[] newPixel = new int[2];
						newPixel[0] = xnew;
						newPixel[1] = ynew;
						fillQueue.add(newPixel);
					}
				}
			}
		}
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (newImage[x + y * height] != image[x + y * height]) {
					//System.out.println("hey");
				}
			}
		}
		
		return newImage;
	}

	ArrayList<Point> path;

	public void createCurve(ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();

		byte[] pixels = (byte[]) ip.getPixels();
		int[] pixelToPointMap = new int[pixels.length];
		Arrays.fill(pixelToPointMap, -1);
		ArrayList<Point> points = new ArrayList<Point>();
		ArrayList<ArrayList<Integer>> adjList;

		int iter = 0;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (pixels[x + y * width] != 0) {
					pixelToPointMap[x + y * width] = iter++;
					points.add(new Point(x, y));
				}
			}
		}

		int[][] conn = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
		adjList = new ArrayList<ArrayList<Integer>>(points.size());
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int i = 0; i < conn.length; ++i) {
					int pointNum = pixelToPointMap[x + y * width];
					int newx = x + conn[i][0];
					int newy = y + conn[i][1];

					if (pixelToPointMap[newx + newy * width] >= 0) {
						int adjPointNum = pixelToPointMap[newx + newy * width];
						adjList.get(pointNum).add(adjPointNum);
					}
				}
			}
		}
	}
}
