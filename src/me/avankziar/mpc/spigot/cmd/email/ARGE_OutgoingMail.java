package me.avankziar.mpc.spigot.cmd.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.assistance.TimeHandler;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGE_OutgoingMail extends ArgumentModule
{
	private MPC plugin;
	
	public ARGE_OutgoingMail(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String pages = "0";
		if(args.length >= 2)
		{
			pages = args[1];
		}
		if(!MatchApi.isNumber(pages))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoNumber"));
			return;
		}
		int page = Integer.valueOf(pages);
		if(!MatchApi.isPositivNumber(page))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("IsNegativ"));
			return;
		}
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player, page);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, int page)
	{
		int start = page*10;
		int last = plugin.getMysqlHandler().getCount(MysqlType.EMAIL,
				"`mail_sender` = ?", player.getUniqueId().toString());
		ArrayList<EMail> emails = plugin.getEMailHandler().getSendedEmails(player, start, last);
		if(emails.size() == 0 && start == 0)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.HasNoOutgoingEMails"));
			return;
		}
		ArrayList<String> texts = new ArrayList<>();
		texts.add(plugin.getYamlHandler().getLang().getString("EMail.OutGoingMail.Headline").replace("%page%", String.valueOf(page)));
		for(EMail e : emails)
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
			texts.add(plugin.getYamlHandler().getLang().getString("EMail.OutGoingMail.ShowMails")
					.replace("%mailid%", String.valueOf(e.getId()))
					.replace("%time%", TimeHandler.getDateTime(e.getSendingDate(),
							plugin.getYamlHandler().getLang().getString("EMail.TimeFormat", "dd.MM-HH:mm")))
					.replace("%subjectdisplay%", e.getSubjectMatter().replace("_", " "))
					.replace("%subject%", e.getSubjectMatter())
					.replace("%receiver%", name)
					.replace("%wasread%", plugin.getReplacerHandler().getBoolean(e.hasReceiverReaded()))
					.replace("%emailread%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_READ))
					.replace("%emailsend%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_SEND))
					.replace("%emaildelete%", CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_DELETE))
					);
		}
		
		String pastNext = pastNextPage(player,
				page, last, CommandSuggest.getCmdString(CommandSuggest.Type.EMAIL_OUTGOINGMAIL));
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