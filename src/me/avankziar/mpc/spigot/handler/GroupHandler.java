package me.avankziar.mpc.spigot.handler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import me.avankziar.mpc.spigot.MPC;

public class GroupHandler
{
	public class Group
	{
		private String abbreviation;
		private String displayname;
		private String permission;
		private UUID uuid;
		
		public Group(String abbreviation, String displayname, String permission, UUID uuid)
		{
			this.abbreviation = abbreviation;
			this.displayname = displayname;
			this.permission = permission;
			this.uuid = uuid;
		}
		
		public String getAbbreviation()
		{
			return this.abbreviation;
		}
		
		public String getDisplayname()
		{
			return this.displayname;
		}
		
		public String getPermission()
		{
			return this.permission;
		}
		
		public UUID getUUID()
		{
			return this.uuid;
		}
	}
	
	private MPC plugin;
	private ArrayList<Group> groups = new ArrayList<>();
	
	public GroupHandler(MPC plugin)
	{
		this.plugin = plugin;
		init();
	}
	
	private void init()
	{
		for(String s : plugin.getYamlHandler().getConfig().getStringList("Groups"))
		{
			String[] sp = s.split(";");
			if(sp.length != 4)
			{
				continue;
			}
			if(sp[0].length() > 2)
			{
				continue;
			}
			Group gr = null;
			try
			{
				gr = new Group(sp[0], sp[1], sp[2], UUID.fromString(sp[3]));
			} catch(Exception e)
			{
				continue;
			}
			Group g = gr;
			if(groups.stream().filter(x -> x.getUUID().equals(g.getUUID())).findAny().isPresent())
			{
				continue;
			}
			MPC.logger.info("Group loaded: "+g.getAbbreviation()+" | "+g.getDisplayname()+" | "+g.getPermission()+" | "+g.getUUID().toString());
			groups.add(g);
		}
	}
	
	public Group getGroup(String abbreviation)
	{
		Optional<Group> g = groups.stream().filter(x -> x.getAbbreviation().equals(abbreviation)).findFirst();
		return g.isPresent() ? g.get() : null;
	}
	
	public Group getGroup(UUID uuid)
	{
		Optional<Group> g = groups.stream().filter(x -> x.getUUID().equals(uuid)).findFirst();
		return g.isPresent() ? g.get() : null;
	}

}