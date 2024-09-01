package me.avankziar.mpc.spigot.cmd.mailboxs;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class ARGMBs_Info extends ArgumentModule
{
	private MPC plugin;
	
	public ARGMBs_Info(MPC plugin, ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = plugin;
	}
	
	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		String othername = player.getName();
		UUID otheruuid = player.getUniqueId();
		String mbid = "0";
		if(args.length >= 2)
		{
			if(MatchApi.isNumber(args[1]))
			{
				mbid = args[1];
			} else
			{
				othername = args[1];
				otheruuid = plugin.getPlayerDataHandler().getPlayerUUID(othername);
				if(otheruuid == null)
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("PlayerDontExist")
							.replace("%player%", othername));
					return;
				}
			}
		}
		String other = othername;
		UUID uuid = otheruuid;
		if(!MatchApi.isInteger(mbid))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoNumber"));
			return;
		}
		int mailboxid = Integer.valueOf(mbid);
		if(!MatchApi.isPositivNumber(mailboxid))
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("IsNegativ"));
			return;
		}
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				doAsync(player, other, uuid, mailboxid);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void doAsync(Player player, String othername, UUID uuid, int mailboxid)
	{
		MailBox mailbox = (mailboxid > 0 
				? plugin.getMailBoxHandler().getMailBox(mailboxid) : 
					plugin.getMailBoxHandler().getMailBox(uuid));
		if(mailbox == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.EMailDontExist"));
			return;
		}
		if(mailbox.getOwner() != null && !mailbox.getOwner().equals(player.getUniqueId()))
		{
			if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.INFO_MAILBOX_OTHER_PLAYERS))
			{
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.CannotDeleteMailBoxOtherPlayers"));
				return;
			}
		}
		plugin.getYamlHandler().getLang().getStringList("MailBoxs.Info").stream()
			.forEach(x -> ChatApi.sendMessage(player, x
					.replace("%id%", String.valueOf(mailbox.getId()))
					.replace("%owner%", (mailbox.getOwner() == null 
							? "/" 
							: plugin.getPlayerDataHandler().getPlayerName(mailbox.getOwner().toString())))
					.replace("%server%", mailbox.getServer())
					.replace("%world%", mailbox.getWorld())
					.replace("%x%", String.valueOf(mailbox.getX()))
					.replace("%y%", String.valueOf(mailbox.getY()))
					.replace("%z%", String.valueOf(mailbox.getZ()))
					.replace("%canbeusedtosend%", plugin.getReplacerHandler().getBoolean(mailbox.canBeUsedForSending()))
					));
	}
}