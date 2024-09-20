package me.avankziar.mpc.spigot.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.avankziar.ifh.general.economy.action.OrdererType;
import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.assistance.BackgroundTask;

public class ParcelListener implements Listener
{
	public MPC plugin;
	public static ArrayList<Material> forbiddenItems = new ArrayList<>();
	
	public ParcelListener(MPC plugin)
	{
		this.plugin = plugin;
		for(String is : plugin.getYamlHandler().getConfig().getStringList("Parcel.ForbiddenItemToSend"))
		{
			try
			{
				forbiddenItems.add(Material.valueOf(is));
			} catch(Exception e)
			{
				continue;
			}
		}
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
		ItemStack[] isa = plugin.getParcelHandler().openParcel(event.getPlayer(), is);
		if(isa == null)
		{
			return;
		}
		List<ItemStack> list = Arrays.asList(isa).stream().filter(x -> x != null).filter(x -> x.getType() != Material.AIR).collect(Collectors.toList());
		HashMap<Integer, ItemStack> map = event.getPlayer().getInventory().addItem(list.toArray(new ItemStack[list.size()]));
		if(!map.isEmpty())
		{
			map.values().stream().filter(x -> x != null).forEach(x -> event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), x));
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
		if(BackgroundTask.isServerRestartImminent())
		{
			ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("ServerRestartIsImminent"));
			return;
		}
		MailBox mb = plugin.getMailBoxHandler().getMailBox(event.getClickedBlock().getLocation());
		if(mb.canBeUsedForSending()
				&& plugin.getParcelHandler().hasInputReceiverForGui(event.getPlayer().getUniqueId()))
		{
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			event.setUseItemInHand(Result.DENY);
			final Player player = event.getPlayer();
			plugin.getParcelHandler().openGuiToDepositParcelContent(player);
		}
	}
	
	@EventHandler
	public void onGuiClose(InventoryCloseEvent event)
	{
		if(!plugin.getParcelHandler().inGui(event.getPlayer().getUniqueId()))
		{
			return;
		}
		final ItemStack[] isa = event.getView().getTopInventory().getStorageContents();
		double cost = plugin.getParcelHandler().getSendingCost(isa);
		Player player = (Player) event.getPlayer();
		if(BackgroundTask.isServerRestartImminent())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("ServerRestartIsImminent"));
			List<ItemStack> list = Arrays.asList(isa).stream()
					.filter(x -> x != null)
					.filter(x -> x.getType() != Material.AIR)
					.collect(Collectors.toList());
			HashMap<Integer, ItemStack> map = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
			if(!map.isEmpty())
			{
				map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
			}
			plugin.getParcelHandler().removeInGui(player.getUniqueId());
			return;
		}
		if(cost > 0.0 && (plugin.getIFHEco() != null || plugin.getVaultEco() != null))
		{			
			if(plugin.getIFHEco() != null)
			{
				me.avankziar.ifh.spigot.economy.account.Account acc = plugin.getIFHEco().getDefaultAccount(player.getUniqueId());
				if(acc.getBalance() < cost)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.NotEnoughMoney")
							.replace("%money%", plugin.getIFHEco().format(cost, acc.getCurrency())));
					plugin.getParcelHandler().removeInGui(player.getUniqueId());
					List<ItemStack> list = Arrays.asList(isa).stream()
							.filter(x -> x != null)
							.filter(x -> x.getType() != Material.AIR)
							.collect(Collectors.toList());
					HashMap<Integer, ItemStack> map = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
					if(!map.isEmpty())
					{
						map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
					}
					return;
				}
				me.avankziar.ifh.general.economy.action.EconomyAction er = 
						plugin.getIFHEco().withdraw(acc, cost, OrdererType.PLAYER, player.getUniqueId().toString(),
						plugin.getYamlHandler().getLang().getString("EMail.Send.MoneyCategory"),
						plugin.getYamlHandler().getLang().getString("EMail.Send.MoneyComment"));
				if(!er.isSuccess())
				{
					ChatApi.sendMessage(player, er.getDefaultErrorMessage());
					plugin.getParcelHandler().removeInGui(player.getUniqueId());
					List<ItemStack> list = Arrays.asList(isa).stream()
							.filter(x -> x != null)
							.filter(x -> x.getType() != Material.AIR)
							.collect(Collectors.toList());
					if(!list.isEmpty())
					{
						HashMap<Integer, ItemStack> map = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
						if(!map.isEmpty())
						{
							map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
						}
					}
					return;
				}
			} else
			{
				if(!plugin.getVaultEco().has(player, cost))
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.NotEnoughMoney")
							.replace("%money%", String.valueOf(cost)+plugin.getVaultEco().currencyNamePlural()));
					plugin.getParcelHandler().removeInGui(player.getUniqueId());
					List<ItemStack> list = Arrays.asList(isa).stream()
							.filter(x -> x != null)
							.filter(x -> x.getType() != Material.AIR)
							.collect(Collectors.toList());
					HashMap<Integer, ItemStack> map = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
					if(!map.isEmpty())
					{
						map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
					}
					return;
				}
				net.milkbowl.vault.economy.EconomyResponse er = plugin.getVaultEco().withdrawPlayer(player, cost);
				if(er != null && !er.transactionSuccess())
				{
					if(er.errorMessage != null)
					{
						ChatApi.sendMessage(player, er.errorMessage);
					}
					plugin.getParcelHandler().removeInGui(player.getUniqueId());
					List<ItemStack> list = Arrays.asList(isa).stream()
							.filter(x -> x != null)
							.filter(x -> x.getType() != Material.AIR)
							.collect(Collectors.toList());
					HashMap<Integer, ItemStack> map = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
					if(!map.isEmpty())
					{
						map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
					}
					return;
				}
			}
		} else
		{
			cost = 0.0;
		}
		List<ItemStack> list = Arrays.asList(isa).stream()
				.filter(x -> x != null)
				.filter(x -> x.getType() != Material.AIR)
				.filter(x -> !forbiddenItems.contains(x.getType()))
				.collect(Collectors.toList());
		List<ItemStack> forbiddenlist = Arrays.asList(isa).stream()
				.filter(x -> x != null)
				.filter(x -> x.getType() != Material.AIR)
				.filter(x -> forbiddenItems.contains(x.getType()))
				.collect(Collectors.toList());
		if(list.isEmpty())
		{
			if(!forbiddenlist.isEmpty())
			{
				HashMap<Integer, ItemStack> forbidden = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
				if(!forbidden.isEmpty())
				{
					forbidden.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
				}
			}
			plugin.getParcelHandler().removeInGui(player.getUniqueId());
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.HasNotPutSomethingIn"));
			return;
		}
		if(!forbiddenlist.isEmpty())
		{
			HashMap<Integer, ItemStack> forbidden = player.getInventory().addItem(list.toArray(new ItemStack[list.size()]));
			if(!forbidden.isEmpty())
			{
				forbidden.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
			}
		}
		plugin.getParcelHandler().closeGuiToDepositParcelContent((Player) event.getPlayer(), list.toArray(new ItemStack[list.size()]), cost);
	}
}