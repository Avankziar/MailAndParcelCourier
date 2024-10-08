package me.avankziar.mpc.spigot.cmd.email;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;

public class ARGE_Delete extends ArgumentModule
{
	private MPC plugin;
	
	public ARGE_Delete(MPC plugin, ArgumentConstructor argumentConstructor)
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
		EMail email = plugin.getEMailHandler().getEMail(mailid);
		if(email == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.EMailDontExist"));
			return;
		}
		if(!email.getOwner().equals(player.getUniqueId()))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.YourAreNotTheEMailOwner"));
			return;
		}
		plugin.getMysqlHandler().deleteData(MysqlType.EMAIL, "`id` = ?", mailid);
		ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.Delete.Deleted"));
	}
}