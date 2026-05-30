package com.github.diogogdias.crimescene;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
class CrimeSceneMarker
{
	private int x;
	private int y;
	private int plane;
	private int number;
	private Map<String, Integer> players = new LinkedHashMap<>();
	private Map<String, Integer> npcs = new LinkedHashMap<>();

	CrimeSceneMarker()
	{
	}

	CrimeSceneMarker(int x, int y, int plane)
	{
		this.x = x;
		this.y = y;
		this.plane = plane;
	}

	/**
	 * Removes a tracked name from this tile, decrementing the death count by the removed total.
	 *
	 * @return the number of deaths removed (0 if the name was not present)
	 */
	int remove(String name, boolean npc)
	{
		Map<String, Integer> source = npc ? npcs : players;
		if (source == null)
		{
			return 0;
		}

		Integer removed = source.remove(name);
		if (removed == null)
		{
			return 0;
		}

		number -= removed;
		return removed;
	}

	boolean isEmpty()
	{
		return number <= 0;
	}

	void record(String name, boolean npc)
	{
		number++;
		if (name == null)
		{
			return;
		}

		if (npc)
		{
			if (npcs == null)
			{
				npcs = new LinkedHashMap<>();
			}
			npcs.merge(name, 1, Integer::sum);
		}
		else
		{
			if (players == null)
			{
				players = new LinkedHashMap<>();
			}
			players.merge(name, 1, Integer::sum);
		}
	}
}
