package me.avankziar.mpc.spigot.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.assistance.TimeHandler;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.handler.GroupHandler.Group;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class PMailsCommandExecutor implements CommandExecutor
{
	private MPC plugin;
	private static CommandConstructor cc;
	
	public PMailsCommandExecutor(MPC plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		PMailsCommandExecutor.cc = cc;
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
			ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
					.replace("%player%", "<?>"));
			return false;
		} else if(args.length == 1) 
		{
			if (!(sender instanceof Player)) 
			{
				plugin.getLogger().info("Cmd is only for Player!");
				return false;
			}
			Player player = (Player) sender;
			UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(args[0]);
			if(uuid == null)
			{
				Group g = plugin.getGroupHandler().getGroup(args[0]);
				if(g == null)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerOrGroupDontExist"));
					return false;
				}
				uuid = g.getUUID();
			}
			if(uuid != null)
			{
				if(!ModifierValueEntry.hasPermission(player, cc))
				{
					///Du hast dafür keine Rechte!
					ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
					return false;
				}
				sendIngoingEMails(player, args[0], uuid, 0); //Base and Info Command
				return true;
			}
		} else if(args.length == 2)
		{
			if (!(sender instanceof Player)) 
			{
				plugin.getLogger().info("Cmd is only for Player!");
				return false;
			}
			Player player = (Player) sender;
			UUID uuid = plugin.getPlayerDataHandler().getPlayerUUID(args[0]);
			if(uuid == null)
			{
				Group g = plugin.getGroupHandler().getGroup(args[0]);
				if(g == null)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerOrGroupDontExist"));
					return false;
				}
				uuid = g.getUUID();
			}
			if(uuid != null)
			{
				if(MatchApi.isInteger(args[1]))
				{
					if(!ModifierValueEntry.hasPermission(player, cc))
					{
						///Du hast dafür keine Rechte!
						ChatApi.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
						return false;
					}
					sendIngoingEMails(player, args[0], uuid, Integer.parseInt(args[1])); //Base and Info Command
					return true;
				}
			}
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
	
	public void sendIngoingEMails(final Player player, String othername, UUID otheruuid, int page)
	{
		int start = page*10;
		int last = plugin.getMysqlHandler().getCount(MysqlType.PMAIL,
				"`mail_owner` = ?  AND `mail_receiver` = ? AND `will_be_delivered` = ?", otheruuid.toString(), otheruuid.toString(), false);
		ArrayList<PMail> pmails = plugin.getPMailHandler().getReceivedEmails(otheruuid, start, last);
		if(pmails.size() == 0 && start == 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMails.HasNoIncomingEMails"));
			return;
		}
		ArrayList<String> texts = new ArrayList<>();
		texts.add(plugin.getYamlHandler().getLang().getString("PMails.Headline")
				.replace("%player%", othername)
				.replace("%page%", String.valueOf(page)));
		for(PMail e : pmails)
		{
			String name = e.getSender();
			UUID uuid = null;
			try
			{
				uuid = UUID.fromString(e.getSender());
				OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
				if(off.hasPlayedBefore())
				{
					name = off.getName();
				}
			} catch(Exception ex) {}
			texts.add(plugin.getYamlHandler().getLang().getString("PMail.ShowMails")
					.replace("%mailid%", String.valueOf(e.getId()))
					.replace("%time%", TimeHandler.getDateTime(e.getSendingDate(),
							plugin.getYamlHandler().getLang().getString("PMail.TimeFormat", "dd.MM-HH:mm")))
					.replace("%subjectdisplay%", e.getSubjectMatter().replace("_", " "))
					.replace("%subject%", e.getSubjectMatter())
					.replace("%sender%", name)
					.replace("%pmailread%", CommandSuggest.getCmdString(CommandSuggest.Type.PMAIL_READ))
					.replace("%pmailwrite%", CommandSuggest.getCmdString(CommandSuggest.Type.PMAIL_WRITE))
					.replace("%pmaildelete%", CommandSuggest.getCmdString(CommandSuggest.Type.PMAIL_DELETE))
					);
		}
		
		String pastNext = pastNextPage(player,
				page, last, CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL));
		if(pastNext != null) 
		{
			texts.add(pastNext);
		}
		texts.stream().forEach(x -> ChatApi.sendMessage(player, x));
	}
	
	public String pastNextPage(Player player,
			int page, int last, String cmdstring, String...objects)
	{
		if(page == 0 && page >= last)
		{
			return null;
		}
		int i = page+1;
		int j = page-1;
		StringBuilder sb = new StringBuilder();
		if(page != 0)
		{
			String msg2 = plugin.getYamlHandler().getLang().getString("Past");
			String cmd = cmdstring+" "+String.valueOf(j);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			sb.append(ChatApi.click(msg2, "RUN_COMMAND", cmd));
		}
		if(page < last)
		{
			String msg1 = plugin.getYamlHandler().getLang().getString("Next");
			String cmd = cmdstring+" "+String.valueOf(i);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			if(sb.length() > 0)
			{
				sb.append(" | ");
			}
			sb.append(ChatApi.click(msg1, "RUN_COMMAND", cmd));
		}
		return sb.toString();
	}
}