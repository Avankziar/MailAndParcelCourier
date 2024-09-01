package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGP_Write extends ArgumentModule
{
	private MPC plugin;
	
	public ARGP_Write(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String other = args[1];
		String subject = args[2];
		ArrayList<String> msgs = new ArrayList<>();
		for(int i = 3; i < args.length; i++)
		{
			msgs.add(args[i]);
		}
		String[] msgarr = msgs.toArray(new String[msgs.size()]);
		String msg = String.join(" ", msgarr);
		Material paper = plugin.getPMailHandler().getPaperType();
		UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(other);
		if(uuid == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
					.replace("%player%", other));
			return;
		}
		/* ADDME back
		if(uuid.equals(player.getUniqueId()))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.SendToYourself"));
			return;
		}*/
		if(plugin.getIgnoreHandler().isIgnored(player.getUniqueId(), uuid))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.PlayerIgnoresYou")
					.replace("%player%", other));
			return;
		}
		int papercost = plugin.getPMailHandler().getPaperCost();		
		if(!plugin.getPMailHandler().hasEnoughPaperInInventoryAsCost(player, paper, papercost))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Write.NotEnoughMaterial"));
			return;
		}
		if(!plugin.getPMailHandler().hasSlotFree(player))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Write.NotFreeSlot"));
			return;
		}
		plugin.getPMailHandler().withdrawPaperFromInventoryAsCost(player, paper, papercost);
		ItemStack is = plugin.getPMailHandler().writePMail(player, paper, subject, msg, uuid, other);
		player.getInventory().addItem(is);
	}
}