package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class IgnoreListPartyBoardHighlighter
	implements PluginLifecycleComponent
{
	private static final int SCRIPT_ID_TOA_PARTYDETAILS_ADDMEMBER = 6722;
	private static final int SCRIPT_ID_TOA_PARTYLIST_ADDLINE = 6601;

	private final EventBus eventBus;
	private final Client client;
	private boolean needScanPartyMembers;
	private boolean needScanPartyList;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState state)
	{
		return state.isInLobby() &&
			config.partyBoardShowIgnores();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		needScanPartyMembers = true;
		needScanPartyList = true;
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == SCRIPT_ID_TOA_PARTYDETAILS_ADDMEMBER)
		{
			needScanPartyMembers = true;
		}
		else if (e.getScriptId() == SCRIPT_ID_TOA_PARTYLIST_ADDLINE)
		{
			needScanPartyList = true;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick e)
	{
		if (needScanPartyMembers)
		{
			scanPartyMembers();
			needScanPartyMembers = false;
		}

		if (needScanPartyList)
		{
			scanPartyList();
			needScanPartyList = false;
		}
	}

	private void scanPartyMembers()
	{
		log.trace("toa addmember");
		Widget memberList = client.getWidget(InterfaceID.ToaPartydetails.MEMBERS_LIST);
		if (memberList == null)
		{
			return;
		}

		for (int i = 0; i < 8; i++)
		{
			// 13 elts per row, name is second in each row
			replaceName(memberList.getChild((13 * i) + 1));
		}
	}

	private void scanPartyList()
	{
		log.trace("scanPartyList");
		Widget partyList = client.getWidget(InterfaceID.ToaPartylist.LIST);
		Widget[] staticChildren;
		if (partyList == null || (staticChildren = partyList.getStaticChildren()) == null)
		{
			return;
		}

		for (Widget w : staticChildren)
		{
			replaceName(w.getChild(2));
		}
	}

	private void replaceName(Widget memberName)
	{
		if (memberName == null || Strings.isNullOrEmpty(memberName.getText()))
		{
			return;
		}

		String member = Text.removeTags(memberName.getText());
		if (isIgnored(member))
		{
			memberName.setText("<img=11> " + member);
			memberName.setTextColor(0xff0000);
		}
	}

	private boolean isIgnored(String name)
	{
		return !Strings.isNullOrEmpty(name) &&
			client.getIgnoreContainer().findByName(Text.removeTags(name)) != null;
	}
}
