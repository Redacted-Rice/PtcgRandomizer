package redactedrice.ptcgr.config.support;

import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;

public class PtcgAdditionalLineArgs 
{
	private CardGroup<MonsterCard> allPokes;
	
	public PtcgAdditionalLineArgs(CardGroup<MonsterCard> allPokes) 
	{
		this.allPokes = allPokes;
	}


	public CardGroup<MonsterCard> getAllPokes() 
	{
		return allPokes;
	}
}
