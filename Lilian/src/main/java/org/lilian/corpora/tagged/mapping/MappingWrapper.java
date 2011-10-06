package org.lilian.corpora.tagged.mapping;



import javax.management.RuntimeErrorException;

import org.lilian.corpora.SequenceCorpus;
import org.lilian.corpora.SequenceIterator;
import org.lilian.corpora.wrappers.SequenceWrapper;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;
import org.lilian.pos.TaggedCorpus;
import org.lilian.pos.TaggedWord;


public class MappingWrapper extends SequenceWrapper<TaggedWord, TaggedWord> 
		implements TaggedCorpus
{

	private TagMapper mapper;
	
	public MappingWrapper(SequenceCorpus<TaggedWord> master, TagMapper mapper) {
		super(master);
		this.mapper = mapper;
	}

	@Override
	public SequenceIterator<TaggedWord> iterator() 
	{
		return new MappingIterator();
	}
	
	public class MappingIterator extends SequenceWrapper<TaggedWord, TaggedWord>.WrapperIterator
	{
		@Override
		public TaggedWord next() 
		{
			TaggedWord in, out;
			
			in = masterIterator.next();
			Tag outTag;
			try
			{
				outTag = mapper.map(in.tag());
			} catch(Exception e)
			{
				throw new RuntimeException("Mapping error in tagged word: " + in + " .", e);				
			}
			out = new TaggedWord(in.word(), outTag);
			
			return out;
		}
	}
	
	public static MappingWrapper wrap(SequenceCorpus<TaggedWord> master, TagMapper mapper)
	{
		return new MappingWrapper(master, mapper);
	}

	@Override
	public TagSet tagSet()
	{
		return mapper.output();
	}
}
