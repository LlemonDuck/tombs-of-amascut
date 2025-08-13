/*
 * Copyright (c) 2016-2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Jordan Atwood <jordan.atwood423@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.duckblade.osrs.toa.util;

import net.runelite.api.Actor;

public final class NpcUtil
{
	private NpcUtil()
	{
	}

	/**
	 * Calculates Actor's current hitpoints
	 * <p>
	 * Adapted from RuneLite OpponentInfo Plugin
	 *
	 * @param actor the actor
	 * @param maxHp the actor's max hp
	 * @return the actor's current hp, else -1
	 */
	public static int calculateActorHp(final Actor actor, final int maxHp)
	{
		final int scale = actor.getHealthScale();
		final int ratio = actor.getHealthRatio();

		if (scale <= 0 || ratio < 0)
		{
			return -1;
		}

		int health = 0;

		if (ratio > 0)
		{
			int minHealth = 1;
			int maxHealth;

			if (scale > 1)
			{
				if (ratio > 1)
				{
					minHealth = (maxHp * (ratio - 1) + scale - 2) / (scale - 1);
				}

				maxHealth = (maxHp * ratio - 1) / (scale - 1);

				if (maxHealth > maxHp)
				{
					maxHealth = maxHp;
				}
			}
			else
			{
				maxHealth = maxHp;
			}

			health = (minHealth + maxHealth + 1) / 2;
		}

		return health;
	}
}
