package org.lilian.pos.penn;

import java.util.HashMap;
import java.util.Map;

import org.lilian.corpora.tagged.mapping.TagMapper;
import org.lilian.pos.SimpleTagSet;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;


public class PennToSimple implements TagMapper {
	
	private TagSet penn = new PennTagSet();
	private TagSet simple = new SimpleTagSet();
	
	private Map<String, String> map = new HashMap<String, String>();
	
	public PennToSimple()
	{
		fillMap();
	}

	@Override
	public Tag map(Tag in) 
	{
		String tag = in.toString();
		
		String simpleTag = null;
		if(map.containsKey(tag.toUpperCase()))
		{
			simpleTag = map.get(tag.toUpperCase());
		} else 
		{
			if(tag.toLowerCase().startsWith("fw"))
				simpleTag = "misc";
			else if(tag.toLowerCase().startsWith("pp"))
				simpleTag = "noun";			
			else
				throw new IllegalArgumentException("Could not map tag "+in.getTag()+". Base ("+tag+") is unknown.");
		}
		
		return simple.getTag(simpleTag);
	}
	
	
	private void fillMap() 
	{
		// issues:
		// * Do we put determiners (these, those) with the functions words
		//   or with the articles?
		// * Do we put modal verbs (should, could, would) with the verbs (which
		//   they are only in germanic languages) or with the modifiers?
		
		// verb
		// noun
		// punct
		// num 
		// art 
		// misc
		
		
		map.put("."     , "punct");  // 
		map.put(","     , "punct");  // 
		map.put("``"    , "punct");  //
		map.put("''"    , "punct");  //
		map.put(":"    , "punct");  //		
		map.put("-NONE-"    , "punct");  //

		
		map.put("CC"    , "misc"  );          //  Coordinating conjunction
		map.put("CD"    , "num"  );           //  Cardinal number
		map.put("DT"    , "art"  );           //  Determiner
		map.put("EX"    , "misc"  );          //  Existential there
		map.put("FW"    , "misc"  );          //  Foreign word
		map.put("IN"    , "misc"  );          //  Preposition or subordinating conjunction
		map.put("JJ"    , "misc"  );          //  Adjective
		map.put("JJR"   , "misc"  );          //  Adjective, comparative
		map.put("JJS"   , "misc"  );              //  Adjective, superlative
		map.put("LS"    , "misc"  );              //  List item marker
		map.put("MD"    , "verb"  );              //  Modal
		map.put("NN"    , "noun"  );              //  Noun, singular or mass
		map.put("NNS"   , "noun"  );              //  Noun, plural
		map.put("NNP"   , "noun"  );              //  Proper noun, singular
		map.put("NNPS"  , "noun"  );              //  Proper noun, plural
		map.put("PDT"   , "misc"  );              //  Predeterminer
		map.put("POS"   , "misc"  );              //  Possessive ending
		map.put("PRP"   , "misc"  );              //  Personal pronoun
		map.put("PRP$"  , "misc"  );              //  Possessive pronoun
		map.put("RB"    , "misc"  );              //  Adverb
		map.put("RBR"   , "misc"  );              //  Adverb, comparative
		map.put("RBS"   , "misc"  );              //  Adverb, superlative
		map.put("RP"    , "misc"  );              //  Particle
		map.put("SYM"   , "misc"  );              //  Symbol
		map.put("TO"    , "misc"  );              //  to
		map.put("UH"    , "misc"  );              //  Interjection
		map.put("VB"    , "verb"  );              //  Verb, base form
		map.put("VBD"   , "verb"  );              //  Verb, past tense
		map.put("VBG"   , "verb"  );              //  Verb, gerund or present participle
		map.put("VBN"   , "verb"  );              //  Verb, past participle
		map.put("VBP"   , "verb"  );              //  Verb, non-3rd person singular present
		map.put("VBZ"   , "verb"  );              //  Verb, 3rd person singular present
		map.put("WDT"   , "misc"  );              //  Wh-determiner
		map.put("WP"    , "misc"  );              //  Wh-pronoun
		map.put("WP$"   , "misc"  );              //  Possessive wh-pronoun
		map.put("WRB"   , "misc"  );              //  Wh-adverb 

	}

	@Override
	public TagSet input()
	{
		return penn;
	}

	@Override
	public TagSet output()
	{
		return simple;
	}

}
