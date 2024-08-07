package me.avankziar.mpc.spigot.assistance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;

public class BackgroundTask
{
	private static MPC plugin;
	
	public BackgroundTask(MPC plugin)
	{
		BackgroundTask.plugin = plugin;
		initBackgroundTask();
	}
	
	public void initBackgroundTask()
	{
		int depositPMail = plugin.getYamlHandler().getConfig().getInt("", 1*60);
		long depositAfter = plugin.getYamlHandler().getConfig().getLong("", 1000 * 60 * 60L);
		new BukkitRunnable() 
		{	
			@Override
			public void run()
			{
				long senddateAfter = System.currentTimeMillis() - depositAfter;
				ArrayList<PMail> list = PMail.convert(plugin.getMysqlHandler().getFullList(MysqlType.PMAIL,
						"`id` ASC",
						"`sending_date` < ? AND `will_be_delivered` = ?", senddateAfter, true));
				LinkedHashMap<UUID, ArrayList<PMail>> map = new LinkedHashMap<>();
				list.stream().forEach(x -> sortingAfterUUID(map, x));
				for(Entry<UUID, ArrayList<PMail>> e : map.entrySet())
				{
					UUID receiver = e.getKey();
					ArrayList<PMail> value = e.getValue();
					MailBox mailbox = plugin.getMailBoxHandler().getMailBox(receiver);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, 20L * depositPMail);
	}
	
	private void sortingAfterUUID(LinkedHashMap<UUID, ArrayList<PMail>> map, PMail pmail)
	{
		ArrayList<PMail> list = new ArrayList<>();
		if(map.containsKey(pmail.getReceiver()))
		{
			list = map.get(pmail.getReceiver());
		}
		list.add(pmail);
		map.put(pmail.getReceiver(), list);
	}
}
