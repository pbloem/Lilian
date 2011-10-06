package org.lilian.pos;

import java.util.Collection;

/**
 * Represents a collection of tags
 *  
 * @author peter
 */
public interface TagSet extends Collection<Tag> 
{
	/**
	 * Returns the name of this tagset
	 * 
	 * @return
	 */
	public String name();
	
	public Tag getTag(String in);
}
