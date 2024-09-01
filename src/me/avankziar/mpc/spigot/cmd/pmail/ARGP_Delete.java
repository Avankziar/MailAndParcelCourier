package me.avankziar.mpc.spigot.cmd.pmail;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGP_Delete extends ArgumentModule
{
	private MPC plugin;
	
	public ARGP_Delete(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String mid = args[1];
		if(!MatchApi.isNumber(mid))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoNumber"));
			return;
		}
		int mailid = Integer.valueOf(mid);
		if(!MatchApi.isPositivNumber(mailid))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("IsNegativ"));
			return;
		}
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player, mailid);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, int mailid)
	{
		PMail pmail = plugin.getPMailHandler().getPMail(mailid, false);
		if(pmail == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.EMailDontExist"));
			return;
		}
		if(!pmail.getOwner().equals(player.getUniqueId()))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.YourAreNotThePMailOwner"));
			return;
		}
		plugin.getMysqlHandler().deleteData(MysqlType.PMAIL, "`id` = ?", mailid);
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PMail.Delete.Deleted"));
	}
}