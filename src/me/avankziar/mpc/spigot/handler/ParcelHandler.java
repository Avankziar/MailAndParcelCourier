package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.Parcel;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;
import net.md_5.bungee.api.ChatColor;

public class ParcelHandler 
{
	private MPC plugin;
	
	public ParcelHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public Material getPackageType()
	{
		String v = plugin.getYamlHandler().getConfig().getString("Parcel.Material", "BRICK");
		try
		{
			Material m = Material.valueOf(v);
			return m;
		} catch(Exception e)
		{
			return Material.BRICK;
		}
	}
	
	public boolean hasSlotFree(Inventory inv, int slotAmounts)
	{
		int i = 0;
		for(ItemStack is : inv.getStorageContents())
		{
			if(is == null || is.getType() == Material.AIR)
			{
				i++;
			}
		}
		return i >= slotAmounts;
	}
	
	public Parcel getParcel(int id)
	{	
		return (Parcel) plugin.getMysqlHandler().getData(MysqlType.PARCEL, "`id` = ?", id);
	}
	
	private ArrayList<UUID> playerInGui = new ArrayList<>();
	private LinkedHashMap<UUID, UUID> playersReceiver = new LinkedHashMap<>();
	private LinkedHashMap<UUID, String> playersSubject = new LinkedHashMap<>();
	
	public boolean inGui(UUID uuid)
	{
		return playerInGui.contains(uuid);
	}
	
	public void addReceiverAndSubject(UUID uuid, UUID receiver, String subject)
	{
		playersReceiver.put(uuid, receiver);
		playersSubject.put(uuid, subject);
	}
	
	public boolean hasInputReceiverForGui(UUID uuid)
	{
		return playersReceiver.containsKey(uuid);
	}
	
	public void removeInGui(UUID uuid)
	{
		playersReceiver.remove(uuid);
		playersSubject.remove(uuid);
		playerInGui.remove(uuid);
	}
	
