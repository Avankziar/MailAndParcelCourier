package me.avankziar.mpc.spigot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.ifh.general.modifier.ModificationType;
import me.avankziar.ifh.general.modifier.Modifier;
import me.avankziar.ifh.general.valueentry.ValueEntry;
import me.avankziar.ifh.spigot.administration.Administration;
import me.avankziar.ifh.spigot.economy.Economy;
import me.avankziar.ifh.spigot.metric.Metrics;
import me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity;
import me.avankziar.mpc.general.assistance.ChatApi;
import me.avankziar.mpc.general.cmdtree.ArgumentConstructor;
import me.avankziar.mpc.general.cmdtree.BaseConstructor;
import me.avankziar.mpc.general.cmdtree.CommandConstructor;
import me.avankziar.mpc.general.cmdtree.CommandSuggest;
import me.avankziar.mpc.general.cmdtree.CommandSuggest.Type;
import me.avankziar.mpc.general.database.MysqlType;
import me.avankziar.mpc.general.database.ServerType;
import me.avankziar.mpc.general.database.YamlHandler;
import me.avankziar.mpc.general.database.YamlManager;
import me.avankziar.mpc.general.objects.PlayerData;
import me.avankziar.mpc.spigot.assistance.BackgroundTask;
import me.avankziar.mpc.spigot.cmd.EMailCommandExecutor;
import me.avankziar.mpc.spigot.cmd.EMailsCommandExecutor;
import me.avankziar.mpc.spigot.cmd.MailCommandExecutor;
import me.avankziar.mpc.spigot.cmd.PMailCommandExecutor;
import me.avankziar.mpc.spigot.cmd.TabCompletion;
import me.avankziar.mpc.spigot.cmd.email.ARGE_Delete;
import me.avankziar.mpc.spigot.cmd.email.ARGE_OutgoingMail;
import me.avankziar.mpc.spigot.cmd.email.ARGE_Read;
import me.avankziar.mpc.spigot.cmd.email.ARGE_Send;
import me.avankziar.mpc.spigot.cmd.emails.ARGEs_OutgoingMail;
import me.avankziar.mpc.spigot.cmd.mail.ARG_Ignore;
import me.avankziar.mpc.spigot.cmd.mail.ARG_ListIgnore;
import me.avankziar.mpc.spigot.cmd.pmail.ARGP_Read;
import me.avankziar.mpc.spigot.cmdtree.ArgumentModule;
import me.avankziar.mpc.spigot.database.MysqlHandler;
import me.avankziar.mpc.spigot.database.MysqlSetup;
import me.avankziar.mpc.spigot.handler.ConfigHandler;
import me.avankziar.mpc.spigot.handler.EMailHandler;
import me.avankziar.mpc.spigot.handler.IgnoreSenderHandler;
import me.avankziar.mpc.spigot.handler.MailBoxHandler;
import me.avankziar.mpc.spigot.handler.PMailHandler;
import me.avankziar.mpc.spigot.handler.PlayerDataHandler;
import me.avankziar.mpc.spigot.handler.ReplacerHandler;
import me.avankziar.mpc.spigot.listener.JoinListener;
import me.avankziar.mpc.spigot.listener.PMailListener;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;

public class MPC extends JavaPlugin
{
	public static Logger logger;
	private static MPC plugin;
	public static String pluginname = "MailAndParcelCourier";
	private YamlHandler yamlHandler;
	private YamlManager yamlManager;
	private MysqlSetup mysqlSetup;
	private MysqlHandler mysqlHandler;
	private BackgroundTask backgroundTask;
	
	private PlayerDataHandler playerdatahandler;
	private ReplacerHandler replacerhandler;
	private IgnoreSenderHandler ignoresenderhandler;
	private EMailHandler emailhandler;
	private PMailHandler pmailhandler;
	private MailBoxHandler mailboxhandler;
	
	private Administration administrationConsumer;
	private ValueEntry valueEntryConsumer;
	private Modifier modifierConsumer;
	private MessageToVelocity mtvConsumer;
	private Economy ecoConsumer;
	
	private net.milkbowl.vault.economy.Economy vEco;
	
