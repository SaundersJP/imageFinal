import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageConverter;
import ij.process.Blitter;

 
public class MaxThreshold_  implements PlugInFilter {
	protected ImagePlus image; 
	
	public void run(ImageProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
		Rectangle roi = ip.getRoi();
		
		ImagePlus maxed = NewImage.createByteImage("maxValImage", w, h, 1, NewImage.FILL_BLACK);
		ImageProcessor max_ip = maxed.getProcessor();
		max_ip.copyBits(ip, 0, 0, Blitter.COPY);

		max_ip = maxed.getProcessor();
		byte[] pixels = (byte[])ip.getPixels();
		
		int max = 0;
		for(int y=roi.y; y < roi.y+roi.height; y++) {
			int offset = w * y;
			for(int x=roi.x; x < roi.x+roi.width; x++) {
				int position = offset + x;
				int currVal = (int)pixels[position];
				if(currVal > max) {
					max = currVal;
				}
			}
		}
		max_ip.max(max);
		ip.copyBits(max_ip, 0, 0, Blitter.COPY);
		image.show();
		image.updateAndDraw();
		IJ.log("Done");
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		// TODO Auto-generated method stub
		image = imp;
		if(arg.equals("about")) {
			showAbout();
			return DONE;
		}
		
		if(image.getType() != ImagePlus.GRAY8) {
			ImageConverter ip_conv = new ImageConverter(image);
			ip_conv.convertToGray8();
		}
		return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
	}
	
	public void showAbout() {
		IJ.showMessage("About TestPlugin_...",
			"This sample plugin is testing \n" +
			"if we can write ImageJ plugins");
	}
}