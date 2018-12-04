
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
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.util.Queue;

import javax.swing.JTextField;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Gui_ implements PlugInFilter {
	protected ImagePlus image;
	protected ImagePlus copyImage;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;

	Double lowX1;
	Double hiX1;
	Double lowY1;
	Double hiY1;

	Double lowX2;
	Double hiX2;
	Double lowY2;
	Double hiY2;

	public class AddEndpoint1 implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Rectangle rect = IJ.getImage().getRoi().getBounds();
			lowX1 = rect.getMinX();
			hiX1 = rect.getMaxX();
			lowY1 = rect.getMinY();
			hiY1 = rect.getMaxY();
		}

	}

	public class AddEndpoint2 implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Rectangle rect = IJ.getImage().getRoi().getBounds();
			lowX2 = rect.getMinX();
			hiX2 = rect.getMaxX();
			lowY2 = rect.getMinY();
			hiY2 = rect.getMaxY();
		}
	}

	public class Text implements ActionListener {

		JTextField tfield;

		Text(JTextField field) {
			tfield = field;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String num = tfield.getText();
			Integer n = Integer.parseInt(num);
			String args = "k=" + n.toString();
			System.out.println(args);
			IJ.run("Process Pixels ", args);
		}

	}

	public class DisposeWindow implements WindowListener {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			arg0.getWindow().dispose();
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			arg0.getWindow().dispose();

		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	public class CP implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Crop");
			image.updateAndDraw();
			ImageProcessor ip = image.getProcessor();
			updateWidthHeight();
			ImageProcessor copy_ip = new ColorProcessor(width, height);
			copy_ip.copyBits(ip, 0, 0, Blitter.COPY);
			copyImage = new ImagePlus("copied_crop", copy_ip);
		}
	}

	public class MB implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("MaxThreshold ");
		}
	}

	public class EC implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Enhance Contrast", "saturated=.35");
		}
	}

	public class DB implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Maximum...", "radius=1");
		}
	}

	public class AT implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Threshold...");
		}
	}

	public class SB implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Subtract Background...");
		}
	}

	public class TM implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Double[] d = {lowX1,lowY1,hiX1,hiY1,lowX2,lowY2,hiX2,hiY2};
			String args = "";
			for (Double db : d) {
				args = args.concat(db.toString());
				args = args.concat(",");
			}
			args = args.substring(0, args.length()-1);
			 
			IJ.run("Trim ", args);
		}
	}

	public class SK implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Skeletonize");
		}
	}

	public class ME implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ImageStack currStack = image.getStack();
			ImageConverter copyConv = new ImageConverter(copyImage);
			copyConv.convertToGray8();
			currStack.addSlice(copyImage.getProcessor());
			image.setStack(currStack);
			IJ.run("mouseEvent ");
		}
	}

	public class OP implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Open");
		}
	}

	public class CL implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Close-");
		}
	}

	public class DL implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Dilate");
		}
	}

	public class ER implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.run("Erode");
		}
	}

	public class spermGUI extends Frame {
		JTextField editTextArea = new JTextField("1");

		public void windowClosing(WindowEvent e) {
			dispose();
			System.exit(0);
		}

		public spermGUI() {
			setLayout(new FlowLayout());

			add(new Label("Segmentation and Preprocessing"));

			Button cropButton = new Button("Crop");
			cropButton.addActionListener(new CP());
			add(cropButton);

			Button setMaxButton = new Button("Set Max");
			setMaxButton.addActionListener(new MB());
			add(setMaxButton);

			Button subBack = new Button("Subtract Background");
			subBack.addActionListener(new SB());
			add(subBack);

			Button thresholdButton = new Button("Threshold");
			thresholdButton.addActionListener(new AT());
			add(thresholdButton);

			Button enhanceContrastButton = new Button("Enhance Contrast");
			enhanceContrastButton.addActionListener(new EC());
			add(enhanceContrastButton);

			add(new Label("        Shape Extraction        "));

			Button trimButton = new Button("Trim");
			trimButton.addActionListener(new TM());
			add(trimButton);

			Button skelButton = new Button("Skeleton");
			skelButton.addActionListener(new SK());
			add(skelButton);

			Button defineGCCButton = new Button("k-Connected Component");
			add(defineGCCButton);
			defineGCCButton.addActionListener(new Text(editTextArea));
			add(editTextArea);

			Button definePathButton = new Button("Draw User Path");
			add(definePathButton);
			definePathButton.addActionListener(new ME());

			Button setROIButton1 = new Button("Set Endpoint1");
			add(setROIButton1);
			setROIButton1.addActionListener(new AddEndpoint1());

			Button setROIButton2 = new Button("Set Endpoint2");
			add(setROIButton2);
			setROIButton2.addActionListener(new AddEndpoint2());

			Button binaryOpen = new Button("Open");
			add(binaryOpen);
			binaryOpen.addActionListener(new OP());

			Button binaryClose = new Button("Close");
			add(binaryClose);
			binaryClose.addActionListener(new CL());

			Button binaryErode = new Button("Erode");
			add(binaryErode);
			binaryErode.addActionListener(new ER());

			Button binaryDilate = new Button("Dilate");
			add(binaryDilate);
			binaryDilate.addActionListener(new DL());

			setTitle("Sperm GUI");
			setSize(205, 400);
			setVisible(true);

			addWindowListener(new DisposeWindow());

		}

	}

	public void updateWidthHeight() {
		width = image.getWidth();
		height = image.getHeight();
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		spermGUI s = new spermGUI();
		image = imp;
		copyImage = imp.duplicate();
		return DOES_ALL;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();
		return;
	}
}
