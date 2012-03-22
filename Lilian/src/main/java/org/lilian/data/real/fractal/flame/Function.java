package org.lilian.data.real.fractal.flame;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

import org.lilian.data.real.AbstractMap;
import org.lilian.data.real.AffineMap;
import org.lilian.data.real.Map;
import org.lilian.data.real.Point;
import org.lilian.search.Builder;
import org.lilian.search.Parametrizable;

/**
 * A single fractal flame function.
 * 
 * A flame function consists of an affine map, with a weighted combination of 
 * 'variations'
 * 
 * @author peter
 *
 */
public class Function extends AbstractMap implements Parametrizable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8442650914200193668L;

	public static final List<Variation> VARIATIONS = new ArrayList<Variation>();
	
	static 
	{
		VARIATIONS.add(new Linear());
		VARIATIONS.add(new Sinusoidal());
		VARIATIONS.add(new Spherical());
		VARIATIONS.add(new Swirl());
		VARIATIONS.add(new HorseShoe());
		VARIATIONS.add(new Polar());
		VARIATIONS.add(new Handkerchief());
		VARIATIONS.add(new Heart());
		VARIATIONS.add(new Disc());
	}
	
	private AffineMap map;
	private List<Double> vWeights;
	private double weight;
	private Point color;	
	
	public Function(
			AffineMap map, List<Double> vWeights, Point color, double weight)
	{
		this.map = map;
		this.vWeights = new ArrayList<Double>(vWeights);
		this.weight = weight;
		this.color = color;
	}
	
	@Override
	public Point map(Point in)
	{
		Point am = map.map(in);
		
		Point p = new Point(2), pv; //* zero
		
		for(int i = 0; i < VARIATIONS.size(); i++)
		{
			pv = VARIATIONS.get(i).map(am);
			
			p.set(0,  p.get(0) + pv.get(0) * vWeights.get(i));
			p.set(1,  p.get(1) + pv.get(1) * vWeights.get(i));
		}
		
		return p;				
	}

	public double weight()
	{
		return weight;
	}

	public Point color()
	{
		return color;
	}

	@Override
	public boolean invertible()
	{
		return false;
	}

	@Override
	public Map inverse()
	{
		return null;	
	}

	@Override
	public int dimension()
	{
		// TODO Auto-generated method stub
		return 0;
	}		

	@Override
	public List<Double> parameters()
	{
		List<Double> params = new ArrayList<Double>(
				AffineMap.numParameters(2) + VARIATIONS.size() + 4 + 1);
				
		params.addAll(map.parameters());
		params.addAll(vWeights);
		params.addAll(color);
		params.add(weight);
		
		return params;
	}
	
	@Override
	public String toString()
	{
		return "Function [map=" + map + ", vWeights=" + vWeights + ", weight="
				+ weight + ", color=" + color + "]";
	}

	public static Builder<Function> builder()
	{
		return new FunctionBuilder();
	}
	
	private static class FunctionBuilder implements Builder<Function>
	{
		private static Builder<AffineMap> amBuilder = AffineMap.affineMapBuilder(2);
		
		@Override
		public Function build(List<Double> parameters)
		{
			int 	am = amBuilder.numParameters(),
					vw = VARIATIONS.size();
					
			List<Double> cp = new Point(parameters.subList(am + vw, am + vw + 3));
			double sum = abs(cp.get(0)) + abs(cp.get(1)) + abs(cp.get(2));
			Point color = new Point(
					abs(cp.get(0))/sum, 
					abs(cp.get(1))/sum,
					abs(cp.get(2))/sum);
						
			return new Function(
					amBuilder.build(parameters.subList(0, am)),
					parameters.subList(am, am + vw),
					color, Math.abs(parameters.get(am + vw + 3))
				);  
		}

		@Override
		public int numParameters()
		{
			return AffineMap.numParameters(2) + VARIATIONS.size() + 3 + 1;
		}
	}
	
	public static abstract class Variation extends AbstractMap
	{
		private static final long serialVersionUID = -4406616180801658545L;

		@Override
		public boolean invertible()
		{
			return false;
		}

		@Override
		public Map inverse()
		{
			return null;
		}

		@Override
		public int dimension()
		{
			return 2;
		}
	}
	
	
	public static class Linear extends Variation
	{
		private static final long serialVersionUID = -8357471789287756422L;

		@Override
		public Point map(Point in)
		{
			return new Point(in);
		}
	}
	
	public static class Sinusoidal extends Variation
	{
		private static final long serialVersionUID = 6760259066673065633L;

		@Override
		public Point map(Point in)
		{		
			return new Point(
					Math.sin(in.get(0)),
					Math.sin(in.get(1)));
		}
	}
	
	
	public static class Spherical extends Variation
	{
		private static final long serialVersionUID = -2636662408332753623L;

		@Override
		public Point map(Point in)
		{
			double p0 = in.get(0), p1 = in.get(1);
			double rSqRec = 1.0 / (p0*p0 + p1*p1);
			return new Point(p0 * rSqRec, p1 * rSqRec);
		}
	}

	public static class Swirl extends Variation
	{
		private static final long serialVersionUID = -8718104151736355582L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	

			double 	rSq = (p0*p0 + p1*p1),
					cosRSq = Math.cos(rSq), 
					sinRSq = Math.sin(rSq);
			
			return new Point(
				p0 * sinRSq - p1 * cosRSq,
				p0 * cosRSq + p1 * sinRSq);
		}
	}
	
	public static class HorseShoe extends Variation
	{
		private static final long serialVersionUID = 8106385027822994544L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	
		
			double 	r = Math.sqrt(p0*p0 + p1*p1);
					
			return new Point(		
					((p0 - p1) * (p0 + p1)) / r,
					p1 = 2.0 * p0 * p1);
		}
	}

	public static class Polar extends Variation
	{
		private static final long serialVersionUID = -1865523581611349041L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	

			double 	theta = Math.atan(p0/p1),
					r = Math.sqrt(p0*p0 + p1*p1);
			
			return new Point(
					theta/Math.PI,
					r - 1.0);
		}
	}

	public static class Handkerchief extends Variation
	{
		private static final long serialVersionUID = -4202829755640888398L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	

			double 	theta = Math.atan(p0/p1),
					r = Math.sqrt(p0*p0 + p1*p1);
			
			return new Point(
					r * Math.sin(theta + r),
					r * Math.cos(theta - r));
		}
	}

	public static class Heart extends Variation
	{
		private static final long serialVersionUID = -5421384030404522899L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	

			double 	theta = Math.atan(p0/p1),
					r = Math.sqrt(p0*0 + p1*p1);
			
			return new Point(
					r *  Math.sin(theta*r),
					r * -Math.cos(theta*r));
		}
	}

	public static class Disc extends Variation
	{
		private static final long serialVersionUID = -4240945712964484081L;

		@Override
		public Point map(Point in)
		{		
			double p0 = in.get(0), p1 = in.get(1);	
	
			double 	theta = Math.atan(p0/p1),
					r = Math.sqrt(p0*p0 + p1*p1);
			
			return new Point(
					(theta/Math.PI) * Math.sin(Math.PI * r),
					(theta/Math.PI) * Math.cos(Math.PI * r));
		}
	}
}
