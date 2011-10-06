package org.lilian.pos;

import org.lilian.corpora.SequenceCorpus;

public interface TaggedCorpus extends SequenceCorpus<TaggedWord> {
	
	/**
	 * Returns this corpus' tagset
	 * @return
	 */
	public TagSet tagSet();
}
