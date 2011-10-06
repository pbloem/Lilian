package org.lilian.pos;

import org.lilian.corpora.wrappers.CorpusWrapper;


/**
 * A corpuswrapper from a stringcorpus to a tagged corpus. This is the preferred
 * way to implement taggers. The class AbstractTagger provides basic buffering 
 * functionality, so that the only method that needs to be implemented is one 
 * that tags a sentence.   
 * 
 * @author peter
 *
 */
public interface Tagger extends TaggedCorpus {
	
}
