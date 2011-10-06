package org.lilian.util.distance;

import java.io.*;

import org.lilian.util.Compressor;

import static java.lang.Math.*;

public class CompressionDistance<T> implements Distance<T> {

	private static final long serialVersionUID = 1L;
	private Compressor<T> comp;
	
	public CompressionDistance(Compressor<T> comp)
	{
		this.comp = comp;
	}

	@Override
	public double distance(T x, T y) 
	{
		double 	cxy = comp.compressedSize(x, y),
				cyx = comp.compressedSize(y, x),
				cx  = comp.compressedSize(x),
				cy  = comp.compressedSize(y);
		
		System.out.println(cxy + " " +cx +" " +cy );
		
		return max(cxy - cx, cyx - cy) / max(cx, cy); 
	}
}
