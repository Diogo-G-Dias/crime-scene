package com.github.diogogdias.crimescene;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup(CrimeScenePlugin.CONFIG_GROUP)
public interface CrimeSceneConfig extends Config
{
	@ConfigItem(
		keyName = "markFriends",
		name = "Mark friends",
		description = "Mark the tile when a player on your friends list dies",
		position = 0
	)
	default boolean markFriends()
	{
		return true;
	}

	@ConfigItem(
		keyName = "markFriendsChat",
		name = "Mark friends chat",
		description = "Also mark friends chat members",
		position = 1
	)
	default boolean markFriendsChat()
	{
		return false;
	}

	@ConfigItem(
		keyName = "markClanMembers",
		name = "Mark clan members",
		description = "Also mark clan members",
		position = 2
	)
	default boolean markClanMembers()
	{
		return false;
	}

	@ConfigItem(
		keyName = "markSelfDeaths",
		name = "Mark my own deaths",
		description = "Also mark the tile where you die (works anywhere, no one else needs to witness it)",
		position = 3
	)
	default boolean markSelfDeaths()
	{
		return true;
	}

	@ConfigItem(
		keyName = "markNpcs",
		name = "Mark NPCs",
		description = "Also mark the tile where any NPC dies",
		position = 4
	)
	default boolean markNpcs()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showSkull",
		name = "Show skull",
		description = "Draw the in-game skull sprite on the death tile",
		position = 5
	)
	default boolean showSkull()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "borderColor",
		name = "Border colour",
		description = "Tile border colour",
		position = 6
	)
	default Color borderColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
		keyName = "fillColor",
		name = "Fill colour",
		description = "Tile fill colour",
		position = 7
	)
	default Color fillColor()
	{
		return new Color(255, 0, 0, 50);
	}

	@Alpha
	@ConfigItem(
		keyName = "fontColor",
		name = "Font colour",
		description = "Colour of the death number drawn on the tile",
		position = 8
	)
	default Color fontColor()
	{
		return Color.WHITE;
	}

	@Range(min = 8, max = 40)
	@ConfigItem(
		keyName = "fontSize",
		name = "Number size",
		description = "Font size of the death number",
		position = 9
	)
	default int fontSize()
	{
		return 14;
	}

	@ConfigItem(
		keyName = "clearHotkey",
		name = "Clear-all hotkey",
		description = "Hotkey to remove every marker (assign a key, and make sure the game has focus)",
		position = 10
	)
	default Keybind clearHotkey()
	{
		return Keybind.NOT_SET;
	}
}
