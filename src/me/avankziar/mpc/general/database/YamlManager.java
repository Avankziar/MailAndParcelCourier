package me.avankziar.mpc.general.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import me.avankziar.mpc.general.database.Language.ISO639_2B;
import me.avankziar.mpc.spigot.modifiervalueentry.Bypass;

public class YamlManager
{	
	public enum Type
	{
		BUNGEE, SPIGOT, VELO;
	}
	
	private ISO639_2B languageType = ISO639_2B.GER;
	//The default language of your plugin. Mine is german.
	private ISO639_2B defaultLanguageType = ISO639_2B.GER;
	private Type type;
	
	//Per Flatfile a linkedhashmap.
	private static LinkedHashMap<String, Language> configKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> commandsKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> languageKeys = new LinkedHashMap<>();
	private static LinkedHashMap<String, Language> mvelanguageKeys = new LinkedHashMap<>();
	/*
	 * Here are mutiplefiles in one "double" map. The first String key is the filename
	 * So all filename muss be predefine. For example in the config.
	 */
	private static LinkedHashMap<String, LinkedHashMap<String, Language>> guisKeys = new LinkedHashMap<>();
	
	public YamlManager(Type type)
	{
		this.type = type;
		initConfig();
		initCommands();
		initLanguage();
		initModifierValueEntryLanguage();
	}
	
	public ISO639_2B getLanguageType()
	{
		return languageType;
	}

	public void setLanguageType(ISO639_2B languageType)
	{
		this.languageType = languageType;
	}
	
	public ISO639_2B getDefaultLanguageType()
	{
		return defaultLanguageType;
	}
	
	public LinkedHashMap<String, Language> getConfigKey()
	{
		return configKeys;
	}
	
	public LinkedHashMap<String, Language> getCommandsKey()
	{
		return commandsKeys;
	}
	
	public LinkedHashMap<String, Language> getLanguageKey()
	{
		return languageKeys;
	}
	
	public LinkedHashMap<String, Language> getModifierValueEntryLanguageKey()
	{
		return mvelanguageKeys;
	}
	
	public LinkedHashMap<String, LinkedHashMap<String, Language>> getGUIKey()
	{
		return guisKeys;
	}
	
