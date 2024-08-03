package me.avankziar.mpc.spigot.handler;

import me.avankziar.mpc.spigot.MPC;

public class ReplacerHandler 
{
	private MPC plugin;
	
	public ReplacerHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public String getBoolean(boolean boo)
	{
		return boo 
				? plugin.getYamlHandler().getLang().getString("IsTrue") 
				: plugin.getYamlHandler().getLang().getString("IsFalse");
	}
}
