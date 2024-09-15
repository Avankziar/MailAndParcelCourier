package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.avankziar.ifh.general.economy.action.OrdererType;
import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.TimeHandler;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;
import net.md_5.bungee.api.ChatColor;

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
	
	public boolean hasEnoughPaperInInventoryAsCost(Player player, Material paper, int cost)
	{
		if(cost <= 0)
		{
			return true;
		}
		int remaincost = cost;
		for(ItemStack is : player.getInventory().getStorageContents())
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
	
	public boolean withdrawPaperFromInventoryAsCost(Player player, Material paper, int cost)
	{
		if(cost <= 0)
		{
			return true;
		}
		int remaincost = cost;
		for(ItemStack is : player.getInventory().getContents())
		{
			if(is == null || is.getType() != paper || is.getAmount() < 1)
			{
				continue;
			}
			if(is.getAmount() > remaincost)
			{
				is.setAmount(is.getAmount()-remaincost);
				remaincost = 0;
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
			READDATE = "mpc_readdate",
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
			String subject, String message, UUID owner, String other)
	{
		ItemStack i = new ItemStack(paper, 1);
		ItemMeta im = i.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(new NamespacedKey(plugin, SUBJECT), PersistentDataType.STRING, subject);
		pdc.set(new NamespacedKey(plugin, MESSAGE), PersistentDataType.STRING, message);
		pdc.set(new NamespacedKey(plugin, OWNER), PersistentDataType.STRING, owner.toString());
		pdc.set(new NamespacedKey(plugin, SENDER), PersistentDataType.STRING, player.getUniqueId().toString());
		pdc.set(new NamespacedKey(plugin, RECEIVER), PersistentDataType.STRING, owner.toString());
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
				ChatApi.convertMiniMessageToOldFormat(
				plugin.getYamlHandler().getLang().getString("PMail.Write.Displayname")
				.replace("%player%", other)
				.replace("%subject%", subject))));
		im.setLore(plugin.getYamlHandler().getLang().getStringList("PMail.Write.Lore")
				.stream()
				.map(x -> ChatColor.translateAlternateColorCodes('&', ChatApi.convertMiniMessageToOldFormat(x)))
				.collect(Collectors.toList()));
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
				false, noww, 0L, false);
		PMail receiver = new PMail(0,
				pdc.get(nsu, PersistentDataType.STRING),
				pdc.get(nme, PersistentDataType.STRING),
				UUID.fromString(pdc.get(now, PersistentDataType.STRING)),
				pdc.get(nse, PersistentDataType.STRING),
				UUID.fromString(pdc.get(nre, PersistentDataType.STRING)),
				false, noww, 0L, true);
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
				"`mail_owner` = ? AND `mail_receiver` = ? AND `will_be_delivered` = ?", uuid.toString(), uuid.toString(), true));
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
		NamespacedKey nrd = new NamespacedKey(plugin, READDATE);
		NamespacedKey nwd = new NamespacedKey(plugin, WILL_BE_DELIVERED);
		ItemStack i = new ItemStack(paper, 1);
		ItemMeta im = i.getItemMeta();
		PlayerData pd = plugin.getPlayerDataHandler().getPlayer(pmail.getReceiver());
		String other = pd != null ? pd.getPlayerName() : pmail.getReceiver().toString();
		im.setLore(plugin.getYamlHandler().getLang().getStringList("PMail.Write.Lore")
				.stream()
				.map(x -> ChatColor.translateAlternateColorCodes('&', ChatApi.convertMiniMessageToOldFormat(x)))
				.collect(Collectors.toList()));
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
				ChatApi.convertMiniMessageToOldFormat(
				plugin.getYamlHandler().getLang().getString("PMail.Write.Displayname")
				.replace("%player%", other)
				.replace("%subject%", pmail.getSubjectMatter()))));
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		pdc.set(nid, PersistentDataType.INTEGER, pmail.getId());
		pdc.set(nsu, PersistentDataType.STRING, pmail.getSubjectMatter());
		pdc.set(nme, PersistentDataType.STRING, pmail.getMessage());
		pdc.set(now, PersistentDataType.STRING, pmail.getOwner().toString());
		pdc.set(nse, PersistentDataType.STRING, pmail.getSender());
		pdc.set(nre, PersistentDataType.STRING, pmail.getReceiver().toString());
		pdc.set(nwr, PersistentDataType.BOOLEAN, pmail.hasReceiverReaded());
		pdc.set(nsd, PersistentDataType.LONG, pmail.getSendingDate());
		pdc.set(nrd, PersistentDataType.LONG, pmail.getReadingDate());
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
		NamespacedKey nrd = new NamespacedKey(plugin, READDATE);
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
				pdc.get(nrd, PersistentDataType.LONG),
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
		long now = System.currentTimeMillis();
		if(correspondig != null)
		{
			correspondig.setWillBeDelivered(false);
			correspondig.setReceiverReaded(true);
			correspondig.setReadingDate(now);
			plugin.getMysqlHandler().updateData(MysqlType.PMAIL, correspondig, "`id` = ?", correspondig.getId());
		}
		pmail.setWillBeDelivered(false);
		pmail.setReceiverReaded(true);
		pmail.setReadingDate(now);
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
				"`mail_owner` = ? AND `mail_receiver` = ? AND `will_be_delivered` = ?", uuid.toString(), uuid.toString(), false));
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
				"`mail_owner` = ? AND `mail_sender` = ? AND `will_be_delivered` = ?", uuid.toString(), uuid.toString(), false));
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
					.replace("%readtime%", TimeHandler.getDateTime(pmail.getReadingDate(),
							plugin.getYamlHandler().getLang().getString("PMail.Read.TimeFormat", "dd.MM-HH:mm")))
					.replace("%subject%", subject)
					.replace("%message%", message)));
		return list;
	}
	
	public double getSendingCost(String subject, String message)
	{
		String type = plugin.getYamlHandler().getConfig().getString("PMail.Cost.SendingCosts", "NONE");
		switch(type)
		{
		default:
		case "NONE": return 0.0;
		case "LUMP_SUM": return plugin.getYamlHandler().getConfig().getDouble("PMail.Cost.Costs", 1.0);
		case "PER_WORD":
			return (double) (subject.split(" ").length + message.split(" ").length) 
					* plugin.getYamlHandler().getConfig().getDouble("PMail.Cost.Costs", 1.0);
		case "PER_LETTER":
			return (double) (subject.length() + message.length()) 
					* plugin.getYamlHandler().getConfig().getDouble("PMail.Cost.Costs", 1.0);
		}
	}
	
	public void doSendPMail(Player player, ItemStack is)
	{
		if(is == null || is.getType() != plugin.getPMailHandler().getPaperType())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.NothingInHandToSend"));
			return;
		}
		ItemMeta im = is.getItemMeta();
		PersistentDataContainer pdc = im.getPersistentDataContainer();
		NamespacedKey nsu = new NamespacedKey(plugin, PMailHandler.SUBJECT);
		NamespacedKey nme = new NamespacedKey(plugin, PMailHandler.MESSAGE);
		NamespacedKey nre = new NamespacedKey(plugin, PMailHandler.RECEIVER);
		if(!pdc.has(nsu) || !pdc.has(nme) || !pdc.has(nre))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.NothingInHandToSend"));
			return;
		}
		String subject = pdc.get(nsu, PersistentDataType.STRING);
		String msg = pdc.get(nme, PersistentDataType.STRING);
		String other = plugin.getPlayerDataHandler().getPlayerName(pdc.get(nre, PersistentDataType.STRING));
		double cost = plugin.getPMailHandler().getSendingCost(subject, msg);
		if(cost > 0.0 && (plugin.getIFHEco() != null || plugin.getVaultEco() != null))
		{
			if(plugin.getIFHEco() != null)
			{
				me.avankziar.ifh.spigot.economy.account.Account acc = plugin.getIFHEco().getDefaultAccount(player.getUniqueId());
				if(acc == null)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("AccountDontExist"));
					return;
				}
				if(acc.getBalance() < cost)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.NotEnoughMoney")
							.replace("%money%", plugin.getIFHEco().format(cost, acc.getCurrency())));
					return;
				}
				me.avankziar.ifh.general.economy.action.EconomyAction er = 
						plugin.getIFHEco().withdraw(acc, cost, OrdererType.PLAYER, player.getUniqueId().toString(),
						plugin.getYamlHandler().getLang().getString("PMail.Send.MoneyCategory"),
						plugin.getYamlHandler().getLang().getString("PMail.Send.MoneyComment"));
				if(!er.isSuccess())
				{
					ChatApi.sendMessage(player, er.getDefaultErrorMessage()
							.replace("%money%", plugin.getIFHEco().format(cost, acc.getCurrency())));
					return;
				}
			} else
			{
				if(!plugin.getVaultEco().has(player, cost))
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.NotEnoughMoney")
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
		} else
		{
			cost = 0.0;
		}
		plugin.getPMailHandler().sendPMail(player, is);
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Send.Sended")
				.replace("%players%", other)
				.replace("%subject%", subject));
		if(cost > 0.0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.SendedHasCosts")
					.replace("%money%", 
							plugin.getIFHEco() != null
							? plugin.getIFHEco().format(cost, plugin.getIFHEco().getDefaultAccount(player.getUniqueId()).getCurrency())
							: String.valueOf(cost) + plugin.getVaultEco().currencyNamePlural()
							));
		}
		is.setAmount(0);
	}
}