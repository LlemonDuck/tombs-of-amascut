package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.LobbyEntranceWarning;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LobbySpellbookWarning
	implements PluginLifecycleComponent, LobbyEntranceWarning.Check
{

	private static final Color WARNING_COLOR = new Color(0x7235C6);

	private final Client client;

	private boolean enabled;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return this.enabled = config.spellbookWarning();
	}

	@Override
	public boolean isTriggered()
	{
		return enabled;
	}

	@Override
	public String getText()
	{
		return "Spellbook: " + getSpellbookName();
	}

	@Override
	public Color getColor()
	{
		return WARNING_COLOR;
	}

	private String getSpellbookName()
	{
		switch (client.getVarbitValue(VarbitID.SPELLBOOK))
		{
			case 0:
				return "Standard";

			case 1:
				return "Ancient";

			case 2:
				return "Lunar";

			case 3:
				return "Arceuus";

			default:
				return "Unknown";
		}
	}
}
