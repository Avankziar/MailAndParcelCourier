package me.avankziar.mpc.spigot.cmd;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.CommandConstructor;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.spigot.MPC;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;
import me.avankziar.mpc.spigot.modifiervalueentry.ModifierValueEntry;

public class MailBoxCommandExecutor implements CommandExecutor
{
	private MPC plugin;
	private static CommandConstructor cc;
	
	public MailBoxCommandExecutor(MPC plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		MailBoxCommandExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if(cc == null)
		{
			return false;
		}
		if (!(sender instanceof Player)) 
		{
			plugin.getLogger().info("Cmd is only for Player!");
			return false;
		}
		Player player = (Player) sender;
		if(!ModifierValueEntry.hasPermission(player, cc))
		{
			///Du hast dafür keine Rechte!
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoPermission"));
			return false;
		}
		RayTraceResult rtr = player.rayTraceBlocks(5);
		if(rtr == null)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.LookingIntoTheAir"));
			return false;
		}
		Block block = rtr.getHitBlock();
		if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
		{
			ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.BlockIsNotAChest"));
			return false;
		}
		MailBox mailbox = plugin.getMailBoxHandler().getMailBox(block.getLocation());
		if(mailbox == null)
		{
			//create
			boolean canSendPMail = false;
			boolean noOwner = false;
			boolean override = false;
			if(args.length >= 0)
			{
				for(int i = 0; i < args.length; i++)
				{
					switch(args[i])
					{
					default:
						break;
					case "-noowner":
						if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.CREATE_MAILBOX_WHICH_HAS_NO_OWNER))
						{
							ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Create.CannotCreateWithoutOwner"));
							return false;
						}
						noOwner = true;
						break;
					case "-cansend":
						if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.CREATE_MAILBOX_WHICH_CAN_SEND))
						{
							ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Create.CannotCreateWhichCanSend"));
							return false;
						}
						canSendPMail = true;
						break;
					case "-override":
						override = true;
						break;
					}
				}
			}
			if(noOwner)
			{
				mailbox = new MailBox(0, null, plugin.getServername(), block.getLocation(), canSendPMail);
				plugin.getMailBoxHandler().createMailBox(mailbox);
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Create.WithoutOwner"));
			} else
			{
				mailbox = plugin.getMailBoxHandler().getMailBox(player.getUniqueId());
				if(mailbox != null)
				{
					if(!override)
					{
						ChatApi.sendMessage(player, 
								plugin.getYamlHandler().getLang().getString("MailBox.Create.HaveAlreadyAMailBox"));
						return false;
					}
					mailbox.setLocation(plugin.getServername(), block.getLocation());
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Create.Override"));
				} else
				{
					mailbox = new MailBox(0, player.getUniqueId(), plugin.getServername(), block.getLocation(), canSendPMail);
					plugin.getMailBoxHandler().createMailBox(mailbox);
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Create.YourOwn"));
				}
			}
			return true;
		} else
		{
			//Delete
			if(mailbox.getOwner() == null)
			{
				if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.DELETE_MAILBOX_WHICH_HAS_NO_OWNER))
				{
					ChatApi.sendMessage(player, 
							plugin.getYamlHandler().getLang().getString("MailBox.CannotDeleteMailBoxWithoutAOwner"));
					return false;
				}
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.Ownerless"));
			} else if(!mailbox.getOwner().equals(player.getUniqueId()))
			{
				String other = plugin.getPlayerDataHandler().getPlayerName(mailbox.getOwner().toString());
				if(!ModifierValueEntry.hasPermission(player, Bypass.Permission.DELETE_MAILBOX_OTHER_PLAYERS))
				{
					ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.CannotDeleteMailBoxOtherPlayers"));
					return false;
				}
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.OtherPlayers")
						.replace("%player%", other));
			} else
			{
				ChatApi.sendMessage(player, plugin.getYamlHandler().getLang().getString("MailBox.Deleted.YourOwn"));
			}
			plugin.getMailBoxHandler().deleteMailBox(block.getLocation());
			return true;
		}
	}
}