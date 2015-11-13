package org.lilian.data.real.fractal;

import static java.lang.Math.E;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static org.lilian.data.real.Point.fromRaw;
import static org.lilian.util.MatrixTools.diag;
import static org.lilian.util.MatrixTools.ones;
import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.batik.css.engine.SystemColorSupport;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.Global;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Rotation;
import org.lilian.data.real.Similitude;
import org.lilian.data.real.SimilitudeTest;
import org.lilian.util.Functions;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

import weka.core.SystemInfo;

public class EM
{
	public static final double SPLIT_VAR = 0.01;
	private static final double S_APPROX = 0.01;
	
	private List<Point> data, dataSub;
	private int sub;
	private int dimension;
	private IFS<Similitude> model;
	
	private RealMatrix p, t;
	private RealVector z;
	
	private List<RealMatrix> pk;
	private List<RealVector> zk;
	
	private List<Double> depths;
	private Similitude post;

	private static MVN mvn = new MVN(2);
	
	public EM(List<Point> data, int sub, IFS<Similitude> initial, int maxDepth, boolean centerPost)
	{
		this.data = data;
		this.dimension = data.get(0).size();
		this.model = initial;
		this.sub = sub;
		
		p = p(sub, model.size(), maxDepth);
		
		t = t(dimension, model.size(), maxDepth);
		z = z(sub, model.size(), maxDepth);
		
		pk = pk(sub, model.size(), maxDepth);
		zk = zk(sub, model.size(), maxDepth);
		
		depths = new ArrayList<Double>(maxDepth + 1);
		for(int i : series(maxDepth + 1))
			depths.add(1.0);
		logNorm(E, depths);
		
		if(centerPost)
		{
			int numAngles = (dimension * dimension - dimension) / 2;
			post = new Similitude(1.0, mean(data), new Point(numAngles));
		} else
			post = Similitude.identity(dimension);

		resample();
	}
	
	private void norm(List<Double> list)
	{
		double sum = 0.0;
		for(double d : list)
			sum += d;
		
		for(int i : series(list.size()))
			list.set(i, list.get(i)/sum);
	}

	public void setDepths(List<Double> depths)
	{
		this.depths.clear();
		this.depths.addAll(depths);
		for(int i : series(depths.size()))
			this.depths.set(i, Math.log(depths.get(i)));
		
		logNorm(E, this.depths);
	}
	
	public void resample()
	{
		if(sub == -1)
			dataSub = data;
		else
			dataSub = Datasets.sample(data, sub);
	}
	
	public void iterate()
	{
		iterate(1.0, 1.0, 1.0);
	}
	
	/**
	 * 
	 * @param depthIts probability that the depths are updated
	 * @param modelIts
	 * @param postIts
	 */
	public void iterate(double depthIts, double modelIts, double postIts)
	{
		iterate(depthIts, modelIts, postIts, false);
	}
	
	public void iterate(double depthIts, double modelIts, double postIts, boolean approx)
	{
		List<Double> newDepths = depths;
		IFS<Similitude> newModel = model;
		Similitude newPost = post;
		
		resample();

		expectation(approx);

		if(Global.random.nextDouble() < depthIts)
			newDepths = maximizeDepths(model.size(), depths.size() - 1, p);
		
		int maxDepth = depths.size() - 1;
		
		if(Global.random.nextDouble() < modelIts)
		{	
			RealMatrix tf;
			RealVector zf;
			
			int k = model.size();

			tf = t.getSubMatrix(
					0, t.getRowDimension() - 1, 
					0, t.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);
			
			zf = z.getSubVector(0, z.getDimension() - (int)Math.pow(k, maxDepth));
			
			newModel = maximizeIFS(this.model.size(), depths.size() - 1, dataSub, post, pk, tf, zf);	
			
		} 
		
		
		if(Global.random.nextDouble() < postIts)
			newPost = maximizePost(dataSub, p, z, t);
		
		if(newModel == null && ! approx) // Try again, but approximate
		{
			Global.log().info("All components null, switching to approximate expectation.");
			iterate(depthIts, modelIts, postIts, true);
			return;
		} else if(newModel == null && approx)
		{
			System.out.println(this.model);
			System.out.println(MatrixTools.toString(p));
			throw new IllegalStateException("All components null, even with approximate expectation.");
		} 
		
		depths = newDepths;
		model = newModel;
		post = newPost;
	}

