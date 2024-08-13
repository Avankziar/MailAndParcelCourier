package me.avankziar.mpc.spigot.cmd.parcel;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGPa_Send extends ArgumentModule
{
	private MPC plugin;
	
	public ARGPa_Send(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String players = args[1];
		String subject = args[2];
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player, players, subject);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, String other, String subject)
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
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.PlayerIgnoresYou")
					.replace("%player%", other));
		}
		plugin.getParcelHandler().addReceiverAndSubject(player.getUniqueId(), uuid, subject);
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				plugin.getParcelHandler().openGuiToDepositParcelContent(player);
			}
		}.runTask(plugin);
	}
}