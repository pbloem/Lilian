package org.lilian.data.real.fractal;

import static java.lang.Math.E;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static org.lilian.data.real.Point.fromRaw;
import static org.lilian.util.MatrixTools.diag;
import static org.lilian.util.Series.series;
import static org.nodes.util.Functions.logSum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.data.real.Datasets;
import org.lilian.data.real.MVN;
import org.lilian.data.real.Point;
import org.lilian.data.real.Rotation;
import org.lilian.data.real.Similitude;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;
import org.nodes.util.Functions;
import org.nodes.util.Pair;

import weka.core.SystemInfo;

public class EM
{
	public static final boolean APPROX_FROM_DATA = true;
	
	private List<Point> data, dataSub;
	private int sub;
	private int maxDepth;
	private int dimension;
	private IFS<Similitude> model;
	
	private RealMatrix p, t;
	private RealVector z;
	
	private List<RealMatrix> pk;
	private List<RealVector> zk;
	
	private List<Double> depths;
	private Similitude post;
	
	public EM(List<Point> data, int sub, IFS<Similitude> initial, int maxDepth)
	{
		this.data = data;
		this.dimension = data.get(0).size();
		this.model = initial;
		this.sub = sub;
		
		p = p(sub, model.size(), maxDepth);
		
		t = t(sub, model.size(), maxDepth);
		z = z(sub, model.size(), maxDepth);
		
		pk = pk(sub, model.size(), maxDepth);
		zk = zk(sub, model.size(), maxDepth);
		
		depths = new ArrayList<Double>(maxDepth + 1);
		for(int i : series(maxDepth + 1))
			depths.add(1.0);
		logNorm(E, depths);
		
		post = Similitude.identity(dimension);

		resample();
		expectation(depths, model, post, dataSub, z, zk, p, pk, t);
	}
	
	public void resample()
	{
		dataSub = Datasets.sample(data, sub);
	}
	
	public void iterate()
	{
		resample();
		expectation();
		maximization();
	}
	
	public void expectation()
	{
		List<Point> dataSub = Datasets.sample(data, sub);
		
		expectation(depths, model, post, dataSub, z, zk, p, pk, t);
	}