	public void expectation(boolean approx)
	{
		expectation(depths, model, post, dataSub, z, zk, p, pk, t, approx);
	}

	public void maximization()
	{
		depths = maximizeDepths(model.size(), depths.size() - 1, p);
	
		int maxDepth = depths.size() - 1;
		
		RealMatrix tf;
		RealVector zf;
		
		int k = model.size();

		tf = t.getSubMatrix(
				0, t.getRowDimension() - 1, 
				0, t.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);
		
		zf = z.getSubVector(0, z.getDimension() - (int)Math.pow(k, maxDepth));

		model = maximizeIFS(model.size(), depths.size() - 1, dataSub, post, pk, tf, zf);		
		
		post = maximizePost(dataSub, p, z, t);
	}

	protected static RealMatrix p(int numData, int numComponents, int maxDepth)
	{
		int m = ((int)pow(numComponents, maxDepth + 1) - 1)/(numComponents - 1);
		
		RealMatrix p = new Array2DRowRealMatrix(numData, m);
		
		return p;
	}
	
	protected static List<RealMatrix> pk(int numData, int numComponents, int maxDepth)
	{
		List<RealMatrix> pks = new ArrayList<RealMatrix>(numComponents);
		
		for(int i : series(numComponents))
			pks.add(p(numData, numComponents, maxDepth - 1));
		
		return pks;
	}
	
	protected static RealVector z(int numData, int numComponents, int maxDepth)
	{
		int m = ((int)pow(numComponents, maxDepth + 1) - 1)/(numComponents - 1);
		
		RealVector z = new ArrayRealVector(m);
		
		return z;
	}
	
	protected static List<RealVector> zk(int numData, int numComponents, int maxDepth)
	{
		List<RealVector> zks = new ArrayList<RealVector>(numComponents);
		
		for(int i : series(numComponents))
			zks.add(z(numData, numComponents, maxDepth - 1));
			
		return zks;
	}
	
	protected static RealMatrix t(int dim, int numComponents, int maxDepth)
	{
		int m = ((int)pow(numComponents, maxDepth + 1) - 1)/(numComponents - 1);

		RealMatrix t = new Array2DRowRealMatrix(dim, m);
		
		return t;
	}

	/**
	 * 
	 * @param depths
	 * @param model
	 * @param post
	 * @param data
	 * @param z
	 * @param zk 
	 * @param p
	 * @param pk
	 */
	public static void expectation(
			List<Double> depths, 
			IFS<Similitude> model, 
			Similitude post,  List<Point> data,
			RealVector z, List<RealVector> zk,
			RealMatrix p, List<RealMatrix> pk,
			RealMatrix t)
	{
		expectation(depths, model, post, data, z, zk, p, pk, t, false);
	}
	
	public static void expectation(
			List<Double> depths, 
			IFS<Similitude> model, 
			Similitude post,  List<Point> data,
			RealVector z, List<RealVector> zk,
			RealMatrix p, List<RealMatrix> pk,
			RealMatrix t, boolean approx)
	{		
		data = post.inverse().map(data);

		// -- Technically this operation requires us to multiply each 
		//    density by 1/s_post. However since this factor is constant, and we 
		//    normalize all densities, we can ignore this requirement.
		
		// * add the unnormalized log-densities to p, pk
		List<Integer> code = Collections.emptyList();
		expectationInner(depths, model, data, z, zk, p, pk, t, code, 
				1.0, MatrixTools.identity(model.dimension()), 
				new ArrayRealVector(model.dimension()), approx);
		
		// * normalize
		List<Double> sums = new ArrayList<Double>(data.size());
		for(int i : series(data.size()))
			sums.add(Functions.logSum(Math.E, p.getRow(i)));
		
		subtract(p, sums);
				
		for(RealMatrix pkk : pk)
			subtract(pkk, sums);
	}
	
