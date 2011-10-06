package org.lilian.pos.perseus;

import java.util.*;

import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;


public final class PerseusTagSet extends AbstractCollection<Tag> implements TagSet 
{
	private List<Tag> tags = new ArrayList<Tag>(239);
	private Map<String, Tag> map = new HashMap<String, Tag>();
	
	public PerseusTagSet()
	{
		for(Tag tag : tags)
			map.put(tag.getTag().toLowerCase(), tag);
	}

	@Override
	public String name() 
	{
		return "Perseus corpus tagset";
	}

	@Override
	public Iterator<Tag> iterator() 
	{
		return tags.iterator();
	}

	@Override
	public int size() 
	{
		return tags.size();
	}
	
	public Tag getTag(String tag)
	{
		String lcTag = tag.toLowerCase();
		
		if(lcTag.length() != 9)
			throw new IllegalArgumentException("Tag ("+tag+") should be 9 characters long.");
		
		if(! map.containsKey(lcTag))
		{
			Tag newTag = new Tag(tag, this);
			tags.add(newTag);
			map.put(lcTag, newTag);
			
			return newTag;		
		}
		
		return map.get(lcTag);
	}
}
