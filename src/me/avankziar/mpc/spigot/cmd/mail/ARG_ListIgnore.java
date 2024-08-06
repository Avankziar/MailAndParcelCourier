package me.avankziar.mpc.spigot.cmd.mail;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.objects.IgnoreSender;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARG_ListIgnore extends ArgumentModule
{
	private MPC plugin;
	
	public ARG_ListIgnore(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String pages = args[1];
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
		new BukkitRunnable() {
			
			@Override
			public void run() {
				doAsync(player, page);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, int page)
	{
		int start = page*20;
		int quantity = 20;
		ArrayList<IgnoreSender> list = plugin.getIgnoreHandler().getIgnored(player.getUniqueId(), start, quantity);
		if(list.isEmpty())
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("Mail.ListIgnore.YouIgnoreNoOne"));
			return;
		}
		ArrayList<String> msg = new ArrayList<>();
		msg.add(plugin.getYamlHandler().getLang().getString("Mail.ListIgnore.Headline")
				.replace("%page%", String.valueOf(page))
				.replace("%player%", player.getName()));
		ArrayList<String> context = new ArrayList<>();
		for(IgnoreSender is : list)
		{
			PlayerData pd = plugin.getPlayerDataHandler().getPlayer(is.getSender());
			if(pd == null)
			{
				plugin.getIgnoreHandler().deleteIgnore(player.getUniqueId(), is.getSender());
				continue;
			}
			context.add(plugin.getYamlHandler().getLang().getString("Mail.ListIgnore.Context")
					.replace("%mailignore%", CommandSuggest.getCmdString(CommandSuggest.Type.MAIL_IGNORE))
					.replace("%player%", pd.getPlayerName()));
		}
		String[] c = context.toArray(new String[context.size()]);
		msg.add(String.join(", ", c));
		msg.stream().forEach(x -> ChatApi.sendMessage(player, x));
	}
}