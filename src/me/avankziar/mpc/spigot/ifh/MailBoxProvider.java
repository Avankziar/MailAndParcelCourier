package me.avankziar.mpc.spigot.ifh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;

public class MailBoxProvider implements me.avankziar.ifh.spigot.sendable.MailBox
{
	private MPC plugin;
	
	public MailBoxProvider(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	/**
	 * Send the item raw to the mailbox of the player.<br>
	 * Only possible if the mailbox if on the same server.<br>
	 * Return all not placeable Items.<br>
	 * Better called Async.
	 * @param uuid
	 * @param isa
	 * @return
	 */
	public HashMap<Integer, ItemStack> sendItem(UUID uuid, ItemStack... isa)
	{
		HashMap<Integer, ItemStack> map = new HashMap<>();
		MailBox mailbox = plugin.getMailBoxHandler().getMailBox(uuid);
		if(mailbox == null || !plugin.getServername().equals(mailbox.getServer())
				|| !Bukkit.getWorlds().stream()
				.filter(x -> x.getName().equals(mailbox.getWorld()))
				.map(x -> x.getName())
				.collect(Collectors.toList()).contains(mailbox.getWorld()))
		{
			int i = 0;
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				map.put(i, is);
				i++;
			}
			return map;
		}
		Block block = mailbox.getLocation().getBlock();
		if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
		{
			int i = 0;
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				map.put(i, is);
				i++;
			}
			return map;
		}
		if(!(block.getState() instanceof Chest))
		{
			int i = 0;
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				map.put(i, is);
				i++;
			}
			return map;
		}
		Chest chest = (Chest) block.getState();
		Inventory inv = chest.getInventory();
		map = inv.addItem(isa);
		return map;
	}
	
	/**
	 * Send all Players the items async.<br>
	 * Only possible if the mailbox if on the same server.<br>
	 * Return all not placeable Items.<br>
	 * @param is
	 * @return
	 */
	public CompletableFuture<HashMap<UUID, HashMap<Integer, ItemStack>>> sendAllItem(ItemStack... is)
	{
		CompletableFuture<HashMap<UUID, HashMap<Integer, ItemStack>>> resultFuture = new CompletableFuture<>();
		CompletableFuture.runAsync(() -> 
		{
			ArrayList<PlayerData> list = PlayerData.convert(plugin.getMysqlHandler().getFullList(MysqlType.PLAYERDATA, "`id` ASC", "`id` > ?", 0));
			HashMap<UUID, HashMap<Integer, ItemStack>> map = new HashMap<>();
			for(PlayerData pd : list)
			{
				HashMap<Integer, ItemStack> m = sendItem(pd.getPlayerUUID(), is);
				if(!m.isEmpty())
				{
					map.put(pd.getPlayerUUID(), m);
				}
			};
			resultFuture.complete(map);
		});
		return resultFuture;
	}
}