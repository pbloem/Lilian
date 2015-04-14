package org.lilian.data.real.fractal;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Similitude;
import org.lilian.util.Functions;
import org.lilian.util.MatrixTools;

public class MCEM
{
	private List<Point> data;

	private RealVector priors;
	private List<Point> sample;

	private List<RealMatrix> p;
	
	private MVN basis;
	private int iterations = 0;
	private int numDepths;
	
	private int numComponents;
	private int dim;
	private int sampleSize;
	
	public MCEM(List<Point> data, int numComponents, int sampleSize, int maxDepth)
	{
		this.data = data;
		this.numComponents = numComponents;
		this.dim = data.get(0).dimensionality();
		this.numDepths = maxDepth;
		this.sampleSize = sampleSize;
		
		basis = MVN.find(data);
		
		sample = Datasets.sample(data, sampleSize); 

		double startVal = 1.0 / (numComponents * sampleSize);
		
		p = new ArrayList<RealMatrix>(); 
		for(int k : series(numComponents))
			p.add(new Array2DRowRealMatrix(sample.size(), sample.size()).scalarAdd(startVal));
		
		priors = new ArrayRealVector(numDepths).mapAdd(1.0/numDepths);
	}
	
	public MCEM(List<Point> data, IFS<Similitude> ifs, int sampleSize, int maxDepth)
	{
		this.data = data;
		this.numComponents = ifs.size();
		this.dim = data.get(0).dimensionality();
		this.numDepths = maxDepth;
		this.sampleSize = sampleSize;
		
		basis = MVN.find(data);
		
		sample = Datasets.sample(data, sampleSize); 

		p = fromModel(ifs, 0.5);
		
		priors = new ArrayRealVector(numDepths).mapAdd(1.0/numDepths);
	}
	
	private List<RealMatrix> fromModel(IFS<Similitude> ifs, double dev)
	{
		List<RealMatrix> out = new ArrayList<RealMatrix>(); 
		for(int k : series(numComponents))
			out.add(new Array2DRowRealMatrix(sample.size(), sample.size()));

		for(int iFrom : series(sample.size()))
			for(int iTo : series(sample.size()))
				for(int k : series(numComponents))
				{
					double density = MVN.logDensity(
							sample.get(iTo), 
							ifs.get(k).map(sample.get(iFrom)),
							dev);
					
					out.get(k).setEntry(iTo, iFrom, density);
				}
		
		// normalize rows
		for(int iTo : series(sample.size()))
		{
			double [][] rows = new double[numComponents][];
			for(int k : series(numComponents))
				rows[k] = out.get(k).getRow(iTo);
			
			rows = normalizeLog(rows, Math.E);
			for(int k : series(numComponents))
				out.get(k).setRow(iTo, rows[k]);
		}	
		
		return out;
	}

	public void iterate(int branches)
	{		
		List<IFS<Similitude>> models = new ArrayList<IFS<Similitude>>(branches);
		
		List<Integer> depths = new ArrayList<Integer>();
		List<List<RealMatrix>> choices = new ArrayList<List<RealMatrix>>();
		
		List<Double> likelihoods = new ArrayList<Double>();
		
		List<Double> priorsList = new Point(priors);
		
		for(int b : series(branches))
		{	
			int depth = priorsList.size() - 1; // Functions.draw(priorsList);
			
			List<RealMatrix> choice = choose(p);
			IFS<Similitude> model = model(p, sample); 
			
			depths.add(depth);
			choices.add(choice);
			models.add(model);
					
			likelihoods.add(
					model == null ? 
					Double.NEGATIVE_INFINITY : 
					IFS.logLikelihood(sample, model, depth + 1, basis()));
			
			if(b % 100 == 0)
				System.out.print(".");
		}
		System.out.println();
		
		likelihoods = Functions.normalizeLog(likelihoods, Math.E);
		
		// * create the new hidden variables
		
		p = new ArrayList<RealMatrix>(); 
		for(int k : series(numComponents))
			p.add(new Array2DRowRealMatrix(sample.size(), sample.size()));
		
		priors = new ArrayRealVector(numDepths);
		
		for(int b : series(branches))
		{
			double weight = likelihoods.get(b);
			int depth = depths.get(b);
			List<RealMatrix> choice = choices.get(b);
						
			RealVector v = new ArrayRealVector(numDepths);
			v.setEntry(depth, weight);
			priors = priors.add(v);
			
			for(int k : series(numComponents))
				p.set(k, p.get(k).add(choice.get(k).scalarMultiply(weight)));
			
		}
		
		for(int k : series(numComponents))
			System.out.println(MatrixTools.toString(p.get(k), 10) + "\n");
		
		Global.log().info("new priors " + priors);
		
	}
	
	
	public MVN basis()
	{
		return basis;
	}
	
