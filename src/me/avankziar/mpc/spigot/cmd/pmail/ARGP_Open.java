package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGP_Open extends ArgumentModule
{
	private MPC plugin;
	
	public ARGP_Open(MPC plugin, ArgumentConstructor argumentConstructor)
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
				doAsync(player, is);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, ItemStack is)
	{
		PMail pmail = plugin.getPMailHandler().readPMail(is);
		if(pmail == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Open.NotPMailOnItem"));
			return;
		}
		plugin.getPMailHandler().openPMail(is, pmail);
		plugin.getPMailHandler().getPMailToRead(pmail).stream().forEach(x -> ChatApi.sendMessage(player, x));
	}
}