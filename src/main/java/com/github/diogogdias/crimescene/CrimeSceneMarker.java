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
