package me.avankziar.mpc.general.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.avankziar.mpc.general.database.MysqlBaseHandler;
import me.avankziar.mpc.general.database.MysqlHandable;
import me.avankziar.mpc.general.database.QueryType;

public class MailBox implements MysqlHandable
{
	private int id;
	private UUID owner;
	private String server;
	private String world;
	private int x;
	private int y;
	private int z;
	private boolean canBeUsedForSending;
	
	public MailBox()
	{
		//Empty
	}
	
	public MailBox(int id, UUID owner, String server, String world, int x, int y, int z, boolean canBeUsedForSending)
	{
		setId(id);
		setOwner(owner);
		setServer(server);
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
		setCanBeUsedForSending(canBeUsedForSending);
	}
	
	public int getId() 
	{
		return id;
	}

	public void setId(int id) 
	{
		this.id = id;
	}

	public UUID getOwner()
	{
		return owner;
	}

	public void setOwner(UUID owner)
	{
		this.owner = owner;
	}

	public String getServer()
	{
		return server;
	}

	public void setServer(String server) 
	{
		this.server = server;
	}

	public String getWorld() 
	{
		return world;
	}

	public void setWorld(String world) 
	{
		this.world = world;
	}

	public int getX() 
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY() 
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z) 
	{
		this.z = z;
	}

	public boolean canBeUsedForSending() 
	{
		return canBeUsedForSending;
	}

	public void setCanBeUsedForSending(boolean canBeUsedForSending) 
	{
		this.canBeUsedForSending = canBeUsedForSending;
	}
	
	public Location getLocation()
	{
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	@Override
	public boolean create(Connection conn, String tablename)
	{
		try
		{
			String sql = "INSERT INTO `" + tablename
					+ "`(`box_owner`, `box_server`, `box_world`, `box_x`, `box_y`, `box_z`,"
					+ " `can_be_used_for_sending`) " 
					+ "VALUES(?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setString(1, getOwner() == null ? "null" : getOwner().toString());
	        ps.setString(2, getServer());
	        ps.setString(3, getWorld());
	        ps.setInt(4, getX());
	        ps.setInt(5, getY());
	        ps.setInt(6, getZ());
	        ps.setBoolean(7, canBeUsedForSending());
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
				+ "` SET `box_owner` = ?, `box_server` = ?,"
				+ " `box_world` = ?, `box_x` = ?, `box_y` = ?, `box_z` = ?, `can_be_used_for_sending` = ?"
				+ " WHERE "+whereColumn;
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, getOwner() == null ? "null" : getOwner().toString());
	        ps.setString(2, getServer());
	        ps.setString(3, getWorld());
	        ps.setInt(4, getX());
	        ps.setInt(5, getY());
	        ps.setInt(6, getZ());
	        ps.setBoolean(7, canBeUsedForSending());
			int i = 8;
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
				String owner = rs.getString("box_owner");
				al.add(new MailBox(rs.getInt("id"),
						owner.equals("null") ? null : UUID.fromString(owner),
						rs.getString("box_server"),
						rs.getString("box_world"),
						rs.getInt("box_x"),
						rs.getInt("box_y"),
						rs.getInt("box_z"),
						rs.getBoolean("can_be_used_for_sending")));
			}
			return al;
		} catch (SQLException e)
		{
			this.log(MysqlBaseHandler.getLogger(), Level.WARNING, "SQLException! Could not get a "+this.getClass().getSimpleName()+" Object!", e);
		}
		return new ArrayList<>();
	}
	
	public static ArrayList<MailBox> convert(ArrayList<Object> arrayList)
	{
		ArrayList<MailBox> l = new ArrayList<>();
		for(Object o : arrayList)
		{
			if(o instanceof MailBox)
			{
				l.add((MailBox) o);
			}
		}
		return l;
	}
}