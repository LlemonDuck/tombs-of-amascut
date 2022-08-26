package com.duckblade.osrs.toa.features.invocationpresets;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.Invocation;
import com.duckblade.osrs.toa.util.RaidMode;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Runnables;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.util.ColorUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Slf4j
public class InvocationPresetsManager implements PluginLifecycleComponent
{

	public static final int WIDGET_ID_INVOCATIONS_PARENT = 774;
	public static final int WIDGET_ID_INVOCATIONS_CHILD = 52;
	private static final String CONFIG_KEY_PRESETS = "presets";

	private final EventBus eventBus;
	private final ConfigManager configManager;

	private final Client client;
	private final ChatboxPanelManager chatboxPanelManager;
	private final RaidStateTracker raidStateTracker;

	@Getter
	private Set<Invocation> activeInvocations = EnumSet.noneOf(Invocation.class);

	@Getter
	@Setter
	private InvocationPreset currentPreset = null;

	private Widget invocationsWidget;
	private final SortedMap<String, InvocationPreset> presets = new TreeMap<>(Comparator.reverseOrder());

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.invocationPresetsEnable();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		loadPresets();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (!raidStateTracker.isInLobby())
		{
			this.invocationsWidget = null;
			return;
		}

		Widget invocationsWidget = client.getWidget(WIDGET_ID_INVOCATIONS_PARENT, WIDGET_ID_INVOCATIONS_CHILD);
		this.invocationsWidget = invocationsWidget != null && !invocationsWidget.isHidden() ? invocationsWidget : null;
		if (this.invocationsWidget == null)
		{
			this.activeInvocations = EnumSet.noneOf(Invocation.class);
			return;
		}

		EnumSet<Invocation> activeCurrent = EnumSet.noneOf(Invocation.class);
		for (Invocation invoc : Invocation.values())
		{
			Widget invocW = getInvocationWidget(invoc);
			if (invocW == null)
			{
				continue;
			}

			Object[] ops = invocW.getOnOpListener();
			if (ops == null || ops.length < 4 || !(ops[3] instanceof Integer))
			{
				continue;
			}

			if ((Integer) ops[3] == 1)
			{
				activeCurrent.add(invoc);
			}
		}

