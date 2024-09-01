package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.IgnoreSender;
import me.avankziar.mpc.spigot.MPC;

public class IgnoreSenderHandler
{
	private MPC plugin;
	
	public IgnoreSenderHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean isIgnored(UUID receiver, UUID sender)
	{
		return plugin.getMysqlHandler().exist(MysqlType.IGNORE_SENDER,
				"`mail_receiver` = ? AND `mail_sender` = ?",
				receiver.toString(), sender.toString());
	}
	
	public void setIgnore(UUID receiver, UUID sender)
	{
		plugin.getMysqlHandler().create(MysqlType.IGNORE_SENDER, new IgnoreSender(0, receiver, sender));
	}
	
	public void deleteIgnore(UUID receiver, UUID sender)
	{
		plugin.getMysqlHandler().deleteData(MysqlType.IGNORE_SENDER,
				"`mail_receiver` = ? AND `mail_sender` = ?",
				receiver.toString(), sender.toString());
	}
	
	public ArrayList<IgnoreSender> getIgnored(UUID receiver, int start, int quantity)
	{
		return IgnoreSender.convert(plugin.getMysqlHandler().getList(MysqlType.IGNORE_SENDER,
				"`id` ASC", start, quantity, "`mail_receiver` = ?", receiver.toString()));
	}
}