	/*
	 * The main methode to set all paths in the yamls.
	 */
	public void setFileInputBukkit(org.bukkit.configuration.file.YamlConfiguration yml,
			LinkedHashMap<String, Language> keyMap, String key, ISO639_2B languageType)
	{
		if(!keyMap.containsKey(key))
		{
			return;
		}
		if(key.startsWith("#"))
		{
			//Comments
			String k = key.replace("#", "");
			if(yml.get(k) == null)
			{
				//return because no aktual key are present
				return;
			}
			if(yml.getComments(k) != null && !yml.getComments(k).isEmpty())
			{
				//Return, because the comments are already present, and there could be modified. F.e. could be comments from a admin.
				return;
			}
			if(keyMap.get(key).languageValues.get(languageType).length == 1)
			{
				if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
				{
					String s = ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", "");
					yml.setComments(k, Arrays.asList(s));
				}
			} else
			{
				List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
				ArrayList<String> stringList = new ArrayList<>();
				if(list instanceof List<?>)
				{
					for(Object o : list)
					{
						if(o instanceof String)
						{
							stringList.add(((String) o).replace("\r\n", ""));
						}
					}
				}
				yml.setComments(k, (List<String>) stringList);
			}
			return;
		}
		if(yml.get(key) != null)
		{
			return;
		}
		if(keyMap.get(key).languageValues.get(languageType).length == 1)
		{
			if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
			{
				yml.set(key, ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", ""));
			} else
			{
				yml.set(key, keyMap.get(key).languageValues.get(languageType)[0]);
			}
		} else
		{
			List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
			ArrayList<String> stringList = new ArrayList<>();
			if(list instanceof List<?>)
			{
				for(Object o : list)
				{
					if(o instanceof String)
					{
						stringList.add(((String) o).replace("\r\n", ""));
					} else
					{
						stringList.add(o.toString().replace("\r\n", ""));
					}
				}
			}
			yml.set(key, (List<String>) stringList);
		}
	}
	
	public void setFileInput(dev.dejvokep.boostedyaml.YamlDocument yml,
			LinkedHashMap<String, Language> keyMap, String key, ISO639_2B languageType)
	{
		if(!keyMap.containsKey(key))
		{
			return;
		}
		if(yml.get(key) != null)
		{
			return;
		}
		if(key.startsWith("#"))
		{
			//Comments
			String k = key.replace("#", "");
			if(yml.get(k) == null)
			{
				//return because no actual key are present
				return;
			}
			if(yml.getBlock(k) == null)
			{
				return;
			}
			if(yml.getBlock(k).getComments() != null && !yml.getBlock(k).getComments().isEmpty())
			{
				//Return, because the comments are already present, and there could be modified. F.e. could be comments from a admin.
				return;
			}
			if(keyMap.get(key).languageValues.get(languageType).length == 1)
			{
				if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
				{
					String s = ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", "");
					yml.getBlock(k).setComments(Arrays.asList(s));
				}
			} else
			{
				List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
				ArrayList<String> stringList = new ArrayList<>();
				if(list instanceof List<?>)
				{
					for(Object o : list)
					{
						if(o instanceof String)
						{
							stringList.add(((String) o).replace("\r\n", ""));
						}
					}
				}
				yml.getBlock(k).setComments((List<String>) stringList);
			}
			return;
		}
		if(keyMap.get(key).languageValues.get(languageType).length == 1)
		{
			if(keyMap.get(key).languageValues.get(languageType)[0] instanceof String)
			{
				yml.set(key, ((String) keyMap.get(key).languageValues.get(languageType)[0]).replace("\r\n", ""));
			} else
			{
				yml.set(key, keyMap.get(key).languageValues.get(languageType)[0]);
			}
		} else
		{
			List<Object> list = Arrays.asList(keyMap.get(key).languageValues.get(languageType));
			ArrayList<String> stringList = new ArrayList<>();
			if(list instanceof List<?>)
			{
				for(Object o : list)
				{
					if(o instanceof String)
					{
						stringList.add(((String) o).replace("\r\n", ""));
					} else
					{
						stringList.add(o.toString().replace("\r\n", ""));
					}
				}
			}
			yml.set(key, (List<String>) stringList);
		}
	}
	
	private void addComments(LinkedHashMap<String, Language> mapKeys, String path, Object[] o)
	{
		mapKeys.put(path, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, o));
	}
	
	private void addConfig(String path, Object[] c, Object[] o)
	{
		configKeys.put(path, new Language(new ISO639_2B[] {ISO639_2B.GER}, c));
		addComments(configKeys, "#"+path, o);
	}
	
