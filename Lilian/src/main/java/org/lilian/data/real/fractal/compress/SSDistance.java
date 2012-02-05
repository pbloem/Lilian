package org.lilian.data.real.fractal.compress;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.lilian.util.distance.Distance;

/**
 * Sum squared distance between images.
 * 
 * If the images are not the same resolution, the one with fewest pixels is 
 * scaled (by nearest neighbour) to fit the one other.
 *
 *    
 * @author peter
 *
 */
public class SSDistance implements Distance<BufferedImage>
{

	@Override
	public double distance(BufferedImage first, BufferedImage second)
	{
		
		BufferedImage big, small;
		
		
		if(first.getWidth() == second.getWidth() && first.getHeight() == second.getHeight())
		{
			big = first;
			small = second;
		} else
		{
		
			// * Find the number of pixels for each and determine the largest image				
			int sizeA = first.getWidth() * first.getHeight();
			int sizeB = second.getWidth() * second.getHeight();
			
			BufferedImage bigOrig;
			if(sizeA > sizeB)
			{
				bigOrig = first;
				small = second;
			} else {
				bigOrig = second;
				small = first;			
			}
			
			// * Scale down the biggest image
			
			big = scale(small.getWidth(), small.getHeight(), bigOrig);
		}
		
		// * Loop through all pixels and all channels and sum the differences 
		double sum = 0.0;
		
		for(int i = 0; i < big.getWidth(null); i++)
			for(int j = 0; j < big.getHeight(null); j++)
			{
				Color b = new Color(big.getRGB(i, j));
				Color s = new Color(small.getRGB(i, j));
				
				int dR = b.getRed() - s.getRed(),
				    dG = b.getGreen() - s.getGreen(),
				    dB = b.getBlue() - s.getBlue(),
				    dA = b.getAlpha() - s.getAlpha();
				    
				sum += dR*dR/65025.0;
				sum += dG*dG/65025.0;
				sum += dB*dB/65025.0;
				sum += dA*dA/65025.0;				
			}
		
		return sum;
	}

	private BufferedImage scale(int width, int height, BufferedImage original)
	{
		BufferedImage resized = new BufferedImage(width, height, original.getType());
		
	    Graphics2D g = resized.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	    
	    g.drawImage(original, 0, 0, width, height, 0, 0, original.getWidth(), original.getHeight(), null);
	    g.dispose();
	    
	    return resized;
	}
}
