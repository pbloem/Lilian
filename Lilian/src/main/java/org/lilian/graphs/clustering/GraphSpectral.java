package org.lilian.graphs.clustering;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;
import org.lilian.data.real.Point;
import org.lilian.data.real.classification.Classification;
import org.lilian.data.real.classification.Classified;
import org.lilian.data.real.clustering.KMeans;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.graphs.algorithms.FloydWarshall;
import org.lilian.util.MatrixTools;
import org.lilian.util.Series;

public class GraphSpectral<L> implements Clusterer<L>
{
	private static int ITS = 50;
	private int k = 3;

	public GraphSpectral()
	{
	}

	public GraphSpectral(int k)
	{
		this.k = k;
	}

	@Override
	public Classified<Node<L>> cluster(Graph<L> graph)
	{
		int n = graph.size();
		
		FloydWarshall<L> fw = new FloydWarshall<L>(graph);
		
		RealMatrix s = fw.matrix();
		RealMatrix d = new Array2DRowRealMatrix(n, n);
		
		for(int i : series(n))
		{
			int sum = 0;
			for(int j : series(n))
				sum += s.getEntry(i, j);

			d.setEntry(i, i, sum);
		}
		
		d = MatrixTools.inverse(d);
		RealMatrix l = d.multiply(s);
		
		EigenDecomposition eig =  new EigenDecompositionImpl(l, Double.NaN);
		List<Point> points = new ArrayList<Point>(n);
		
		for(int i : series(n))
		{
			Point p = new Point(k);
			
			for(int j : series(k))
				p.set(k, eig.getEigenvector(j).getEntry(i));
			
			points.add(p);
		}
		
		KMeans kmeans = new KMeans(points, k);
		kmeans.iterate(ITS);
		
		List<Integer> classes = kmeans.clustered().classes();
		List<Node<L>> nodes = (List<Node<L>>) graph.nodes();
		return Classification.combine(nodes, classes);
	}
}