	public IFS<Similitude> model()
	{
		return model(p, sample);
	}

	private List<RealMatrix> choose(List<RealMatrix> p)
	{
		List<RealMatrix> choice = new ArrayList<RealMatrix>();
		for(int k : series(numComponents))
			choice.add(new Array2DRowRealMatrix(sample.size(), sample.size()));
		
		for(int row : series(sampleSize))
		{
			double [][] rows = new double[numComponents][];
			for(int k : series(numComponents))
				rows[k] = p.get(k).getRow(row);
			
			rows = choose(rows);
			for(int k : series(numComponents))
				choice.get(k).setRow(row, rows[k]);
		}
		
		return choice;
	}
	
	private IFS<Similitude> model(List<RealMatrix> correspondences, List<Point> sample)
	{
		IFS<Similitude> ifs = null;
		for(int k : series(correspondences.size()))
		{
			Similitude map = Similitude.find(sample, sample, correspondences.get(k));
			
			if(map == null)
				return null;
			
			if(ifs == null)
				ifs = new IFS<Similitude>(map, 1.0);
			else
				ifs.addMap(map, 1.0);
		}
		
		return ifs;
	}
	
	/**
	 * Samples an index from the given probabilities and returns a set of arrays
	 * with all entries 0 except the given index (which is 1).
	 * 
	 * Note that we assume that the combined values over all rows sum to one.
	 * 
	 * @param probs
	 * @return
	 */
	private double[][] choose(double[][] probs)
	{
		// * copy
    	double[][] out = new double[probs.length][];
    	for(int i : series(probs.length))
    	{
    		out[i] = new double[probs[0].length];
    		for(int j : series(probs[0].length))
    			out[i][j] = 0.0;
    	}
    	
		// * select random element
		double draw = Global.random.nextDouble();
		double total = 0.0;
	
		int i, j;
		boolean done = false;
		for(i = 0; i < probs.length && !done; i++)
			for(j = 0; j < probs[i].length && !done; j++)
		    {
					total += probs[i][j];
					
					if(total > draw)
					{
						out[i][j] = 1.0;
						done = true;
					}
		    }
		
		// account for floating point problems
		if(!done)
		{
			int iMax = probs.length - 1,
			    jMax = probs[iMax].length - 1;
			out[iMax][jMax] = 1.0;
		}

		return out;
	}
	
	private static double[][] normalizeLog(double[][] logs, double base)
   	{    	    	
    	// We're implementing this algorithm:
    	// http://stats.stackexchange.com/questions/66616/converting-normalizing-very-small-likelihood-values-to-probability
    	
    	double[][] ll = new double[logs.length][];
    	for(int i : series(logs.length))
    		ll[i] = new double[logs[0].length];
    	
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			ll[m][i] = logs[m][i];
    	
    	double maxLog = Double.NEGATIVE_INFINITY;
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			maxLog = Math.max(ll[m][i], maxLog);
    	
    	if(maxLog == Double.NEGATIVE_INFINITY)
    		return uniform(logs);
    	
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			ll[m][i] -= maxLog;
    	    	
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			ll[m][i] = Math.pow(base, ll[m][i]);
    	
    	// * No need to check for underflow. Java sets to 0 automatically
    	
    	double sum = 0.0;
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			sum += ll[m][i];
    	
    	for(int m : series(ll.length))
    		for(int i : series(ll[0].length))
    			ll[m][i] = ll[m][i]/sum;;
    		
    	return ll;
    }
	
	public static double[][] uniform(double[][] logs)
	{
    	double[][] ll = new double[logs.length][];
    	int n = 0;
    	for(int i : series(logs.length))
    	{
    		ll[i] = new double[logs[0].length];
    		n += logs[0].length;
    	}
    	
    	for(int i : series(ll.length))
    		for(int j : series(ll[i].length))
    			ll[i][j] = 1.0/n;
    
		return ll;
	}
}
