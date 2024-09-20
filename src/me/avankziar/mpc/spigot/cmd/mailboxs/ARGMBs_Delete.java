package me.avankziar.mpc.spigot.cmd.mailboxs;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class ARGMBs_Delete extends ArgumentModule
{
	private MPC plugin;
	
	public ARGMBs_Delete(MPC plugin, ArgumentConstructor argumentConstructor)
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
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBoxs.NoMailBoxes"));
			return;
		}
		if(mailbox.getOwner() == null)
		{
			if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.DELETE_MAILBOX_WHICH_HAS_NO_OWNER))
			{
				ChatApi.sendMessage(player, 
						plugin.getYamlHandler().getLang().getString("MailBox.CannotDeleteMailBoxWithoutAOwner"));
				return;
			}
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.Ownerless"));
		} else if(!mailbox.getOwner().equals(player.getUniqueId()))
		{
			String other = plugin.getPlayerDataHandler().getPlayerName(mailbox.getOwner().toString());
			if(other.equals(mailbox.getOwner().toString()))
			{
				other = plugin.getGroupHandler().getGroup(mailbox.getOwner()).getDisplayname();
			}
			if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.DELETE_MAILBOX_OTHER_PLAYERS))
			{
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.CannotDeleteMailBoxOtherPlayers"));
				return;
			}
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.OtherPlayers")
					.replace("%player%", other));
		} else
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.YourOwn"));
		}
		plugin.getMysqlHandler().deleteData(MysqlType.MAILBOX, "`id` = ?", mailbox.getId());
	}
}