	private static void expectationInner(
			List<Double> depths, 			
			IFS<Similitude> model, 
			List<Point> data,
			RealVector z, List<RealVector> zk,
			RealMatrix p, List<RealMatrix> pk,
			RealMatrix t,
			List<Integer> code,
			double sim0Scale, RealMatrix sim0Rot, RealVector sim0Trans, 
			boolean approx)
	{
		int maxDepth = depths.size() - 1;
		
		// -- base case
		// * find the log density under the current code for all points
		final int j = indexOf(code, model.size());
		
		for(int i : Series.series(data.size()))
		{
			RealVector x = data.get(i).getVector();
			RealVector xm = x.subtract(sim0Trans);
						
			// * Note that we are omitting any factors independent of the 
			//   parameters, since they will fall out after normalization anyway
			double s0 = approx ? S_APPROX : sim0Scale;
			
			double logDensity = 0.0;
			// * p(c) = p(|c|) \prod_i p(c_i)
			logDensity += depths.get(code.size());
			for(int codon : code)
				logDensity += log(model.probability(codon));
			// * n(x|c)
			
			logDensity += - xm.getDimension() * log(s0) - xm.dotProduct(xm)/(2.0 * s0 * s0);
			
			p.setEntry(i, j, logDensity);
			z.setEntry(j, - 2.0 * log(s0));
			t.setColumnVector(j, sim0Trans);

			if(! code.isEmpty())
			{
				int comp = code.get(0);
				int jk = indexOf(code.subList(1, code.size()), model.size());
				
				pk.get(comp).setEntry(i, jk, logDensity);
				zk.get(comp).setEntry(jk, - 2.0 * log(s0));
			}
		}
		
		if(code.size() == maxDepth)
			return;
		
		// -- recursion
		for(int next : series(model.size()))
		{
			Similitude comp = model.get(next);
			double sim1Scale = sim0Scale * comp.scalar();
			RealMatrix sim1Rot = comp.rotation().multiply(sim0Rot);
			
			RealVector sim1Trans = comp.rotation().operate(sim0Trans);
			sim1Trans.mapMultiplyToSelf(comp.scalar());
			sim1Trans = sim1Trans.add(comp.getTranslation());
			
			List<Integer> nextCode = new ArrayList<Integer>(code.size() + 1);
			nextCode.add(next);
			nextCode.addAll(code);
			
			expectationInner(depths, model, data, z, zk, p, pk, t, nextCode, sim1Scale, sim1Rot, sim1Trans, approx);
		}
	}
	
	/**
	 * Returns the log-priors (base e) of the depths
	 * @param k
	 * @param maxDepth
	 * @param data
	 * @param post
	 * @param p
	 * @return
	 */
	public static List<Double> maximizeDepths(
			int k, int maxDepth, RealMatrix p)
	{
		List<Double> depths = new ArrayList<Double>(maxDepth + 1);
		
		for(int i : series(maxDepth + 1))
			depths.add(Double.NEGATIVE_INFINITY);
		
		for(int j : series(p.getColumnDimension()))
		{
			double dl = Math.ceil(log((j+1)*(k-1) + 1)/log(k)) - 1.0; 
			int length = (dl == Double.NEGATIVE_INFINITY ? 0 : (int) dl);
			
			double jSum = Functions.logSum(E, p.getColumn(j));
			depths.set(length, Functions.logSum(E, depths.get(length), jSum));
		}

		logNorm(E, depths);
		
		return depths;
	}
	
	public static RealMatrix tFromData(int k, int maxDepth, List<Point> data, Similitude post, RealMatrix p)
	{		
		data = post.inverse().map(data);
		RealMatrix y = MatrixTools.matrix(data);
		
		// * create the t matrix
		p = p.getSubMatrix(
				0, p.getRowDimension() - 1, 
				0, p.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);

		RealMatrix pNormCol = logNormColumns(E, p);
		expInPlace(pNormCol);
		
		return y.multiply(pNormCol);
	}
	
	public static RealVector zFromData(int k, int maxDepth, List<Point> data, Similitude post, RealMatrix p)
	{
		data = post.inverse().map(data);
		RealMatrix y = MatrixTools.matrix(data);
		
		RealMatrix t = tFromData(k, maxDepth, data, post, p);
		
		// * create the t matrix
		p = p.getSubMatrix(
				0, p.getRowDimension() - 1, 
				0, p.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);
		
		RealMatrix pNormCol = logNormColumns(E, p);
		expInPlace(pNormCol);
		
		// * create the zk vector
		RealVector z = new ArrayRealVector(pNormCol.getColumnDimension());
		for(int j : series(pNormCol.getColumnDimension()))
		{			
			RealMatrix yCentered = 
					y.subtract(
						MatrixTools.outer(t.getColumnVector(j), MatrixTools.ones(y.getColumnDimension()))
						);
			
			z.setEntry(j, - Math.log(Similitude.inTrace(
					yCentered, pNormCol.getColumnVector(j), yCentered)));
		}

		return z;
	}
	
