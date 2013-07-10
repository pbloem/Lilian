package org.lilian.graphs.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.lilian.graphs.DGraph;
import org.lilian.graphs.Graph;
import org.lilian.graphs.UGraph;
import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTLink;
import org.lilian.graphs.UTNode;

public class MatrixZIPCompressor<N> extends AbstractGraphCompressor<N>
{

	@Override
	public double compressedSize(Graph<N> graph, List<Integer> order)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream goz = new GZIPOutputStream(baos, 4096);
			
			if(graph instanceof UGraph<?>)
				Functions.toBits(goz, (UGraph<N>)graph, order);
			else if(graph instanceof DGraph<?>)
				Functions.toBits(goz, (DGraph<N>)graph, order);
			else
				throw new IllegalArgumentException("Can only handle graphs of type UGraph or DGraph");
			
			goz.finish();
			goz.close();
			
			return baos.size() + 1; // the +1 is there to distinguish between U and D
			
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}		
	}

}
