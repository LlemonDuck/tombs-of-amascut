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
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpriteID;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUploadStyle;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class InvocationScreenshot implements PluginLifecycleComponent
{
	private static final int INVOCATION_GROUP_ID = 774;
	private static final int INVOCATION_TITLE_CHILD_ID = 3;
	private static final int INVOCATION_CONTAINER_CHILD_ID = 52;
	private static final int INVOCATION_RIGHT_SIDE_CONTAINER_CHILD_ID = 57;
	private static final int INVOCATION_INFO_CONTAINER_CHILD_ID = 58;
	private static final int INVOCATION_REWARDS_CONTAINER_CHILD_ID = 87;
	private static final int REWARD_BUTTON_SELECTED_VARC = 1086;

	private static final int TOA_PARTY_WIDGET_SCRIPT_ID = 6617;

	private static final BufferedImage CAMERA_IMG = ImageUtil.loadImageResource(InvocationScreenshot.class, "camera.png");
	private static final int CAMERA_OVERRIDE_SPRITE_IDX = -420;
	private static final int CAMERA_HOVER_OVERRIDE_SPRITE_IDX = -421;

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final ImageCapture imageCapture;
	private final ItemManager itemManager;

	private Widget button = null;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return config.invocationScreenshotEnable();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		clientThread.invokeLater(this::createButton);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		button = null;
		removeCameraIconOverride();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() != TOA_PARTY_WIDGET_SCRIPT_ID)
		{
			return;
		}

		createButton();
	}

	private void addCameraIconOverride()
	{
		client.getWidgetSpriteCache().reset();
		// Add images to a sprite background so it works with resource packs
		spriteManager.getSpriteAsync(SpriteID.EQUIPMENT_SLOT_TILE, 0, (img) ->
		{
			final BufferedImage cameraImg = overlapImages(CAMERA_IMG, img);
			client.getSpriteOverrides().put(CAMERA_OVERRIDE_SPRITE_IDX, ImageUtil.getImageSpritePixels(cameraImg, client));
		});
		spriteManager.getSpriteAsync(SpriteID.EQUIPMENT_SLOT_SELECTED, 0, (img) ->
		{
			final BufferedImage cameraImg = overlapImages(CAMERA_IMG, img);
			client.getSpriteOverrides().put(CAMERA_HOVER_OVERRIDE_SPRITE_IDX, ImageUtil.getImageSpritePixels(cameraImg, client));
		});
	}

	private void removeCameraIconOverride()
	{
		client.getWidgetSpriteCache().reset();
		client.getSpriteOverrides().remove(CAMERA_OVERRIDE_SPRITE_IDX);
		client.getSpriteOverrides().remove(CAMERA_HOVER_OVERRIDE_SPRITE_IDX);
	}

	private void createButton()
	{
		final Widget parent = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_TITLE_CHILD_ID);
		if (parent == null)
		{
			return;
		}

		final Widget[] children = parent.getDynamicChildren();
		if (children == null || children.length == 0)
		{
			return;
		}

		// Check if the button was already added as the script will fire again once you add it
		for (Widget child : children)
		{
			if (child.equals(button))
			{
				return;
			}
		}
		// Call this again in case the sprite used as the background of the button changed with a recent ResourcePack change
		addCameraIconOverride();

		button = parent.createChild(-1, WidgetType.GRAPHIC);
		button.setOriginalHeight(20);
		button.setOriginalWidth(20);
		button.setOriginalX(430);
		button.setOriginalY(8);
		button.setSpriteId(CAMERA_OVERRIDE_SPRITE_IDX);
		button.setAction(0, "Screenshot Invocations");
		button.setOnOpListener((JavaScriptCallback) (e) -> clientThread.invokeLater(this::screenshot));
		button.setHasListener(true);
		button.revalidate();

		button.setOnMouseOverListener((JavaScriptCallback) (e) -> button.setSpriteId(CAMERA_HOVER_OVERRIDE_SPRITE_IDX));
		button.setOnMouseLeaveListener((JavaScriptCallback) (e) -> button.setSpriteId(CAMERA_OVERRIDE_SPRITE_IDX));
	}

	private void screenshot()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		client.getWidgetSpriteCache().reset();

		final Widget container = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_CONTAINER_CHILD_ID);
		if (container == null)
		{
			return;
		}

		final Widget[] children = container.getDynamicChildren();
		if (children.length == 0)
		{
			return;
		}

		// Base the height on the first element, including its offset from the top of the page
		int height = children[0].getHeight() + children[0].getRelativeY();
		int y = 0;
		for (Widget invocation : children)
		{
			if (invocation.getRelativeY() > y)
			{
				y = invocation.getRelativeY() + 2; // Ensure at least 2pixels of padding exist between elements
				height = y + invocation.getHeight();
			}
		}

		int width = 287; // Hardcoded minimum width of the interface so that it can be drawn correctly even when on another tab
		boolean rewardButtonSelected = isRewardButtonSelected();
		if (!rewardButtonSelected)
		{
			// If the reward button isn't selected then the correct size can be pulled from the container regardless of the selected tab
			width = container.getWidth();
		}
		else if (config.showRewardsSection())
		{
			final Widget rightSideContainer = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_RIGHT_SIDE_CONTAINER_CHILD_ID);
			assert rightSideContainer != null;
			width += rightSideContainer.getWidth();
		}
		final BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = screenshot.getGraphics();

		final BufferedImage background = getSprite(SpriteID.DIALOG_BACKGROUND);
		int x = screenshot.getWidth() / background.getWidth() + 1;
		y = screenshot.getHeight() / background.getHeight() + 1;
		for (int i = 0; i < x; i++)
		{
			for (int z = 0; z < y; z++)
			{
				graphics.drawImage(background, i * background.getWidth(), z * background.getHeight(), null);
			}
		}

		for (final Widget w : children)
		{
			if (w.getType() == WidgetType.RECTANGLE)
			{
				continue;
			}

			drawWidget(graphics, w, w.getRelativeX(), w.getRelativeY());
		}

		if (rewardButtonSelected && config.showRewardsSection())
		{
			final Widget rightSideContainer = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_RIGHT_SIDE_CONTAINER_CHILD_ID);
			assert rightSideContainer != null;

			// Draw Invocation Level/Bar section
			final Widget infoContainer = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_INFO_CONTAINER_CHILD_ID);
			int infoContainerHeight = 5; // default to 5 so there's at least a 5 px gap between these elements
			if (infoContainer != null && infoContainer.getStaticChildren().length > 0)
			{
				infoContainerHeight += infoContainer.getHeight();
				// Move the reward container closer since we don't draw the scrollbar
				Graphics layer = graphics.create(
					rightSideContainer.getRelativeX() - 30,
					rightSideContainer.getRelativeY() - 60,
					rightSideContainer.getWidth(),
					rightSideContainer.getHeight()
				);
				// We want to skip the child elements that draw the border
				Widget[] infoContainerChildren = infoContainer.getStaticChildren();
				infoContainerChildren[0] = null;
				infoContainerChildren[1] = null;

				drawWidgets(layer, infoContainerChildren);
				layer.dispose();
			}

			// Draw reward section
			final Widget rewardsContainer = client.getWidget(INVOCATION_GROUP_ID, INVOCATION_REWARDS_CONTAINER_CHILD_ID);
			if (rewardsContainer != null && rewardsContainer.getDynamicChildren().length > 0)
			{
				// Move the reward container closer since we don't draw the scrollbar
				Graphics layer = graphics.create(
					rightSideContainer.getRelativeX() - 30,
					rightSideContainer.getRelativeY() - 60 + infoContainerHeight,
					rightSideContainer.getWidth(),
					rightSideContainer.getHeight()
				);
				drawWidgets(layer, rewardsContainer.getDynamicChildren());
				layer.dispose();
			}
			else
			{
				log.warn("Couldn't find the invocation rewards container when it should have existed");
			}
		}

		// Convert from ARGB to RGB so it can be stored on the clipboard
		BufferedImage out = toBufferedImageOfType(screenshot, BufferedImage.TYPE_INT_RGB);

		imageCapture.takeScreenshot(out, "invocationscreenshot", "invocations", true, ImageUploadStyle.CLIPBOARD);
		final String message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append("A screenshot of your current invocations was saved and inserted into your clipboard!")
			.build();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	private void drawWidgets(Graphics graphics, Widget[] widgets)
	{
		for (Widget w : widgets)
		{
			if (w == null)
			{
				continue;
			}

			if (w.getDynamicChildren().length > 0)
			{
				Graphics layer = graphics.create(w.getRelativeX(), w.getRelativeY(), w.getWidth(), w.getHeight());
				drawWidgets(layer, w.getDynamicChildren());
				layer.dispose();
			}
			else
			{
				drawWidget(graphics, w, w.getRelativeX(), w.getRelativeY());
			}
		}
	}

	private void drawWidget(Graphics graphics, Widget child, int x, int y)
	{
		if (child == null || child.getType() == 0)
		{
			return;
		}

		int width = child.getWidth();
		int height = child.getHeight();

		if (child.getSpriteId() > 0)
		{
			SpritePixels sp = getPixels(child.getSpriteId());
			assert sp != null;
			BufferedImage childImage = sp.toBufferedImage();

			if (child.getSpriteTiling())
			{
				Rectangle clips = graphics.getClipBounds();
				graphics.setClip(x, y, child.getWidth(), child.getHeight());

				for (int dx = x; dx < child.getWidth() + x; dx += sp.getMaxWidth())
				{
					for (int dy = y; dy < child.getHeight() + y; dy += sp.getMaxHeight())
					{

						drawAt(graphics, childImage, dx + sp.getOffsetX(), dy + sp.getOffsetY());
					}
				}

				graphics.setClip(clips);
			}
			else
			{
				if (width == childImage.getWidth() && height == childImage.getHeight())
				{
					drawAt(graphics, childImage, x, y);
				}
				else
				{
					drawScaled(graphics, childImage, x, y, width, height);
				}
			}
		}
		else if (child.getItemId() > 0)
		{
			BufferedImage image = itemManager.getImage(itemManager.canonicalize(child.getItemId()), 1, false);
			if (child.getOpacity() > 0)
			{
				image = ImageUtil.alphaOffset(image, child.getOpacity() / 255f);
				// Convert to RGB
				BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = copy.createGraphics();
				g.setComposite(AlphaComposite.Src);
				g.setColor(new Color(0, 0, 0, child.getOpacity()));
				g.drawImage(image, 0, 0, null);
				g.dispose();
			}
			graphics.drawImage(image, child.getRelativeX(), child.getRelativeY(), null);
		}
		else if (child.getType() == WidgetType.TEXT)
		{
			final String text = Text.removeTags(child.getText());
			Font font = FontManager.getRunescapeSmallFont();

			x = child.getRelativeX();
			y = child.getRelativeY();
			width = child.getWidth();
			height = child.getHeight();

			final Graphics textLayer = graphics.create(x, y, width, height);
			textLayer.setFont(font);

			int xPos = 0;
			int yPos = 0;

			int textWidth = textLayer.getFontMetrics().stringWidth(text);

			if (child.getXTextAlignment() == 1)
			{
				xPos = (width - textWidth) / 2 + 1;
			}

			if (child.getYTextAlignment() == 0)
			{
				yPos = font.getSize() - 3;
			}
			else if (child.getYTextAlignment() == 1)
			{
				yPos = (height + font.getSize()) / 2 - 1;
			}
			else if (child.getYTextAlignment() == 2)
			{
				yPos = height;
			}

			if (child.getTextShadowed())
			{
				textLayer.setColor(Color.BLACK);
				textLayer.drawString(text, xPos, yPos);
				xPos -= 1;
				yPos -= 1;
			}

			textLayer.setColor(new Color(child.getTextColor()));
			textLayer.drawString(text, xPos, yPos);
			textLayer.dispose();
		}
		else if (child.getType() == WidgetType.RECTANGLE || child.getType() == WidgetType.GRAPHIC)
		{
			Color c = new Color(child.getTextColor());
			if (child.getOpacity() > 0)
			{
				c = new Color(c.getRed(), c.getGreen(), c.getBlue(), child.getOpacity());
			}
			graphics.setColor(c);
			final Rectangle r = child.getBounds();
			graphics.drawRect(child.getRelativeX(), child.getRelativeY(), r.width, r.height);
		}
	}

	@Nullable
	private SpritePixels getPixels(int archive)
	{
		if (config.useResourcePack())
		{
			SpritePixels pixels = client.getSpriteOverrides().get(archive);
			if (pixels != null)
			{
				return pixels;
			}
		}

		SpritePixels[] sp = client.getSprites(client.getIndexSprites(), archive, 0);
		assert sp != null;
		return sp[0];
	}

	private BufferedImage getSprite(int id)
	{
		SpritePixels sp = getPixels(id);
		assert sp != null;
		return sp.toBufferedImage();
	}

	private void drawScaled(Graphics graphics, BufferedImage image, int x, int y, int width, int height)
	{
		image = ImageUtil.resizeCanvas(image, width, height);
		graphics.drawImage(image, x, y, null);
	}

	private void drawAt(Graphics graphics, BufferedImage image, int x, int y)
	{
		graphics.drawImage(image, x, y, null);
	}

	private static BufferedImage overlapImages(final BufferedImage foreground, final BufferedImage background)
	{
		final int centeredX = background.getWidth() / 2 - foreground.getWidth() / 2;
		final int centeredY = background.getHeight() / 2 - foreground.getHeight() / 2;

		BufferedImage combined = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = combined.createGraphics();
		g2d.drawImage(background, 0, 0, null);

		g2d.drawImage(foreground, centeredX, centeredY, null);
		g2d.dispose();

		return combined;
	}

	private static BufferedImage toBufferedImageOfType(BufferedImage original, int type)
	{
		if (original == null || original.getType() == type)
		{
			return original;
		}

		BufferedImage out = new BufferedImage(original.getWidth(), original.getHeight(), type);
		Graphics2D g = out.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(original, 0, 0, null);
		g.dispose();

		return out;
	}

	private boolean isRewardButtonSelected()
	{
		return client.getVarcIntValue(REWARD_BUTTON_SELECTED_VARC) == 0;
	}
}
