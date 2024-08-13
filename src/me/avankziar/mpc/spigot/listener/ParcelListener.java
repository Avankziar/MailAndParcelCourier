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
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.ifh.general.economy.action.OrdererType;
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
		if(event.getMaterial() != plugin.getParcelHandler().getPackageType())
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
				&& plugin.getParcelHandler().hasInputReceiverForGui(event.getPlayer().getUniqueId()))
		{
			event.setUseInteractedBlock(Result.DENY);
			final Player player = event.getPlayer();
			new BukkitRunnable() 
			{
				@Override
				public void run() 
				{
					plugin.getParcelHandler().openGuiToDepositParcelContent(player);
				}
			}.runTaskAsynchronously(plugin);
		}
	}
	
	@EventHandler
	public void onGuiClose(InventoryCloseEvent event)
	{
		final ItemStack[] isa = event.getView().getTopInventory().getStorageContents();
		double cost = plugin.getParcelHandler().getSendingCost(isa);
		if(cost > 0.0 && (plugin.getIFHEco() != null || plugin.getVaultEco() != null))
		{
			Player player = (Player) event.getPlayer();
			if(plugin.getIFHEco() != null)
			{
				me.avankziar.ifh.spigot.economy.account.Account acc = plugin.getIFHEco().getDefaultAccount(player.getUniqueId());
				if(acc.getBalance() < cost)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.NotEnoughMoney")
							.replace("%money%", plugin.getIFHEco().format(cost, acc.getCurrency())));
					return;
				}
				me.avankziar.ifh.general.economy.action.EconomyAction er = 
						plugin.getIFHEco().withdraw(acc, cost, OrdererType.PLAYER, player.getUniqueId().toString(),
						plugin.getYamlHandler().getLang().getString("EMail.Send.MoneyCategory"),
						plugin.getYamlHandler().getLang().getString("EMail.Send.MoneyComment"));
				if(!er.isSuccess())
				{
					ChatApi.sendMessage(player, er.getDefaultErrorMessage());
					return;
				}
			} else
			{
				if(!plugin.getVaultEco().has(player, cost))
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.NotEnoughMoney")
							.replace("%money%", String.valueOf(cost)+plugin.getVaultEco().currencyNamePlural()));
					return;
				}
				net.milkbowl.vault.economy.EconomyResponse er = plugin.getVaultEco().withdrawPlayer(player, cost);
				if(er != null && !er.transactionSuccess())
				{
					if(er.errorMessage != null)
					{
						ChatApi.sendMessage(player, er.errorMessage);
					}
					return;
				}
			}
		}
		plugin.getParcelHandler().closeGuiToDepositParcelContent((Player) event.getPlayer(), isa);
	}
}