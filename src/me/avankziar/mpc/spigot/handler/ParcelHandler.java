package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

public class ParcelHandler 
{
	private MPC plugin;
	
	public ParcelHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean isParcelInElectronicDelivering()
	{
		String parceltype = plugin.getYamlHandler().getConfig().getString("Parcel.UsingType", "ELECTRONIC");
		return parceltype.equals("ELECTRONIC") ? true : false;
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
	
	public void openGuiToDepositParcelContent(Player player)
	{
		if(!playersReceiver.containsKey(player.getUniqueId())
				|| playersSubject.containsKey(player.getUniqueId()))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString(""));
			return;
		}
		playerInGui.add(player.getUniqueId());
		player.closeInventory();
		Inventory inv = Bukkit.createInventory(null, 6*9, plugin.getYamlHandler().getLang().getString(""));
		player.openInventory(inv);
	}
	
	public void closeGuiToDepositParcelContent(Player player, ItemStack[] isa)
	{
		if(!playerInGui.contains(player.getUniqueId()))
		{
			return;
		}
		final UUID receiver = playersReceiver.get(player.getUniqueId());
		final String subject = playersSubject.get(player.getUniqueId());
		Parcel parcel = new Parcel(0, player.getUniqueId().toString(), receiver, subject,
				System.currentTimeMillis(), isa, true);
		plugin.getMysqlHandler().create(MysqlType.PARCEL, parcel);
		playerInGui.remove(player.getUniqueId());
		playersReceiver.remove(player.getUniqueId());
		playersSubject.remove(player.getUniqueId());
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString(""));
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
		im.setDisplayName(ChatApi.convertMiniMessageToOldFormat(
				plugin.getYamlHandler().getLang().getString("Parcel.Write.Displayname")
				.replace("%player%", other)
				.replace("%subject%", parcel.getSubject())));
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(nid, PersistentDataType.INTEGER, parcel.getId());
		pdc.set(nsu, PersistentDataType.STRING, parcel.getSubject());
		pdc.set(nse, PersistentDataType.STRING, parcel.getSender());
		pdc.set(nre, PersistentDataType.STRING, parcel.getReceiver().toString());
		i.setItemMeta(im);
		return i;
	}
	
	public ItemStack[] openParcel(ItemStack is)
	{
		NamespacedKey nid = new NamespacedKey(plugin, ID);
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		if(!pdc.has(nid))
		{
			return null;
		}
		Parcel parcel = getParcel(pdc.get(nid, PersistentDataType.INTEGER));
		final ItemStack[] isa = parcel.getParcel();
		plugin.getMysqlHandler().deleteData(MysqlType.PARCEL, "`id` = ?", parcel.getId());
		is.setAmount(is.getAmount() - 1);
		return isa;
	}
}