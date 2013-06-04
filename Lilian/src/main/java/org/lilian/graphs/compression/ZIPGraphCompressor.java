package org.lilian.graphs.compression;

import static org.lilian.util.Series.series;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import org.lilian.graphs.UTGraph;
import org.lilian.graphs.UTLink;
import org.lilian.graphs.UTNode;
import org.lilian.util.BitString;
import org.lilian.util.Compressor;
import org.lilian.util.GZIPCompressor;
import org.lilian.util.Series;

/**
 * 
 * @author Peter
 *
 * @param <L>
 * @param <T>
 */
public class ZIPGraphCompressor<L, T> implements Compressor<UTGraph<L, T>>
{
	private int bufferSize = 512;
	@Override
	public double compressedSize(Object... objects)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream goz = new GZIPOutputStream(baos, bufferSize);
			ObjectOutputStream oos = new ObjectOutputStream(goz);

			for(Object object : objects)
			{
				if(! (object instanceof UTGraph<?, ?>))
					oos.writeObject(object);
				else {
					UTGraph<?, ?> graph = (UTGraph<?, ?>)object;
					
					oos.writeObject(toBits(graph));
					for(UTNode<?, ?> node : graph.nodes())
						oos.writeObject(node.label());
					for(UTLink<?, ?> link : graph.links())
						oos.writeObject(link.tag());
				}
			}
			
			oos.close();
			goz.finish();
			goz.close();
			
			return baos.size();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}		
	}

	@Override
	public double ratio(Object... object)
	{
		throw new UnsupportedOperationException();
	}
	
	public static <L, T> BitString toBits(UTGraph<L, T> graph)
	{
		int n = graph.size();
		BitString string = new BitString( (n * n + n) / 2 );
		
		for(int i : series(n))
			for(int j : series(i, n))
				if(graph.nodes().get(i).connected(graph.nodes().get(j)))
					string.add(true);
				else
					string.add(false);
		
		return string;
	}
}