	public void initConfig() //INFO:Config
	{
		addConfig("useIFHAdministration",
				new Object[] {
				true},
				new Object[] {
				"Boolean um auf das IFH Interface Administration zugreifen soll.",
				"Wenn 'true' eingegeben ist, aber IFH Administration ist nicht vorhanden, so werden automatisch die eigenen Configwerte genommen.",
				"Boolean to access the IFH Interface Administration.",
				"If 'true' is entered, but IFH Administration is not available, the own config values are automatically used."});
		addConfig("IFHAdministrationPath", 
				new Object[] {
				"mpc"},
				new Object[] {
				"",
				"Diese Funktion sorgt dafür, dass das Plugin auf das IFH Interface Administration zugreifen kann.",
				"Das IFH Interface Administration ist eine Zentrale für die Daten von Sprache, Servername und Mysqldaten.",
				"Diese Zentralisierung erlaubt für einfache Änderung/Anpassungen genau dieser Daten.",
				"Sollte das Plugin darauf zugreifen, werden die Werte in der eigenen Config dafür ignoriert.",
				"",
				"This function ensures that the plugin can access the IFH Interface Administration.",
				"The IFH Interface Administration is a central point for the language, server name and mysql data.",
				"This centralization allows for simple changes/adjustments to precisely this data.",
				"If the plugin accesses it, the values in its own config are ignored."});
		addConfig("ServerName",
				new Object[] {
				"hub"},
				new Object[] {
				"",
				"Der Server steht für den Namen des Spigotservers, wie er in BungeeCord/Waterfall/Velocity config.yml unter dem Pfad 'servers' angegeben ist.",
				"Sollte kein BungeeCord/Waterfall oder andere Proxys vorhanden sein oder du nutzt IFH Administration, so kannst du diesen Bereich ignorieren.",
				"",
				"The server stands for the name of the spigot server as specified in BungeeCord/Waterfall/Velocity config.yml under the path 'servers'.",
				"If no BungeeCord/Waterfall or other proxies are available or you are using IFH Administration, you can ignore this area."});
		
		addConfig("Mysql.Status",
				new Object[] {
				false},
				new Object[] {
				"",
				"'Status' ist ein simple Sicherheitsfunktion, damit nicht unnötige Fehler in der Konsole geworfen werden.",
				"Stelle diesen Wert auf 'true', wenn alle Daten korrekt eingetragen wurden.",
				"",
				"'Status' is a simple security function so that unnecessary errors are not thrown in the console.",
				"Set this value to 'true' if all data has been entered correctly."});
		addComments(configKeys, "#Mysql", 
				new Object[] {
				"",
				"Mysql ist ein relationales Open-Source-SQL-Databaseverwaltungssystem, das von Oracle entwickelt und unterstützt wird.",
				"'My' ist ein Namenkürzel und 'SQL' steht für Structured Query Language. Eine Programmsprache mit der man Daten auf einer relationalen Datenbank zugreifen und diese verwalten kann.",
				"Link https://www.mysql.com/de/",
				"Wenn du IFH Administration nutzt, kann du diesen Bereich ignorieren.",
				"",
				"Mysql is an open source relational SQL database management system developed and supported by Oracle.",
				"'My' is a name abbreviation and 'SQL' stands for Structured Query Language. A program language that can be used to access and manage data in a relational database.",
				"Link https://www.mysql.com",
				"If you use IFH Administration, you can ignore this section."});
		addConfig("Mysql.Host",
				new Object[] {
				"127.0.0.1"},
				new Object[] {
				"",
				"Der Host, oder auch die IP. Sie kann aus einer Zahlenkombination oder aus einer Adresse bestehen.",
				"Für den Lokalhost, ist es möglich entweder 127.0.0.1 oder 'localhost' einzugeben. Bedenke, manchmal kann es vorkommen,",
				"das bei gehosteten Server die ServerIp oder Lokalhost möglich ist.",
				"",
				"The host, or IP. It can consist of a number combination or an address.",
				"For the local host, it is possible to enter either 127.0.0.1 or >localhost<.",
				"Please note that sometimes the serverIp or localhost is possible for hosted servers."});
		addConfig("Mysql.Port",
				new Object[] {
				3306},
				new Object[] {
				"",
				"Ein Port oder eine Portnummer ist in Rechnernetzen eine Netzwerkadresse,",
				"mit der das Betriebssystem die Datenpakete eines Transportprotokolls zu einem Prozess zuordnet.",
				"Ein Port für Mysql ist standart gemäß 3306.",
				"",
				"In computer networks, a port or port number ",
				"is a network address with which the operating system assigns the data packets of a transport protocol to a process.",
				"A port for Mysql is standard according to 3306."});
		addConfig("Mysql.DatabaseName",
				new Object[] {
				"mydatabase"},
				new Object[] {
				"",
				"Name der Datenbank in Mysql.",
				"",
				"Name of the database in Mysql."});
		addConfig("Mysql.SSLEnabled",
				new Object[] {
				false},
				new Object[] {
				"",
				"SSL ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"SSL is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.AutoReconnect",
				new Object[] {
				true},
				new Object[] {
				"",
				"AutoReconnect ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"AutoReconnect is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.VerifyServerCertificate",
				new Object[] {
				false},
				new Object[] {
				"",
				"VerifyServerCertificate ist einer der drei Möglichkeiten, welcher, solang man nicht weiß, was es ist, es so lassen sollte wie es ist.",
				"",
				"VerifyServerCertificate is one of the three options which, as long as you don't know what it is, you should leave it as it is."});
		addConfig("Mysql.User",
				new Object[] {
				"admin"},
				new Object[] {
				"",
				"Der User, welcher auf die Mysql zugreifen soll.",
				"",
				"The user who should access the Mysql."});
		addConfig("Mysql.Password",
				new Object[] {
				"not_0123456789"},
				new Object[] {
				"",
				"Das Passwort des Users, womit er Zugang zu Mysql bekommt.",
				"",
				"The user's password, with which he gets access to Mysql."});
		
		addConfig("EnableMechanic.Modifier",
				new Object[] {
				true},
				new Object[] {
				"",
				"Ermöglicht TT die Benutzung von IFH Interface Modifier.",
				"Es erlaubt, dass externe Plugins oder per Befehl Zahlenmodifikatoren in bestimmte Werten einfließen.",
				"Bspw. könnte es dazu führen, dass die Spieler mehr regestrierte Öfen besitzen dürfen.",
				"",
				"Enables TT to use IFH interface modifiers.",
				"It allows external plugins or by command to include number modifiers in certain values.",
				"For example, it could lead to players being allowed to own more registered furnace."});
		addConfig("EnableMechanic.ValueEntry",
				new Object[] {
				true},
				new Object[] {
				"",
				"Ermöglicht TT die Benutzung von IFH Interface ValueEntry.",
				"Es erlaubt, dass externe Plugins oder per Befehl Werteeinträge vornehmen.",
				"Bspw. könnte man dadurch bestimmte Befehle oder Technologien für Spieler freischalten.",
				"",
				"Enables TT to use the IFH interface ValueEntry.",
				"It allows external plugins or commands to make value entries.",
				"For example, it could be used to unlock certain commands or technologies for players."});		
		addConfig("ValueEntry.OverrulePermission",
				new Object[] {
				false},
				new Object[] {
				"",
				"Sollte ValueEntry eingeschalten und installiert sein, so wird bei fast allen Permissionabfragen ValueEntry mit abgefragt.",
				"Fall 1: ValueEntry ist nicht vorhanden oder nicht eingschaltet. So wird die Permission normal abgefragt.",
				"Für alle weitern Fälle ist ValueEntry vorhanden und eingeschaltet.",
				"Fall 2: Der Werteeintrag für den Spieler für diesen abgefragten Wert ist nicht vorhanden,",
				"so wird wenn 'OverrulePermission'=true immer 'false' zurückgegeben.",
				"Ist 'OverrulePermission'=false wird eine normale Permissionabfrage gemacht.",
				"Fall 3: Der Werteeintrag für den Spieler für diesen abgefragten Wert ist vorhanden,",
				"so wird wenn 'OverrulePermission'=true der hinterlegte Werteeintrag zurückgegebn.",
				"Wenn 'OverrulePermission'=false ist, wird 'true' zurückgegeben wenn der hinterlegte Werteeintrag ODER die Permissionabfrage 'true' ist.",
				"Sollten beide 'false' sein, wird 'false' zurückgegeben.",
				"",
				"If ValueEntry is switched on and installed, ValueEntry is also queried for almost all permission queries.",
				"Case 1: ValueEntry is not present or not switched on. The permission is queried normally.",
				"For all other cases, ValueEntry is present and switched on.",
				"Case 2: The value entry for the player for this queried value is not available,",
				"so if 'OverrulePermission'=true, 'false' is always returned.",
				"If 'OverrulePermission'=false, a normal permission query is made.",
				"Case 3: The value entry for the player for this queried value exists,",
				"so if 'OverrulePermission'=true the stored value entry is returned.",
				"If 'OverrulePermission'=false, 'true' is returned if the stored value entry OR the permission query is 'true'.",
				"If both are 'false', 'false' is returned."});
		
		addConfig("EMail.Cost.SendingCosts",
				new Object[] {
				"LUMP_SUM"},
				new Object[] {
				"",
				"Wählt aus, ob das senden von E-Mails etwas kostet. Betreff und Nachricht werden zusammengezählt für Wort oder Zeichenlänge.",
				"Möglich sind:",
				"LUMP_SUM -> E-Mails senden kosten einen Pauschalbetrag",
				"PER_WORD -> E-Mailkosten werden an der Anzahl Wörter bemessen. Wörter sind pro Leerzeichen getrennt.",
				"PER_LETTER -> E-Mailkosten werden an der Anzahl Zeichen bemessen. Alle Zeichen zählen dabei.",
				"NONE -> Keine Kosten.",
				"",
				"Select whether sending emails costs anything. Subject and message are counted together for word or character length.",
				"Possible are:",
				"LUMP_SUM -> Sending emails costs a flat rate.",
				"PER_WORD -> Email costs are based on the number of words. Words are separated by spaces.",
				"PER_LETTER -> Email costs are based on the number of characters. All characters count.",
				"NONE -> No Costs."});
		addConfig("EMail.Cost.Costs",
				new Object[] {
				50.0},
				new Object[] {
				"",
				"Der Geldbetrag, welcher das Senden von E-Mails kosten bezogen auf die Art und Weise der Kosten.",
				"",
				"The amount of money it costs to send emails based on the method of payment."});
	}
	
	@SuppressWarnings("unused") //INFO:Commands
	public void initCommands()
	{
		comBypass();
		String path = "";
		String basePermission = "email.command.email";
		commandsInput("email", "email", "basePermission", 
				"/email [pagenumber]", "/email ", false,
				"<red>/email <white>| Infoseite für alle Befehle.",
				"<red>/email <white>| Info page for all commands.",
				"<aqua>Befehlsrecht für <white>/email",
				"<aqua>Commandright for <white>/email",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("email_send", "send", basePermission,
				"/email send <playername, multiple player with @ as seperator> <subject> <message>", "/email send ", false,
				"<red>/email send <playername, multiple player with @ as seperator> <subject> <message> <white>| Ein Subbefehl",
				"<red>/email send <playername, multiple player with @ as seperator> <subject> <message> <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/email send",
				"<aqua>Commandright for <white>/email send",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("email_read", "read", basePermission,
				"/email read <mailid>", "/email read ", false,
				"<red>/email read <mailid> <white>| Ein Subbefehl",
				"<red>/email read <mailid> <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/email read",
				"<aqua>Commandright for <white>/email read",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("email_delete", "delete", basePermission,
				"/email delete <mailid>", "/email delete ", false,
				"<red>/email delete <mailid> <white>| Ein Subbefehl",
				"<red>/email delete <mailid> <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/email delete",
				"<aqua>Commandright for <white>/email delete",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("email_outgoingmail", "outgoingmail", basePermission,
				"/email outgoingmail [pagenumber]", "/email outgoingmail ", false,
				"<red>/email outgoingmail [Seitennumber] <white>| Ein Subbefehl",
				"<red>/email outgoingmail [pagenumber] <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/email outgoingmail",
				"<aqua>Commandright for <white>/email outgoingmail",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		
		basePermission =  "emails.cmd.emails";
		commandsInput("emails", "emails", "emails.cmd.emails", 
				"/emails <playername> [Pagenumber]", "/emails ", false,
				"<red>/emails <Spielername> [Seitenzahl] <white>| Infoseite für alle Befehle.",
				"<red>/emails <playername> [Pagenumber] <white>| Info page for all commands.",
				"<aqua>Befehlsrecht für <white>/emails",
				"<aqua>Commandright for <white>/emails",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("emails_outgoingmail", "outgoingmail", basePermission,
				"/emails outgoingmail <playername> [pagenumber]", "/email outgoingmail ", false,
				"<red>/emails outgoingmail <playername> [Seitennumber] <white>| Ein Subbefehl",
				"<red>/emails outgoingmail <playername> [pagenumber] <white>| A Subcommand.",
				"<aqua>Befehlsrecht für <white>/emails outgoingmail",
				"<aqua>Commandright for <white>/emails outgoingmail",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
	}
	
	private void comBypass() //INFO:ComBypass
	{
		List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
		for(Bypass.Permission ept : list)
		{
			commandsKeys.put("Bypass."+ept.toString().replace("_", ".")
					, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"mpc."+ept.toString().toLowerCase().replace("_", ".")}));
		}
		
		List<Bypass.Counter> list2 = new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class));
		for(Bypass.Counter ept : list2)
		{
			if(!ept.forPermission())
			{
				continue;
			}
			commandsKeys.put("Count."+ept.toString().replace("_", ".")
					, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
					"mpc."+ept.toString().toLowerCase().replace("_", ".")}));
		}
	}
	
	private void commandsInput(String path, String name, String basePermission, 
			String suggestion, String commandString, boolean putUpCmdPermToBonusMalusSystem,
			String helpInfoGerman, String helpInfoEnglish,
			String dnGerman, String dnEnglish,
			String exGerman, String exEnglish)
	{
		commandsKeys.put(path+".Name"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				name}));
		commandsKeys.put(path+".Permission"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				basePermission}));
		commandsKeys.put(path+".Suggestion"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				suggestion}));
		commandsKeys.put(path+".PutUpCommandPermToBonusMalusSystem"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				putUpCmdPermToBonusMalusSystem}));
		commandsKeys.put(path+".CommandString"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				commandString}));
		commandsKeys.put(path+".HelpInfo"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				helpInfoGerman,
				helpInfoEnglish}));
		commandsKeys.put(path+".Displayname"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				dnGerman,
				dnEnglish}));
		commandsKeys.put(path+".Explanation"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				exGerman,
				exEnglish}));
	}
	
	private void argumentInput(String path, String argument, String basePermission, 
			String suggestion, String commandString, boolean putUpCmdPermToBonusMalusSystem,
			String helpInfoGerman, String helpInfoEnglish,
			String dnGerman, String dnEnglish,
			String exGerman, String exEnglish)
	{
		commandsKeys.put(path+".Argument"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				argument}));
		commandsKeys.put(path+".Permission"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				basePermission+"."+argument}));
		commandsKeys.put(path+".Suggestion"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				suggestion}));
		commandsKeys.put(path+".PutUpCommandPermToBonusMalusSystem"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				putUpCmdPermToBonusMalusSystem}));
		commandsKeys.put(path+".CommandString"
				, new Language(new ISO639_2B[] {ISO639_2B.GER}, new Object[] {
				commandString}));
		commandsKeys.put(path+".HelpInfo"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				helpInfoGerman,
				helpInfoEnglish}));
		commandsKeys.put(path+".Displayname"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				dnGerman,
				dnEnglish}));
		commandsKeys.put(path+".Explanation"
				, new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
				exGerman,
				exEnglish}));
	}
	
	public void initLanguage() //INFO:Languages
	{
		languageKeys.put("InputIsWrong",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Deine Eingabe ist fehlerhaft! Klicke hier auf den Text, um weitere Infos zu bekommen!",
						"<red>Your input is incorrect! Click here on the text to get more information!"}));
		languageKeys.put("NoPermission",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast dafür keine Rechte!",
						"<red>You dont not have the rights!"}));
		languageKeys.put("NoPlayerExist",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler existiert nicht!",
						"<red>The player does not exist!"}));
		languageKeys.put("PlayerDontExist",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler <white>%player% <red>existiert nicht!",
						"<red>The player white>%player% <red>does not exist!"}));
		languageKeys.put("NoNumber",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine ganze Zahl sein.",
						"<red>The argument <white>%value% &must be an integer."}));
		languageKeys.put("NoDouble",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine Gleitpunktzahl sein!",
						"<red>The argument <white>%value% &must be a floating point number!"}));
		languageKeys.put("IsNegativ",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Das Argument <white>%value% <red>muss eine positive Zahl sein!",
						"<red>The argument <white>%value% <red>must be a positive number!"}));
		languageKeys.put("GeneralHover",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Klick mich!",
						"<yellow>Click me!"}));
		languageKeys.put("Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>MailAndParcelCourier<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>MailAndParcelCourier<gray>]<yellow>====="}));
		languageKeys.put("Next", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>&nnächste Seite <yellow>==>",
						"<yellow>&nnext page <yellow>==>"}));
		languageKeys.put("Past", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow><== &nvorherige Seite",
						"<yellow><== &nprevious page"}));
		languageKeys.put("IsTrue", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<green>✔",
						"<green>✔"}));
		languageKeys.put("IsFalse", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>✖",
						"<red>✖"}));
		initEMailLang();
	}
	
	public void initEMailLang()
	{
		String path = "EMail.";
		languageKeys.put(path+"HasNoIncomingEMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keine eingegangenen E-Mails.",
						"<red>You have no incoming e-mails."}));
		languageKeys.put(path+"HasNoOutgoingEMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keine versendeten E-Mails.",
						"<red>You have no outgoing e-mails."}));
		languageKeys.put(path+"Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>E-Mails <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>E-Mails <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"ShowMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>Von %sender%'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Lesen</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%emailsend%%sender% re:%subject%'><gray>[</gray><aqua>Antworten</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%emaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>",
						
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>From %sender%'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Read</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%emailsend%%sender% re:%subject%'><gray>[</gray><aqua>Answere</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%emaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>"}));
		languageKeys.put(path+"TimeFormat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"dd.MM-HH:mm",
						"dd.MM-HH:mm"}));
		languageKeys.put(path+"EMailDontExist", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Diese E-Mail existiert nicht!",
						"<red>This E-Mail dont exist!"}));
		languageKeys.put(path+"YourAreNotTheEMailOwner", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Diese E-Mail gehört dir nicht!",
						"<red>This E-Mail dont belong to you!"}));
		languageKeys.put(path+"PlayerJoin", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<click:run_command:'%email%'><yellow>Du hast </yellow><white>%emails%</white> <yellow>ungelesene E-Mails!</click>",
						"<click:run_command:'%email%'><yellow>You have </yellow><white>%emails%</white> <yellow>unreaded eMails!</click>"}));
		languageKeys.put(path+"Delete.Deleted", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast die E-Mail gelöscht!",
						"<red>You deleted the email!"}));
		languageKeys.put(path+"Read.TimeFormat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"dd.MM.yyyy-HH:mm",
						"dd.MM.yyyy-HH:mm"}));
		languageKeys.put(path+"Read.Reading", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>==============================",
						"<red>Sender: <white>%sender%",
						"<red>Empfänger: <white>%receiver%",
						"<red>Zeitstempel: <white>%time%",
						"<red>Betreff: <reset>%subject%",
						"%message%",
						"<gray>==============================",
						"<gray>==============================",
						"<red>Sender: <white>%sender%",
						"<red>Empfänger: <white>%receiver%",
						"<red>Zeitstempel: <white>%time%",
						"<red>Betreff: <reset>%subject%",
						"%message%",
						"<gray>==============================",}));
		languageKeys.put(path+"Send.NotEnoughMoney", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast nicht Geld zum senden der E-Mail! Kosten: <white>%money%",
						"<red>You have not enough money to send the email! Costs:: <white>%money%"}));
		languageKeys.put(path+"Send.MoneyCategory", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"E-Mail",
						"E-Mail"}));
		languageKeys.put(path+"Send.MoneyComment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"E-Mail gesendet",
						"E-Mail sended"}));
		languageKeys.put(path+"Send.Sended", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>%players% <yellow>wude die E-Mail <white>%subject% <yellow>gesendet.",
						"<white>%players% <yellow>was send the E-Mail <white>%subject%<yellow>."}));
		languageKeys.put(path+"Send.HasEMail", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast eine neue E-Mail!</yellow> <click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Lesen</yellow><gray>]</gray></click>",
						"<yellow>You have a new E-Mail!</yellow> <click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Read</yellow><gray>]</gray></click>"}));
		languageKeys.put(path+"OutGoingMail.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>Gesendete E-Mails <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>Sended E-Mails <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"OutGoingMail.ShowMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>An %receiver%<newline><red>Wurde gelesen:</red> <white>%wasread%</white>'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Lesen</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%emailsend%%receiver% re:%subject%'><gray>[</gray><aqua>Antworten</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%emaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>",
						
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>To %receiver%<newline><red>Wurde gelesen:</red> <white>%wasread%</white>'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%emailread%%mailid%'><gray>[</gray><yellow>Read</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%emailsend%%receiver% re:%subject%'><gray>[</gray><aqua>Answere</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%emaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>"}));
		
		path = "EMails.";
		languageKeys.put(path+"HasNoIncomingEMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler hat keine eingegangenen E-Mails.",
						"<red>The player have no incoming e-mails."}));
		languageKeys.put(path+"HasNoOutgoingEMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler hat keine versendeten E-Mails.",
						"<red>The player have no outgoing e-mails."}));
		languageKeys.put(path+"Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>E-Mails von <white>%player% Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>E-Mails from <white>%player% Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"OutGoingMail.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>Gesendete E-Mails von <white>%player% Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>Sendet E-Mails from <white>%player% Page %page%<gray>]<yellow>====="}));
	}
	
	public void initModifierValueEntryLanguage() //INFO:BonusMalusLanguages
	{
		mvelanguageKeys.put(Bypass.Permission.READ_OTHER_MAIL.toString()+".Displayname",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Byasspermission für das Lesen anderer Mails",
						"<yellow>Bypasspermission for reading other mails"}));
		mvelanguageKeys.put(Bypass.Permission.READ_OTHER_MAIL.toString()+".Explanation",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Byasspermission für",
						"<yellow>das Plugin BaseTemplate",
						"<yellow>Bypasspermission for",
						"<yellow>the plugin BaseTemplate"}));
		mvelanguageKeys.put(Bypass.Counter.BASE.toString()+".Displayname",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Zählpermission für",
						"<yellow>Countpermission for"}));
		mvelanguageKeys.put(Bypass.Counter.BASE.toString()+".Explanation",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Zählpermission für",
						"<yellow>das Plugin BaseTemplate",
						"<yellow>Countpermission for",
						"<yellow>the plugin BaseTemplate"}));
	}
}