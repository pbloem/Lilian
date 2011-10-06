package org.lilian.pos.brown;

import java.util.*;

import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;


public final class BrownTagSet extends AbstractCollection<Tag> implements TagSet 
{
	private List<Tag> tags = new ArrayList<Tag>(239);
	private Map<String, Tag> map = new HashMap<String, Tag>();
	
	public BrownTagSet()
	{
		addTags();
		
		for(Tag tag : tags)
			map.put(tag.getTag().toLowerCase(), tag);
	}

	@Override
	public String name() 
	{
		return "Brown corpus tagset";
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
		tags.add(new Tag("("			  ,	this));
		tags.add(new Tag(")"              , this));
		tags.add(new Tag("*"              , this));
		tags.add(new Tag(","              , this));
		tags.add(new Tag("--"             , this));
		tags.add(new Tag("."              , this));
		tags.add(new Tag(":"              , this));
		tags.add(new Tag("ABL"            , this));
		tags.add(new Tag("ABN"            , this));
		tags.add(new Tag("ABX"            , this));
		tags.add(new Tag("AP"             , this));
		tags.add(new Tag("AP$"            , this));
		tags.add(new Tag("AP+AP"          , this));
		tags.add(new Tag("AT"             , this));
		tags.add(new Tag("BE"             , this));
		tags.add(new Tag("BED"            , this));
		tags.add(new Tag("BED*"           , this));
		tags.add(new Tag("BEDZ"           , this));
		tags.add(new Tag("BEDZ*"          , this));
		tags.add(new Tag("BEG"            , this));
		tags.add(new Tag("BEM"            , this));
		tags.add(new Tag("BEM*"           , this));
		tags.add(new Tag("BEN"            , this));
		tags.add(new Tag("BER"            , this));
		tags.add(new Tag("BER*"           , this));
		tags.add(new Tag("BEZ"            , this));
		tags.add(new Tag("BEZ*"           , this));
		tags.add(new Tag("CC"             , this));
		tags.add(new Tag("CD"             , this));
		tags.add(new Tag("CD$"            , this));
		tags.add(new Tag("CS"             , this));
		tags.add(new Tag("DO"             , this));
		tags.add(new Tag("DO*"            , this));
		tags.add(new Tag("DO+PPSS"        , this));
		tags.add(new Tag("DOD"            , this));
		tags.add(new Tag("DOD*"           , this));
		tags.add(new Tag("DOZ"            , this));
		tags.add(new Tag("DOZ*"           , this));
		tags.add(new Tag("DT"             , this));
		tags.add(new Tag("DT$"            , this));
		tags.add(new Tag("DT+BEZ"         , this));
		tags.add(new Tag("DT+MD"          , this));
		tags.add(new Tag("DTI"            , this));
		tags.add(new Tag("DTS"            , this));
		tags.add(new Tag("DTS+BEZ"        , this));
		tags.add(new Tag("DTX"            , this));
		tags.add(new Tag("EX"             , this));
		tags.add(new Tag("EX+BEZ"         , this));
		tags.add(new Tag("EX+HVD"         , this));
		tags.add(new Tag("EX+HVZ"         , this));
		tags.add(new Tag("EX+MD"          , this));
		tags.add(new Tag("FW-*"           , this));
		tags.add(new Tag("FW-AT"          , this));
		tags.add(new Tag("FW-AT+NN"       , this));
		tags.add(new Tag("FW-AT+NP"       , this));
		tags.add(new Tag("FW-BE"          , this));
		tags.add(new Tag("FW-BER"         , this));
		tags.add(new Tag("FW-BEZ"         , this));
		tags.add(new Tag("FW-CC"          , this));
		tags.add(new Tag("FW-CD"          , this));
		tags.add(new Tag("FW-CS"          , this));
		tags.add(new Tag("FW-DT"          , this));
		tags.add(new Tag("FW-DT+BEZ"      , this));
		tags.add(new Tag("FW-DTS"         , this));
		tags.add(new Tag("FW-HV"          , this));
		tags.add(new Tag("FW-IN"          , this));
		tags.add(new Tag("FW-IN+AT"       , this));
		tags.add(new Tag("FW-IN+NN"       , this));
		tags.add(new Tag("FW-IN+NP"       , this));
		tags.add(new Tag("FW-JJ"          , this));
		tags.add(new Tag("FW-JJR"         , this));
		tags.add(new Tag("FW-JJT"         , this));
		tags.add(new Tag("FW-NN"          , this));
		tags.add(new Tag("FW-NN$"         , this));
		tags.add(new Tag("FW-NNS"         , this));
		tags.add(new Tag("FW-NP"          , this));
		tags.add(new Tag("FW-NPS"         , this));
		tags.add(new Tag("FW-NR"          , this));
		tags.add(new Tag("FW-OD"          , this));
		tags.add(new Tag("FW-PN"          , this));
		tags.add(new Tag("FW-PP$"         , this));
		tags.add(new Tag("FW-PPL"         , this));
		tags.add(new Tag("FW-PPL+VBZ"     , this));
		tags.add(new Tag("FW-PPO"         , this));
		tags.add(new Tag("FW-PPO+IN"      , this));
		tags.add(new Tag("FW-PPS"         , this));
		tags.add(new Tag("FW-PPSS"        , this));
		tags.add(new Tag("FW-PPSS+HV"     , this));
		tags.add(new Tag("FW-QL"          , this));
		tags.add(new Tag("FW-RB"          , this));
		tags.add(new Tag("FW-RB+CC"       , this));
		tags.add(new Tag("FW-TO+VB"       , this));
		tags.add(new Tag("FW-UH"          , this));
		tags.add(new Tag("FW-VB"          , this));
		tags.add(new Tag("FW-VBD"         , this));
		tags.add(new Tag("FW-VBG"         , this));
		tags.add(new Tag("FW-VBN"         , this));
		tags.add(new Tag("FW-VBZ"         , this));
		tags.add(new Tag("FW-WDT"         , this));
		tags.add(new Tag("FW-WPO"         , this));
		tags.add(new Tag("FW-WPS"         , this));
		tags.add(new Tag("HV"             , this));
		tags.add(new Tag("HV*"            , this));
		tags.add(new Tag("HV+TO"          , this));
		tags.add(new Tag("HVD"            , this));
		tags.add(new Tag("HVD*"           , this));
		tags.add(new Tag("HVG"            , this));
		tags.add(new Tag("HVN"            , this));
		tags.add(new Tag("HVZ"            , this));
		tags.add(new Tag("HVZ*"           , this));
		tags.add(new Tag("IN"             , this));
		tags.add(new Tag("IN+IN"          , this));
		tags.add(new Tag("IN+PPO"         , this));
		tags.add(new Tag("JJ"             , this));
		tags.add(new Tag("JJ$"            , this));
		tags.add(new Tag("JJ+JJ"          , this));
		tags.add(new Tag("JJR"            , this));
		tags.add(new Tag("JJR+CS"         , this));
		tags.add(new Tag("JJS"            , this));
		tags.add(new Tag("JJT"            , this));
		tags.add(new Tag("MD"             , this));
		tags.add(new Tag("MD*"            , this));
		tags.add(new Tag("MD+HV"          , this));
		tags.add(new Tag("MD+PPSS"        , this));
		tags.add(new Tag("MD+TO"          , this));
		tags.add(new Tag("NN"             , this));
		tags.add(new Tag("NN$"            , this));
		tags.add(new Tag("NN+BEZ"         , this));
		tags.add(new Tag("NN+HVD"         , this));
		tags.add(new Tag("NN+HVZ"         , this));
		tags.add(new Tag("NN+IN"          , this));
		tags.add(new Tag("NN+MD"          , this));
		tags.add(new Tag("NN+NN"          , this));
		tags.add(new Tag("NNS"            , this));
		tags.add(new Tag("NNS$"           , this));
		tags.add(new Tag("NNS+MD"         , this));
		tags.add(new Tag("NP"             , this));
		tags.add(new Tag("NP$"            , this));
		tags.add(new Tag("NP+BEZ"         , this));
		tags.add(new Tag("NP+HVZ"         , this));
		tags.add(new Tag("NP+MD"          , this));
		tags.add(new Tag("NPS"            , this));
		tags.add(new Tag("NPS$"           , this));
		tags.add(new Tag("NR"             , this));
		tags.add(new Tag("NR$"            , this));
		tags.add(new Tag("NR+MD"          , this));
		tags.add(new Tag("NRS"            , this));
		tags.add(new Tag("OD"             , this));
		tags.add(new Tag("PN"             , this));
		tags.add(new Tag("PN$"            , this));
		tags.add(new Tag("PN+BEZ"         , this));
		tags.add(new Tag("PN+HVD"         , this));
		tags.add(new Tag("PN+HVZ"         , this));
		tags.add(new Tag("PN+MD"          , this));
		tags.add(new Tag("PP$"            , this));
		tags.add(new Tag("PP$$"           , this));
		tags.add(new Tag("PPL"            , this));
		tags.add(new Tag("PPLS"           , this));
		tags.add(new Tag("PPO"            , this));
		tags.add(new Tag("PPS"            , this));
		tags.add(new Tag("PPS+BEZ"        , this));
		tags.add(new Tag("PPS+HVD"        , this));
		tags.add(new Tag("PPS+HVZ"        , this));
		tags.add(new Tag("PPS+MD"         , this));
		tags.add(new Tag("PPSS"           , this));
		tags.add(new Tag("PPSS+BEM"       , this));
		tags.add(new Tag("PPSS+BER"       , this));
		tags.add(new Tag("PPSS+BEZ"       , this));
		tags.add(new Tag("PPSS+BEZ*"      , this));
		tags.add(new Tag("PPSS+HV"        , this));
		tags.add(new Tag("PPSS+HVD"       , this));
		tags.add(new Tag("PPSS+MD"        , this));
		tags.add(new Tag("PPSS+VB"        , this));
		tags.add(new Tag("QL"             , this));
		tags.add(new Tag("QLP"            , this));
		tags.add(new Tag("RB"             , this));
		tags.add(new Tag("RB$"            , this));
		tags.add(new Tag("RB+BEZ"         , this));
		tags.add(new Tag("RB+CS"          , this));
		tags.add(new Tag("RBR"            , this));
		tags.add(new Tag("RBR+CS"         , this));
		tags.add(new Tag("RBT"            , this));
		tags.add(new Tag("RN"             , this));
		tags.add(new Tag("RP"             , this));
		tags.add(new Tag("RP+IN"          , this));
		tags.add(new Tag("TO"             , this));
		tags.add(new Tag("TO+VB"          , this));
		tags.add(new Tag("UH"             , this));
		tags.add(new Tag("VB"             , this));
		tags.add(new Tag("VB+AT"          , this));
		tags.add(new Tag("VB+IN"          , this));
		tags.add(new Tag("VB+JJ"          , this));
		tags.add(new Tag("VB+PPO"         , this));
		tags.add(new Tag("VB+RP"          , this));
		tags.add(new Tag("VB+TO"          , this));
		tags.add(new Tag("VB+VB"          , this));
		tags.add(new Tag("VBD"            , this));
		tags.add(new Tag("VBG"            , this));
		tags.add(new Tag("VBG+TO"         , this));
		tags.add(new Tag("VBN"            , this));
		tags.add(new Tag("VBN+TO"         , this));
		tags.add(new Tag("VBZ"            , this));
		tags.add(new Tag("WDT"            , this));
		tags.add(new Tag("WDT+BER"        , this));
		tags.add(new Tag("WDT+BER+PP"     , this));
		tags.add(new Tag("WDT+BEZ"        , this));
		tags.add(new Tag("WDT+DO+PPS"     , this));
		tags.add(new Tag("WDT+DOD"        , this));
		tags.add(new Tag("WDT+HVZ"        , this));
		tags.add(new Tag("WP$"            , this));
		tags.add(new Tag("WPO"            , this));
		tags.add(new Tag("WPS"            , this));
		tags.add(new Tag("WPS+BEZ"        , this));
		tags.add(new Tag("WPS+HVD"        , this));
		tags.add(new Tag("WPS+HVZ"        , this));
		tags.add(new Tag("WPS+MD"         , this));
		tags.add(new Tag("WQL"            , this));
		tags.add(new Tag("WRB"            , this));
		tags.add(new Tag("WRB+BER"        , this));
		tags.add(new Tag("WRB+BEZ"        , this));
		tags.add(new Tag("WRB+DO"         , this));
		tags.add(new Tag("WRB+DOD"        , this));
		tags.add(new Tag("WRB+DOD*"       , this));
		tags.add(new Tag("WRB+DOZ"        , this));
		tags.add(new Tag("WRB+IN"         , this));
		tags.add(new Tag("WRB+MD"         , this));		
	}
}
