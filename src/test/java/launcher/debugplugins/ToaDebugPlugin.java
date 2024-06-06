package launcher.debugplugins;

import ch.qos.logback.classic.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import ch.qos.logback.classic.Logger;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.LoggerFactory;

@Singleton
@PluginDescriptor(
	name = "ToA Debug"
)
public class ToaDebugPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HetSolverDebugOverlay hetSolverDebugOverlay;

	@Inject
	private AkkhaShadowOverridesOverlay shadowOverridesOverlay;

	@Inject
	private EventBus eventBus;

	@Override
	protected void startUp()
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.WARN);
		((Logger) LoggerFactory.getLogger("com.duckblade.osrs.toa")).setLevel(Level.DEBUG);

		overlayManager.add(hetSolverDebugOverlay);
		overlayManager.add(shadowOverridesOverlay);
		eventBus.register(shadowOverridesOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
		overlayManager.remove(hetSolverDebugOverlay);
		overlayManager.remove(shadowOverridesOverlay);
		eventBus.unregister(shadowOverridesOverlay);
	}
}
