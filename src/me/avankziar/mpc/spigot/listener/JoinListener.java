package me.avankziar.mpc.spigot.listener;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

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
				"`mail_owner` = ? AND `mail_receiver` = ? AND `has_receiver_readed` = ?",
				player.getUniqueId().toString(), player.getUniqueId().toString(), false);
		if(emails > 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.PlayerJoin")
					.replace("%email%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL).strip())
					.replace("%emails%", String.valueOf(emails)));
		}
		boolean mailboxExist = plugin.getMailBoxHandler().getMailBox(player.getUniqueId()) != null;
		ArgumentConstructor pmail = plugin.getArgumentMap().get("pmail_deliverincomingmail") != null 
				? plugin.getArgumentMap().get("pmail_deliverincomingmail").argumentConstructor
				: null;
		if(pmail != null && !mailboxExist)
		{
			if(ModifierValueEntry.hasPermission(player, pmail))
			{
				int pmails = plugin.getMysqlHandler().getCount(MysqlType.PMAIL,
						"`id` ASC", "`mail_owner` = ? AND `mail_receiver` = ? AND `will_be_delivered` = ?",
						player.getUniqueId().toString(), player.getUniqueId().toString(), true);
				if(pmails > 0)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.PlayerJoin")
							.replace("%pmail%", CommandSuggest.getCmdString(CommandSuggest.Type.PMAIL_DELIVERINCOMINGMAIL).strip())
							.replace("%pmails%", String.valueOf(pmails)));
					return;
				}
			}
		}
		Optional<CommandConstructor> parcel = plugin.getCommandTree().stream().filter(x -> x.getPath().equals("parcel")).findFirst();
		if(parcel.isPresent() && !mailboxExist)
		{
			if(ModifierValueEntry.hasPermission(player, parcel.get()))
			{
				int parcels = plugin.getMysqlHandler().getCount(MysqlType.PARCEL,
						"`parcel_receiver` = ? AND `in_delivering` = ?", player.getUniqueId().toString(), true);
				if(parcels > 0)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.PlayerJoin")
							.replace("%parcel%", CommandSuggest.getCmdString(CommandSuggest.Type.PARCEL).strip())
							.replace("%parcels%", String.valueOf(parcels)));
					return;
				}
			}
		}
	}
}
