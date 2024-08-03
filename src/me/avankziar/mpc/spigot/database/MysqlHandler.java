package me.avankziar.mpc.spigot.database;

import me.avankziar.mpc.general.database.MysqlBaseHandler;
import me.avankziar.mpc.spigot.MPC;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(MPC plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}
