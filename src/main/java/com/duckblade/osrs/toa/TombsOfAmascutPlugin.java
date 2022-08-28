package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.module.ComponentManager;
import com.duckblade.osrs.toa.module.TombsOfAmascutModule;
import com.google.inject.Binder;
import java.io.File;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Tombs of Amascut",
	description = "Utilities and information for raiding the Tombs of Amascut.",
	tags = {"toa", "raid", "3", "invocation", "preset"}
)
public class TombsOfAmascutPlugin extends Plugin
{

	public static final File TOA_FOLDER = new File(RuneLite.RUNELITE_DIR, "tombs-of-amascut");

	@Inject
	private ComponentManager componentManager;

	@Override
	public void configure(Binder binder)
	{
		binder.install(new TombsOfAmascutModule());
	}

	@Override
	protected void startUp() throws Exception
	{
		if (!TOA_FOLDER.exists() && !TOA_FOLDER.mkdirs())
		{
			log.warn("Failed to create ToA folder {}", TOA_FOLDER.getAbsolutePath());
		}
		componentManager.onPluginStart();
	}

	@Override
	protected void shutDown() throws Exception
	{
		componentManager.onPluginStop();
	}
}
