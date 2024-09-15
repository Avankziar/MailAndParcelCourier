package me.avankziar.mpc.spigot.ifh;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;

public class EMailProvider implements me.avankziar.ifh.general.interfaces.EMail
{
	private MPC plugin;
	
	public EMailProvider(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public void sendEMail(UUID receiver, String sender, String subject, String message)
	{
		final long senddate = System.currentTimeMillis();
		EMail receivers = new EMail(0, subject, message, receiver,
				sender, receiver, false, senddate, 0L);
		plugin.getMysqlHandler().create(MysqlType.EMAIL, receivers);
		ArrayList<EMail> emails = plugin.getEMailHandler().getReceivedEmails(receiver, 0, 1);
		if(emails == null || emails.isEmpty())
		{
			return;
		}
		EMail email = emails.getFirst();
		plugin.getPlayerDataHandler().sendMessageToOtherPlayer(receiver, 
				plugin.getYamlHandler().getLang().getString("EMail.Send.HasEMail")
				.replace("%emailread%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_READ))
				.replace("%mailid%", String.valueOf(email.getId())));
	}
	
	public void sendAllEMail(String sender, String subject, String message)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				ArrayList<PlayerData> list = PlayerData.convert(plugin.getMysqlHandler().getFullList(MysqlType.PLAYERDATA, "`id` ASC", "`id` > ?", 0));
				list.stream().forEach(x -> sendEMail(x.getPlayerUUID(), sender, subject, message));
			}
		}.runTaskAsynchronously(plugin);
	}
}