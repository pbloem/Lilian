package org.lilian.corpora;

import java.util.*;

/**
 *
 * This is the root interface of the hierarchy of corpus-readers. 
 * 
 * The simplest corpus is just an implementation of the java collection 
 * interface
 * 
 * Notes for implementers:
 * <ul>
 * 	</li> The corpus itself should, if possible, be lightweight and low on 
 *        memory use. For instance, a corpus that reads and parses text files 
 *        should only store the information required to start parsing the file 
 *        (file names, paramaeters etc.)<br/> 
 *        The actual parsing should take place on-the-fly in the iterator. For
 *        most corpora, all the complexity will be in the irterator. *         
 * 	</li> Corpus implementations that read files should handle their own 
 *        io exceptions instead of expeosing them.
 *  </li> Corpora should be unmodifiable.
 * </ul>
 *
 * @param <T>
 */

public interface Corpus<T> extends Collection<T>
{
	
	/**
	 * The (precise) number of tokens in this corpus. Note that this will usually
	 * require walking through the entire corpus, so this method should not be 
	 * considered lightweight. 
	 */
	public int size();
}
