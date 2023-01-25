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
package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.io.File;
import java.io.IOException;
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
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SarcophagusOpeningSoundPlayer implements PluginLifecycleComponent
{
	private static final int SARCOPHAGUS_ID = 44934;

	private final EventBus eventBus;
	private final TombsOfAmascutConfig config;

	private Clip clip = null;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.chestAudioEnable();
	}

	@Override
	public void startUp()
	{
		clip = null;
		eventBus.register(this);
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
		if (e.getGameObject().getId() != SARCOPHAGUS_ID)
		{
			return;
		}

		// The sarcophagus spawns as the player starts looting the chest
		playClip();
	}

	private boolean loadClip()
	{
		final File f = new File(TombsOfAmascutPlugin.TOA_FOLDER, "toa-chest.wav");
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
}
