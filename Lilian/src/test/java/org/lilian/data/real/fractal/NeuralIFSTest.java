package org.lilian.data.real.fractal;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.data.real.Draw;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.neural.ThreeLayer;
import org.lilian.util.Functions;
import org.lilian.util.Series;

public class NeuralIFSTest
{

	@Test
	public void test()
	{
		IFS<Similitude> master = IFSs.sierpinskiSim();
		IFS<ThreeLayer> neural = NeuralIFS.copy(master, 3, 10000000, 0.0001, 0.5);
		
		for(int i : series(50))
		{
			// Point p = new MVN(2).generate();
			Point p = master.generator().generate();
			
			System.out.println(IFS.code(master, p, 6));
			System.out.println(NeuralIFS.search(p, neural, 6, 0.9, 1).code());
			System.out.println();
		}
		
	}
	
    @Test
	public void testCodeSmooth() throws IOException
	{
		int depth = 7;
		double[] xrange = new double[]{-1.0, 1.0};
		double[] yrange = new double[]{-1.0, 1.0};
		
		Functions.tic();		
		
		BufferedImage image = null;

		File dir = new File("/Users/Peter/Documents/PhD/output/ann-codes-smooth");
		dir.mkdirs();

		IFS<Similitude> model = IFSs.sierpinskiSim();
		IFS<ThreeLayer> ifs = NeuralIFS.copy(model, 2, 10000000, 0.001, 0.5);
		
		image = Draw.draw(ifs.generator(depth), 100000, 1000, true); 
		ImageIO.write(image, "PNG", new File(dir, "ifs.png") );
		
		for(int i : Series.series(1, 20))
		{
			image = NeuralIFS.drawMultiCodes(ifs, xrange, yrange, 100, depth, i);
			ImageIO.write(image, "PNG", new File(dir, "codes-smooth-"+i+".png") );
		}

		
		System.out.println("codes-again: " + Functions.toc() + " seconds");
	}	

}
