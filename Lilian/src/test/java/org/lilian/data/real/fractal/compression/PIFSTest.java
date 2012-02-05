package org.lilian.data.real.fractal.compression;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.data.real.fractal.compress.PIFS;
import org.lilian.data.real.fractal.compress.SSDistance;
import org.lilian.util.distance.Distance;

public class PIFSTest
{

	@Test
	public void testBlocks()
	{
		File dir = new File("/home/peter/Documents/PhD/output/pifs/");
		dir.mkdirs();
		
		BufferedImage sexy = null, pig = null, nature = null;
		try
		{
			sexy = ImageIO.read(new File(dir, "sexy.bmp"));
			pig = ImageIO.read(new File(dir, "pig.bmp"));
			nature = ImageIO.read(new File(dir, "nature.bw.bmp"));			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PIFS pifs = new PIFS(nature, 120);
		pifs.search();		

	}

}
