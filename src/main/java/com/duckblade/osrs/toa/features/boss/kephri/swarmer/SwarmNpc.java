package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

@Getter
public class SwarmNpc
{
	private final NPC npc;

	private final int id;

	private final int index;

	@Setter
	private boolean isAlive;

	@Setter
	private boolean isLeaked;

	private final int waveSpawned;
	private final int phase;

	public SwarmNpc(NPC npc)
	{
		this.npc = npc;
		this.id = npc.getId();
		this.index = npc.getIndex();
		this.isAlive = true;
		this.isLeaked = false;
		this.waveSpawned = Swarmer.WaveNumber;
		this.phase = Swarmer.KephriDownCount;
	}
}