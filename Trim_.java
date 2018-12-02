
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

		byte[] pixels = (byte[]) ip.getPixels();
		ij.gui.Roi[] rois = new ij.gui.Roi[0];
		byte[] output = trimSperm(pixels, width, height, rois);

		ByteProcessor bp = new ByteProcessor(width, height, output);
		ip.copyBits(bp, 0, 0, Blitter.COPY);
		image.show();
		image.updateAndDraw();
	}
	
	/**
	 * Process an image.
	 * <p>
	 * Please provide this method even if {@link IJ.plugin.filter.PlugInFilter} does
	 * require it; the method
	 * {@link IJ.plugin.filter.PlugInFilter#run(IJ.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return
	 * the results and change the {@link #setup(java.lang.String, IJ.ImagePlus)}
	 * method to return also the <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image
	 *            the image (possible multi-dimensional)
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
	 * @param args
	 *            unused
	 */

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

	public void updateNextPath(Point nextPoint) {

	}

	public class MouseMove implements PlugIn, MouseMotionListener {

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
