/**
 * BSD 2-Clause License
 * <p>
 * Copyright (c) 2023, rdutta
 * Copyright (c) 2020, AnkouOSRS
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.WallObject;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
public class SarcophagusRecolorer implements PluginLifecycleComponent
{

	private static final Set<String> CONFIG_KEYS = ImmutableSet.of(
		TombsOfAmascutConfig.SARCOPHAGUS_RECOLOR_WHITE,
		TombsOfAmascutConfig.SARCOPHAGUS_WHITE_RECOLOR,
		TombsOfAmascutConfig.SARCOPHAGUS_RECOLOR_MY_PURPLE,
		TombsOfAmascutConfig.SARCOPHAGUS_MY_PURPLE_RECOLOR,
		TombsOfAmascutConfig.SARCOPHAGUS_RECOLOR_OTHER_PURPLE,
		TombsOfAmascutConfig.SARCOPHAGUS_OTHER_PURPLE_RECOLOR
	);

	private static final int[] VARBIT_MULTILOC_IDS_CHEST = new int[]{
		14356, 14357, 14358, 14359, 14360, 14370, 14371, 14372
	};

	private static final int VARBIT_VALUE_CHEST_KEY = 2;
	private static final int VARBIT_ID_SARCOPHAGUS = 14373;
	private static final int WALL_OBJECT_ID_SARCOPHAGUS = 46221;

	@Inject
	private EventBus eventBus;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private TombsOfAmascutConfig config;

	private final List<WallObject> wallObjects = new ArrayList<>();

	private int[] defaultFaceColors1;

	private boolean sarcophagusIsPurple;
	private boolean purpleIsMine = true;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.TOMB;
	}

	@Override
	public void startUp()
	{
		clientThread.invokeLater(() ->
		{
			parseVarbits();
			recolor(wallObjects);
		});

		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		wallObjects.clear();
		sarcophagusIsPurple = false;
		purpleIsMine = true;
		defaultFaceColors1 = null;
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (!event.getGroup().equals(TombsOfAmascutConfig.CONFIG_GROUP) || wallObjects.isEmpty())
		{
			return;
		}

		final String key = event.getKey();

		if (CONFIG_KEYS.contains(key))
		{
			clientThread.invokeLater(() -> recolor(wallObjects));
		}
	}

	@Subscribe
	public void onWallObjectSpawned(final WallObjectSpawned event)
	{
		final WallObject wallObject = event.getWallObject();

		if (wallObject.getId() != WALL_OBJECT_ID_SARCOPHAGUS)
		{
			return;
		}

		if (defaultFaceColors1 == null)
		{
			initDefaultFaceColors1(wallObject);
		}

		recolor(wallObject);
		wallObjects.add(wallObject);
	}

	@Subscribe
	public void onWallObjectDespawned(final WallObjectDespawned event)
	{
		final WallObject wallObject = event.getWallObject();

		if (wallObject.getId() == WALL_OBJECT_ID_SARCOPHAGUS)
		{
			wallObjects.remove(wallObject);
		}
	}

	private void parseVarbits()
	{
		sarcophagusIsPurple = client.getVarbitValue(VARBIT_ID_SARCOPHAGUS) % 2 != 0;
		purpleIsMine = true;

		for (final int varbitId : VARBIT_MULTILOC_IDS_CHEST)
		{
			if (client.getVarbitValue(varbitId) == VARBIT_VALUE_CHEST_KEY)
			{
				purpleIsMine = false;
				break;
			}
		}
	}

	private void initDefaultFaceColors1(final WallObject wallObject)
	{
		final Model model = wallObject.getRenderable1().getModel();

		if (model == null)
		{
			return;
		}

		defaultFaceColors1 = model.getFaceColors1().clone();
	}

	private void recolor(final Collection<WallObject> wallObjects)
	{
		for (final WallObject wallObject : wallObjects)
		{
			recolor(wallObject);
		}
	}

	private void recolor(final WallObject wallObject)
	{
		final Model model = wallObject.getRenderable1().getModel();

		if (model == null)
		{
			return;
		}

		final int[] faceColors1 = model.getFaceColors1();

		final Color color;

		if (sarcophagusIsPurple)
		{
			if (purpleIsMine)
			{
				if (!config.sarcophagusRecolorMyPurple())
				{
					resetFaceColors1(faceColors1);
					return;
				}

				color = config.sarcophagusMyPurpleRecolor();
			}
			else
			{
				if (!config.sarcophagusRecolorOtherPurple())
				{
					resetFaceColors1(faceColors1);
					return;
				}

				color = config.sarcophagusOtherPurpleRecolor();
			}
		}
		else
		{
			if (!config.sarcophagusRecolorWhite())
			{
				resetFaceColors1(faceColors1);
				return;
			}

			color = config.sarcophagusWhiteRecolor();
		}

		Arrays.fill(faceColors1, colorToRs2hsb(color));
	}

	private void resetFaceColors1(final int[] faceColors1)
	{
		if (defaultFaceColors1 == null)
		{
			log.error("defaultFaceColors1 was not initialized. Failed to reset faceColors1.");
			return;
		}

		System.arraycopy(defaultFaceColors1, 0, faceColors1, 0, faceColors1.length);
	}

	private static int colorToRs2hsb(final Color color)
	{
		final float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

		// "Correct" the brightness level to avoid going to white at full saturation, or having a low brightness at
		// low saturation
		hsbVals[2] -= Math.min(hsbVals[1], hsbVals[2] / 2);

		final int encode_hue = (int) (hsbVals[0] * 63);
		final int encode_saturation = (int) (hsbVals[1] * 7);
		final int encode_brightness = (int) (hsbVals[2] * 127);
		return (encode_hue << 10) + (encode_saturation << 7) + (encode_brightness);
	}

}
