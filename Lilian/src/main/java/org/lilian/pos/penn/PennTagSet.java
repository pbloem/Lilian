package org.lilian.pos.penn;

import java.util.*;

import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;


public final class PennTagSet extends AbstractCollection<Tag> implements TagSet 
{
	private List<Tag> tags = new ArrayList<Tag>();
	private Map<String, Tag> map = new HashMap<String, Tag>();
	
	public PennTagSet()
	{
		addTags();
		
		for(Tag tag : tags)
			map.put(tag.getTag().toLowerCase(), tag);
	}

	@Override
	public String name() 
	{
		return "Penn treebank corpus tagset";
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
		
		if(! map.containsKey(lcTag))
		{
			//throw new IllegalArgumentException("Tag ["+tag+"] not recognized");
			Tag newTag = new Tag(tag, this);
			tags.add(newTag);
			map.put(lcTag, newTag);
			
			return newTag;		
		}
		
		return map.get(lcTag);
	}
	
	private void addTags()
	{
		tags.add(new Tag("CC"    , this));
		tags.add(new Tag("CD"    , this));
		tags.add(new Tag("DT"    , this));
		tags.add(new Tag("EX"    , this));
		tags.add(new Tag("FW"    , this));
		tags.add(new Tag("IN"    , this));
		tags.add(new Tag("JJ"    , this));
		tags.add(new Tag("JJR"    , this));
		tags.add(new Tag("JJS"    , this));
		tags.add(new Tag("LS"    , this));
		tags.add(new Tag("MD"    , this));
		tags.add(new Tag("NN"    , this));
		tags.add(new Tag("NNS"    , this));
		tags.add(new Tag("NNP"    , this));
		tags.add(new Tag("NNPS"    , this));
		tags.add(new Tag("PDT"    , this));
		tags.add(new Tag("POS"    , this));
		tags.add(new Tag("PRP"    , this));
		tags.add(new Tag("PRP$"    , this));
		tags.add(new Tag("RB"    , this));
		tags.add(new Tag("RBR"    , this));
		tags.add(new Tag("RBS"    , this));
		tags.add(new Tag("RP"    , this));
		tags.add(new Tag("SYM"    , this));
		tags.add(new Tag("TO"    , this));
		tags.add(new Tag("UH"    , this));
		tags.add(new Tag("VB"    , this));
		tags.add(new Tag("VBD"    , this));
		tags.add(new Tag("VBG"    , this));
		tags.add(new Tag("VBN"    , this));
		tags.add(new Tag("VBP"    , this));
		tags.add(new Tag("VBZ"    , this));
		tags.add(new Tag("WDT"    , this));
		tags.add(new Tag("WP"    , this));
		tags.add(new Tag("WP$"    , this));
		tags.add(new Tag("WRB"    , this));
	
	}
}
