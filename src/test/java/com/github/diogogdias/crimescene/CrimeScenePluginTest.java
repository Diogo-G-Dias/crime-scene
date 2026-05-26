package com.github.diogogdias.crimescene;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CrimeScenePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CrimeScenePlugin.class);
		RuneLite.main(args);
	}
}
