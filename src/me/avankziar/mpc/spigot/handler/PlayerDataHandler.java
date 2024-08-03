package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;

public class PlayerDataHandler 
{
	private MPC plugin;
	
	public PlayerDataHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public UUID getPlayerUUID(String playername)
	{
		PlayerData pd = (PlayerData) plugin.getMysqlHandler().getData(MysqlType.PLAYERDATA, 
				"`player_name` = ?", playername);
		return pd != null ? pd.getPlayerUUID() : null;
	}
	
	public String getPlayerName(String playeruuid)
	{
		PlayerData pd = (PlayerData) plugin.getMysqlHandler().getData(MysqlType.PLAYERDATA, 
				"`player_uuid` = ?", playeruuid);
		return pd != null ? pd.getPlayerName() : playeruuid;
	}
	
	public PlayerData getPlayer(UUID playeruuid)
	{
		PlayerData pd = (PlayerData) plugin.getMysqlHandler().getData(MysqlType.PLAYERDATA, 
				"`player_uuid` = ?", playeruuid.toString());
		return pd;
	}
	
	public void updatePlayerData(PlayerData pd)
	{
		plugin.getMysqlHandler().updateData(MysqlType.PLAYERDATA, pd, "`player_uuid` = ?", pd.getPlayerUUID().toString());
	}
	
	public void updatePlayerName(Player player)
	{
		PlayerData pd = getPlayer(player.getUniqueId());
		if(pd == null)
		{
			return;
		}
		if(pd.getPlayerName().equals(player.getName()))
		{
			return;
		}
		pd.setPlayerName(player.getName());
		updatePlayerData(pd);
	}
	
	public void sendMessageToOtherPlayer(UUID uuid, String text)
	{
		Player player = Bukkit.getPlayer(uuid);
		if(player != null)
		{
			ChatApi.sendMessage(player, text);
		} else if(plugin.getMtV() != null)
		{
			plugin.getMtV().sendMessage(uuid, text);
		}
	}
	
	public void sendMessageToOtherPlayer(UUID uuid, ArrayList<String> list)
	{
		Player player = Bukkit.getPlayer(uuid);
		if(player != null)
		{
			list.stream().forEach(x -> ChatApi.sendMessage(player, x));
		} else if(plugin.getMtV() != null)
		{
			plugin.getMtV().sendMessage(uuid, list.toArray(new String[list.size()]));
		}
	}
}
