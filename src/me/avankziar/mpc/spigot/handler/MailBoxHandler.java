package me.avankziar.mpc.spigot.handler;

import java.util.UUID;

import org.bukkit.Location;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;

public class MailBoxHandler 
{
	private MPC plugin;
	private String server;
	
	public MailBoxHandler(MPC plugin, String server)
	{
		this.plugin = plugin;
		this.server = server;
	}
	
	public boolean existThereMailBox(Location loc)
	{
		return plugin.getMysqlHandler().exist(MysqlType.MAILBOX,
				"`box_server` = ? AND `box_world` = ? AND `box_x` = ? AND `box_y` = ? AND `box_z` = ?",
				server, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public MailBox getMailBox(Location loc)
	{
		return (MailBox) plugin.getMysqlHandler().getData(MysqlType.MAILBOX,
				"`box_server` = ? AND `box_world` = ? AND `box_x` = ? AND `box_y` = ? AND `box_z` = ?",
				server, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public MailBox getMailBox(UUID uuid)
	{
		return (MailBox) plugin.getMysqlHandler().getData(MysqlType.MAILBOX,
				"`box_server` = ? AND `box_world` = ? AND `box_x` = ? AND `box_y` = ? AND `box_z` = ?",
				server, loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
}
