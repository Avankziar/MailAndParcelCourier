package me.avankziar.mpc.spigot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PlayerData;
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
		PlayerData pd = plugin.getPlayerDataHandler().getPlayer(player.getUniqueId());
		if(pd == null)
		{
			pd = new PlayerData(player.getUniqueId(), player.getName());
			plugin.getMysqlHandler().create(MysqlType.PLAYERDATA, pd);
		} else
		{
			plugin.getPlayerDataHandler().updatePlayerName(player);
		}
		int emails = plugin.getMysqlHandler().getCount(MysqlType.EMAIL,
				"`mail_receiver` = ? AND `has_receiver_readed` = ?", player.getUniqueId().toString(), false);
		if(emails > 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.PlayerJoin")
					.replace("%email%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL).strip())
					.replace("%emails%", String.valueOf(emails)));
		}
	}
}
