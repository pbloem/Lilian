package org.lilian.randomness;

import java.util.List;

/**
 * Tests a sequence of T's for randomness. The higher the output, the
 * greater the level of randomness. 
 *  
 * @author peter
 *
 * @param <T>
 */
public interface RandomnessTest<T> {
	
	public double test(List<T> sequence);

}
