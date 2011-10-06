package org.lilian.util;

import java.io.*;

/**
 * An abstract Compressor for serializable objects.
 * 
 * Extending class need only implement the compress/1 function to create a valid 
 * Compressor.
 * 
 * @author peter
 *
 * @param <T>
 */
public abstract class AbstractCompressor<T> implements Compressor<T> {

	public abstract Serializable compress(Object... object);
	
	/**
	 * The size of the uncompressed object. 
	 * 
	 * The values returned must be positive nonzero.
	 * 
	 * The base implementation measures the size of the input in bits
	 */
	public double size(Object object)
	{
		return sizeSerialized(object);
	}
	
	/**
	 * The size of the uncompressed object. 
	 * 
	 * The values returned must be positive nonzero.
	 * 
	 * The base implementation compresses the input and measures its size in 
	 * bits
	 */	
	public double compressedSize(Object... objects)
	{
		return sizeSerialized(compress(objects));
	}
	
	public double compressionRatio(Object object) {
		return compressedSize(object)/size(object);
	}
	
	/**
	 * Serializes the input and returns its size in bytes.
	 * 
	 * @param object An object whose size we want to determine.
	 * @return The size in bytes of the serialized object
	 */
	public static int sizeSerialized(Object... objects)
	{
		try
		{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
			ObjectOutputStream in = new ObjectOutputStream(byteStream);
			
			for(Object object : objects)
			{
				Serializable sobj = (Serializable) object;
				in.writeObject(sobj);
			}
			
			in.close();
		
			return byteStream.size();
			
		} catch(IOException e)
		{
			// This should never happen, since we don't do any disk IO
			throw new RuntimeException(e);
		}
	}

}
