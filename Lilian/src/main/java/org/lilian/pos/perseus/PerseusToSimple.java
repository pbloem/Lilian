package org.lilian.pos.perseus;

import java.util.HashMap;
import java.util.Map;

import org.lilian.corpora.tagged.mapping.TagMapper;
import org.lilian.pos.SimpleTagSet;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;
import org.lilian.pos.brown.BrownTagSet;

public class PerseusToSimple implements TagMapper
{
	private TagSet simple = new SimpleTagSet();
	
	private Map<String, String> map = new HashMap<String, String>();
	
	public PerseusToSimple()
	{
	}
	
	@Override
	public Tag map(Tag in)
	{
		char base = in.getTag().charAt(0);
		
		String simpleTag = null;
		switch (base)
		{
			case 'n':
				simpleTag = "noun";
				break;				
			case 'v':	
				simpleTag = "verb";
				break;				
			case 't':
				simpleTag = "verb";
				break;				
			case 'a':
				simpleTag = "mod";
				break;				
			case 'd':
				simpleTag = "mod";
				break;
			case 'c':
				simpleTag = "func";
				break;				
			case 'r':
				simpleTag = "func";
				break;				
			case 'p':
				simpleTag = "noun";
				break;				
			case 'm':
				simpleTag = "num";
				break;				
			case 'i':
				simpleTag = "misc";
				break;				
			case 'e':
				simpleTag = "punct";
				break;				
			case 'u':
				simpleTag = "punct";
				break;
			case '-':
				simpleTag = "misc";
				break;
		}
		
		return simple.getTag(simpleTag);
	}
	
	@Override
	public TagSet input()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TagSet output()
	{
		return simple;
	}		

}

