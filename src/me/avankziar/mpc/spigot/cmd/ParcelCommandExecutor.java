package me.avankziar.mpc.spigot.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.Parcel;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.assistance.BackgroundTask;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class ParcelCommandExecutor implements CommandExecutor
{
	private MPC plugin;
	private static CommandConstructor cc;
	
	public ParcelCommandExecutor(MPC plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		ParcelCommandExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if(cc == null)
		{
			return false;
		}
		if(args.length == 0)
		{
			if (!(sender instanceof Player)) 
			{
				plugin.getLogger().info("Cmd is only for Player!");
				return false;
			}
			Player player = (Player) sender;
			if(!ModifierValueEntry.hasPermission(player, cc))
			{
				///Du hast dafür keine Rechte!
				ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
				return false;
			}
			sendIngoingParcel(player); //Base and Info Command
			return true;
		}
		int length = args.length-1;
		ArrayList<ArgumentConstructor> aclist = cc.subcommands;
		for(int i = 0; i <= length; i++)
		{
			for(ArgumentConstructor ac : aclist)
			{
				if(args[i].equalsIgnoreCase(ac.getName()))
				{
					if(length >= ac.minArgsConstructor && length <= ac.maxArgsConstructor)
					{
						if (sender instanceof Player)
						{
							Player player = (Player) sender;
							if(ModifierValueEntry.hasPermission(player, ac))
							{
								ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
								if(am != null)
								{
									try
									{
										am.run(sender, args);
									} catch (IOException e)
									{
										e.printStackTrace();
									}
								} else
								{
									plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									ChatApi.sendMessage(sender, 
											"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									return false;
								}
								return false;
							} else
							{
								ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
								return false;
							}
						} else
						{
							ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
							if(am != null)
							{
								try
								{
									am.run(sender, args);
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							} else
							{
								plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								ChatApi.sendMessage(sender, 
										"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								return false;
							}
							return false;
						}
					} else
					{
						aclist = ac.subargument;
						break;
					}
				}
			}
		}
		ChatApi.sendMessage(sender, ChatApi.click(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
				"RUN_COMMAND", CommandSuggest.getCmdString(CommandSuggest.Type.MPC)));
		return false;
	}
	
	public void sendIngoingParcel(final Player player)
	{
		if(BackgroundTask.isServerRestartImminent())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("ServerRestartIsImminent"));
			return;
		}
		int start = 0;
		int last = plugin.getMysqlHandler().getCount(MysqlType.PARCEL,
				"`parcel_receiver` = ? AND `in_delivering` = ?", player.getUniqueId().toString(), true);
		ArrayList<Parcel> parcel = plugin.getParcelHandler().getReceivedParcel(player.getUniqueId(), start, last);
		if(parcel.size() == 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.HasNoIncomingParcel"));
			return;
		}
		int i = 0;
		for(Parcel e : parcel)
		{
			if(i >= 10)
			{
				break;
			}
			ItemStack is = plugin.getParcelHandler().getParcelToDeposit(e, plugin.getParcelHandler().getPackageType());
			HashMap<Integer, ItemStack> map = player.getInventory().addItem(is);
			if(!map.isEmpty())
			{
				map.values().stream().forEach(x -> player.getWorld().dropItem(player.getLocation(), x));
			}
			i++;
		}
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Parcel.ReceivedIncomingParcel")
				.replace("%amount%", String.valueOf(i)));
	}
}