	public static IFS<Similitude> maximizeIFS(
			int k, int maxDepth, List<Point> data, 
			Similitude post, List<RealMatrix> pk,
			RealMatrix t, RealVector z)
	{
		// * Weights
		List<Double> weights = new ArrayList<Double>(k);
		for(int component : series(k))
			weights.add(logSum(E, pk.get(component)));
		
		logNorm(E, weights);
		for(int i : series(k))
			weights.set(i, Math.exp(weights.get(i)));
		
		data = post.inverse().map(data);
		
		List<Similitude> comps = new ArrayList<Similitude>(k);
		for(int i : series(k))
			comps.add(null);

		// * create the data matrix
		RealMatrix y = MatrixTools.matrix(data);

		for(int comp : series(k))
		{						
			// * compute the p^kz sum
			RealMatrix pkz = scaleColumns(pk.get(comp), z);
			double pkzSum = logSum(E, pkz);
			
			// * Unlog
			RealMatrix pkzNorm = pkz.scalarAdd(- pkzSum);
			expInPlace(pkzNorm); 
			
			expInPlace(pkz);
			pkzSum = Math.exp(pkzSum);
			
			RealVector yWeights = pkzNorm.operate(MatrixTools.ones(pkz.getColumnDimension()));
			RealVector yMean = y.operate(yWeights);
			
			RealVector tWeights = pkzNorm.preMultiply(MatrixTools.ones(pkz.getRowDimension()));
			RealVector tMean = t.operate(tWeights);
			
			RealMatrix yCentered = 
					y.subtract(
						MatrixTools.outer(yMean, MatrixTools.ones(y.getColumnDimension()))
						);
			RealMatrix tCentered = 
					t.subtract(
						MatrixTools.outer(tMean, MatrixTools.ones(t.getColumnDimension()))
						);
			
			// * Find the rotation
			RealMatrix a = yCentered.multiply(pkzNorm).multiply(tCentered.transpose());
			
			SingularValueDecomposition svd = null;
			try {
				svd = new SingularValueDecompositionImpl(a);
			} catch(InvalidMatrixException e)
			{
				continue;
			}
				
			RealVector c = MatrixTools.ones(a.getColumnDimension());
			double last = MatrixTools.getDeterminant(svd.getU().multiply(svd.getVT()));
			c.setEntry(c.getDimension()-1, last);
			
			RealMatrix rot = svd.getU().multiply(MatrixTools.diag(c)).multiply(svd.getVT());
			
			if(containsNaN(rot))
				continue;
			
			List<Double> angles = Rotation.findAngles(rot);
			
			// * Find the scale
			double sa, sb, sc;
			sa = Similitude.inTrace(yCentered, pkz.operate(MatrixTools.ones(pkz.getColumnDimension())), yCentered);
			sb = - tCentered.multiply(pkz.transpose()).multiply(yCentered.transpose()).multiply(rot).getTrace();
			sc = - Math.exp(logSum(E, pk.get(comp))) * data.get(0).dimensionality() ;

			double s = Similitude.chooseS(Functions.quadratic(sa, sb, sc));
			if(Double.isNaN(s) || Double.isInfinite(s))
				continue;
			
			RealVector tr = yMean.subtract(rot.operate(tMean).mapMultiply(s));
			
			comps.set(comp, new Similitude(s, new Point(tr), angles));
		}
		
		return IFS.ifs(comps, weights, SPLIT_VAR);
	}
	