	public void maximization()
	{
		depths = maximizeDepths(model.size(), depths.size()-1, p);
		
		RealMatrix tfd = tFromData(model.size(), depths.size() + 1, dataSub, post, p);
		RealVector zfd = zFromData(model.size(), depths.size() + 1, dataSub, post, p);
		
		model = maximizeIFS(model.size(), depths.size() - 1, dataSub, post, pk, 
				APPROX_FROM_DATA ? tfd : t, 
				APPROX_FROM_DATA ? zfd : z);
		
		post = maximizePost(data, p, z, t);
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
		data = post.inverse().map(data);
		// -- Technically this operation requires us to multiply each 
		//    density by 1/s_post. However since this factor is constant, and we 
		//    normalize all densities, we can ignore this requirement.
		
		// * add the unnormalized log-densities to p, pk
		List<Integer> code = Collections.emptyList();
		expectationInner(depths, model, data, z, zk, p, pk, t, code, 
				Similitude.identity(data.get(0).dimensionality()));
		
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
			Similitude sim0)
	{
		int maxDepth = depths.size() - 1;
		
		// -- base case
		// * find the log density under the current code for all points
		
		for(int i : Series.series(data.size()))
		{
			
			RealVector x = data.get(i).getVector();
			RealVector xm = x.subtract(sim0.getTranslation());
						
			// * Note that we are omitting any factors independent of the 
			//   parameters, since they will fall out after normalization anyway
			double s0 = sim0.scalar();
			
			double logDensity = 0.0;
			// * p(c) = p(|c|) \prod_i p(c_i)
			logDensity += depths.get(code.size());
			for(int codon : code)
				logDensity += log(model.probability(codon));
			// * n(x|c)
			logDensity += - log(s0) - xm.dotProduct(xm)/(2.0 * s0 * s0);

			double cd = 0.0;
			for(int codon : code)
				cd += log(model.probability(codon));
			
			int j = indexOf(code, model.size());
			p.setEntry(i, j, logDensity);
			z.setEntry(j, - log(s0 * s0));
			t.setColumnVector(j, sim0.getTranslation());

			if(code.size() > 0)
			{
				int comp = code.get(0);
				j = indexOf(code.subList(1, code.size()), model.size());
				
				pk.get(comp).setEntry(i, j, logDensity);
				zk.get(comp).setEntry(j, - log(s0 * s0));
				
			}
		}
		
		if(code.size() == maxDepth)
			return;
		
		// -- recursion
		for(int next : series(model.size()))
		{
			Similitude sim1 = (Similitude) model.get(next).compose(sim0);
			List<Integer> nextCode = new ArrayList<Integer>(code.size() + 1);
			nextCode.add(next);
			nextCode.addAll(code);
			
			expectationInner(depths, model, data, z, zk, p, pk, t, nextCode, sim1);
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
			depths.add(0.0);
		
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
		RealMatrix y = matrix(data);
		
		// * create the t matrix
		p = p.getSubMatrix(
				0, p.getRowDimension() - 1, 
				0, p.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);

		RealMatrix pNormCol = logNormColumns(E, p);
		exp(pNormCol);
		
		return y.multiply(pNormCol);
	}
	
	public static RealVector zFromData(int k, int maxDepth, List<Point> data, Similitude post, RealMatrix p)
	{
		data = post.inverse().map(data);
		RealMatrix y = matrix(data);
		
		RealMatrix t = tFromData(k, maxDepth, data, post, p);
		
		// * create the t matrix
		p = p.getSubMatrix(
				0, p.getRowDimension() - 1, 
				0, p.getColumnDimension() - (int)Math.pow(k, maxDepth) - 1);
		
		RealMatrix pNormCol = logNormColumns(E, p);
		exp(pNormCol);
		
		// * create the zk vector
		RealVector z = new ArrayRealVector(pNormCol.getColumnDimension());
		for(int j : series(pNormCol.getColumnDimension()))
		{			
			RealMatrix yCentered = 
					y.subtract(
						outer(t.getColumnVector(j), ones(y.getColumnDimension()))
						);
			
			z.setEntry(j, - Math.log(inTrace(
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
		RealMatrix y = matrix(data);

		for(int comp : series(k))
		{						
			// * compute the p^kz sum
			RealMatrix pkz = scaleColumns(pk.get(comp), z);
			double pkzSum = logSum(E, pkz);
			
			// * Unlog
			exp(pkz); 
			pkzSum = Math.exp(pkzSum);
			
			RealVector yWeights = pkz.operate(ones(pkz.getColumnDimension()));
			yWeights.mapMultiplyToSelf(1.0/pkzSum);
			RealVector yMean = y.operate(yWeights);
			
			RealVector tWeights = pkz.preMultiply(ones(pkz.getRowDimension()));
			tWeights.mapMultiplyToSelf(1.0/pkzSum);
			RealVector tMean = t.operate(tWeights);
			
			RealMatrix yCentered = 
					y.subtract(
						outer(yMean, ones(y.getColumnDimension()))
						);
			RealMatrix tCentered = 
					t.subtract(
						outer(tMean, ones(t.getColumnDimension()))
						);
			
			System.out.println(MatrixTools.toString(yCentered));
			System.out.println(MatrixTools.toString(tCentered));
			
			// * Find the rotation
			RealMatrix a = yCentered.multiply(pkz).multiply(tCentered.transpose());
			SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
			
			RealVector c = ones(a.getColumnDimension());
			double last = MatrixTools.getDeterminant(svd.getU().multiply(svd.getVT()));
			c.setEntry(c.getDimension()-1, last);
			
			RealMatrix rot = svd.getU().multiply(MatrixTools.diag(c)).multiply(svd.getVT());
			List<Double> angles = Rotation.findAngles(rot);
			
			// * Find the scale
			double sa, sb, sc;
			sa = inTrace(yCentered, pkz.operate(ones(pkz.getColumnDimension())), yCentered);
			sb = - tCentered.multiply(pkz.transpose()).multiply(yCentered.transpose()).multiply(rot).getTrace();
			sc = - Math.exp(logSum(E, pk.get(comp)));
			
			double s = chooseS(Functions.quadratic(sa, sb, sc));
			
			RealVector tr = yMean.subtract(rot.operate(tMean).mapMultiply(s));
			
			comps.set(comp, new Similitude(s, new Point(tr), angles));
		}
		
		return IFS.ifs(comps, weights);
	}
	
	public static Similitude maximizePost(List<Point> data, RealMatrix p, RealVector z, RealMatrix t)
	{
		RealMatrix x =  matrix(data);
		
		RealMatrix pz = scaleColumns(p, z);
		double pzSum = logSum(E, pz);
		
		// * Unlog
		exp(pz);
		pzSum = Math.exp(pzSum);
		
		RealVector xWeights = pz.operate(ones(pz.getColumnDimension()));
		xWeights.mapMultiplyToSelf(1.0/pzSum);
		RealVector xMean = x.operate(xWeights);
		
		RealVector tWeights = pz.preMultiply(ones(pz.getRowDimension()));
		tWeights.mapMultiplyToSelf(1.0/pzSum);
		RealVector tMean = t.operate(tWeights);
		
		RealMatrix xCentered = 
				x.subtract(
					outer(xMean, ones(x.getColumnDimension()))
					);
		RealMatrix tCentered = 
				t.subtract(
					outer(tMean, ones(t.getColumnDimension()))
					);
		
		// * Find the rotation
		RealMatrix a = xCentered.multiply(pz).multiply(tCentered.transpose());
		SingularValueDecomposition svd = new SingularValueDecompositionImpl(a);
		
		RealVector c = ones(a.getColumnDimension());
		double last = MatrixTools.getDeterminant(svd.getU().multiply(svd.getVT()));
		c.setEntry(c.getDimension()-1, last);
		
		RealMatrix rot = svd.getU().multiply(diag(c)).multiply(svd.getVT());
		List<Double> angles = Rotation.findAngles(rot);
				
		// * Find the scale
		double sa, sb, sc;
		sa = inTrace(xCentered, pz.operate(ones(pz.getColumnDimension())), xCentered);
		sb = - tCentered.multiply(pz.transpose()).multiply(xCentered.transpose()).multiply(rot).getTrace();
		sc = - Math.exp(logSum(E, p));
				
		double s = chooseS(Functions.quadratic(sa, sb, sc));
		RealVector tr = xMean.subtract(rot.operate(tMean).mapMultiply(s));
		
		return new Similitude(s, new Point(tr), angles);
	}
	
	private static double chooseS(Pair<Double, Double> pair)
	{
		double x = pair.first(), y = pair. second();
		x = 1.0/x;
		y = 1.0/y;
		
		if(x > 0.0 && y > 0.0)
			return Math.max(x, y);
		
		if(x > 0.0)
			return x;
		
		if(y > 0.0)
			return y;
		
		throw new IllegalStateException("Both solutions nonpositive: ("+x+", "+y+")");
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

	protected static RealVector ones(int d) {
		return new ArrayRealVector(d, 1.0);
	}

	/**
	 * Scales every column of by the corresponding value in v
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

	protected static int indexOf(List<Integer> code, int k)
	{
		if(code.isEmpty())
			return 0;
		
		int res = (1 - (int)pow(k, code.size()))/(1 - k) - 1;
		
		for(int i : Series.series(code.size()))
			res += code.get(i) * Math.pow(k, i);
		
		return res + 1;
	}
	
	protected static List<Integer> code(int index, int k)
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
	
	/**
	 * Computes the sum over the inner product of column i in left with column 
	 * i in right, weighted with the i-th weight.
	 * @param left
	 * @param weights
	 * @param right
	 * @return
	 */
	private static double inTrace(RealMatrix left, RealVector weights, RealMatrix right)
	{
		double sum = 0.0;
		for(int i : series(left.getColumnDimension()))
		{
			double in = left.getColumnVector(i).dotProduct(right.getColumn(i));
			sum += weights.getEntry(i) * in;
		}
		
		return sum;
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
	private static RealMatrix logNormColumns(double base, RealMatrix m)
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
	private static RealMatrix logNormRows(double base, RealMatrix m)
	{
		RealVector totals = new ArrayRealVector(m.getRowDimension());
		
		for(int i : series(m.getRowDimension()))
		{
			double t = Functions.logSum(base, m.getColumn(i));
			totals.setEntry(i, t);
		}
		
		RealMatrix normalized = m.copy();
		
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				normalized.addToEntry(i, j, - totals.getEntry(i));

		return normalized;
	}
	
	static RealMatrix matrix(List<Point> data)
	{
		int rows = data.get(0).dimensionality();
		int columns = data.size();
		
		RealMatrix result = new Array2DRowRealMatrix(rows, columns);
		
		for(int j : series(data.size()))
			result.setColumn(j, data.get(j).getBackingData());
		
		return result;
	}
	
	private static void exp(RealMatrix m)
	{
		for(int i : series(m.getRowDimension()))
			for(int j : series(m.getColumnDimension()))
				m.setEntry(i, j, Math.exp(m.getEntry(i,j)));
	}
	
	/**
	 * Outer product ab^T without regards to length.
	 * @param a
	 * @param b
	 * @return
	 */
	private static RealMatrix outer(RealVector a, RealVector b)
	{
		RealMatrix out = new Array2DRowRealMatrix(a.getDimension(), b.getDimension());
		
		for(int i : series(a.getDimension()))
			for(int j : series(b.getDimension()))
				out.setEntry(i, j, a.getEntry(i) * b.getEntry(j));
		
		return out;
	}

	public IFS model() {
		return model;
	}

	public List<Double> depths() {
		List<Double> depths = new ArrayList<Double>(this.depths.size());
		
		for(double d : this.depths)
			depths.add(Math.exp(d));
		
		return depths;
	}

	public Similitude post() 
	{
		return post;
	}
}
