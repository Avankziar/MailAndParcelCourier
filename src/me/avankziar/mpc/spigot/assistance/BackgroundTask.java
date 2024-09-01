package me.avankziar.mpc.spigot.assistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.general.objects.Parcel;
import me.avankziar.mpc.spigot.MPC;

public class BackgroundTask
{
	private static MPC plugin;
	
	public BackgroundTask(MPC plugin)
	{
		BackgroundTask.plugin = plugin;
		initPMailDeposit();
		initParcelDeposit();
	}
	
	public void initPMailDeposit()
	{
		int depositPMail = plugin.getYamlHandler().getConfig().getInt("PMail.Task.RunInLoop", 60);
		long depositAfter = plugin.getYamlHandler().getConfig().getLong("PMail.Task.DepositPMailWhichAreOlderThan", 60 * 60L);
		final Material paper = plugin.getPMailHandler().getPaperType();
		new BukkitRunnable() 
		{	
			@Override
			public void run()
			{
				long senddateAfter = System.currentTimeMillis() - (depositAfter * 1000 * 60);
				ArrayList<PMail> list = PMail.convert(plugin.getMysqlHandler().getFullList(MysqlType.PMAIL,
						"`id` ASC",
						"`sending_date` < ? AND `will_be_delivered` = ?", senddateAfter, true));
				LinkedHashMap<UUID, ArrayList<PMail>> map = new LinkedHashMap<>();
				list.stream().forEach(x -> sortingAfterUUID(map, x));
				for(Entry<UUID, ArrayList<PMail>> e : map.entrySet())
				{
					final UUID receiver = e.getKey();
					final ArrayList<PMail> value = e.getValue();
					MailBox mailbox = plugin.getMailBoxHandler().getMailBox(receiver);
					if(mailbox == null || !plugin.getServername().equals(mailbox.getServer())
							|| !Bukkit.getWorlds().stream()
							.filter(x -> x.getName().equals(mailbox.getWorld()))
							.map(x -> x.getName())
							.collect(Collectors.toList()).contains(mailbox.getWorld()))
					{
						continue;
					}
					new BukkitRunnable() 
					{
						
						@Override
						public void run() 
						{
							depositMail(mailbox, value, paper);
						}
					}.runTask(plugin);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, 20L * 60 * depositPMail);
	}
	
	private void sortingAfterUUID(LinkedHashMap<UUID, ArrayList<PMail>> map, PMail pmail)
	{
		ArrayList<PMail> list = new ArrayList<>();
		if(map.containsKey(pmail.getReceiver()))
		{
			list = map.get(pmail.getReceiver());
		}
		list.add(pmail);
		map.put(pmail.getReceiver(), list);
	}
	
	private void depositMail(MailBox mailbox, ArrayList<PMail> pmails, Material paper)
	{
		Block block = mailbox.getLocation().getBlock();
		if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
		{
			return;
		}
		if(block.getState() instanceof Chest)
		{
			Chest chest = (Chest) block.getState();
			Inventory inv = chest.getInventory();
			int i = 0;
			
			for(PMail pmail : pmails)
			{
				ItemStack is = plugin.getPMailHandler().getPMailToDeposit(pmail, paper);
				HashMap<Integer, ItemStack> map = inv.addItem(is);
				if(map.isEmpty())
				{
					PMail up = pmail;
					up.setWillBeDelivered(false);
					plugin.getMysqlHandler().updateData(MysqlType.PMAIL, up, "`id` = ?", up.getId());
					i++;
				}
			}
			if(i > 0)
			{
				plugin.getPlayerDataHandler().sendMessageToOtherPlayer(mailbox.getOwner(), 
						plugin.getYamlHandler().getLang().getString("PMail.Send.HasPMail")
						.replace("%pmailread%", CommandSuggest.getCmdString(CommandSuggest.Type.PMAIL))
						.replace("%amount%", String.valueOf(i)));
			}
		}
	}
	
	public void initParcelDeposit()
	{
		int depositParcel = plugin.getYamlHandler().getConfig().getInt("Parcel.Task.RunInLoop", 60);
		long depositAfter = plugin.getYamlHandler().getConfig().getLong("Parcel.Task.DepositParcelWhichAreOlderThan", 60 * 60L);
		final Material packages = plugin.getParcelHandler().getPackageType();
		if(depositParcel < 0 || depositAfter < 0)
		{
			return;
		}
		new BukkitRunnable() 
		{	
			@Override
			public void run()
			{
				long senddateAfter = System.currentTimeMillis() - (depositAfter * 1000 * 60);
				ArrayList<Parcel> list = Parcel.convert(plugin.getMysqlHandler().getFullList(MysqlType.PARCEL,
						"`id` ASC",
						"`sending_date` < ? AND `in_delivering` = ?", senddateAfter, true));
				LinkedHashMap<UUID, ArrayList<Parcel>> map = new LinkedHashMap<>();
				list.stream().forEach(x -> sortingAfterUUID(map, x));
				for(Entry<UUID, ArrayList<Parcel>> e : map.entrySet())
				{
					final UUID receiver = e.getKey();
					final ArrayList<Parcel> value = e.getValue();
					MailBox mailbox = plugin.getMailBoxHandler().getMailBox(receiver);
					if(mailbox == null || !plugin.getServername().equals(mailbox.getServer())
							|| !Bukkit.getWorlds().stream()
							.filter(x -> x.getName().equals(mailbox.getWorld()))
							.map(x -> x.getName())
							.collect(Collectors.toList()).contains(mailbox.getWorld()))
					{
						continue;
					}
					new BukkitRunnable() 
					{
						@Override
						public void run() 
						{
							depositParcel(mailbox, value, packages);
						}
					}.runTask(plugin);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0L, 20L * 60 * depositParcel);
	}
	
	private void sortingAfterUUID(LinkedHashMap<UUID, ArrayList<Parcel>> map, Parcel parcel)
	{
		ArrayList<Parcel> list = new ArrayList<>();
		if(map.containsKey(parcel.getReceiver()))
		{
			list = map.get(parcel.getReceiver());
		}
		list.add(parcel);
		map.put(parcel.getReceiver(), list);
	}
	
	private void depositParcel(MailBox mailbox, ArrayList<Parcel> parcels, Material packages)
	{
		Block block = mailbox.getLocation().getBlock();
		if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
		{
			return;
		}
		if(block.getState() instanceof Chest)
		{
			Chest chest = (Chest) block.getState();
			Inventory inv = chest.getInventory();
			int i = 0;
			
			for(Parcel parcel : parcels)
			{
				ItemStack is = plugin.getParcelHandler().getParcelToDeposit(parcel, packages);
				HashMap<Integer, ItemStack> map = inv.addItem(is);
				if(map.isEmpty())
				{
					Parcel p = parcel;
					p.setInDelivering(false);
					plugin.getMysqlHandler().updateData(MysqlType.PARCEL, p, "`id` = ?", p.getId());
					i++;
				}
			}
			if(i > 0)
			{
				plugin.getPlayerDataHandler().sendMessageToOtherPlayer(mailbox.getOwner(), 
						plugin.getYamlHandler().getLang().getString("Parcel.HasParcel")
						.replace("%amount%", String.valueOf(i)));
			}
		}
	}
}