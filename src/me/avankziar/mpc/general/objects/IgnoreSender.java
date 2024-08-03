package me.avankziar.mpc.general.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import me.avankziar.mpc.general.database.MysqlBaseHandler;
import me.avankziar.mpc.general.database.MysqlHandable;
import me.avankziar.mpc.general.database.QueryType;

public class IgnoreSender implements MysqlHandable
{
	private int id;
	private UUID receiver;
	private UUID sender;
	
	public IgnoreSender()
	{
		// TODO Auto-generated constructor stub
	}
	
	public IgnoreSender(int id, UUID receiver, UUID sender)
	{
		setId(id);
		setReceiver(receiver);
		setSender(sender);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public UUID getReceiver()
	{
		return receiver;
	}

	public void setReceiver(UUID receiver)
	{
		this.receiver = receiver;
	}

	public UUID getSender()
	{
		return sender;
	}

	public void setSender(UUID sender)
	{
		this.sender = sender;
	}
	
	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`mail_receiver`, `mail_sender`) " 
					+ "VALUES(?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getReceiver().toString());
	        ps.setString(2, getSender().toString());
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
				+ "` SET `mail_receiver` = ?, `mail_sender` = ?"
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getReceiver().toString());
	        ps.setString(2, getSender().toString());
			int i = 3;
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
				al.add(new IgnoreSender(rs.getInt("id"),
						UUID.fromString(rs.getString("mail_receiver")),
						UUID.fromString(rs.getString("mail_sender"))));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<IgnoreSender> convert(ArrayList<Object> arrayList)
	{
		ArrayList<IgnoreSender> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof IgnoreSender)
			{
				l.add((IgnoreSender) o);
			}
		}
		return l;
	}
}