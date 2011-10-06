package org.lilian.corpora.tagged.mapping;

import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;

/**
 * A tag mapper defines a translation from one tagset to another
 * 
 * @author peter
 *
 */
public interface TagMapper {
	
	public Tag map(Tag in);
	
	public TagSet input();
	public TagSet output();
}
