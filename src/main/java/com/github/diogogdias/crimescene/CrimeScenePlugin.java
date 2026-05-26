package com.github.diogogdias.crimescene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Crime Scene",
	description = "Marks the tile where each friend dies and labels it with that tile's death count",
	tags = {"death", "friend", "marker", "tile", "pvm", "pvp"}
)
public class CrimeScenePlugin extends Plugin
{
	static final String CONFIG_GROUP = "crime-scene";
	private static final String MARKERS_KEY = "markers";
	private static final Type MARKER_LIST_TYPE = new TypeToken<List<CrimeSceneMarker>>()
	{
	}.getType();

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CrimeSceneOverlay overlay;

	@Inject
	private CrimeSceneConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private CrimeScenePanel panel;

	@Getter(AccessLevel.PACKAGE)
	private final Map<WorldPoint, CrimeSceneMarker> markers = new ConcurrentHashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private volatile BufferedImage skullImage;

	private NavigationButton navButton;

	private final HotkeyListener clearHotkey = new HotkeyListener(() -> config.clearHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			clearMarkers();
		}
	};

	@Override
	protected void startUp()
	{
		loadMarkers();
		spriteManager.getSpriteAsync(SpriteID.ICON_SKULL, 0, image -> skullImage = image);
		overlayManager.add(overlay);
		keyManager.registerKeyListener(clearHotkey);

		navButton = NavigationButton.builder()
			.tooltip("Crime Scene")
			.icon(ImageUtil.loadImageResource(CrimeScenePlugin.class, "panel_icon.png"))
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		panel.setClearCallback(this::clearMarkers);
		refreshPanel();

		log.debug("Crime Scene started");
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(clearHotkey);
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
		markers.clear();
		skullImage = null;
		log.debug("Crime Scene stopped");
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		Actor actor = event.getActor();
		boolean isPlayer = actor instanceof Player;

		boolean mark;
		if (!isPlayer)
		{
			mark = config.markNpcs();
		}
		else if (actor == client.getLocalPlayer())
		{
			mark = config.markSelfDeaths();
		}
		else
		{
			mark = isTracked((Player) actor);
		}

		if (!mark)
		{
			return;
		}

		WorldPoint location;
		if (client.isInInstancedRegion())
		{
			// In instances, store the template chunk coordinate so the marker maps back on return
			LocalPoint local = actor.getLocalLocation();
			location = local != null ? WorldPoint.fromLocalInstance(client, local) : null;
		}
		else
		{
			location = actor.getWorldLocation();
		}

		if (location == null)
		{
			return;
		}

		CrimeSceneMarker marker = markers.computeIfAbsent(location,
			point -> new CrimeSceneMarker(point.getX(), point.getY(), point.getPlane()));
		marker.record(actor.getName(), !isPlayer);
		saveMarkers();
		refreshPanel();
	}

	private boolean isTracked(Player player)
	{
		String name = player.getName();
		if (name == null)
		{
			return false;
		}

		if (config.markFriends() && client.isFriended(name, false))
		{
			return true;
		}

		if (config.markFriendsChat() && player.isFriendsChatMember())
		{
			return true;
		}

		return config.markClanMembers() && player.isClanMember();
	}

	void clearMarkers()
	{
		markers.clear();
		configManager.unsetConfiguration(CONFIG_GROUP, MARKERS_KEY);
		refreshPanel();
	}

	/**
	 * Aggregates total deaths per name across every marked tile, ranked highest first.
	 *
	 * @param npcs true for the NPC leaderboard, false for the player leaderboard
	 */
	List<Map.Entry<String, Integer>> getLeaderboard(boolean npcs)
	{
		Map<String, Integer> totals = new HashMap<>();
		for (CrimeSceneMarker marker : markers.values())
		{
			Map<String, Integer> source = npcs ? marker.getNpcs() : marker.getPlayers();
			if (source != null)
			{
				source.forEach((name, count) -> totals.merge(name, count, Integer::sum));
			}
		}

		return totals.entrySet().stream()
			.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
			.collect(Collectors.toList());
	}

	private void refreshPanel()
	{
		panel.update(getLeaderboard(false), getLeaderboard(true));
	}

	private void loadMarkers()
	{
		markers.clear();

		String json = configManager.getConfiguration(CONFIG_GROUP, MARKERS_KEY);
		if (json != null && !json.isEmpty())
		{
			List<CrimeSceneMarker> stored = gson.fromJson(json, MARKER_LIST_TYPE);
			if (stored != null)
			{
				for (CrimeSceneMarker marker : stored)
				{
					markers.put(new WorldPoint(marker.getX(), marker.getY(), marker.getPlane()), marker);
				}
			}
		}
	}

	private void saveMarkers()
	{
		List<CrimeSceneMarker> stored = new ArrayList<>(markers.values());
		configManager.setConfiguration(CONFIG_GROUP, MARKERS_KEY, gson.toJson(stored, MARKER_LIST_TYPE));
	}

	@Provides
	CrimeSceneConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CrimeSceneConfig.class);
	}
}
