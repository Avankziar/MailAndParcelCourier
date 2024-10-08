package me.avankziar.mpc.spigot.cmd.mpc;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARG_Ignore extends ArgumentModule
{
	private MPC plugin;
	
	public ARG_Ignore(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String other = args[1];
		new BukkitRunnable() {
			
			@Override
			public void run() {
				doAsync(player, other);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, String other)
	{
		UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(other);
		if(uuid == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
					.replace("%player%", other));
			return;
		}
		if(plugin.getIgnoreHandler().isIgnored(player.getUniqueId(), uuid))
		{
			plugin.getIgnoreHandler().deleteIgnore(player.getUniqueId(), uuid);
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MPC.Ignore.DontIgnore")
					.replace("%player%", other));
		} else
		{
			plugin.getIgnoreHandler().setIgnore(player.getUniqueId(), uuid);
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MPC.Ignore.Ignore")
					.replace("%player%", other));
		}
	}
}