package org.lilian.graphs.draw;

import static org.lilian.util.Series.series;

import java.util.ArrayList;
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
				if(i == j)
					l.setEntry(i, j, graph.nodes().get(i).degree());
				else
					l.setEntry(i, j, 
							graph.nodes().get(i).connected(graph.nodes().get(j)) ? -1 : 0);

		System.out.println(MatrixTools.toString(l, 3));
				
		EigenDecomposition eig =  new EigenDecompositionImpl(l, Double.NaN);
		points = new ArrayList<Point>(n);
		
		for(int i : series(n))
		{
			Point p = new Point(2);
			
			for(int j : series(2))
				p.set(j, eig.getEigenvector(j).getEntry(i));
			
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
