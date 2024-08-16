package me.avankziar.mpc.spigot.ifh;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;

public class PMailProvider
{
	private MPC plugin;
	
	public PMailProvider(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public void sendPMail(UUID receiver, String sender, String subject, String message)
	{
		final long senddate = System.currentTimeMillis();
		PMail pmail = new PMail(0,
				subject,
				message,
				receiver,
				sender,
				receiver,
				false, senddate, true);
		plugin.getMysqlHandler().create(MysqlType.PMAIL, pmail);
	}
	
	public void sendAllPMail(String sender, String subject, String message)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				ArrayList<PlayerData> list = PlayerData.convert(plugin.getMysqlHandler().getFullList(MysqlType.PLAYERDATA, "`id` ASC", "`id` > ?", 0));
				list.stream().forEach(x -> sendPMail(x.getPlayerUUID(), sender, subject, message));
			}
		}.runTaskAsynchronously(plugin);
	}
}