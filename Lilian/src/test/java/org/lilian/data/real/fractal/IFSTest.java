package org.lilian.data.real.fractal;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.Global;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Point;

public class IFSTest
{

	@Test
	public void testGenerator()
	{	
		Global.random = new Random();
//		IFS<AffineMap> sierpinski = IFSs.cantorB();
//		IFS<AffineMap> sierpinski = IFSs.sierpinskiOff();
		IFS<AffineMap> sierpinski = IFSs.random(2, 3, 0.33);
		
		
		System.out.println(sierpinski);
		
		BufferedImage image = Draw.draw(sierpinski.generator(), 1000000, 1000, true);
		try
		{
			ImageIO.write(image, "PNG", new File("/Users/Peter/Documents/PhD/output/out.png"));
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
