package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.ifh.general.economy.action.OrdererType;
import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.handler.PMailHandler;

public class ARGP_Send  extends ArgumentModule
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
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player)
	{
		ItemStack is = player.getInventory().getItemInMainHand();
		if(is == null || is.getType() != plugin.getPMailHandler().getPaperType())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString(""));
			return;
		}
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		NamespacedKey nsu = new NamespacedKey(plugin, PMailHandler.SUBJECT);
		NamespacedKey nre = new NamespacedKey(plugin, PMailHandler.RECEIVER);
		if(!pdc.has(nsu) || !pdc.has(nre))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString(""));
			return;
		}
		String subject = pdc.get(nsu, PersistentDataType.STRING);
		String other = plugin.getPlayerDataHandler().getPlayerName(pdc.get(nre, PersistentDataType.STRING));
		plugin.getPMailHandler().sendPMail(player, is);
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.Sended")
				.replace("%players%", other)
				.replace("%subject%", subject));
	}
}