		if (log.isDebugEnabled() && !activeCurrent.equals(activeInvocations))
		{
			Sets.SetView<Invocation> adds = Sets.difference(activeCurrent, activeInvocations);
			Sets.SetView<Invocation> removes = Sets.difference(activeInvocations, activeCurrent);
			log.debug("Invocations changed! Add: {}, Remove: {}", adds, removes);
		}
		this.activeInvocations = activeCurrent;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!raidStateTracker.isInLobby() || !e.getOption().equals("Preset"))
		{
			return;
		}

		String nameColorTag = ColorUtil.colorTag(new Color(255, 152, 31));
		boolean deleteMode = client.isKeyPressed(KeyCode.KC_SHIFT);
		presets.values().forEach(preset ->
			{
				String modeTag = ColorUtil.colorTag(RaidMode.forRaidLevel(preset.getRaidLevel()).getColor());
				Consumer<MenuEntry> onClick = deleteMode ? ignored -> confirmDeletePreset(preset) : ignored -> setCurrentPreset(preset);
				client.createMenuEntry(-1)
					.setType(MenuAction.RUNELITE)
					.setOption(deleteMode ? "<col=ff0000>Delete" : "Load")
					.setTarget(nameColorTag + preset.getName() + " " + modeTag + "(Lvl " + preset.getRaidLevel() + ")")
					.onClick(onClick);
			}
		);

		if (currentPreset != null)
		{
			client.createMenuEntry(-1)
				.setType(MenuAction.RUNELITE)
				.setOption("Export")
				.setTarget(nameColorTag + currentPreset.getName())
				.onClick(ignored -> exportCurrentPreset());
		}

		client.createMenuEntry(-1)
			.setType(MenuAction.RUNELITE)
			.setOption("Import")
			.setTarget("Preset")
			.onClick(ignored -> importPreset());

		client.createMenuEntry(-1)
			.setType(MenuAction.RUNELITE)
			.setOption("Save")
			.setTarget("New Preset")
			.onClick(ignored -> savePreset());
	}

	public Widget getInvocationWidget(Invocation invocation)
	{
		if (invocationsWidget == null)
		{
			return null;
		}

		Widget[] children = invocationsWidget.getChildren();
		if (children != null && invocation.getWidgetIx() < children.length)
		{
			return children[invocation.getWidgetIx()];
		}

		return null;
	}

	public void addPreset(InvocationPreset preset)
	{
		log.debug("Saving new preset {}" + preset.serialize());
		presets.put(preset.getName(), preset);
		setCurrentPreset(preset);
		configManager.setConfiguration(TombsOfAmascutConfig.CONFIG_GROUP, CONFIG_KEY_PRESETS + "." + preset.getName(), preset.serialize());
	}

	private void confirmDeletePreset(InvocationPreset preset)
	{
		chatboxPanelManager.openTextMenuInput("Are you sure you want to delete your preset called \"" + preset.getName() + "\"?")
			.option("Yes", () -> deletePreset(preset))
			.option("No", Runnables::doNothing)
			.build();
	}

	private void deletePreset(InvocationPreset preset)
	{
		log.debug("Deleting preset {}", preset.getName());
		configManager.unsetConfiguration(TombsOfAmascutConfig.CONFIG_GROUP, CONFIG_KEY_PRESETS + "." + preset.getName());
		presets.remove(preset.getName());
	}

	private void savePreset()
	{
		chatboxPanelManager.openTextInput("Enter new preset name (or existing name to overwrite):")
			.onDone(name ->
			{
				addPreset(new InvocationPreset(name, activeInvocations));
			})
			.build();
	}

	private void loadPresets()
	{
		for (String key : configManager.getConfigurationKeys(TombsOfAmascutConfig.CONFIG_GROUP + "." + CONFIG_KEY_PRESETS))
		{
			try
			{
				String keySuffix = key.split("\\.", 2)[1];
				String configValue = configManager.getConfiguration(TombsOfAmascutConfig.CONFIG_GROUP, keySuffix);
				log.debug("Parsing preset config key = {} value = {}", keySuffix, configValue);

				InvocationPreset preset = InvocationPreset.parse(configValue);
				if (!preset.getName().equals(key))
				{
					log.warn("Mismatched key name from preset name key = {}, name = {}", key, preset.getName());
				}
				InvocationPreset prev = presets.put(preset.getName(), preset);
				if (prev != null)
				{
					log.warn("Config contains duplicate preset name {}", preset.getName());
				}
			}
			catch (Exception e)
			{
				log.error("Failed to parse ToA preset at config key {}", key, e);
			}
		}
	}

	private void importPreset()
	{
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString();
		}
		catch (IOException | UnsupportedFlavorException ex)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, null, "Unable to read system clipboard.", null, false);
			log.warn("error reading clipboard", ex);
			return;
		}

		try
		{
			InvocationPreset preset = InvocationPreset.parse(clipboardText);
			chatboxPanelManager.openTextMenuInput("Import preset \"" + preset.getName() + "\" with " + preset.getInvocations().size() + " invocations?")
				.option("Yes", () -> addPreset(preset))
				.option("No", Runnables::doNothing)
				.build();
		}
		catch (Exception e)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Clipboard does not contain a valid invocation preset.", "", false);
			log.warn("Failed to parse invocation preset", e);
		}
	}

	private void exportCurrentPreset()
	{
		if (currentPreset == null)
		{
			return;
		}

		Toolkit.getDefaultToolkit()
			.getSystemClipboard()
			.setContents(new StringSelection(currentPreset.serialize()), null);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Copied preset \"" + currentPreset.getName() + "\" to clipboard.", "", false);
	}

}