	private String server;
	
	public void onEnable()
	{
		plugin = this;
		logger = getLogger();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=MPC
		logger.info(" ███╗   ███╗██████╗  ██████╗ | API-Version: "+plugin.getDescription().getAPIVersion());
		logger.info(" ████╗ ████║██╔══██╗██╔════╝ | Author: "+plugin.getDescription().getAuthors().toString());
		logger.info(" ██╔████╔██║██████╔╝██║      | Plugin Website: "+plugin.getDescription().getWebsite());
		logger.info(" ██║╚██╔╝██║██╔═══╝ ██║      | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		logger.info(" ██║ ╚═╝ ██║██║     ╚██████╗ | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		logger.info(" ╚═╝     ╚═╝╚═╝      ╚═════╝ | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());
		
		setupIFHAdministration();
		
		yamlHandler = new YamlHandler(YamlManager.Type.SPIGOT, pluginname, logger, plugin.getDataFolder().toPath(),
        		(plugin.getAdministration() == null ? null : plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
        
        server = plugin.getAdministration() == null
        		? plugin.getYamlHandler().getConfig().getString("ServerName")
        				: plugin.getAdministration().getSpigotServerName();
		
		String path = plugin.getYamlHandler().getConfig().getString("IFHAdministrationPath");
		boolean adm = plugin.getAdministration() != null 
				&& plugin.getYamlHandler().getConfig().getBoolean("useIFHAdministration")
				&& plugin.getAdministration().isMysqlPathActive(path);
		if(adm || yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlSetup = new MysqlSetup(plugin, adm, path);
			mysqlHandler = new MysqlHandler(plugin);
		} else
		{
			logger.severe("MySQL is not set in the Plugin " + pluginname + "!");
			Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(this);
			return;
		}
		
		ChatApi.init(plugin);
		BaseConstructor.init(yamlHandler);
		backgroundTask = new BackgroundTask(this);
		
		ignoresenderhandler = new IgnoreSenderHandler(plugin);
		emailhandler = new EMailHandler(plugin);
		pmailhandler = new PMailHandler(plugin);
		playerdatahandler = new PlayerDataHandler(plugin);
		replacerhandler = new ReplacerHandler(plugin);
		mailboxhandler = new MailBoxHandler(plugin, server);
		
		setupBypassPerm();
		setupCommandTree();
		setupListeners();
		setupIFHConsumer();
		setupBstats();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		yamlHandler = null;
		yamlManager = null;
		mysqlSetup = null;
		mysqlHandler = null;
		if(getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	getServer().getServicesManager().unregisterAll(plugin);
	    }
		
		ignoresenderhandler = null;
		
		logger.info(pluginname + " is disabled!");
	}

	public static MPC getPlugin()
	{
		return plugin;
	}
	
	public static void shutdown()
	{
		MPC.getPlugin().onDisable();
	}
	
	public YamlHandler getYamlHandler() 
	{
		return yamlHandler;
	}
	
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}

	public void setYamlManager(YamlManager yamlManager)
	{
		this.yamlManager = yamlManager;
	}
	
	public MysqlSetup getMysqlSetup() 
	{
		return mysqlSetup;
	}
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public BackgroundTask getBackgroundTask()
	{
		return backgroundTask;
	}
	
	public String getServername()
	{
		return getPlugin().getAdministration() != null ? getPlugin().getAdministration().getSpigotServerName() 
				: getPlugin().getYamlHandler().getConfig().getString("ServerName");
	}
	
	private void setupCommandTree()
	{
		ArrayList<String> players = (ArrayList<String>) PlayerData.convert(
				plugin.getMysqlHandler().getFullList(MysqlType.PLAYERDATA, 
				"`player_name` ASC", "`id` > ?", 0)).stream().map(x -> x.getPlayerName()).collect(Collectors.toList());
		TabCompletion tab = new TabCompletion();
		
		ArgumentConstructor mail_listignore = new ArgumentConstructor(Type.MAIL_LISTIGNORE,
				"mail_listignore", 0, 0, 1, false, false, null);
		ArgumentConstructor mail_ignore = new ArgumentConstructor(Type.MAIL_IGNORE,
				"mail_ignore", 0, 1, 1, false, false, new LinkedHashMap<Integer, ArrayList<String>>(Map.of(1, players)));
		
		CommandConstructor mail = new CommandConstructor(CommandSuggest.Type.MAIL, "mail", false, false,
				mail_ignore, mail_listignore);
		registerCommand(mail.getPath(), mail.getName());
		getCommand(mail.getName()).setExecutor(new MailCommandExecutor(plugin, mail));
		getCommand(mail.getName()).setTabCompleter(tab);
		
		new ARG_Ignore(plugin, mail_ignore);
		new ARG_ListIgnore(plugin, mail_listignore);
		
		ArgumentConstructor email_send = new ArgumentConstructor(Type.EMAIL_SEND,
				"email_send", 0, 3, 999, false, false, new LinkedHashMap<Integer, ArrayList<String>>(Map.of(1, players)));
		ArgumentConstructor email_read = new ArgumentConstructor(Type.EMAIL_READ,
				"email_read", 0, 1, 1, false, false, null);
		ArgumentConstructor email_outgoingmail = new ArgumentConstructor(Type.EMAIL_OUTGOINGMAIL,
				"email_outgoingmail", 0, 0, 1, false, false, null);
		ArgumentConstructor email_delete = new ArgumentConstructor(Type.EMAIL_DELETE,
				"email_delete", 0, 1, 1, false, false, null);
		
		CommandConstructor email = new CommandConstructor(CommandSuggest.Type.EMAIL, "email", false, false,
				email_delete, email_read, email_send, email_outgoingmail);
		registerCommand(email.getPath(), email.getName());
		getCommand(email.getName()).setExecutor(new EMailCommandExecutor(plugin, email));
		getCommand(email.getName()).setTabCompleter(tab);
		
		new ARGE_Send(plugin, email_send);
		new ARGE_Read(plugin, email_read);
		new ARGE_OutgoingMail(plugin, email_outgoingmail);
		new ARGE_Delete(plugin, email_delete);
		
		ArgumentConstructor emails_outgoingmail = new ArgumentConstructor(Type.EMAILS_OUTGOINGMAIL,
				"emails_outgoingmail", 0, 1, 2, false, false, null);
		
		CommandConstructor emails = new CommandConstructor(CommandSuggest.Type.EMAILS, "emails", false, false,
				emails_outgoingmail);
		registerCommand(emails.getPath(), emails.getName());
		getCommand(emails.getName()).setExecutor(new EMailsCommandExecutor(plugin, emails));
		getCommand(emails.getName()).setTabCompleter(tab);
		
		new ARGEs_OutgoingMail(plugin, emails_outgoingmail);
		
		ArgumentConstructor pmail_send = new ArgumentConstructor(Type.PMAIL_SEND,
				"email_send", 0, 0, 0, false, false, null);
		ArgumentConstructor pmail_read = new ArgumentConstructor(Type.PMAIL_READ,
				"pmail_read", 0, 1, 1, false, false, null);
		
		CommandConstructor pmail = new CommandConstructor(CommandSuggest.Type.PMAIL, "pmail", false, false,
				email_delete, pmail_read, pmail_send, email_outgoingmail);
		registerCommand(pmail.getPath(), pmail.getName());
		getCommand(pmail.getName()).setExecutor(new PMailCommandExecutor(plugin, pmail));
		getCommand(pmail.getName()).setTabCompleter(tab);
		
		new ARGP_Read(plugin, pmail_read);
	}
	
	public void setupBypassPerm()
	{
		String path = "Count.";
		for(Bypass.Counter bypass : new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class)))
		{
			if(!bypass.forPermission())
			{
				continue;
			}
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString()));
		}
		path = "Bypass.";
		for(Bypass.Permission bypass : new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class)))
		{
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString()));
		}
	}
	
	public ArrayList<BaseConstructor> getHelpList()
	{
		return BaseConstructor.getHelpList();
	}
	
	public ArrayList<CommandConstructor> getCommandTree()
	{
		return BaseConstructor.getCommandTree();
	}
	
	public void registerCommand(String... aliases) 
	{
		PluginCommand command = getCommand(aliases[0], plugin);
	 
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}
	 
	private static PluginCommand getCommand(String name, MPC plugin) 
	{
		PluginCommand command = null;
		try 
		{
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
	 
			command = c.newInstance(name, plugin);
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} catch (InstantiationException e) 
		{
			e.printStackTrace();
		} catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
		}
	 
		return command;
	}
	 
	private static CommandMap getCommandMap() 
	{
		CommandMap commandMap = null;
	 
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) 
			{
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
	 
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) 
		{
			e.printStackTrace();
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	 
		return commandMap;
	}
	
	public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return BaseConstructor.getArgumentMapSpigot();
	}
	
	public void setupListeners()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinListener(plugin), plugin);
		pm.registerEvents(new PMailListener(plugin), plugin);
	}
	
	public boolean reload() throws IOException
	{
		if(!yamlHandler.loadYamlHandler(YamlManager.Type.SPIGOT))
		{
			return false;
		}
		if(yamlHandler.getConfig().getBoolean("Mysql.Status", false))
		{
			if(!mysqlSetup.loadMysqlSetup(ServerType.SPIGOT))
			{
				return false;
			}
		} else
		{
			return false;
		}
		return true;
	}
	
	public boolean existHook(String externPluginName)
	{
		if(plugin.getServer().getPluginManager().getPlugin(externPluginName) == null)
		{
			return false;
		}
		logger.info(pluginname+" hook with "+externPluginName);
		return true;
	}
	
	private void setupIFHAdministration()
	{ 
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
		RegisteredServiceProvider<me.avankziar.ifh.spigot.administration.Administration> rsp = 
                getServer().getServicesManager().getRegistration(Administration.class);
		if (rsp == null) 
		{
		   return;
		}
		administrationConsumer = rsp.getProvider();
		logger.info(pluginname + " detected InterfaceHub >>> Administration.class is consumed!");
	}
	
	public Administration getAdministration()
	{
		return administrationConsumer;
	}
	
	public void setupIFHConsumer()
	{
		setupIFHValueEntry();
		setupIFHModifier();
		setupIFHMessageToVelocity();
		setupIFHEconomy();
	}
	
	public void setupIFHValueEntry()
	{
		if(!new ConfigHandler().isMechanicValueEntryEnabled())
		{
			return;
		}
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
				    	return;
				    }
					RegisteredServiceProvider<me.avankziar.ifh.general.valueentry.ValueEntry> rsp = 
                            getServer().getServicesManager().getRegistration(
                           		 me.avankziar.ifh.general.valueentry.ValueEntry.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    valueEntryConsumer = rsp.getProvider();
				    logger.info(pluginname + " detected InterfaceHub >>> ValueEntry.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}
				if(getValueEntry() != null)
				{
					//Command Bonus/Malus init
					for(BaseConstructor bc : getHelpList())
					{
						if(!bc.isPutUpCmdPermToValueEntrySystem())
						{
							continue;
						}
						if(getValueEntry().isRegistered(bc.getValueEntryPath(pluginname)))
						{
							continue;
						}
						String[] ex = {plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Explanation")};
						getValueEntry().register(
								bc.getValueEntryPath(pluginname),
								plugin.getYamlHandler().getCommands().getString(bc.getPath()+".Displayname", "Command "+bc.getName()),
								ex);
					}
					//Bypass Perm Bonus/Malus init
					List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
					for(Bypass.Permission ept : list)
					{
						if(getValueEntry().isRegistered(ept.getValueLable()))
						{
							continue;
						}
						List<String> lar = plugin.getYamlHandler().getMVELang().getStringList(ept.toString()+".Explanation");
						getValueEntry().register(
								ept.getValueLable(),
								plugin.getYamlHandler().getMVELang().getString(ept.toString()+".Displayname", ept.toString()),
								lar.toArray(new String[lar.size()]));
					}
				}
			}
        }.runTaskTimer(plugin, 0L, 20*2);
	}
	
	public ValueEntry getValueEntry()
	{
		return valueEntryConsumer;
	}
	
	private void setupIFHModifier() 
	{
		if(!new ConfigHandler().isMechanicModifierEnabled())
		{
			return;
		}
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.general.modifier.Modifier> rsp = 
                            getServer().getServicesManager().getRegistration(
                           		 me.avankziar.ifh.general.modifier.Modifier.class);
				    if(rsp == null) 
				    {
				    	//Check up to 20 seconds after the start, to connect with the provider
				    	i++;
				        return;
				    }
				    modifierConsumer = rsp.getProvider();
				    logger.info(pluginname + " detected InterfaceHub >>> Modifier.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}
				if(getModifier() != null)
				{
					//Bypass CountPerm init
					List<Bypass.Counter> list = new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class));
					for(Bypass.Counter ept : list)
					{
						if(getModifier().isRegistered(ept.getModification()))
						{
							continue;
						}
						ModificationType bmt = null;
						switch(ept)
						{
						case BASE:
							bmt = ModificationType.UP;
							break;
						}
						List<String> lar = plugin.getYamlHandler().getMVELang().getStringList(ept.toString()+".Explanation");
						getModifier().register(
								ept.getModification(),
								plugin.getYamlHandler().getMVELang().getString(ept.toString()+".Displayname", ept.toString()),
								bmt,
								lar.toArray(new String[lar.size()]));
					}
				}
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public Modifier getModifier()
	{
		return modifierConsumer;
	}
	
	private void setupIFHMessageToVelocity()
	{
        if(Bukkit.getPluginManager().getPlugin("InterfaceHub") == null) 
        {
            return;
        }
        new BukkitRunnable()
        {
        	int i = 0;
			@Override
			public void run()
			{
				try
				{
					if(i == 20)
				    {
						cancel();
						return;
				    }
				    RegisteredServiceProvider<me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity> rsp = 
		                             getServer().getServicesManager().getRegistration(
		                            		 me.avankziar.ifh.spigot.tovelocity.chatlike.MessageToVelocity.class);
				    if(rsp == null) 
				    {
				    	i++;
				        return;
				    }
				    mtvConsumer = rsp.getProvider();
				    logger.info(pluginname + " detected InterfaceHub >>> MessageToVelocity.class is consumed!");
				    cancel();
				} catch(NoClassDefFoundError e)
				{
					cancel();
				}			    
			}
        }.runTaskTimer(plugin, 20L, 20*2);
	}
	
	public MessageToVelocity getMtV()
	{
		return mtvConsumer;
	}
	
	private void setupIFHEconomy()
    {
		if(plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub"))
		{
			RegisteredServiceProvider<me.avankziar.ifh.spigot.economy.Economy> rsp = 
	                getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp != null) 
			{
				ecoConsumer = rsp.getProvider();
				logger.info(pluginname + " detected InterfaceHub >>> Economy.class is consumed!");
			}
		}
		if(plugin.getServer().getPluginManager().isPluginEnabled("Vault"))
		{
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer()
	        		.getServicesManager()
	        		.getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (rsp != null) 
	        {
	        	vEco = rsp.getProvider();
		        logger.info(pluginname + " detected Vault >>> Economy.class is consumed!");
	        }
		}
        return;
    }
	
	public Economy getIFHEco()
	{
		return this.ecoConsumer;
	}
	
	public net.milkbowl.vault.economy.Economy getVaultEco()
	{
		return this.vEco;
	}
	
	public void setupBstats()
	{
		int pluginId = 0;
        new Metrics(this, pluginId);
	}
	
	public ReplacerHandler getReplacerHandler()
	{
		return replacerhandler;
	}
	
	public PlayerDataHandler getPlayerDataHandler()
	{
		return playerdatahandler;
	}
	
	public IgnoreSenderHandler getIgnoreHandler()
	{
		return ignoresenderhandler;
	}
	
	public EMailHandler getEMailHandler()
	{
		return emailhandler;
	}
	
	public PMailHandler getPMailHandler()
	{
		return pmailhandler;
	}
	
	public MailBoxHandler getMailBoxHandler()
	{
		return mailboxhandler;
	}
}