	public static Similitude maximizePost(List<Point> data, RealMatrix p, RealVector z, RealMatrix t)
	{
		RealMatrix x = MatrixTools.matrix(data);
		
		RealMatrix pz = scaleColumns(p, z);
		double pzSum = logSum(E, pz);
		
		RealMatrix pzNorm = pz.scalarAdd(- pzSum);
		expInPlace(pzNorm); 
		
		// * Unlog
		expInPlace(pz);
		pzSum = Math.exp(pzSum);
		
		
		
		RealVector xWeights = pzNorm.operate(MatrixTools.ones(pz.getColumnDimension()));
		RealVector xMean = x.operate(xWeights);
		
		RealVector tWeights = pzNorm.preMultiply(MatrixTools.ones(pz.getRowDimension()));
		RealVector tMean = t.operate(tWeights);
		
		RealMatrix xCentered = 
				x.subtract(
					MatrixTools.outer(xMean, MatrixTools.ones(x.getColumnDimension()))
					);
		RealMatrix tCentered = 
				t.subtract(
					MatrixTools.outer(tMean, MatrixTools.ones(t.getColumnDimension()))
					);
		
		// * Find the rotation
		RealMatrix a = xCentered.multiply(pzNorm).multiply(tCentered.transpose());
		SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
		
		RealVector c = MatrixTools.ones(a.getColumnDimension());
		double last = MatrixTools.getDeterminant(svd.getU().multiply(svd.getVT()));
		c.setEntry(c.getDimension()-1, last);
		
		RealMatrix rot = null;
		try {
			 rot = svd.getU().multiply(diag(c)).multiply(svd.getVT());
		} catch (InvalidMatrixException e)
		{
			return null;
		}
		
		if(containsNaN(rot))
			return null;
		
		List<Double> angles = Rotation.findAngles(rot);
				
		// * Find the scale
		double sa, sb, sc;
		sa = Similitude.inTrace(xCentered, pz.operate(MatrixTools.ones(pz.getColumnDimension())), xCentered);
		sb = - tCentered.multiply(pz.transpose())
				.multiply(xCentered.transpose()).multiply(rot).getTrace();
		sc = - Math.exp(logSum(E, p)) * data.get(0).dimensionality();
				
		double s = Similitude.chooseS(Functions.quadratic(sa, sb, sc));
		RealVector tr = xMean.subtract(rot.operate(tMean).mapMultiply(s));
		
		return new Similitude(s, new Point(tr), angles);
	}
	
	private static void subtract(RealMatrix mat, List<Double> vals)
	{
		for(int i : series(mat.getRowDimension()))
		{
			double val = vals.get(i);
			for(int j : series(mat.getColumnDimension()))
				mat.setEntry(i, j, mat.getEntry(i, j) - val);
		}
	}

	/**
	 * Scales every column of m by the corresponding value in v
	 * (in log space).
	 * 
	 * @param m matrix of log-values
	 * @param v matrix of log-values
	 * @return
	 */
	private static RealMatrix scaleColumns(RealMatrix m, RealVector v) 
	{
		RealMatrix result = m.copy();
		
		for(int j : series(result.getColumnDimension()))
		{
			double weight = v.getEntry(j);
			for(int i : series(result.getRowDimension()))
				result.addToEntry(i, j, weight);
		}
		
		return result;
	}

	public static int indexOf(List<Integer> code, int k)
	{
		if(code.isEmpty())
			return 0;
		
		int res = (1 - (int)pow(k, code.size()))/(1 - k) - 1;
		
		for(int i = 0; i < code.size(); i++)
			res += code.get(i) * ipow(k, i);
		
		return res + 1;
	}
	
	private static int ipow(int base, int exp)
	{
	    int result = 1;
	    while (exp != 0)
	    {
	        if ((exp & 1) == 1)
	            result *= base;
	        exp >>= 1;
	        base *= base;
	    }

	    return result;
	}
	
	public static List<Integer> code(int index, int k)
	{
		if(index == 0)
			return Collections.emptyList();
		
		double dl = Math.ceil(log((index+1)*(k-1) + 1)/log(k)) - 1.0; 
		int length = (dl == Double.NEGATIVE_INFINITY ? 0 : (int) dl);
		int base = (1 - (int)pow(k, length)) / (1 - k) ;
		
		List<Integer> code = new ArrayList<Integer>(length);
		for(int i : series(length))
			code.add(-1);
		
		index -= base;
		for(int i : series(length-1, -1, -1))
		{			
			int pow = (int)pow(k, i);
			
			int digit = index / pow ;
			code.set(i, digit);
			index = index % pow;
		}
		return code;
	}
	
	private static double logSum(double base, RealMatrix m)
	{
		double max = Double.NEGATIVE_INFINITY;
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				max = Math.max(max, m.getEntry(i, j));
		
		double sum = 0.0;
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				sum += pow(base, m.getEntry(i, j) - max);
		
		return Functions.log(sum, base) + max;
	}
	
