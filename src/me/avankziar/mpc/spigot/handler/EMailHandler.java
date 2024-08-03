package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.spigot.MPC;

public class EMailHandler
{
	private MPC plugin;
	public EMailHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public ArrayList<EMail> getReceivedEmails(Player player, int start, int quantity)
	{
		return EMail.convert(plugin.getMysqlHandler().getList(MysqlType.EMAIL, "`id` DESC", start, quantity,
				"`mail_receiver` = ?", player.getUniqueId().toString()));
	}
	
	public EMail getCorrespondingEmail(int id, long sendDate)
	{
		if(plugin.getMysqlHandler().exist(MysqlType.EMAIL, "`id` != ? AND `sending_date` = ?", id, sendDate))
		{
			return null;
		}
		return (EMail) plugin.getMysqlHandler().getData(MysqlType.EMAIL, "`id` != ? AND `sending_date` = ?", id, sendDate);
		
	}
}