	public void openGuiToDepositParcelContent(Player player)
	{
		if(!playersReceiver.containsKey(player.getUniqueId())
				|| !playersSubject.containsKey(player.getUniqueId()))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.HasNoInfosForSending"));
			return;
		}
		player.closeInventory();
		UUID uuid = playersReceiver.get(player.getUniqueId());
		String other = plugin.getPlayerDataHandler().getPlayerName(uuid  .toString());
		String subject = playersSubject.get(player.getUniqueId());
		Inventory inv = Bukkit.createInventory(null, 6*9, plugin.getYamlHandler().getLang().getString("Parcel.InventarTitle")
				.replace("%player%", other)
				.replace("%subject%", subject));
		playerInGui.add(player.getUniqueId());
		player.openInventory(inv);
	}
	
	public void closeGuiToDepositParcelContent(Player player, ItemStack[] isa, double cost)
	{
		if(!playerInGui.contains(player.getUniqueId()))
		{
			return;
		}
		final UUID receiver = playersReceiver.get(player.getUniqueId());
		final String other = plugin.getPlayerDataHandler().getPlayerName(receiver.toString());
		final String subject = playersSubject.get(player.getUniqueId());
		Parcel parcel = new Parcel(0, player.getUniqueId().toString(), receiver, subject,
				System.currentTimeMillis(), isa, true);
		plugin.getMysqlHandler().create(MysqlType.PARCEL, parcel);
		playerInGui.remove(player.getUniqueId());
		playersReceiver.remove(player.getUniqueId());
		playersSubject.remove(player.getUniqueId());
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.Sended")
				.replace("%player%", other)
				.replace("%subject%", subject));
		if(cost > 0.0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.SendedHasCosts")
					.replace("%money%", 
							plugin.getIFHEco() != null
							? plugin.getIFHEco().format(cost, plugin.getIFHEco().getDefaultAccount(player.getUniqueId()).getCurrency())
							: String.valueOf(cost) + plugin.getVaultEco().currencyNamePlural()
							));
		}
	}
	
	public final static String 
		ID = "mpc_parcel_id",
		SUBJECT = "mpc_parcel_subject",
		SENDER = "mpc_parcel_sender",
		RECEIVER = "mpc_parcel_receiver";
	
	/**
	 * Backgroundtask deposit the pmail on the mailbox
	 * @param pmail
	 * @param packages
	 * @return
	 */
	public ItemStack getParcelToDeposit(Parcel parcel, Material packages)
	{
		NamespacedKey nid = new NamespacedKey(plugin, ID);
		NamespacedKey nsu = new NamespacedKey(plugin, SUBJECT);
		NamespacedKey nse = new NamespacedKey(plugin, SENDER);
		NamespacedKey nre = new NamespacedKey(plugin, RECEIVER);
		ItemStack i = new ItemStack(packages, 1);
		ItemMeta im = i.getItemMeta();
		PlayerData pd = plugin.getPlayerDataHandler().getPlayer(parcel.getReceiver());
		String other = pd != null ? pd.getPlayerName() : parcel.getReceiver().toString();
		im.setDisplayName(
				ChatColor.translateAlternateColorCodes('&', 
				ChatApi.convertMiniMessageToOldFormat(
				plugin.getYamlHandler().getLang().getString("Parcel.Write.Displayname")
				.replace("%player%", other)
				.replace("%subject%", parcel.getSubject()))));
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(nid, PersistentDataType.INTEGER, parcel.getId());
		pdc.set(nsu, PersistentDataType.STRING, parcel.getSubject());
		pdc.set(nse, PersistentDataType.STRING, parcel.getSender());
		pdc.set(nre, PersistentDataType.STRING, parcel.getReceiver().toString());
		i.setItemMeta(im);
		return i;
	}
	
	public ItemStack[] openParcel(Player player, ItemStack is)
	{
		NamespacedKey nid = new NamespacedKey(plugin, ID);
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		if(!pdc.has(nid))
		{
			return null;
		}
		Parcel parcel = getParcel(pdc.get(nid, PersistentDataType.INTEGER));
		if(!parcel.getReceiver().equals(player.getUniqueId()))
		{
			return null;
		}
		final ItemStack[] isa = parcel.getParcel();
		plugin.getMysqlHandler().deleteData(MysqlType.PARCEL, "`id` = ?", parcel.getId());
		is.setAmount(is.getAmount() - 1);
		return isa;
	}
	
	public ArrayList<Parcel> getReceivedParcel(UUID uuid, int start, int quantity)
	{
		return Parcel.convert(plugin.getMysqlHandler().getList(MysqlType.PARCEL, "`id` DESC", start, quantity, 
				"`parcel_receiver` = ? AND `in_delivering` = ?", uuid.toString(), true));
	}
	
	public double getSendingCost(ItemStack[] isa)
	{
		String type = plugin.getYamlHandler().getConfig().getString("Parcel.Cost.SendingCosts", "NONE");
		List<String> list = null;
		String[] s = null;
		double d = 0.0;
		double i = 0.0;
		switch(type)
		{
		default:
		case "NONE": return 0.0;
		case "LUMP_SUM": 
			list = plugin.getYamlHandler().getConfig().getStringList("Parcel.Cost.Costs");
			s = list.stream().filter(x -> x.startsWith("default")).findFirst().orElse("default:50.0").split(";");
			if(s.length == 2)
			{
				try
				{
					d = Double.valueOf(s[1]);
				} catch(Exception e) {}
			}
			return d;
		case "PER_STACK":
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				i++;
			}
			list = plugin.getYamlHandler().getConfig().getStringList("Parcel.Cost.Costs");
			s = list.stream().filter(x -> x.startsWith("default")).findFirst().orElse("default:1.0").split(";");
			if(s.length == 2)
			{
				try
				{
					d = Double.valueOf(s[1]) * i;
				} catch(Exception e) {}
			}
			return d;
		case "PER_AMOUNT":
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				i += is.getAmount();
			}
			list = plugin.getYamlHandler().getConfig().getStringList("Parcel.Cost.Costs");
			s = list.stream().filter(x -> x.startsWith("default")).findFirst().orElse("default:1.0").split(";");
			if(s.length == 2)
			{
				try
				{
					d = Double.valueOf(s[1]) * i;
				} catch(Exception e) {}
			}
			return d;
		case "PER_MATERIAL_AMOUNT":
			list = plugin.getYamlHandler().getConfig().getStringList("Parcel.Cost.Costs");
			for(ItemStack is : isa)
			{
				if(is == null || is.getType() == Material.AIR)
				{
					continue;
				}
				double a = 0.0;
				Optional<String> os = list.stream().filter(x -> x.startsWith(is.getType().toString())).findFirst();
				if(os.isPresent())
				{
					s = os.get().split(";");
					if(s.length == 2)
					{
						try
						{
							a = Double.valueOf(s[1]);
						} catch(Exception e) {}
					}
				} else
				{
					s = list.stream().filter(x -> x.startsWith("default")).findFirst().orElse("default:1.0").split(";");
					if(s.length == 2)
					{
						try
						{
							a = Double.valueOf(s[1]);
						} catch(Exception e) {}
					}
					d += a * is.getAmount();
				}
			}
			return d;
		}
	}
}