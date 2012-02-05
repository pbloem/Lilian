package org.lilian.data.real.fractal.flame;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.util.GZIPCompressor;
import org.lilian.util.distance.CompressionDistance;
import org.lilian.util.distance.Distance;

public class ExampleTargetTest
{

	@Test
	public void testScore()
	{
		BufferedImage sexy = null, pig = null, nature = null;
		try
		{
			sexy = ImageIO.read(new File("/home/peter/Documents/PhD/output/es_flame/example_1/sexy.bmp"));
			pig = ImageIO.read(new File("/home/peter/Documents/PhD/output/es_flame/example_1/pig.bmp"));
			nature = ImageIO.read(new File("/home/peter/Documents/PhD/output/es_flame/example_1/nature.bmp"));			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Distance<BufferedImage> d = new ExampleTarget.IMGDistance();
		
		System.out.println(d.distance(sexy, sexy));
		System.out.println(d.distance(pig, pig));
		System.out.println(d.distance(nature, nature));
		
		System.out.println();
		
		System.out.println(d.distance(sexy, pig));
		System.out.println(d.distance(sexy, nature));
		System.out.println(d.distance(pig, nature));
	}
	
	@Test
	public void gzipTest()
	{
		GZIPCompressor<String> comp = new GZIPCompressor<String>(1);
		
		String reg = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
		String ran = "0010110001001011101010110101001100101010101010010101110001010100010101001001000100001000011101010110001010100100111000101010100101001001101010011001010010010010001001101101100001010100111111110100000010101001001010101001000100100100101001000100010100100100101001110100100100";		
		System.out.println(comp.compressedSize(reg));
		System.out.println(comp.compressedSize(ran));
		
		CompressionDistance<String> dist = new CompressionDistance<String>(comp);
		System.out.println(dist.distance(reg, reg));
		System.out.println(dist.distance(ran, ran));		
		System.out.println(dist.distance(reg, ran));		
		
	}

}
