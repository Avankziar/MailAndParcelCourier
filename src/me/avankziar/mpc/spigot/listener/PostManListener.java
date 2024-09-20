package me.avankziar.mpc.spigot.listener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.assistance.BackgroundTask;

public class PostManListener implements Listener
{
	public MPC plugin;
	
	public PostManListener(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	private LinkedHashMap<UUID, Long> cooldown = new LinkedHashMap<>();
	
	public boolean isOnCooldown(UUID uuid)
	{
		Long c = cooldown.get(uuid);
		return c == null ? false : c.longValue() > System.currentTimeMillis();
	}
	
	public void setCooldown(UUID uuid, long duration, TimeUnit timeUnit)
	{
		cooldown.put(uuid, timeUnit.convert(duration, TimeUnit.MILLISECONDS)+System.currentTimeMillis());
	}
	
	public void removeCooldown(UUID uuid)
	{
		cooldown.remove(uuid);
	}
	
	@EventHandler(ignoreCancelled = true)
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
		if(event.getHand() != EquipmentSlot.HAND)
		{
			return;
		}
		if(isOnCooldown(event.getPlayer().getUniqueId()))
		{
			return;
		}
		if(BackgroundTask.isServerRestartImminent())
		{
			ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("ServerRestartIsImminent"));
			return;
		}
		setCooldown(event.getPlayer().getUniqueId(), 2, TimeUnit.SECONDS);
		if(plugin.getParcelHandler().hasInputReceiverForGui(event.getPlayer().getUniqueId()))
		{
			String npcname = event.getRightClicked().getName();
		    List<String> npc = this.plugin.getYamlHandler().getConfig().getStringList("PostmanNPC")
		    		.stream().map(x -> x.replace(" ", "_")).collect(Collectors.toList());
		    if(!npc.contains(npcname))
		    {
		    	return;
		    }
			final Player player = event.getPlayer();
			plugin.getParcelHandler().openGuiToDepositParcelContent(player);
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