	private static void logNorm(double base, List<Double> values)
	{
		double sum = Functions.logSum(E, values);
		
		for(int i : series(values.size()))
			values.set(i, values.get(i) - sum);
	}
	
	/**
	 * Normalizes a matrix of log values so that the columns sum to one.
	 * @param base
	 * @param m
	 * @return
	 */
	public static RealMatrix logNormColumns(double base, RealMatrix m)
	{
		RealVector totals = new ArrayRealVector(m.getColumnDimension());
		
		for(int j : series(m.getColumnDimension()))
		{
			double t = Functions.logSum(base, m.getColumn(j));
			totals.setEntry(j, t);
		}
		
		RealMatrix normalized = m.copy();
		
		for(int j : series(m.getColumnDimension()))
			for(int i : series(m.getRowDimension()))
				normalized.addToEntry(i, j, - totals.getEntry(j));

		return normalized;
	}
	
	/**
	 * Normalizes a matrix of log-values so that the rows sum to one.
	 */
	public static RealMatrix logNormRows(double base, RealMatrix m)
	{
		RealVector totals = new ArrayRealVector(m.getRowDimension());
		
		for(int i : series(m.getRowDimension()))
		{
			double t = Functions.logSum(base, m.getRow(i));
			totals.setEntry(i, t);
		}
		
		RealMatrix normalized = m.copy();
		
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				normalized.addToEntry(i, j, - totals.getEntry(i));

		return normalized;
	}
	
	
	public static void logNormRowsInPlace(double base, RealMatrix m)
	{
		RealVector totals = new ArrayRealVector(m.getRowDimension());
		
		for(int i : series(m.getRowDimension()))
		{
			double t = Functions.logSum(base, m.getRow(i));
			totals.setEntry(i, t);
		}
		
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				m.addToEntry(i, j, - totals.getEntry(i));
	}
	/**
	 * In-place
	 * @param m
	 */
	public static void expInPlace(RealMatrix m)
	{
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				m.setEntry(i, j, Math.exp(m.getEntry(i,j)));
	}
	
	public static List<Double> exp(List<Double> list)
	{
		List<Double> out = new ArrayList<Double>(list);
		
		for(int i : series(out.size()))
			out.set(i, Math.exp(out.get(i)));
		
		return out;
	}
	
	public IFS<Similitude> model() {
		return model;
	}

	public List<Double> depths() 
	{
		return exp(depths);
	}

	public Similitude post() 
	{
		return post;
	}
	
	public static boolean containsNaN(List<Double> m)
	{
		for(double d : m)
			if(Double.isNaN(d))
				return true;
		return false;
	}
	
	public static boolean containsNaN(RealMatrix m)
	{
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				if(Double.isNaN(m.getEntry(i, j)))
					return true;
					
		return false;
	}
	
	public static Point mean(List<Point> list)
	{
		RealVector mean = new ArrayRealVector(list.get(0).dimensionality());
		for(Point p : list)
			mean = mean.add(p.getBackingData());
		
		mean.mapMultiplyToSelf(1.0/list.size());
		
		return new Point(mean);
	}
	
	public double logLikelihood(List<Point> data)
	{
		data = post.inverse().map(data);
		
		List<Double> terms = new ArrayList<Double>();
		logLikelihoodInner(data, new ArrayList<Integer>(), Similitude.identity(dimension), terms);
		
		return Functions.logSum(E, terms);
	}
	
	public void logLikelihoodInner(List<Point> data, List<Integer> code, Similitude sim0, List<Double> terms)
	{
		double logDensity = 0.0;
		// * p(c) = p(|c|) \prod_i p(c_i)
		logDensity += depths.get(code.size());
		for(int codon : code)
			logDensity += log(model.probability(codon));
		// * n(x|c)
		Similitude sim0p = (Similitude) post.compose(sim0); 
		MVN mvn = new MVN(sim0p);
		logDensity += mvn.logDensity(data); 
		
		terms.add(logDensity);
		
		if(code.size() < depths.size() - 1)
			for(int comp : series(model.size()))
			{
				Similitude sim1 = (Similitude) model.get(comp).compose(sim0);
				List<Integer> newCode = new ArrayList<Integer>(code.size() + 1);
				newCode.add(comp);
				newCode.addAll(code);
				logLikelihoodInner(data, newCode, sim1, terms);
			}
	}
}
