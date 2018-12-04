
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
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.util.Queue;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	
	public class MB implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("MaxThreshold ");			
		}
		
		
	}
	
	public class spermGUI extends Frame{
		
		public void runMaxThresh() {

		}
		public spermGUI() {
			setLayout(new FlowLayout());
			
			add(new Label("Segmentation and Preprocessing"));
			
			Button setMaxButton = new Button("Set Max");
			setMaxButton.addActionListener(new MB());
			add(setMaxButton);
			
			Button subBack = new Button("Subtract Background");
			add(subBack);
			
			Button thresholdButton = new Button("Threshold");
			add(thresholdButton);
			
			Button enhanceContrastButton = new Button("Enhance Contrast");
			add(enhanceContrastButton);
			
			add(new Label("        Shape Extraction        "));
			
			Button trimButton = new Button("Trim");
			add(trimButton);
			
			Button definePathButton = new Button("Create Path");
			add(definePathButton);
			
			definePathButton.setEnabled(false);
			
			setTitle("Sperm GUI");
			setSize(205,250);
			setVisible(true);
			
		}
		
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		spermGUI s = new spermGUI();
		image = imp;
		
		return 1;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();

		byte[] pixels = (byte[]) ip.getPixels();
		ij.gui.Roi[] rois = new ij.gui.Roi[0];
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
			
			for (int i = 0; i < rois.length; ++i) {
				if (rois[i].contains(x, y)) {
					continue;
				}
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
					System.out.println("hey");
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
