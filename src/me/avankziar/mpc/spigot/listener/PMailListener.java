package me.avankziar.mpc.spigot.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;

public class PMailListener implements Listener
{
	public MPC plugin;
	
	public PMailListener(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onAirClick(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_AIR)
		{
			return;
		}
		if(event.getMaterial() != plugin.getPMailHandler().getPaperType())
		{
			return;
		}
		ItemStack is = event.getItem();
		PMail pmail = plugin.getPMailHandler().readPMail(is);
		if(pmail == null)
		{
			return;
		}
		event.setCancelled(true);
		event.setUseInteractedBlock(Result.DENY);
		event.setUseItemInHand(Result.DENY);
		if(!pmail.getOwner().equals(event.getPlayer().getUniqueId()))
		{
			return;
		}
		plugin.getPMailHandler().openPMail(is, pmail);
		String other = plugin.getPlayerDataHandler().getPlayerName(pmail.getSender());
		ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("PMail.Open.Opened")
				.replace("%players%", other)
				.replace("%subject%", pmail.getSubjectMatter()));
		plugin.getPMailHandler().getPMailToRead(pmail).stream().forEach(x -> ChatApi.sendMessage(event.getPlayer(), x));
	}
	
	@EventHandler
	public void onMailBoxClick(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
		if(event.getClickedBlock() == null)
		{
			return;
		}
		if(event.getClickedBlock().getType() != Material.CHEST && event.getClickedBlock().getType() != Material.TRAPPED_CHEST)
		{
			return;
		}
		if(!plugin.getMailBoxHandler().existThereMailBox(event.getClickedBlock().getLocation()))
		{
			return;
		}
		MailBox mb = plugin.getMailBoxHandler().getMailBox(event.getClickedBlock().getLocation());
		if(mb.canBeUsedForSending()
				&& event.getItem() != null 
				&& event.getItem().getType() == plugin.getPMailHandler().getPaperType())
		{
			final ItemStack is = event.getItem();
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			event.setUseItemInHand(Result.DENY);
			final Player player = event.getPlayer();
			plugin.getPMailHandler().doSendPMail(player, is);
			return;
		}
	}
	
	@EventHandler
	public void onPostManClick(PlayerInteractEntityEvent event)
	{
		if(!(event.getRightClicked() instanceof Player))
		{
			return;
		}
		if(!event.getRightClicked().hasMetadata("NPC"))
		{
			return;
		}
		if(!plugin.getParcelHandler().hasInputReceiverForGui(event.getPlayer().getUniqueId()))
		{
			return;
		}
		final Player player = event.getPlayer();
		if(player.getInventory().getItemInMainHand() == null 
				|| player.getInventory().getItemInMainHand().getType() != plugin.getPMailHandler().getPaperType())
		{
			return;
		}
	    String npcname = event.getRightClicked().getName();
	    List<String> npc = this.plugin.getYamlHandler().getConfig().getStringList("PostmanNPC")
	    		.stream().map(x -> x.replace(" ", "_")).collect(Collectors.toList());
	    if(!npc.contains(npcname))
	    {
	    	return;
	    }
	    final ItemStack is = player.getInventory().getItemInMainHand();
		event.setCancelled(true);
		plugin.getPMailHandler().doSendPMail(player, is);
		return;
	}
}