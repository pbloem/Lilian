package org.lilian.pos.brown;

import java.util.HashMap;
import java.util.Map;

import org.lilian.corpora.tagged.mapping.TagMapper;
import org.lilian.pos.SimpleTagSet;
import org.lilian.pos.Tag;
import org.lilian.pos.TagSet;


public class BrownToSimple implements TagMapper {
	
	private TagSet brown = new BrownTagSet();
	private TagSet simple = new SimpleTagSet();
	
	private Map<String, String> map = new HashMap<String, String>();
	
	public BrownToSimple()
	{
		fillMap();
	}

	@Override
	public Tag map(Tag in) 
	{
		String tag = in.toString();
		
		// Cut off headline and title suffixes
		while(	tag.toLowerCase().endsWith("-hl") || 
				tag.toLowerCase().endsWith("-tl") ||
				tag.toLowerCase().endsWith("-nc"))				
			tag = tag.substring(0, tag.length() -3);
		
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
		
		map.put("NIL"  			   ,"misc");		
		
		map.put("``"  			   ,"punct");
		map.put("''"  			   ,"punct");		
		map.put("'"  			   ,"punct");		
		map.put("("			       ,"punct");
		map.put(")"                ,"punct");
		map.put("*"                ,"punct");
		map.put(","                ,"punct");
		map.put("--"               ,"punct");
		map.put("."                ,"punct");
		map.put(":"                ,"punct");
		map.put("ABL"              ,"mod");
		map.put("ABN"              ,"mod");
		map.put("ABX"              ,"mod");
		map.put("AP"               ,"mod");
		map.put("AP$"              ,"mod");
		map.put("AP+AP"            ,"mod");
		map.put("AT"               ,"art");
		map.put("BE"               ,"verb");
		map.put("BED"              ,"verb");
		map.put("BED*"             ,"verb");
		map.put("BEDZ"             ,"verb");
		map.put("BEDZ*"            ,"verb");
		map.put("BEG"              ,"verb");
		map.put("BEM"              ,"verb");
		map.put("BEM*"             ,"verb");
		map.put("BEN"              ,"verb");
		map.put("BER"              ,"verb");
		map.put("BER*"             ,"verb");
		map.put("BEZ"              ,"verb");
		map.put("BEZ*"             ,"verb");
		map.put("CC"               ,"func");
		map.put("CD"               ,"num");
		map.put("CD$"              ,"num");
		map.put("CS"               ,"func");
		map.put("DO"               ,"verb");
		map.put("DO*"              ,"verb");
		map.put("DO+PPSS"          ,"verb");
		map.put("DOD"              ,"verb");
		map.put("DOD*"             ,"verb");
		map.put("DOZ"              ,"verb");
		map.put("DOZ*"             ,"verb");
		map.put("DT"               ,"art");
		map.put("DT$"              ,"art");
		map.put("DT+BEZ"           ,"art");
		map.put("DT+MD"            ,"art");
		map.put("DTI"              ,"art");
		map.put("DTS"              ,"art");
		map.put("DTS+BEZ"          ,"art");
		map.put("DTX"              ,"art");
		map.put("EX"               ,"art");
		map.put("EX+BEZ"           ,"art");
		map.put("EX+HVD"           ,"art");
		map.put("EX+HVZ"           ,"art");
		map.put("EX+MD"            ,"art");
		map.put("FW-*"             ,"misc");
		map.put("FW-AT"            ,"misc");
		map.put("FW-AT+NN"         ,"misc");
		map.put("FW-AT+NP"         ,"misc");
		map.put("FW-BE"            ,"misc");
		map.put("FW-BER"           ,"misc");
		map.put("FW-BEZ"           ,"misc");
		map.put("FW-CC"            ,"misc");
		map.put("FW-CD"            ,"misc");
		map.put("FW-CS"            ,"misc");
		map.put("FW-DT"            ,"misc");
		map.put("FW-DT+BEZ"        ,"misc");
		map.put("FW-DTS"           ,"misc");
		map.put("FW-HV"            ,"misc");
		map.put("FW-IN"            ,"misc");
		map.put("FW-IN+AT"         ,"misc");
		map.put("FW-IN+NN"         ,"misc");
		map.put("FW-IN+NP"         ,"misc");
		map.put("FW-JJ"            ,"misc");
		map.put("FW-JJR"           ,"misc");
		map.put("FW-JJT"           ,"misc");
		map.put("FW-NN"            ,"misc");
		map.put("FW-NN$"           ,"misc");
		map.put("FW-NNS"           ,"misc");
		map.put("FW-NP"            ,"misc");
		map.put("FW-NPS"           ,"misc");
		map.put("FW-NR"            ,"misc");
		map.put("FW-OD"            ,"misc");
		map.put("FW-PN"            ,"misc");
		map.put("FW-PP$"           ,"misc");
		map.put("FW-PPL"           ,"misc");
		map.put("FW-PPL+VBZ"       ,"misc");
		map.put("FW-PPO"           ,"misc");
		map.put("FW-PPO+IN"        ,"misc");
		map.put("FW-PPS"           ,"misc");
		map.put("FW-PPSS"          ,"misc");
		map.put("FW-PPSS+HV"       ,"misc");
		map.put("FW-QL"            ,"misc");
		map.put("FW-RB"            ,"misc");
		map.put("FW-RB+CC"         ,"misc");
		map.put("FW-TO+VB"         ,"misc");
		map.put("FW-UH"            ,"misc");
		map.put("FW-VB"            ,"misc");
		map.put("FW-VBD"           ,"misc");
		map.put("FW-VBG"           ,"misc");
		map.put("FW-VBN"           ,"misc");
		map.put("FW-VBZ"           ,"misc");
		map.put("FW-WDT"           ,"misc");
		map.put("FW-WPO"           ,"misc");
		map.put("FW-WPS"           ,"misc");
		map.put("HV"               ,"verb");
		map.put("HV*"              ,"verb");
		map.put("HV+TO"            ,"verb");
		map.put("HVD"              ,"verb");
		map.put("HVD*"             ,"verb");
		map.put("HVG"              ,"verb");
		map.put("HVN"              ,"verb");
		map.put("HVZ"              ,"verb");
		map.put("HVZ*"             ,"verb");
		map.put("IN"               ,"func");
		map.put("IN+IN"            ,"func");
		map.put("IN+PPO"           ,"func");
		map.put("JJ"               ,"mod");
		map.put("JJ$"              ,"mod");
		map.put("JJ+JJ"            ,"mod");
		map.put("JJR"              ,"mod");
		map.put("JJR+CS"           ,"mod");
		map.put("JJS"              ,"mod");
		map.put("JJT"              ,"mod");
		map.put("MD"               ,"mod");
		map.put("MD*"              ,"mod");
		map.put("MD+HV"            ,"mod");
		map.put("MD+PPSS"          ,"mod");
		map.put("MD+TO"            ,"mod");
		map.put("NN"               ,"noun");
		map.put("NN$"              ,"noun");
		map.put("NN+BEZ"           ,"noun");
		map.put("NN+HVD"           ,"noun");
		map.put("NN+HVZ"           ,"noun");
		map.put("NN+IN"            ,"noun");
		map.put("NN+MD"            ,"noun");
		map.put("NN+NN"            ,"noun");
		map.put("NNS"              ,"noun");
		map.put("NNS$"             ,"noun");
		map.put("NNS+MD"           ,"noun");
		map.put("NP"               ,"noun");
		map.put("NP$"              ,"noun");
		map.put("NP+BEZ"           ,"noun");
		map.put("NP+HVZ"           ,"noun");
		map.put("NP+MD"            ,"noun");
		map.put("NPS"              ,"noun");
		map.put("NPS$"             ,"noun");
		map.put("NR"               ,"noun");
		map.put("NR$"              ,"noun");
		map.put("NR+MD"            ,"noun");
		map.put("NRS"              ,"noun");
		map.put("OD"               ,"num");
		map.put("PN"               ,"noun");
		map.put("PN$"              ,"noun");
		map.put("PN+BEZ"           ,"noun");
		map.put("PN+HVD"           ,"noun");
		map.put("PN+HVZ"           ,"noun");
		map.put("PN+MD"            ,"noun");
		map.put("PP$"              ,"noun");
		map.put("PP$$"             ,"noun");
		map.put("PPL"              ,"noun");
		map.put("PPLS"             ,"noun");
		map.put("PPO"              ,"noun");
		map.put("PPS"              ,"noun");
		map.put("PPS+BEZ"          ,"noun");
		map.put("PPS+HVD"          ,"noun");
		map.put("PPS+HVZ"          ,"noun");
		map.put("PPS+MD"           ,"noun");
		map.put("PPSS"             ,"noun");
		map.put("PPSS+BEM"         ,"noun");
		map.put("PPSS+BER"         ,"noun");
		map.put("PPSS+BEZ"         ,"noun");
		map.put("PPSS+BEZ*"        ,"noun");
		map.put("PPSS+HV"          ,"noun");
		map.put("PPSS+HVD"         ,"noun");
		map.put("PPSS+MD"          ,"noun");
		map.put("PPSS+VB"          ,"noun");
		map.put("QL"               ,"mod");
		map.put("QLP"              ,"mod");
		map.put("RB"               ,"mod");
		map.put("RB$"              ,"mod");
		map.put("RB+BEZ"           ,"mod");
		map.put("RB+CS"            ,"mod");
		map.put("RBR"              ,"mod");
		map.put("RBR+CS"           ,"mod");
		map.put("RBT"              ,"mod");
		map.put("RN"               ,"mod");
		map.put("RP"               ,"mod");
		map.put("RP+IN"            ,"mod");
		map.put("TO"               ,"func");
		map.put("TO+VB"            ,"verb");
		map.put("UH"               ,"misc");
		map.put("VB"               ,"verb");
		map.put("VB+AT"            ,"verb");
		map.put("VB+IN"            ,"verb");
		map.put("VB+JJ"            ,"verb");
		map.put("VB+PPO"           ,"verb");
		map.put("VB+RP"            ,"verb");
		map.put("VB+TO"            ,"verb");
		map.put("VB+VB"            ,"verb");
		map.put("VBD"              ,"verb");
		map.put("VBG"              ,"verb");
		map.put("VBG+TO"           ,"verb");
		map.put("VBN"              ,"verb");
		map.put("VBN+TO"           ,"verb");
		map.put("VBZ"              ,"verb");
		map.put("WDT"              ,"art");
		map.put("WDT+BER"          ,"art");
		map.put("WDT+BER+PP"       ,"art");
		map.put("WDT+BEZ"          ,"art");
		map.put("WDT+DO+PPS"       ,"art");
		map.put("WDT+DOD"          ,"art");
		map.put("WDT+HVZ"          ,"art");
		map.put("WP$"              ,"art");
		map.put("WPO"              ,"art");
		map.put("WPS"              ,"art");
		map.put("WPS+BEZ"          ,"art");
		map.put("WPS+HVD"          ,"art");
		map.put("WPS+HVZ"          ,"art");
		map.put("WPS+MD"           ,"art");
		map.put("WQL"              ,"art");
		map.put("WRB"              ,"mod");
		map.put("WRB+BER"          ,"mod");
		map.put("WRB+BEZ"          ,"mod");
		map.put("WRB+DO"           ,"mod");
		map.put("WRB+DOD"          ,"mod");
		map.put("WRB+DOD*"         ,"mod");
		map.put("WRB+DOZ"          ,"mod");
		map.put("WRB+IN"           ,"mod");
		map.put("WRB+MD"           ,"mod");

	}

	@Override
	public TagSet input()
	{
		return brown;
	}

	@Override
	public TagSet output()
	{
		return simple;
	}

}
