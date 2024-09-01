package me.avankziar.mpc.spigot.ifh;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.objects.Parcel;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.MPC;

public class ParcelProvider implements me.avankziar.ifh.spigot.sendable.Parcel
{
	private MPC plugin;
	
	public ParcelProvider(MPC plugin)
	{
		this.plugin = plugin;
	}
	
	/**
	 * Send a parcel to the receiver.<br>
	 * Should be called async.
	 * @param receiver
	 * @param sender
	 * @param subject
	 * @param is
	 */
	public void sendParcel(UUID receiver, String sender, String subject, ItemStack... is)
	{
		Parcel parcel = new Parcel(0, sender, receiver, subject,
				System.currentTimeMillis(), is, true);
		plugin.getMysqlHandler().create(MysqlType.PARCEL, parcel);
	}
	
	/**
	 * Send all players a parcel.<br>
	 * Will be processed async.
	 * @param sender
	 * @param subject
	 * @param is
	 */
	public void sendAllParcel(String sender, String subject, ItemStack... is)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				ArrayList<PlayerData> list = PlayerData.convert(plugin.getMysqlHandler().getFullList(MysqlType.PLAYERDATA, "`id` ASC", "`id` > ?", 0));
				list.stream().forEach(x -> sendParcel(x.getPlayerUUID(), sender, subject, is));
			}
		}.runTaskAsynchronously(plugin);
	}
}