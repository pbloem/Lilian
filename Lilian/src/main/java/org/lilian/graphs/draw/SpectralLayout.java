package org.lilian.graphs.draw;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.lilian.data.real.Maps;
import org.lilian.data.real.Point;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;
import org.lilian.util.MatrixTools;

public class SpectralLayout<L> implements Layout<L>
{
	private List<Point> points; 

	public SpectralLayout(Graph<L> graph)
	{
		int n = graph.size();
		RealMatrix l = new Array2DRowRealMatrix(n, n);
		
		for(int i : series(n))
			for(int j : series(n))
			{
				if(i == j)
					l.setEntry(i, j, graph.nodes().get(i).degree());
				else
					l.setEntry(i, j, 
							graph.nodes().get(i).connected(graph.nodes().get(j)) ? -1 : 0);
			}

		System.out.println(MatrixTools.toString(l, 3));
				
		EigenDecomposition eig = new EigenDecompositionImpl(l, Double.NaN);
		points = new ArrayList<Point>(n);
		
		System.out.println(Arrays.toString(eig.getRealEigenvalues()));
		
		double factor1 = 1.0/Math.sqrt(eig.getRealEigenvalue(n-2));
		double factor2 = 1.0/Math.sqrt(eig.getRealEigenvalue(n-3));
		
		for(int i : series(n))
		{
			Point p = new Point(2);
			
			p.set(0, eig.getEigenvector(n-2).getEntry(i) * factor1);
			p.set(1, eig.getEigenvector(n-3).getEntry(i) * factor2);
			
			points.add(p);
		}
		
		// * Scale to the bi-unit cube
		points = Maps.centerUniform(points).map(points);
		
	}
	
	@Override
	public Point point(Node<L> node)
	{
		return points.get(node.index());
	}

}
