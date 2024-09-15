package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.assistance.BackgroundTask;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGP_Send extends ArgumentModule
{
	private MPC plugin;
	
	public ARGP_Send(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		if(BackgroundTask.isServerRestartImminent())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("ServerRestartIsImminent"));
			return;
		}
		final ItemStack is = player.getInventory().getItemInMainHand();
		plugin.getPMailHandler().doSendPMail(player, is);
	}
}