package me.avankziar.mpc.spigot.cmd.email;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.assistance.MatchApi;
import me.avankziar.mpc.general.assistance.TimeHandler;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class ARGE_Read extends ArgumentModule
{
	private MPC plugin;
	
	public ARGE_Read(MPC plugin, ArgumentConstructor argumentConstructor)
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
		new BukkitRunnable() {
			
			@Override
			public void run() {
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
			if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.READ_OTHER_MAIL))
			{
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("EMail.YourAreNotTheEMailOwner"));
				return;
			}
		}
		if(email.getReceiver().equals(player.getUniqueId()))
		{
			//IF player is receiver, set sender a flag with hasreademail
			EMail corresponding = plugin.getEMailHandler().getCorrespondingEmail(email.getSendingDate(), email.getId());
			if(corresponding != null) //If Correspondig Email wasnt deleted
			{
				if(!corresponding.hasReceiverReaded())
				{
					corresponding.setReceiverReaded(true);
					plugin.getMysqlHandler().updateData(MysqlType.EMAIL, corresponding, "`id` = ?", corresponding.getId());
				}
			}
			if(!email.hasReceiverReaded())
			{
				email.setReceiverReaded(true);
			}
			EMail update = email;
			plugin.getMysqlHandler().updateData(MysqlType.EMAIL, update, "`id` = ?", update.getId());
		}
		ArrayList<String> list = new ArrayList<>();
		String sender = plugin.getPlayerDataHandler().getPlayerName(email.getSender());
		String receiver = plugin.getPlayerDataHandler().getPlayerName(email.getReceiver().toString());
		String subject = email.getSubjectMatter()
				.replace("run_command", "suggest_command")
				.replace("open_url", "copy_to_clipboard");
		String message = email.getMessage()
				.replace("run_command", "suggest_command")
				.replace("open_url", "copy_to_clipboard");
		plugin.getYamlHandler().getLang().getStringList("EMail.Read.Reading").stream()
			.forEach(x -> list.add(x
					.replace("%sender%", sender)
					.replace("%receiver%", receiver)
					.replace("%time%", TimeHandler.getDateTime(email.getSendingDate(),
							plugin.getYamlHandler().getLang().getString("EMail.Read.TimeFormat", "dd.MM-HH:mm")))
					.replace("%subject%", subject)
					.replace("%message%", message)));
		list.stream().forEach(x -> ChatApi.sendMessage(player, x));	
	}
}