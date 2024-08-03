package me.avankziar.mpc.spigot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.spigot.MPC;

public class JoinListener implements Listener
{
	private MPC plugin;
	
	public JoinListener(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		int emails = plugin.getMysqlHandler().getCount(MysqlType.EMAIL,
				"`mail_receiver` = ? AND `has_receiver_readed` = ?", player.getUniqueId().toString(), false);
		if(emails > 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.PlayerJoin"));
		}
	}
}
