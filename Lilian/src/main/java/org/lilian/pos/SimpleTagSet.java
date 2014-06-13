package org.lilian.pos;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * @author peter
 *
 */
public class SimpleTagSet extends AbstractCollection<Tag> implements TagSet 
{
	private Set<Tag> tags = new LinkedHashSet<Tag>(); 
	private Map<String, Tag> tagMap = new LinkedHashMap<String, Tag>();	
	
	public SimpleTagSet()
	{
		addTags();
		
		for(Tag tag : tags)
			tagMap.put(tag.getTag(), tag);
	}

	private void addTags() 
	{
		tags.add(new Tag("verb", this));  // verbs
		tags.add(new Tag("noun", this));  // nouns
		tags.add(new Tag("punct", this)); // punctuation 		 
		tags.add(new Tag("num", this));   // numbers in any form
		tags.add(new Tag("art", this));   // articles and determiners
		tags.add(new Tag("misc", this));  // others

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

	@Override
	public String name() 
	{
		return "Simple tag set";
	}
	
	@Override
	public Tag getTag(String tag) 
	{
		if(! tagMap.containsKey(tag))
			throw new IllegalArgumentException("Tag ["+tag+"] not recognized");
		
		return tagMap.get(tag);
	}
}

