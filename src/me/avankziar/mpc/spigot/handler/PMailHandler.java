package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.avankziar.mpc.general.assistance.TimeHandler;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;

public class PMailHandler 
{
	private MPC plugin;
	
	public PMailHandler(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	public int getPaperCost()
	{
		return plugin.getYamlHandler().getConfig().getInt("PMail.Cost.MaterialCosts", 1);
	}
	
	public Material getPaperType()
	{
		String v = plugin.getYamlHandler().getConfig().getString("PMail.Cost.Material", "PAPER");
		try
		{
			Material m = Material.valueOf(v);
			return m;
		} catch(Exception e)
		{
			return Material.PAPER;
		}
	}
	
	public PMail getPMail(int id, boolean will_be_delivered)
	{
		return (PMail) plugin.getMysqlHandler().getData(MysqlType.PMAIL, "`id` = ? AND `will_be_delivered` = ?", id, will_be_delivered);
	}
	
	/**
	 * Return all found ItemStack with Paper within inventory..
	 * @param player
	 * @return
	 */
	public ArrayList<ItemStack> hasPaperWithinInventory(Player player, Material paper)
	{
		ArrayList<ItemStack> i = new ArrayList<>();
		for(ItemStack is : player.getInventory().getStorageContents())
		{
			if(is == null || is.getType() != paper)
			{
				continue;
			}
			i.add(is);
			break;
		}
		return i;
	}
	
	public boolean hasEnoughPaperInInventoryAsCost(ArrayList<ItemStack> list, Material paper, int cost)
	{
		if(cost <= 0)
		{
			return true;
		}
		int remaincost = cost;
		for(ItemStack is : list)
		{
			if(is == null || is.getType() != paper || is.getAmount() < 1)
			{
				continue;
			}
			if(is.getAmount() > remaincost)
			{
				remaincost = 0;
				break;
			} else if(is.getAmount() == remaincost)
			{
				remaincost = 0;
				break;
			} else if(is.getAmount() < remaincost)
			{
				remaincost = remaincost - is.getAmount();
			}
		}
		return remaincost == 0;
	}
	
	public boolean withdrawPaperFromInventoryAsCost(ArrayList<ItemStack> list, Material paper, int cost)
	{
		if(cost <= 0)
		{
			return true;
		}
		int remaincost = cost;
		for(ItemStack is : list)
		{
			if(is == null || is.getType() != paper || is.getAmount() < 1)
			{
				continue;
			}
			if(is.getAmount() > remaincost)
			{
				remaincost = 0;
				is.setAmount(is.getAmount()-remaincost);
				break;
			} else if(is.getAmount() == remaincost)
			{
				remaincost = 0;
				is.setAmount(0);
				break;
			} else if(is.getAmount() < remaincost)
			{
				remaincost = remaincost - is.getAmount();
				is.setAmount(0);
			}
		}
		return remaincost == 0;
	}
	
	public boolean hasSlotFree(Player player)
	{
		for(ItemStack is : player.getInventory().getStorageContents())
		{
			if(is == null || is.getType() == Material.AIR)
			{
				return true;
			}
		}
		return false;
	}
	
	public final static String 
			ID = "mpc_id",
			SUBJECT = "mpc_subject",
			MESSAGE = "mpc_message",
			OWNER = "mpc_owner",
			SENDER = "mpc_sender",
			RECEIVER = "mpc_receiver",
			WAS_READED = "mpc_was_readed",
			SENDDATE = "mpc_senddate",
			WILL_BE_DELIVERED = "mpc_will_be_delivered";
	
	/**
	 * Player write the PMail on Paper, but leaves it in his inventory
	 * @param player
	 * @param paper
	 * @param subject
	 * @param message
	 * @param owner
	 * @return
	 */
	public ItemStack writePMail(Player player, Material paper,
			String subject, String message, UUID owner)
	{
		ItemStack i = new ItemStack(paper, 1);
		ItemMeta im = i.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(new NamespacedKey(plugin, SUBJECT), PersistentDataType.STRING, subject);
		pdc.set(new NamespacedKey(plugin, MESSAGE), PersistentDataType.STRING, message);
		pdc.set(new NamespacedKey(plugin, OWNER), PersistentDataType.STRING, owner.toString());
		pdc.set(new NamespacedKey(plugin, SENDER), PersistentDataType.STRING, player.getUniqueId().toString());
		pdc.set(new NamespacedKey(plugin, RECEIVER), PersistentDataType.STRING, owner.toString());
		i.setItemMeta(im);
		return i;
	}
	
	/**
	 * Player send the pmail now.
	 * @param player
	 * @param is
	 */
	public void sendPMail(Player player, ItemStack is)
	{
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		NamespacedKey nsu = new NamespacedKey(plugin, SUBJECT);
		if(!pdc.has(nsu))
		{
			return;
		}
		NamespacedKey nme = new NamespacedKey(plugin, MESSAGE);
		NamespacedKey now = new NamespacedKey(plugin, OWNER);
		NamespacedKey nse = new NamespacedKey(plugin, SENDER);
		NamespacedKey nre = new NamespacedKey(plugin, RECEIVER);
		if(!pdc.has(nme) || !pdc.has(now) || !pdc.has(nse)
				|| !pdc.has(nre))
		{
			return;
		}
		long noww = System.currentTimeMillis();
		PMail sender = new PMail(0,
				pdc.get(nsu, PersistentDataType.STRING),
				pdc.get(nme, PersistentDataType.STRING),
				player.getUniqueId(),
				pdc.get(nse, PersistentDataType.STRING),
				UUID.fromString(pdc.get(nre, PersistentDataType.STRING)),
				false, noww, true);
		PMail receiver = new PMail(0,
				pdc.get(nsu, PersistentDataType.STRING),
				pdc.get(nme, PersistentDataType.STRING),
				UUID.fromString(pdc.get(now, PersistentDataType.STRING)),
				pdc.get(nse, PersistentDataType.STRING),
				UUID.fromString(pdc.get(nre, PersistentDataType.STRING)),
				false, noww, true);
		plugin.getMysqlHandler().create(MysqlType.PMAIL, sender);
		plugin.getMysqlHandler().create(MysqlType.PMAIL, receiver);
		is.setAmount(is.getAmount() - 1);
	}
	
	/**
	 * Get all to delivered mails.
	 * @param uuid
	 * @param start
	 * @param quantity
	 * @return
	 */
	public ArrayList<PMail> getToDeliverIncomingMail(UUID uuid, int start, int quantity)
	{
		return PMail.convert(plugin.getMysqlHandler().getList(MysqlType.PMAIL, "`id` DESC", start, quantity, 
				"`mail_receiver` = ? AND `will_be_delivered` = ?", uuid.toString(), true));
	}
	
	/**
	 * Backgroundtask deposit the pmail on the mailbox
	 * @param pmail
	 * @param paper
	 * @return
	 */
	public ItemStack getPMailToDeposit(PMail pmail, Material paper)
	{
		NamespacedKey nid = new NamespacedKey(plugin, ID);
		NamespacedKey nsu = new NamespacedKey(plugin, SUBJECT);
		NamespacedKey nme = new NamespacedKey(plugin, MESSAGE);
		NamespacedKey now = new NamespacedKey(plugin, OWNER);
		NamespacedKey nse = new NamespacedKey(plugin, SENDER);
		NamespacedKey nre = new NamespacedKey(plugin, RECEIVER);
		NamespacedKey nwr = new NamespacedKey(plugin, WAS_READED);
		NamespacedKey nsd = new NamespacedKey(plugin, SENDDATE);
		NamespacedKey nwd = new NamespacedKey(plugin, WILL_BE_DELIVERED);
		ItemStack i = new ItemStack(paper, 1);
		ItemMeta im = i.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(nid, PersistentDataType.INTEGER, pmail.getId());
		pdc.set(nsu, PersistentDataType.STRING, pmail.getSubjectMatter());
		pdc.set(nme, PersistentDataType.STRING, pmail.getMessage());
		pdc.set(now, PersistentDataType.STRING, pmail.getOwner().toString());
		pdc.set(nse, PersistentDataType.STRING, pmail.getSender());
		pdc.set(nre, PersistentDataType.STRING, pmail.getReceiver().toString());
		pdc.set(nwr, PersistentDataType.BOOLEAN, pmail.hasReceiverReaded());
		pdc.set(nsd, PersistentDataType.LONG, pmail.getSendingDate());
		pdc.set(nwd, PersistentDataType.BOOLEAN, pmail.willBeDelivered());
		i.setItemMeta(im);
		return i;
	}
	
	/**
	 * Get the PMail which was written on paper
	 * @param player
	 * @param is
	 * @return
	 */
	public PMail readPMail(ItemStack is)
	{
		NamespacedKey nid = new NamespacedKey(plugin, ID);
		NamespacedKey nsu = new NamespacedKey(plugin, SUBJECT);
		NamespacedKey nme = new NamespacedKey(plugin, MESSAGE);
		NamespacedKey now = new NamespacedKey(plugin, OWNER);
		NamespacedKey nse = new NamespacedKey(plugin, SENDER);
		NamespacedKey nre = new NamespacedKey(plugin, RECEIVER);
		NamespacedKey nwr = new NamespacedKey(plugin, WAS_READED);
		NamespacedKey nsd = new NamespacedKey(plugin, SENDDATE);
		NamespacedKey nwd = new NamespacedKey(plugin, WILL_BE_DELIVERED);
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		if(!pdc.has(nid) || !pdc.has(nsu) || !pdc.has(nme) || !pdc.has(now) || !pdc.has(nse)
				|| !pdc.has(nre) || !pdc.has(nwr) || !pdc.has(nsd) || !pdc.has(nwd))
		{
			return null;
		}
		PMail receiver = new PMail(
				pdc.get(nid, PersistentDataType.INTEGER),
				pdc.get(nsu, PersistentDataType.STRING),
				pdc.get(nme, PersistentDataType.STRING),
				UUID.fromString(pdc.get(now, PersistentDataType.STRING)),
				pdc.get(nse, PersistentDataType.STRING),
				UUID.fromString(pdc.get(nre, PersistentDataType.STRING)),
				pdc.get(nwr, PersistentDataType.BOOLEAN),
				pdc.get(nsd, PersistentDataType.LONG),
				pdc.get(nwd, PersistentDataType.BOOLEAN));
		return receiver;
	}
	
	/**
	 * Open the PMail and upadates the mysql, which now pmail was delivered and read.
	 * @param pmail
	 */
	public void openPMail(ItemStack is, PMail pmail)
	{
		PMail correspondig = getCorrespondingEmail(pmail.getSendingDate(), pmail.getId());
		if(correspondig != null)
		{
			correspondig.setWillBeDelivered(false);
			correspondig.setReceiverReaded(true);
			plugin.getMysqlHandler().updateData(MysqlType.PMAIL, correspondig, "`id` = ?", correspondig.getId());
		}
		pmail.setWillBeDelivered(false);
		pmail.setReceiverReaded(true);
		plugin.getMysqlHandler().updateData(MysqlType.PMAIL, pmail, "`id` = ?", pmail.getId());
		is.setAmount(is.getAmount() - 1);
	}
	
	public PMail getCorrespondingEmail(long sendDate, int id)
	{
		if(!plugin.getMysqlHandler().exist(MysqlType.PMAIL, "`id` != ? AND `sending_date` = ?", id, sendDate))
		{
			return null;
		}
		return (PMail) plugin.getMysqlHandler().getData(MysqlType.PMAIL,"`id` != ? AND `sending_date` = ?", id, sendDate);	
	}
	
	/**
	 * All Pmails which was delivered!
	 * @param player
	 * @return
	 */
	public ArrayList<PMail> getReceivedEmails(UUID uuid, int start, int quantity)
	{
		return PMail.convert(plugin.getMysqlHandler().getList(MysqlType.PMAIL, "`id` DESC", start, quantity, 
				"`mail_receiver` = ? AND `will_be_delivered` = ?", uuid.toString(), false));
	}
	
	/**
	 * All PMails which are sended!
	 * @param uuid
	 * @param start
	 * @param quantity
	 * @return
	 */
	public ArrayList<PMail> getSendedEmails(UUID uuid, int start, int quantity)
	{
		return PMail.convert(plugin.getMysqlHandler().getList(MysqlType.PMAIL, "`id` DESC", start, quantity, 
				"`mail_sender` = ? AND `will_be_delivered` = ?", uuid.toString(), false));
	}
	
	public ArrayList<String> getPMailToRead(PMail pmail)
	{
		ArrayList<String> list = new ArrayList<>();
		String sender = plugin.getPlayerDataHandler().getPlayerName(pmail.getSender());
		String receiver = plugin.getPlayerDataHandler().getPlayerName(pmail.getReceiver().toString());
		String subject = pmail.getSubjectMatter()
				.replace("run_command", "suggest_command")
				.replace("open_url", "copy_to_clipboard");
		String message = pmail.getMessage()
				.replace("run_command", "suggest_command")
				.replace("open_url", "copy_to_clipboard");
		plugin.getYamlHandler().getLang().getStringList("PMail.Read.Reading").stream()
			.forEach(x -> list.add(x
					.replace("%sender%", sender)
					.replace("%receiver%", receiver)
					.replace("%time%", TimeHandler.getDateTime(pmail.getSendingDate(),
							plugin.getYamlHandler().getLang().getString("PMail.Read.TimeFormat", "dd.MM-HH:mm")))
					.replace("%subject%", subject)
					.replace("%message%", message)));
		return list;
	}
}