package org.lilian.data.real.fractal.compression;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.data.real.fractal.compress.PIFS;
import org.lilian.data.real.fractal.compress.SSDistance;
import org.lilian.util.Series;
import org.lilian.util.distance.Distance;

public class PIFSTest
{
	private static final int ITERATIONS = 15;
	
//	@Test
//	public void testBlocks() throws IOException
//	{
//		File dir = new File("/Users/Peter/Documents/PhD/output/pifs/");
//		dir.mkdirs();
//		
//		BufferedImage face = null, koala = null, nature = null;
//		try
//		{
//			face = ImageIO.read(new File(dir, "face.jpg"));
//			koala = ImageIO.read(new File(dir, "koala.jpg"));
//			nature = ImageIO.read(new File(dir, "nature.jpg"));			
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		PIFS pifs = new PIFS(face, new int[]{480, 270}, 10, new int[]{240, 135}, 1);
//		pifs.search();
//		
//		System.out.println("Search finished.");
//		
//		File dirOut = new File(dir, "result/");
//		dirOut.mkdirs();
//		
//		pifs.reconstruct(ITERATIONS, koala, dirOut);
//	}
//
}

// 		"PIFS pifs = new PIFS(face, new int[]{480, 270}, 50, new int[]{240, 135});"
//      finished in 467 seconds 