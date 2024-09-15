package me.avankziar.mpc.spigot.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class MailBoxListener implements Listener
{
	private MPC plugin;
	
	public MailBoxListener(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onMailBoxBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}
		if(event.getBlock().getType() != Material.CHEST && event.getBlock().getType() != Material.TRAPPED_CHEST)
		{
			return;
		}
		MailBox mailbox = plugin.getMailBoxHandler().getMailBox(event.getBlock().getLocation());
		if(mailbox == null)
		{
			return;
		}
		String owner = mailbox.getOwner() != null 
				? plugin.getPlayerDataHandler().getPlayerName(mailbox.getOwner().toString()) 
				: null;
		if(ModifierValueEntry.hasPermission(event.getPlayer(), Bypass.Permission.MAILBOX_BREAK))
		{
			plugin.getMailBoxHandler().deleteMailBox(event.getBlock().getLocation());
			ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("MailBox.Deleted.BlockBreak")
					.replace("%owner%", owner != null ? owner : "/"));
			return;
		}
		event.setCancelled(true);
		if(owner == null)
		{
			ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("MailBox.BlockBreak.NoOwner"));
		} else
		{
			if(event.getPlayer().getUniqueId().equals(mailbox.getOwner()))
			{
				ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("MailBox.BlockBreak.SamePlayerAsOwner")
						.replace("%cmd%", CommandSuggest.getCmdString(CommandSuggest.Type.MAILBOX).strip()));
			} else
			{
				ChatApi.sendMessage(event.getPlayer(), plugin.getYamlHandler().getLang().getString("MailBox.BlockBreak.NotSamePlayerAsOwner")
						.replace("%owner%", owner));
			}
		}
	}
}