package me.avankziar.mpc.spigot.listener;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;

public class ParcelListener implements Listener
{
	public MPC plugin;
	
	public ParcelListener(MPC plugin)
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
		ItemStack[] isa = plugin.getParcelHandler().openParcel(is);
		if(isa == null)
		{
			return;
		}
		HashMap<Integer, ItemStack> map = event.getPlayer().getInventory().addItem(isa);
		if(!map.isEmpty())
		{
			map.values().stream().forEach(x -> event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), x));
		}
		ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("Parcel.Open.Opened"));
	}
	
	@EventHandler
	public void onMailBoxClick(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
		if(event.getMaterial() != Material.CHEST && event.getMaterial() != Material.TRAPPED_CHEST)
		{
			return;
		}
		if(event.getClickedBlock() == null)
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
			event.setUseInteractedBlock(Result.DENY);
			final Player player = event.getPlayer();
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
	
	@EventHandler
	public void onGuiClose(InventoryCloseEvent event)
	{
		final ItemStack[] isa = event.getView().getTopInventory().getStorageContents();
		plugin.getParcelHandler().closeGuiToDepositParcelContent((Player) event.getPlayer(), isa);
	}
}