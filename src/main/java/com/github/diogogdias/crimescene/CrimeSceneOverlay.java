package com.github.diogogdias.crimescene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ImageUtil;

class CrimeSceneOverlay extends Overlay
{
	private final Client client;
	private final CrimeScenePlugin plugin;
	private final CrimeSceneConfig config;
	private final TooltipManager tooltipManager;

	private static final int SKULL_SCALE_PERCENT = 25;

	private BufferedImage scaledSkull;
	private BufferedImage scaledSource;
	private int scaledPercent = -1;

	@Inject
	CrimeSceneOverlay(Client client, CrimeScenePlugin plugin, CrimeSceneConfig config, TooltipManager tooltipManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.tooltipManager = tooltipManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<WorldPoint, CrimeSceneMarker> markers = plugin.getMarkers();
		if (markers.isEmpty())
		{
			return null;
		}

		int plane = client.getPlane();
		Color border = config.borderColor();
		Color fill = config.fillColor();
		Color font = config.fontColor();
		BufferedImage skull = config.showSkull()
			? getScaledSkull(plugin.getSkullImage(), SKULL_SCALE_PERCENT)
			: null;
		Point mouse = client.getMouseCanvasPosition();
		graphics.setFont(graphics.getFont().deriveFont((float) config.fontSize()));
		graphics.setStroke(new BasicStroke(2));

		for (Map.Entry<WorldPoint, CrimeSceneMarker> entry : markers.entrySet())
		{
			WorldPoint worldPoint = entry.getKey();
			if (worldPoint.getPlane() != plane)
			{
				continue;
			}

			CrimeSceneMarker marker = entry.getValue();
			LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
			if (localPoint == null)
			{
				continue;
			}

			Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
			if (poly != null)
			{
				graphics.setColor(fill);
				graphics.fill(poly);
				graphics.setColor(border);
				graphics.draw(poly);

				if (mouse != null && poly.contains(mouse.getX(), mouse.getY()))
				{
					tooltipManager.add(new Tooltip(buildTooltip(marker)));
				}
			}

			boolean drawSkull = skull != null;
			if (drawSkull)
			{
				OverlayUtil.renderImageLocation(client, graphics, localPoint, skull, 0);
			}

			String text = String.valueOf(marker.getNumber());
			Point textLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, text, 0);
			if (textLocation != null)
			{
				int yOffset = drawSkull ? skull.getHeight() / 2 + 4 : 0;
				Point numberLocation = new Point(textLocation.getX(), textLocation.getY() - yOffset);
				OverlayUtil.renderTextLocation(graphics, numberLocation, text, font);
			}
		}

		return null;
	}

	private BufferedImage getScaledSkull(BufferedImage source, int percent)
	{
		if (source == null)
		{
			return null;
		}

		if (scaledSkull == null || scaledSource != source || scaledPercent != percent)
		{
			int width = Math.max(1, source.getWidth() * percent / 100);
			int height = Math.max(1, source.getHeight() * percent / 100);
			scaledSkull = ImageUtil.resizeImage(source, width, height);
			scaledSource = source;
			scaledPercent = percent;
		}

		return scaledSkull;
	}

	private static String buildTooltip(CrimeSceneMarker marker)
	{
		StringBuilder builder = new StringBuilder();
		appendVictims(builder, marker.getPlayers());
		appendVictims(builder, marker.getNpcs());
		return builder.toString();
	}

	private static void appendVictims(StringBuilder builder, Map<String, Integer> victims)
	{
		if (victims == null)
		{
			return;
		}

		for (Map.Entry<String, Integer> victim : victims.entrySet())
		{
			if (builder.length() > 0)
			{
				builder.append("</br>");
			}
			builder.append(victim.getKey()).append(": ").append(victim.getValue());
		}
	}
}
