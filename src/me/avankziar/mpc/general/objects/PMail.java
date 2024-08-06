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

public class PMail implements MysqlHandable
{
	private int id;
	private String subjectMatter; //Betreff
	private String message;
	private UUID owner;
	private String sender; //Sender kann auch das Sytem oder ein Plugin sein.
	private UUID receiver;
	private boolean receiverReaded; //Ob der Empfänger die Email gelesen hat.
	private long sendingDate; 
	private boolean willBeDelivered;
	
	public PMail()
	{
		//Empty
	}
	
	public PMail(int id, String subjectMatter, String message,
			UUID owner, String sender, UUID receiver,
			boolean receiverReaded, long sendingDate, boolean willBeDelivered)
	{
		setId(id);
		setSubjectMatter(subjectMatter);
		setMessage(message);
		setOwner(owner);
		setSender(sender);
		setReceiver(receiver);
		setReceiverReaded(receiverReaded);
		setSendingDate(sendingDate);
		setWillBeDelivered(willBeDelivered);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getSubjectMatter()
	{
		return subjectMatter;
	}

	public void setSubjectMatter(String subjectMatter)
	{
		this.subjectMatter = subjectMatter;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public UUID getOwner()
	{
		return owner;
	}

	public void setOwner(UUID owner)
	{
		this.owner = owner;
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

	public boolean hasReceiverReaded()
	{
		return receiverReaded;
	}

	public void setReceiverReaded(boolean receiverReaded)
	{
		this.receiverReaded = receiverReaded;
	}

	public long getSendingDate()
	{
		return sendingDate;
	}

	public void setSendingDate(long sendingDate)
	{
		this.sendingDate = sendingDate;
	}
	
	public boolean willBeDelivered()
{
		return willBeDelivered;
	}

	public void setWillBeDelivered(boolean willBeDelivered) 
	{
		this.willBeDelivered = willBeDelivered;
	}

	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`subject_matter`, `message_content`,"
					+ " `mail_owner`, `mail_sender`, `mail_receiver`,"
					+ " `has_receiver_readed`, `sending_date`, `will_be_delivered`) " 
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getSubjectMatter());
	        ps.setString(2, getMessage());
	        ps.setString(3, getOwner().toString());
	        ps.setString(4, getSender());
	        ps.setString(5, getReceiver().toString());
	        ps.setBoolean(6, hasReceiverReaded());
	        ps.setLong(7, getSendingDate());
	        ps.setBoolean(8, willBeDelivered());
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
				+ "` SET `subject_matter` = ?, `message_content` = ?,"
				+ " `mail_owner` = ?, `mail_sender` = ?, `mail_receiver` = ?, `has_receiver_readed` = ?, `sending_date` = ?,"
				+ " `will_be_delivered` = ?"
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getSubjectMatter());
	        ps.setString(2, getMessage());
	        ps.setString(3, getOwner().toString());
	        ps.setString(4, getSender());
	        ps.setString(5, getReceiver().toString());
	        ps.setBoolean(6, hasReceiverReaded());
	        ps.setLong(7, getSendingDate());
	        ps.setBoolean(8, willBeDelivered());
			int i = 9;
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
				al.add(new PMail(rs.getInt("id"),
						rs.getString("subject_matter"),
						rs.getString("message_content"),
						UUID.fromString(rs.getString("mail_owner")),
						rs.getString("mail_sender"),
						UUID.fromString(rs.getString("mail_receiver")),
						rs.getBoolean("has_receiver_readed"),
						rs.getLong("sending_date"),
						rs.getBoolean("will_be_delivered")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<PMail> convert(ArrayList<Object> arrayList)
	{
		ArrayList<PMail> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof PMail)
			{
				l.add((PMail) o);
			}
		}
		return l;
	}
}