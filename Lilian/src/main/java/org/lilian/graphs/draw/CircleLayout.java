package org.lilian.graphs.draw;

import org.lilian.data.real.Point;
import org.lilian.graphs.Graph;
import org.lilian.graphs.Node;

public class CircleLayout<L> implements Layout<L>
{
	int total;
	
	public CircleLayout(Graph<L> graph)
	{
		total = graph.size();
	}
	
	@Override
	public Point point(Node<L> node)
	{
		int i = node.index();
		double angle = (Math.PI * 2.0) * (i/(double)total);
		
		double x = Math.cos(angle);
		double y = Math.sin(angle);
		
		return new Point(x, y);
	}

}
