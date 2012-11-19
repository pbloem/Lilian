package org.lilian.neural;

import static org.junit.Assert.*;
import static org.lilian.util.Series.series;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Draw;
import org.lilian.data.real.Generators;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Map;
import org.lilian.data.real.Maps;
import org.lilian.data.real.Point;
import org.lilian.util.Series;

public class ThreeLayerTest
{

	@Test
	public void test()
	{
		double rate = 0.001;
		Map target = Maps.henon();
		ThreeLayer model = ThreeLayer.random(2, 10, 0.5, Activations.sigmoid());
		
		List<Point> from = new MVN(2).generate(10000),
		            to = target.map(from);
		
		model.train(from, to, rate, 100);
				
		for(int i : series(20))
		{
			Point p = new MVN(2).generate();
			
			System.out.println();
			System.out.println(target.map(p));
			System.out.println(model.map(p));
		}
		
		File dir = new File("/Users/Peter/Documents/PhD/output/henon");
		dir.mkdirs();
		
		try
		{
			BufferedImage image;
			
			image = Draw.draw(Generators.fromMap(target, new Point(2)), 1000000, 1000, true);
			ImageIO.write(image, "PNG", new File(dir, "henon.png"));
			
			image = Draw.draw(Generators.fromMap(model, new Point(2)), 1000000, 1000, true);
			ImageIO.write(image, "PNG", new File(dir, "henon-model.png"));
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
