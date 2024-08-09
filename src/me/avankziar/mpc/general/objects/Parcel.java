package me.avankziar.mpc.general.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import me.avankziar.mpc.general.database.MysqlBaseHandler;
import me.avankziar.mpc.general.database.MysqlHandable;
import me.avankziar.mpc.general.database.QueryType;
import me.avankziar.mpc.spigot.handler.Base64Handler;

public class Parcel implements MysqlHandable
{
	private int id;
	private String sender;
	private UUID receiver;
	private String subject;
	private ItemStack[] parcel;
	private long sendingdate;
	private boolean inDelivering; //Only for electronic path
	
	public Parcel()
	{
		//Empty
	}
	
	public Parcel(int id, String sender, UUID receiver, String subject, long sendingdate,
			ArrayList<ItemStack> parcel, boolean inDelivering)
	{
		setId(id);
		setSender(sender);
		setReceiver(receiver);
		setSubject(subject);
		setParcel(parcel.toArray(new ItemStack[parcel.size()]));
		setSendingdate(sendingdate);
		setInDelivering(inDelivering);
	}
	
	public Parcel(int id, String sender, UUID receiver, String subject, long sendingdate,
			ItemStack[] parcel, boolean inDelivering)
	{
		setId(id);
		setSender(sender);
		setReceiver(receiver);
		setSubject(subject);
		setParcel(parcel);
		setSendingdate(sendingdate);
		setInDelivering(inDelivering);
	}

	public int getId() 
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getSender()
	{
		return sender;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public UUID getReceiver() 
	{
		return receiver;
	}

	public void setReceiver(UUID receiver) 
	{
		this.receiver = receiver;
	}

	public String getSubject() 
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public ItemStack[] getParcel()
	{
		return parcel;
	}

	public void setParcel(ItemStack[] parcel)
	{
		this.parcel = parcel;
	}

	public long getSendingdate()
{
		return sendingdate;
	}

	public void setSendingdate(long sendingdate) 
	{
		this.sendingdate = sendingdate;
	}

	public boolean isInDelivering()
	{
		return inDelivering;
	}

	public void setInDelivering(boolean inDelivering)
	{
		this.inDelivering = inDelivering;
	}
	
	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`parcel_sender`, `parcel_receiver`,"
					+ " `subject_matter`, `parcel_items`, `sending_date`, `in_delivering`) " 
					+ "VALUES(?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getSender());
	        ps.setString(2, getReceiver().toString());
	        ps.setString(3, getSubject());
	        ps.setString(4, new Base64Handler(getParcel()).toBase64());
	        ps.setLong(5, getSendingdate());
	        ps.setBoolean(6, isInDelivering());
	        int i = ps.executeUpdate();
	        MysqlBaseHandler.addRows(QueryType.INSERT, i);
	        return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not create a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public boolean update(Connection conn, String tablename, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "UPDATE `" + tablename
				+ "` SET `parcel_sender` = ?, `parcel_receiver` = ?,"
				+ " `subject_matter` = ?, `parcel_items` = ?, `sending_date` = ?, `in_delivering` = ?"
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getSender());
	        ps.setString(2, getReceiver().toString());
	        ps.setString(3, getSubject());
	        ps.setString(4, new Base64Handler(getParcel()).toBase64());
	        ps.setLong(5, getSendingdate());
	        ps.setBoolean(6, isInDelivering());
			int i = 7;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}			
			int u = ps.executeUpdate();
			MysqlBaseHandler.addRows(QueryType.UPDATE, u);
			return true;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not update a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return false;
	}

	@Override
	public ArrayList<Object> get(Connection conn, String tablename, String orderby, String limit, String whereColumn, Object... whereObject)
	{
		try
		{
			String sql = "SELECT * FROM `" + tablename
				+ "` WHERE "+whereColumn+" ORDER BY "+orderby+limit;
			PreparedStatement ps = conn.prepareStatement(sql);
			int i = 1;
			for(Object o : whereObject)
			{
				ps.setObject(i, o);
				i++;
			}
			
			ResultSet rs = ps.executeQuery();
			MysqlBaseHandler.addRows(QueryType.READ, rs.getMetaData().getColumnCount());
			ArrayList<Object> al = new ArrayList<>();
			while (rs.next()) 
			{
				al.add(new Parcel(rs.getInt("id"),
						rs.getString("parcel_sender"),
						UUID.fromString(rs.getString("parcel_receiver")),
						rs.getString("subject_matter"),
						rs.getLong("sending_date"),
						new Base64Handler(rs.getString("parcel_items")).fromBase64(),
						rs.getBoolean("in_delivering")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<Parcel> convert(ArrayList<Object> arrayList)
	{
		ArrayList<Parcel> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof Parcel)
			{
				l.add((Parcel) o);
			}
		}
		return l;
	}
}