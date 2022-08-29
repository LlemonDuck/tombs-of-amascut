/*
 * Copyright (c) 2022, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SarcophagusOpeningSoundPlayer implements PluginLifecycleComponent
{
	private static final Set<Integer> SARCOPHAGUS_IDS = ImmutableSet.of(46220, 46221);
	private static final int CHEST_OPENING_ANIMATION_ID = 9505;

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;

	private Clip clip = null;
	private GameObject chest = null;

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.chestAudioEnable();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		clip = null;
		chest = null;

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Check for sarcophagus in case they turned this on while inside the loot room
		final Tile[][] planeTiles = client.getScene().getTiles()[client.getPlane()];
		for (Tile[] planeTiles1d : planeTiles)
		{
			for (final Tile t : planeTiles1d)
			{
				for (GameObject obj : t.getGameObjects())
				{
					if (obj == null)
					{
						continue;
					}

					if (checkGameObjectIsSarcophagus(obj))
					{
						return;
					}
				}
			}
		}
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);

		if (clip != null)
		{
			clip.close();
		}
		clip = null;
		chest = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged c)
	{
		if (!c.getGroup().equals(TombsOfAmascutConfig.CONFIG_GROUP))
		{
			return;
		}

		if (c.getKey().equals(TombsOfAmascutConfig.CHEST_AUDIO_VOLUME_KEY))
		{
			// Play clip when changing audio config so they can preview the change
			playClip();
		}
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e)
	{
		checkGameObjectIsSarcophagus(e.getGameObject());
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned e)
	{
		if (chest != null && e.getGameObject().getId() == chest.getId())
		{
			chest = null;
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (chest == null)
		{
			return;
		}

		if (((DynamicObject) chest).getAnimation().getId() == CHEST_OPENING_ANIMATION_ID)
		{
			chest = null;
			playClip();
		}
	}

	private boolean loadClip()
	{
		final File f = new File(RuneLite.RUNELITE_DIR, "toa-chest.wav");
		if (!f.exists())
		{
			log.warn("ToA chest opening sound file does not exist, expected " + f.getAbsolutePath());
			return false;
		}

		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f))
		{
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			return true;
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
		{
			if (clip.isOpen())
			{
				clip.close();
			}
			clip = null;
			log.warn("Failed to load toa chest audio");
		}
		return false;
	}

	public void playClip()
	{
		if (clip == null || !clip.isOpen())
		{
			if (!loadClip())
			{
				log.warn("Unable to play audio clip");
				return;
			}
		}

		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = 20f * (float) Math.log10(config.chestAudioVolume() / 100f);

		// Ensure the value we pass is between the clips maximum and minimum value to prevent an IllegalArgumentException
		gain = Math.max(Math.min(gain, volume.getMaximum()), volume.getMinimum());
		volume.setValue(gain);

		// Reset audio to starting frame in case its been played or is currently being played
		clip.setFramePosition(0);

		// From RuneLite base client Notifier class:
		// Using loop prevents the clip from not being played sometimes, presumably from a race condition in the underlying line driver
		clip.loop(0);
	}

	private boolean checkGameObjectIsSarcophagus(GameObject obj)
	{
		if (SARCOPHAGUS_IDS.contains(obj.getId()))
		{
			if (!(obj instanceof DynamicObject))
			{
				log.warn("Found object with sarcophagus id {} but it's not a DynamicObject", obj.getId());
				return false;
			}

			chest = obj;
			return true;

		}

		return false;
	}
}
