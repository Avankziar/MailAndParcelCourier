package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGP_DeliverIncomingMail extends ArgumentModule
{
	private MPC plugin;
	
	public ARGP_DeliverIncomingMail(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		String other = args[1];
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(sender, other);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(CommandSender sender, String other)
	{
		UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(other);
		if(uuid == null)
		{
			ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
					.replace("%player%", other));
			return;
		}
		Player player = Bukkit.getPlayer(uuid);
		if(player == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerDontOnline")
					.replace("%player%", other));
			return;
		}
		ArrayList<PMail> list = PMail.convert(plugin.getMysqlHandler().getFullList(MysqlType.PMAIL,
				"`mail_sender` = ? AND `will_be_delivered` = ?", uuid.toString(), true));
		ArrayList<ItemStack> isl = new ArrayList<>();
		Material paper = plugin.getPMailHandler().getPaperType();
		list.stream().forEach(x -> isl.add(plugin.getPMailHandler().getPMailToDeposit(x, paper)));
		new BukkitRunnable() 
		{	
			@Override
			public void run() 
			{
				HashMap<Integer, ItemStack> map = player.getInventory().addItem(isl.toArray(new ItemStack[isl.size()]));
				if(!map.isEmpty())
				{
					map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
				}
			}
		}.runTask(plugin);
	}
}