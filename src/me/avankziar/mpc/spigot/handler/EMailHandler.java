package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;

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
	
	public EMail getEMail(int id)
	{
		return (EMail) plugin.getMysqlHandler().getData(MysqlType.EMAIL, "`id` = ?", id);
	}
	
	public ArrayList<EMail> getReceivedEmails(UUID uuid, int start, int quantity)
	{
		return EMail.convert(plugin.getMysqlHandler().getList(MysqlType.EMAIL, "`id` DESC", start, quantity,
				"`mail_receiver` = ?", uuid.toString()));
	}
	
	public ArrayList<EMail> getSendedEmails(UUID uuid, int start, int quantity)
	{
		return EMail.convert(plugin.getMysqlHandler().getList(MysqlType.EMAIL, "`id` DESC", start, quantity,
				"`mail_sender` = ?", uuid.toString()));
	}
	
	public EMail getCorrespondingEmail(long sendDate, int id)
	{
		if(!plugin.getMysqlHandler().exist(MysqlType.EMAIL, "`id` != ? AND `sending_date` = ?", id, sendDate))
		{
			return null;
		}
		return (EMail) plugin.getMysqlHandler().getData(MysqlType.EMAIL,"`id` != ? AND `sending_date` = ?", id, sendDate);	
	}
	
	public double getSendingCost(String subject, String message)
	{
		String type = plugin.getYamlHandler().getConfig().getString("EMail.Cost.SendingCosts", "NONE");
		switch(type)
		{
		default:
		case "NONE": return 0.0;
		case "LUMP_SUM": return plugin.getYamlHandler().getConfig().getDouble("EMail.Cost.Costs", 1.0);
		case "PER_WORD":
			return (double) (subject.split(" ").length + message.split(" ").length) 
					* plugin.getYamlHandler().getConfig().getDouble("EMail.Cost.Costs", 1.0);
		case "PER_LETTER":
			return (double) (subject.length() + message.length()) 
					* plugin.getYamlHandler().getConfig().getDouble("EMail.Cost.Costs", 1.0);
		}
	}
}