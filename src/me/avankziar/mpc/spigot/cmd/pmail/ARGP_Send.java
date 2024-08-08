package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.spigot.MPC;
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
		final ItemStack is = player.getInventory().getItemInMainHand();
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				plugin.getPMailHandler().doSendPMail(player, is);
			}
		}.runTaskAsynchronously(plugin);
	}
}