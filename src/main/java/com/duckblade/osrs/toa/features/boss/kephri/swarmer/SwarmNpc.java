package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import lombok.Data;
import net.runelite.api.NPC;

@Data
public class SwarmNpc
{
	private final NPC npc;

	private final int waveSpawned;

	private final int phase;

	private boolean isAlive;

	private boolean isLeaked;
}