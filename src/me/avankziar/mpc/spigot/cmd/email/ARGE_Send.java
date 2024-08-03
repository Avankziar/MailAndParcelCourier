package me.avankziar.mpc.spigot.cmd.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.ifh.general.economy.action.OrdererType;
import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGE_Send extends ArgumentModule
{
	private MPC plugin;
	
	public ARGE_Send(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String players = args[1];
		String subject = args[2];
		ArrayList<String> msg = new ArrayList<>();
		for(int i = 3; i < args.length; i++)
		{
			msg.add(args[i]);
		}
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player, players, subject, String.join(" ", msg.toArray(new String[msg.size()])));
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, String players, String subject, String msg)
	{
		ArrayList<String> playerlist = (ArrayList<String>) Arrays.asList(players.split("@"));
		//Filters Duplicate Entrys
		playerlist = (ArrayList<String>) playerlist.stream().distinct().collect(Collectors.toList());
		double cost = plugin.getEMailHandler().getSendingCost(subject, msg) * playerlist.size();
		if(cost > 0.0 && (plugin.getIFHEco() != null || plugin.getVaultEco() != null))
		{			
			if(plugin.getIFHEco() != null)
			{
				me.avankziar.ifh.spigot.economy.account.Account acc = plugin.getIFHEco().getDefaultAccount(player.getUniqueId());
				if(acc.getBalance() < cost)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.NotEnoughMoney")
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
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.NotEnoughMoney")
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
		ArrayList<UUID> uuidlist = new ArrayList<>();
		for(String other : playerlist)
		{
			UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(other);
			if(uuid == null)
			{
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
						.replace("%player%", other));
				return;
			}
			uuidlist.add(uuid);
		}
		for(UUID uuid : uuidlist)
		{
			final long senddate = System.currentTimeMillis();
			EMail receivers = new EMail(0, subject, msg, uuid,
					player.getUniqueId().toString(), uuid, false, senddate);
			plugin.getMysqlHandler().create(MysqlType.EMAIL, receivers);
			EMail senders = new EMail(0, subject, msg, player.getUniqueId(),
					player.getUniqueId().toString(), uuid, false, senddate);
			plugin.getMysqlHandler().create(MysqlType.EMAIL, senders);
			int lastemail = plugin.getMysqlHandler().lastID(MysqlType.EMAIL)-1;
			EMail email = plugin.getEMailHandler().getEMail(lastemail);
			if(email == null)
			{
				continue;
			}
			plugin.getPlayerDataHandler().sendMessageToOtherPlayer(uuid, 
					plugin.getYamlHandler().getLang().getString("EMail.Send.HasEMail")
					.replace("%emailread%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_READ))
					.replace("%mailid%", String.valueOf(email.getId())));
		}
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Send.Sended")
				.replace("%players%", String.join(", ", players.split("@")))
				.replace("%subject%", subject));
	}
}