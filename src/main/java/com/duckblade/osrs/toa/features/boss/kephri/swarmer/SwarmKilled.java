package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.google.gson.annotations.SerializedName;
import net.runelite.client.party.messages.PartyMemberMessage;

/** Party message indicating a swarm was killed by a party member. */
public class SwarmKilled extends PartyMemberMessage
{
	@SerializedName("i")
	private int npcIndex;

	// Needed for deserialization by the party websocket
	public SwarmKilled()
	{
	}

	public SwarmKilled(int npcIndex)
	{
		this.npcIndex = npcIndex;
	}

	public int getNpcIndex()
	{
		return npcIndex;
	}
}
