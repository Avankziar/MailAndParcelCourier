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
	//private Type type;
	
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
		//this.type = type;
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
		addConfig("Language",
				new Object[] {
				"ENG"},
				new Object[] {
				"",
				"Die eingestellte Sprache. Von Haus aus sind 'ENG=Englisch' und 'GER=Deutsch' mit dabei.",
				"Falls andere Sprachen gewünsch sind, kann man unter den folgenden Links nachschauen, welchs Kürzel für welche Sprache gedacht ist.",
				"Siehe hier nach, sowie den Link, welche dort auch für Wikipedia steht.",
				"https://github.com/Avankziar/RootAdministration/blob/main/src/main/java/me/avankziar/roota/general/Language.java",
				"",
				"The set language. By default, ENG=English and GER=German are included.",
				"If other languages are required, you can check the following links to see which abbreviation is intended for which language.",
				"See here, as well as the link, which is also there for Wikipedia.",
				"https://github.com/Avankziar/RootAdministration/blob/main/src/main/java/me/avankziar/roota/general/Language.java"});
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
		addConfig("PMail.Cost.SendingCosts",
				new Object[] {
				"LUMP_SUM"},
				new Object[] {
				"",
				"Wählt aus, ob das senden von P-Mails etwas kostet. Betreff und Nachricht werden zusammengezählt für Wort oder Zeichenlänge.",
				"Möglich sind:",
				"LUMP_SUM -> P-Mails senden kosten einen Pauschalbetrag",
				"PER_WORD -> P-Mailkosten werden an der Anzahl Wörter bemessen. Wörter sind pro Leerzeichen getrennt.",
				"PER_LETTER -> P-Mailkosten werden an der Anzahl Zeichen bemessen. Alle Zeichen zählen dabei.",
				"NONE -> Keine Kosten.",
				"",
				"Select whether sending pmails costs anything. Subject and message are counted together for word or character length.",
				"Possible are:",
				"LUMP_SUM -> Sending pmails costs a flat rate.",
				"PER_WORD -> Pmail costs are based on the number of words. Words are separated by spaces.",
				"PER_LETTER -> Pmail costs are based on the number of characters. All characters count.",
				"NONE -> No Costs."});
		addConfig("PMail.Cost.Costs",
				new Object[] {
				50.0},
				new Object[] {
				"",
				"Der Geldbetrag, welcher das Senden von P-Mails kosten bezogen auf die Art und Weise der Kosten.",
				"",
				"The amount of money it costs to send pmails based on the method of payment."});
		addConfig("PMail.Cost.Material",
				new Object[] {
				"PAPER"},
				new Object[] {
				"",
				"Das Material, welches als Kosten und für die Versendung von PMails herhalten muss.",
				"",
				"The material that has to be used as costs and for sending PMails."});
		addConfig("PMail.Cost.MaterialCosts",
				new Object[] {
				1},
				new Object[] {
				"",
				"Der Anzahl an Materialien, die man dafür haben muss um eine PMail zu erstellen.",
				"",
				"The amount of materials you need to create a PMail."});
		addConfig("PMail.Task.RunInLoop",
				new Object[] {
				60},
				new Object[] {
				"",
				"Der Anzahl an Minuten, nach wieviel Minuten der Task zum Pmail verteilen wiederholt wird!",
				"",
				"The number of minutes after which the task to distribute the Pmail is repeated!"});
		addConfig("PMail.Task.DepositPMailWhichAreOlderThan",
				new Object[] {
				60},
				new Object[] {
				"",
				"Der Anzahl an Minuten, welche PMails alt sein müssen um zugestellt werden müssen.",
				"Bedeutet, die PMail muss vor mehr als x Minuten versenden worden sein, um zugestellt werden zu können.",
				"",
				"The number of minutes old pmails must be to be delivered.",
				"This means that the PMail must have been sent more than x minutes ago in order to be delivered."});
		addConfig("Parcel.Material",
				new Object[] {
				"BRICK"},
				new Object[] {
				"",
				"Das Material, welches als Parcel herhalten muss.",
				"",
				"The material to be used as a parcel."});
		addConfig("Parcel.Cost.SendingCosts",
				new Object[] {
				"LUMP_SUM"},
				new Object[] {
				"",
				"Wählt aus, ob das Senden von Parcel(Packeten) etwas kostet.",
				"Möglich sind:",
				"LUMP_SUM -> Parcel senden kosten einen Pauschalbetrag. <default> wird dazu genommen in der Parcel.Cost.Costs Liste.",
				"PER_STACK -> Parcelkosten werden an der Anzahl Stacks bemessen. <default> wird dazu genommen in der Parcel.Cost.Costs Liste.",
				"PER_AMOUNT -> Parcelkosten werden an der Anzahl Items im Gesamten bemessen. <default> wird dazu genommen in der Parcel.Cost.Costs Liste.",
				"PER_MATERIAL_AMOUNT -> Alle Items in Parcel.Cost.Costs werden durchlaufen und jedes Item nach Material und Anzahl berechnet. Falls keins gefunden wird, wird <default> genommen.",
				"NONE -> Keine Kosten.",
				"",
				"Select whether sending parcel costs anything.",
				"Possible are:",
				"LUMP_SUM -> Sending parcels costs a flat rate. <default> is added to the Parcel.Cost.Costs list.",
				"PER_STACK -> Parcel costs are based on the number of stacks. <default> is used in the Parcel.Cost.Costs list.",
				"PER_AMOUNT -> Parcel costs are based on the total number of items. <default> is used in the Parcel.Cost.Costs list.",
				"PER_MATERIAL_AMOUNT -> All items in Parcel.Cost.Costs are processed and each item is calculated according to material and quantity.",
				"NONE -> No Costs."});
		addConfig("Parcel.Cost.Costs",
				new Object[] {
				"default;50.0",
				"DIRT;1.0",
				"DIAMOND_PICKAXE;100.0"},
				new Object[] {
				"",
				"Materialliste. Determiniert welches Item pro Material pro Anzahl kosten wird. <default> wird für alle anderen Kostentypen genommen.",
				"",
				"Material list. Determines which item will cost per material per quantity. <default> is used for all other cost types."});
		addConfig("Parcel.Task.RunInLoop",
				new Object[] {
				60},
				new Object[] {
				"",
				"Der Anzahl an Minuten, wann der Task zum Verteilen der Parcel wiederholt wird.",
				"",
				"The number of minutes when the task for distributing the parcel is repeated."});
		addConfig("Parcel.Task.DepositParcelWhichAreOlderThan",
				new Object[] {
				60},
				new Object[] {
				"",
				"Der Anzahl an Minuten, welche Parcel alt sein müssen um zugestellt werden müssen.",
				"Bedeutet, die Parcel muss vor mehr als x Minuten versenden worden sein, um zugestellt werden zu können.",
				"",
				"The number of minutes old parcel must be to be delivered.",
				"This means that the parcel must have been sent more than x minutes ago in order to be delivered."});
	}
	
	public void initCommands()
	{
		comBypass();
		String basePermission = "mpc.cmd.mpc";
		commandsInput("mpc", "mpc", basePermission, 
				"/mpc [pagenumber]", "/mpc ", false,
				"<red>/mpc <white>| Infoseite für alle Befehle.",
				"<red>/mpc <white>| Info page for all commands.",
				"<aqua>Befehlsrecht für <white>/mpc",
				"<aqua>Commandright for <white>/mpc",
				"<yellow>Basisbefehl für das MPC Plugin.",
				"<yellow>Groundcommand for the MPC Plugin.");
		argumentInput("mpc_ignore", "ignore", basePermission,
				"/mpc ignore <playername>", "/mpc ignore ", false,
				"<red>/mpc ignore <Spielername> <white>| Setzt oder entfernt den Spieler auf die Liste der ignorierten Spieler.",
				"<red>/mpc ignore <playername> <white>| Adds or removes the player to the ignored players list.",
				"<aqua>Befehlsrecht für <white>/mpc ignore",
				"<aqua>Commandright for <white>/mpc ignore",
				"<yellow>Setzt oder entfernt den Spieler auf die Liste der ignorierten Spieler.",
				"<yellow>Adds or removes the player to the ignored players list.");
		argumentInput("mpc_listignore", "listignore", basePermission,
				"/mpc listignore [pagenumber]", "/mpc listignore ", false,
				"<red>/mpc listignore [Seitenzahl] <white>| Listet alle ignorierten Spieler auf.",
				"<red>/mpc listignore [pagenumber] <white>| Lists all ignored players.",
				"<aqua>Befehlsrecht für <white>/mpc listignore",
				"<aqua>Commandright for <white>/mpc listignore",
				"<yellow>Listet alle ignorierten Spieler auf.",
				"<yellow>Lists all ignored players.");
		
		basePermission = "email.cmd.email";
		commandsInput("email", "email", basePermission, 
				"/email [pagenumber]", "/email ", false,
				"<red>/email <white>| Listet alle eingegangen Emails auf.",
				"<red>/email <white>| ",
				"<aqua>Befehlsrecht für <white>/email",
				"<aqua>Commandright for <white>/email",
				"<yellow>Listet alle eingegangen Emails auf.",
				"<yellow>");
		argumentInput("email_send", "send", basePermission,
				"/email send <playername, multiple player with @ as seperator> <subject> <message>", "/email send ", false,
				"<red>/email send <Spielername, mehrfache Spieler mit @ seperieren> <Betreff> <Nachricht> <white>| Sendet eine E-mail.",
				"<red>/email send <playername, multiple player with @ as seperator> <subject> <message> <white>| Send a email.",
				"<aqua>Befehlsrecht für <white>/email send",
				"<aqua>Commandright for <white>/email send",
				"<yellow>Sendet eine Email.",
				"<yellow>Send a email.");
		argumentInput("email_read", "read", basePermission,
				"/email read <mailid>", "/email read ", false,
				"<red>/email read <mailid> <white>| Liest eine E-Mail.",
				"<red>/email read <mailid> <white>| Read a email.",
				"<aqua>Befehlsrecht für <white>/email read",
				"<aqua>Commandright for <white>/email read",
				"<yellow>Liest eine E-Mail.",
				"<yellow>Read a email.");
		argumentInput("email_delete", "delete", basePermission,
				"/email delete <mailid>", "/email delete ", false,
				"<red>/email delete <mailid> <white>| Löscht eine E-Mail.",
				"<red>/email delete <mailid> <white>| Delete a email.",
				"<aqua>Befehlsrecht für <white>/email delete",
				"<aqua>Commandright for <white>/email delete",
				"<yellow>Löscht eine E-Mail.",
				"<yellow>Delete a email.");
		argumentInput("email_outgoingmail", "outgoingmail", basePermission,
				"/email outgoingmail [pagenumber]", "/email outgoingmail ", false,
				"<red>/email outgoingmail [Seitennumber] <white>| Listet alle selbst gesendeten E-Mails auf.",
				"<red>/email outgoingmail [pagenumber] <white>| Lists all self sended emails.",
				"<aqua>Befehlsrecht für <white>/email outgoingmail",
				"<aqua>Commandright for <white>/email outgoingmail",
				"<yellow>Listet alle selbst gesendeten E-Mails auf.",
				"<yellow>Lists all self sended emails.");
		
		basePermission =  "emails.cmd.emails";
		commandsInput("emails", "emails", basePermission, 
				"/emails <playername> [Pagenumber]", "/emails ", false,
				"<red>/emails <Spielername> [Seitenzahl] <white>| Listet alle eingegangenen E-Mails des Spielers auf.",
				"<red>/emails <playername> [Pagenumber] <white>| Lists all incoming emails for the player.",
				"<aqua>Befehlsrecht für <white>/emails",
				"<aqua>Commandright for <white>/emails",
				"<yellow>Listet alle eingegangenen E-Mails des Spielers auf.",
				"<yellow>Lists all incoming emails for the player.");
		argumentInput("emails_outgoingmail", "outgoingmail", basePermission,
				"/emails outgoingmail <playername> [pagenumber]", "/email outgoingmail ", false,
				"<red>/emails outgoingmail <playername> [Seitennumber] <white>| Listet alle vom Spieler gesendeten E-Mails auf.",
				"<red>/emails outgoingmail <playername> [pagenumber] <white>| Lists all emails sent by the player.",
				"<aqua>Befehlsrecht für <white>/emails outgoingmail",
				"<aqua>Commandright for <white>/emails outgoingmail",
				"<yellow>Listet alle vom Spieler gesendeten E-Mails auf.",
				"<yellow>Lists all emails sent by the player.");
		
		basePermission =  "pmail.cmd.pmail";
		commandsInput("pmail", "pmail", basePermission, 
				"/pmail [pagenumber]", "/pmail ", false,
				"<red>/pmail [Seitenzahl] <white>| Listet alle geöffneten P-Mails auf.",
				"<red>/pmail [pagenumber] <white>| Lists all opened pmails.",
				"<aqua>Befehlsrecht für <white>/pmail",
				"<aqua>Commandright for <white>/pmail",
				"<yellow>Listet alle geöffneten P-Mails auf.",
				"<yellow>Lists all opened pmails.");
		argumentInput("pmail_write", "write", basePermission,
				"/pmail write <playername> <subject> <message>", "/pmail write ", false,
				"<red>/pmail write <Spielername> <Betreff> <Nachricht> <white>| Schreibt einen Brief und legt den in dein Inventar.",
				"<red>/pmail write <playername> <subject> <message> <white>| Write a letter and put it in your inventory.",
				"<aqua>Befehlsrecht für <white>/pmail write",
				"<aqua>Commandright for <white>/pmail write",
				"<yellow>Schreibt einen Brief und legt den in dein Inventar.",
				"<yellow>Write a letter and put it in your inventory.");
		argumentInput("pmail_send", "send", basePermission,
				"/pmail send", "/pmail send ", false,
				"<red>/pmail send <white>| Sendet den Brief in der Hand an den Empfänger.",
				"<red>/pmail send <white>| Send the letter in hand to the recipient.",
				"<aqua>Befehlsrecht für <white>/pmail send",
				"<aqua>Commandright for <white>/pmail send",
				"<yellow>Sendet den Brief in der Hand an den Empfänger.",
				"<yellow>Send the letter in hand to the recipient.");
		argumentInput("pmail_open", "open", basePermission,
				"/pmail open", "/pmail open ", false,
				"<red>/pmail open <white>| Öffnet den Brief in der Hand. Legt den Inhalt in den Chat und regestriert es in der Datenbank. Löscht das Item.",
				"<red>/pmail open <white>| Open the letter in your hand. Put the contents in the chat and register it in the database. Delete the item.",
				"<aqua>Befehlsrecht für <white>/pmail open",
				"<aqua>Commandright for <white>/pmail open",
				"<yellow>Öffnet den Brief in der Hand. Legt den Inhalt in den Chat und regestriert es in der Datenbank. Löscht das Item.",
				"<yellow>Open the letter in your hand. Put the contents in the chat and register it in the database. Delete the item.");
		argumentInput("pmail_silentopen", "silentopen", basePermission,
				"/pmail silentopen", "/pmail silentopen ", false,
				"<red>/pmail silentopen <white>| Öffnet einen Brief in der Hand obwohl man nicht der Empfänger ist. Legt den Inhalt in den Chat aber setzt nichts in die Datenbank.",
				"<red>/pmail silentopen <white>| Opens a letter in your hand even though you are not the recipient. Puts the content in the chat but does not put anything into the database.",
				"<aqua>Befehlsrecht für <white>/pmail silentopen",
				"<aqua>Commandright for <white>/pmail silentopen",
				"<yellow>Öffnet einen Brief in der Hand obwohl man nicht der Empfänger ist. Legt den Inhalt in den Chat aber setzt nichts in die Datenbank.",
				"<yellow>Opens a letter in your hand even though you are not the recipient. Puts the content in the chat but does not put anything into the database.");
		argumentInput("pmail_read", "read", basePermission,
				"/pmail read <mailid>", "/pmail read ", false,
				"<red>/pmail read <MailId> <white>| Liest die PMail aus der Datenbank. Gilt nur für zugestellte und geöffnete PMails.",
				"<red>/pmail read <mailid> <white>| Reads the PMail from the database. Only applies to delivered and opened PMails.",
				"<aqua>Befehlsrecht für <white>/pmail read",
				"<aqua>Commandright for <white>/pmail read",
				"<yellow>Liest die PMail aus der Datenbank. Gilt nur für zugestellte und geöffnete PMails.",
				"<yellow>Reads the PMail from the database. Only applies to delivered and opened PMails.");
		argumentInput("pmail_delete", "delete", basePermission,
				"/pmail delete <mailid>", "/pmail delete ", false,
				"<red>/pmail delete <MailId> <white>| Löscht die PMail.",
				"<red>/pmail delete <mailid> <white>| Delete the pmail.",
				"<aqua>Befehlsrecht für <white>/pmail delete",
				"<aqua>Commandright for <white>/pmail delete",
				"<yellow>Löscht die PMail.",
				"<yellow>Delete the pmail.");
		argumentInput("pmail_outgoingmail", "outgoingmail", basePermission,
				"/pmail outgoingmail [pagenumber]", "/pmail outgoingmail ", false,
				"<red>/pmail outgoingmail [Seitenzahl] <white>| Listet alle gesendeten PMails auf.",
				"<red>/pmail outgoingmail [pagenumber] <white>| Lists all sended pmail.",
				"<aqua>Befehlsrecht für <white>/pmail outgoingmail",
				"<aqua>Commandright for <white>/pmail outgoingmail",
				"<yellow>Listet alle gesendeten PMails auf.",
				"<yellow>Lists all sended pmail.");
		argumentInput("pmail_deliverincomingmail", "deliverincomingmail", basePermission,
				"/pmail deliverincomingmail <playername>", "/pmail deliverincomingmail ", false,
				"<red>/pmail deliverincomingmail <Spielername> <white>| PMails welche in der Zustellung sind, werden dem online Spieler sofort ans Inventar zugestellt. Bei vollem Inventar droppen die Pmails.",
				"<red>/pmail deliverincomingmail <playername> <white>| PMails that are being delivered are immediately delivered to the online player's inventory. The PMails drop when the inventory is full.",
				"<aqua>Befehlsrecht für <white>/pmail deliverincomingmail",
				"<aqua>Commandright for <white>/pmail deliverincomingmail",
				"<yellow>PMails welche in der Zustellung sind, werden dem online Spieler sofort ans Inventar zugestellt. Bei vollem Inventar droppen die Pmails.",
				"<yellow>PMails that are being delivered are immediately delivered to the online player's inventory. The PMails drop when the inventory is full.");
		
		basePermission =  "pmails.cmd.pmails";
		commandsInput("pmails", "pmails", basePermission, 
				"/pmails <playername> [pagenumber]", "/pmails ", false,
				"<red>/pmails <Spielername> [Seitenzahl] <white>| Listet alle geöffneten P-Mails des Spielers auf.",
				"<red>/pmails <playername> [pagenumber] <white>| Lists all opened of the player pmails.",
				"<aqua>Befehlsrecht für <white>/pmails",
				"<aqua>Commandright for <white>/pmails",
				"<yellow>Listet alle geöffneten P-Mails des Spielers auf.",
				"<yellow>Lists all opened of the players pmails.");
		argumentInput("pmails_outgoingmail", "outgoingmail", basePermission,
				"/pmails outgoingmail <playername> [pagenumber]", "/pmail outgoingmail ", false,
				"<red>/pmails outgoingmail <Spielername> [Seitenzahl] <white>| Listet alle gesendeten PMails auf.",
				"<red>/pmails outgoingmail <playername> [pagenumber] <white>| Lists all sended pmail.",
				"<aqua>Befehlsrecht für <white>/pmail outgoingmail",
				"<aqua>Commandright for <white>/pmail outgoingmail",
				"<yellow>Listet alle gesendeten PMails auf.",
				"<yellow>Lists all sended pmail.");
	
		basePermission =  "mailbox.cmd.mailbox";
		commandsInput("mailbox", "mailbox", basePermission, 
				"/mailbox [[-noowner] | [-cansend] | [-override]]", "/mailbox ", false,
				"<red>/mailbox [[-noowner] | [-cansend] | [-override]] <white>| Erstellt MailBox. -noowner, ohne Eigentümer. -cansend, kann PMails versenden. -override, überschreibt die eigene alte MailBox auf die neue Position.",
				"<red>/mailbox [[-noowner] | [-cansend] | [-override]] <white>| Creates MailBox. -noowner, without owner. -cansend, can send PMails. -override, overwrites the old MailBox to the new position.",
				"<aqua>Befehlsrecht für <white>/mailbox",
				"<aqua>Commandright for <white>/mailbox",
				"<yellow>Erstellt MailBox. -noowner, ohne Eigentümer. -cansend, kann PMails versenden. -override, überschreibt die eigene alte MailBox auf die neue Position.",
				"<yellow>Creates MailBox. -noowner, without owner. -cansend, can send PMails. -override, overwrites the old MailBox to the new position.");
		
		basePermission =  "mailboxs.cmd.mailboxs";
		commandsInput("mailboxs", "mailboxs", basePermission, 
				"/mailboxs [pagenumber]", "/mailboxs ", false,
				"<red>/mailboxs [Seitenzahl] <white>| Listet alle existierenden Mailboxen auf.",
				"<red>/mailboxs [pagenumber] <white>| Lists all existing mailboxes.",
				"<aqua>Befehlsrecht für <white>/mailboxs",
				"<aqua>Commandright for <white>/mailboxs",
				"<yellow>Listet alle existierenden Mailboxen auf.",
				"<yellow>Lists all existing mailboxes.");
		argumentInput("mailboxs_delete", "delete", basePermission,
				"/mailboxs delete <playerrname/id>", "/mailboxs delete ", false,
				"<red>/mailboxs delete <Spielername/Id> <white>| Löscht die MailBox.",
				"<red>/mailboxs delete <playerrname/id> <white>| Delete the mailbox.",
				"<aqua>Befehlsrecht für <white>/mailboxs delete",
				"<aqua>Commandright for <white>/mailboxs delete",
				"<yellow>Löscht die MailBox.",
				"<yellow>Delete the mailbox.");
		argumentInput("mailboxs_info", "info", basePermission,
				"/mailboxs info <playerrname/id>", "/mailboxs info ", false,
				"<red>/mailboxs info <Spielername/Id> <white>| Zeigt Information zu der MailBox an.",
				"<red>/mailboxs info <playerrname/id> <white>| Displays information about the mailbox.",
				"<aqua>Befehlsrecht für <white>/mailboxs info",
				"<aqua>Commandright for <white>/mailboxs info",
				"<yellow>Zeigt Information zu der MailBox an.",
				"<yellow>Displays information about the mailbox.");
		
		basePermission =  "parcel.cmd.parcel";
		commandsInput("parcel", "parcel", basePermission, 
				"/parcel", "/parcel ", false,
				"<red>/parcel <white>| Erhält bis zu 10 Parcels, welche sich in Zustellung befinden.",
				"<red>/parcel <white>| Receives up to 10 parcels that are in delivery.",
				"<aqua>Befehlsrecht für <white>/parcel",
				"<aqua>Commandright for <white>/parcel",
				"<yellow>Erhält bis zu 10 Parcels, welche sich in Zustellung befinden.",
				"<yellow>Receives up to 10 parcels that are in delivery.");
		argumentInput("parcel_send", "send", basePermission,
				"/parcel send <playername> <subject>", "/parcel send ", false,
				"<red>/parcel send <Spielername> <Betreff> <white>| Öffnet ein Gui, wo der Sender alle Items hineinlegen kann. Welche beim Schließen der Gui abgesendet werden.",
				"<red>/parcel send <playername> <subject> <white>| Opens a GUI where the sender can put all items. Which are sent when the GUI is closed.",
				"<aqua>Befehlsrecht für <white>/parcel send",
				"<aqua>Commandright for <white>/parcel send",
				"<yellow>Öffnet ein Gui, wo der Sender alle Items hineinlegen kann. Welche beim Schließen der Gui abgesendet werden.",
				"<yellow>Opens a GUI where the sender can put all items. Which are sent when the GUI is closed.");
		argumentInput("parcel_pack", "pack", basePermission,
				"/parcel pack <playername> <subject>", "/parcel pack ", false,
				"<red>/parcel pack <Spielername> <Betreff> <white>| Speichert den Empfänger und den Betreff für die nächste Sendung über eine Mailbox.",
				"<red>/parcel pack <playername> <subject> <white>| Saves the recipient and subject for the next transmission via a mailbox.",
				"<aqua>Befehlsrecht für <white>/parcel pack",
				"<aqua>Commandright for <white>/parcel pack",
				"<yellow>Speichert den Empfänger und den Betreff für die nächste Sendung über eine Mailbox.",
				"<yellow>Saves the recipient and subject for the next transmission via a mailbox.");
	}
	
	private void comBypass() //INFO:ComBypass
	{
		List<Bypass.Permission> list = new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class));
		for(Bypass.Permission ept : list)
		{
			commandsKeys.put("Bypass."+ept.toString()
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
			commandsKeys.put("Count."+ept.toString()
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
						"<red>The player <white>%player% <red>does not exist!"}));
		languageKeys.put("PlayerDontOnline",
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler <white>%player% <red>ist nicht online!",
						"<red>The player <white>%player% <red>is not online!"}));
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
		languageKeys.put("AccountDontExist", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Account existiert nicht!",
						"<red>The account dont exist!"}));
		initMPCLang();
		initEMailLang();
		initPMailLang();
		initMailBoxLang();
		initParcel();
	}
	
	private void initMPCLang()
	{
		String path = "MPC.";
		languageKeys.put(path+"Ignore.Ignore", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du ignorierst nun den Spieler <white>%player%<yellow>!",
						"<yellow>You are now ignoring the player <white>%player%<yellow>!"}));
		languageKeys.put(path+"Ignore.DontIgnore", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du ignorierst nun nicht mehr den Spieler <white>%player%<yellow>!",
						"<yellow>You no longer ignore the player <white>%player%<yellow>!"}));
		languageKeys.put(path+"ListIgnore.YouIgnoreNoOne", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keinen auf deiner Liste, welchen du ignorierst!",
						"<red>You do not have anyone on your list that you ignore!"}));
		languageKeys.put(path+"ListIgnore.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>IgnorierListe von %player% <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>Ignorelist of %player% <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"ListIgnore.Context", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<click:run_command:'%mailignore%%player%'><yellow>%player%</yellow></click><white>",
						"<click:run_command:'%mailignore%%player%'><yellow>%player%</yellow></click><white>"}));
	}
	
	private void initEMailLang()
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
						"<red>Nachricht:",
						"%message%",
						"<gray>==============================",
						"<gray>==============================",
						"<red>Sender: <white>%sender%",
						"<red>Receiver: <white>%receiver%",
						"<red>Timestamp: <white>%time%",
						"<red>Subject: <reset>%subject%",
						"<red>Message:",
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
		languageKeys.put(path+"Send.SendToYourself", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du kannst keine Nachricht an dich selber senden!",
						"<red>You cannot send a message to yourself!"}));
		languageKeys.put(path+"Send.PlayerIgnoresYou", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler %player% ignoriert deine Emails und wird keine von dir erhalten können!",
						"<red>The player %player% is ignoring your sent emails and will not be able to receive any from you!"}));
		languageKeys.put(path+"Send.NoValidReceiver", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keinen Empfänger angegeben oder diese ignorieren dich!",
						"<red>You have not specified a recipient or they are ignoring you!"}));
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
		languageKeys.put(path+"SendedHasCosts", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Versandkosten betrugen %money%",
						"<yellow>Shipping costs amounted to %money%"}));
		
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
	
	private void initPMailLang()
	{
		String path = "PMail.";
		languageKeys.put(path+"HasNoIncomingPMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keine eingegangenen P-Mails.",
						"<red>You have no incoming pmails."}));
		languageKeys.put(path+"HasNoOutgoingPMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keine versendeten PMails.",
						"<red>You have no outgoing pmails."}));
		languageKeys.put(path+"YourAreNotThePMailOwner", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Diese PMail gehört dir nicht!",
						"<red>This PMail dont belong to you!"}));
		languageKeys.put(path+"PMailDontExist", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Diese PMail existiert nicht!",
						"<red>This PMail dont exist!"}));
		languageKeys.put(path+"Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>P-Mails <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>P-Mails <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"ShowMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>Von %sender%'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%pmailread%%mailid%'><gray>[</gray><yellow>Lesen</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmailwrite%%sender% re:%subject%'><gray>[</gray><aqua>Antworten</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>",
						
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>From %sender%'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%pmailread%%mailid%'><gray>[</gray><yellow>Read</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmailwrite%%sender% re:%subject%'><gray>[</gray><aqua>Answere</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>"}));
		languageKeys.put(path+"TimeFormat", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"dd.MM-HH:mm",
						"dd.MM-HH:mm"}));
		languageKeys.put(path+"Write.Displayname", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Brief für <white>%player% <gold>- <white>%subject%",
						"<red>Letter for <white>%player% <gold>- <white>%subject%"}));
		languageKeys.put(path+"Write.NotEnoughMaterial", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast nicht Materialien um die PMail zu schreiben!",
						"<red>You dont have not enough materials top write the pmail!"}));
		languageKeys.put(path+"Write.NotFreeSlot", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keinen freien Slot im Inventar um den Brief zu deponieren!",
						"<red>You do not have a free slot in your inventory to deposit the letter!"}));
		languageKeys.put(path+"Open.NotPMailOnItem", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Auf diesem Item ist kein Briefinhalt zu finden!",
						"<red>There is no letter content on this item!"}));
		languageKeys.put(path+"Open.Opened", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast die PMail des Spielers %players% mit dem Betreff %subject% <yellow>geöffnet!",
						"<yellow>You have opened the PMail of the player %players% with the subject %subject% <yellow>!"}));
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
						"<red>Nachricht:",
						"%message%",
						"<gray>==============================",
						"<gray>==============================",
						"<red>Sender: <white>%sender%",
						"<red>Receiver: <white>%receiver%",
						"<red>Timestamp: <white>%time%",
						"<red>Subject: <reset>%subject%",
						"<red>Message:",
						"%message%",
						"<gray>==============================",}));
		languageKeys.put(path+"Send.NotEnoughMoney", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast nicht Geld zum senden der P-Mail! Kosten: <white>%money%",
						"<red>You have not enough money to send the pmail! Costs:: <white>%money%"}));
		languageKeys.put(path+"Send.NothingInHandToSend", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast kein Brief in der Hand!",
						"<red>You dont have a letter in your hand!"}));
		languageKeys.put(path+"Send.MoneyCategory", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"P-Mail",
						"P-Mail"}));
		languageKeys.put(path+"Send.MoneyComment", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"P-Mail gesendet",
						"P-Mail sended"}));
		languageKeys.put(path+"Send.Sended", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>%players% <yellow>wude die P-Mail <white>%subject% <yellow>gesendet.",
						"<white>%players% <yellow>was send the P-Mail <white>%subject%<yellow>."}));
		languageKeys.put(path+"Send.HasPMail", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast %amount% neue P-Mails!</yellow> <click:run_command:'%pmail%'><gray>[</gray><yellow>Einsehen</yellow><gray>]</gray></click>",
						"<yellow>You have %amount% new pMails!</yellow> <click:run_command:'%pmail%'><gray>[</gray><yellow>View</yellow><gray>]</gray></click>"}));
		languageKeys.put(path+"Delete.Deleted", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast die P-Mail gelöscht!",
						"<red>You deleted the pmail!"}));
		languageKeys.put(path+"OutGoingMail.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>Gesendete P-Mails <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>Sended P-Mails <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"OutGoingMail.ShowMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>An %receiver%<newline><red>Wurde gelesen:</red> <white>%wasread%</white>'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%pmailread%%mailid%'><gray>[</gray><yellow>Lesen</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmailwrite%%receiver% re:%subject%'><gray>[</gray><aqua>Antworten</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>",
						
						"<gray>[%time%]</gray> <hover:show_text:'<yellow>To %receiver%<newline><red>Wurde gelesen:</red> <white>%wasread%</white>'><white>%subjectdisplay%</hover> "
						+ "<click:run_command:'%pmailread%%mailid%'><gray>[</gray><yellow>Read</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmailwrite%%receiver% re:%subject%'><gray>[</gray><aqua>Answere</aqua><gray>]</gray></click> "
						+ "<click:suggest_command:'%pmaildelete%%mailid%'><gray>[</gray><red>X</red><gray>]</gray></click>"}));
		languageKeys.put(path+"Send.HasPMail", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast %amount% P-Mails!</yellow>",
						"<yellow>You have %amount% pmails!</yellow>"}));
		languageKeys.put(path+"SendedHasCosts", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Versandkosten betrugen %money%",
						"<yellow>Shipping costs amounted to %money%"}));
		
		path = "PMails.";
		languageKeys.put(path+"HasNoIncomingPMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler hat keine eingegangenen P-Mails.",
						"<red>The player have no incoming p-mails."}));
		languageKeys.put(path+"HasNoOutgoingPMails", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der Spieler hat keine versendeten P-Mails.",
						"<red>The player have no outgoing p-mails."}));
		languageKeys.put(path+"Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>P-Mails von <white>%player% Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>P-Mails from <white>%player% Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"OutGoingMail.Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>Gesendete P-Mails von <white>%player% Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>Sendet P-Mails from <white>%player% Page %page%<gray>]<yellow>====="}));
	}
	
	private void initMailBoxLang()
	{
		String path = "MailBox.";
		languageKeys.put(path+"CannotDeleteMailBoxWithoutAOwner", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du kannst keine MailBox löschen, die keinem gehört!",
						"<red>You cannot delete a mailbox that does not belong to anyone!"}));
		languageKeys.put(path+"CannotDeleteMailBoxOtherPlayers", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du kannst keine MailBox löschen, die einem anderen Spieler gehört!",
						"<red>You cannot delete a MailBox that belongs to another player!"}));
		languageKeys.put(path+"LookingIntoTheAir", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du schaust gerade in die Luft!",
						"<red>You looking into air!"}));
		languageKeys.put(path+"BlockIsNotAChest", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Der angeschaute Block ist keine Kiste oder Redstonekiste!",
						"<red>The block you are looking at is not a chest or trappedchest!"}));
		languageKeys.put(path+"Deleted.YourOwn", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast deine MailBox gelöscht.",
						"<yellow>You have deleted your mailbox."}));
		languageKeys.put(path+"Deleted.Ownerless", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast eine MailBox ohne Eigentümer gelöscht.",
						"<yellow>You deleted a mailbox without an owner."}));
		languageKeys.put(path+"Deleted.OtherPlayers", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast die MailBox von %player% gelöscht.",
						"<yellow>You have deleted the mailbox of %player%."}));
		languageKeys.put(path+"Create.CannotCreateWithoutOwner", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du kannst keine MailBox ohne Eigentümer erstellen!",
						"<red>You cannot create a MailBox without an owner!"}));
		languageKeys.put(path+"Create.CannotCreateWhichCanSend", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du kannst keine MailBox erstellen, die gleichzeitig auch versenden kann!",
						"<red>You cannot create a MailBox that can also send!"}));
		languageKeys.put(path+"Create.WithoutOwner", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast eine MailBox erstellt, welche keinen Eigentümer hat!",
						"<yellow>You have created a MailBox which has no owner!"}));
		languageKeys.put(path+"Create.HaveAlreadyAMailBox", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast schon eine MailBox!",
						"<red>You already have a mailbox!"}));
		languageKeys.put(path+"Create.Override", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Die Position deiner MailBox wurden umgesetzt und die Eigenschaften überschrieben.",
						"<yellow>The position of your MailBox has been moved and the properties overwritten."}));
		languageKeys.put(path+"Create.YourOwn", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast eine MailBox erstellt!",
						"<yellow>You have created a MailBox!"}));
		
		path = "MailBoxs.";
		languageKeys.put(path+"NoMailBoxes", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Es existieren keine MailBoxen!",
						"<red>There are no mailboxes!"}));
		languageKeys.put(path+"Headline", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>=====<gray>[<gold>MailBoxes <white>Seite %page%<gray>]<yellow>=====",
						"<yellow>=====<gray>[<gold>MailBoxes <white>Page %page%<gray>]<yellow>====="}));
		languageKeys.put(path+"Show", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<white>%value%</white> "
						+ "<click:run_command:'%mailboxsinfo%%value%'><gray>[</gray><yellow>Info</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%mailboxsdelete%%value%'><gray>[</gray><red>X</red><gray>]</gray></click>",
						
						"<white>%value%</white> "
						+ "<click:run_command:'%mailboxsinfo%%value%'><gray>[</gray><yellow>Info</yellow><gray>]</gray></click> "
						+ "<click:suggest_command:'%mailboxsdelete%%value%'><gray>[</gray><red>X</red><gray>]</gray></click>"}));
		languageKeys.put(path+"Info", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<gray>=====<gold>MailBox</gold> <white>%id%</white><gray>=====",
						"<red>Eigentümer: <white>%owner%",
						"<red>Location: <white>%server% - %world% | %x% %y% %z%",
						"<red>Kann zum Versenden benutzt werden: <white>%canbeusedtosend%",
						"<gray>==============================",
						"<gray>=====<gold>MailBox</gold> <white>%id%</white><gray>=====",
						"<red>Owner: <white>%owner%",
						"<red>Location: <white>%server% - %world% | %x% %y% %z%",
						"<red>Can be used to send: <white>%canbeusedtosend%",
						"<gray>==============================",}));
	}
	
	private void initParcel()
	{
		String path = "Parcel.";
		languageKeys.put(path+"HasNoIncomingParcel", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keine eingegangenen Parcels.",
						"<red>You have no incoming parcels."}));
		languageKeys.put(path+"HasNoInfosForSending", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast keinen Empfänger hinterlegt um ein Parcel zu versenden!",
						"<red>You have not specified a recipient to send a parcel!"}));
		languageKeys.put(path+"InventarTitle", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"§6An §7%player% §0- §r%subject%",
						"§6To §7%player% §0- §r%subject%"}));
		languageKeys.put(path+"Sended", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Parcel gesendet an <white>%player% <yellow>mit dem Betreff <reset>%subject%",
						"<yellow>Parcel sended to <white>%player% <yellow>with the subject <reset>%subject%"}));
		languageKeys.put(path+"SendedHasCosts", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Versandkosten betrugen %money%",
						"<yellow>Shipping costs amounted to %money%"}));
		languageKeys.put(path+"Write.Displayname", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Parcel für <white>%player% <gold>- <white>%subject%",
						"<red>Parcel for <white>%player% <gold>- <white>%subject%"}));
		languageKeys.put(path+"Open.Opened", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast das Parcel geöffnet!",
						"<yellow>You have opened the Parcel!"}));
		languageKeys.put(path+"ReceivedIncomingParcel", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Dir wurden %amount% Parcels direkt zugestellt.",
						"<yellow>%amount% parcels were delivered directly to you."}));
		languageKeys.put(path+"NotEnoughMoney", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<red>Du hast nicht Geld zum senden der Parcel! Kosten: <white>%money%",
						"<red>You have not enough money to send the parcel! Costs: <white>%money%"}));
		languageKeys.put(path+"HasPack", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast die Empfängerdaten aufgeschrieben. Nun versende das Parcel mit der leeren Hand und einem Rechtsklick auf eine MailBox.",
						"<yellow>You have written down the recipient data. Now send the parcel with the empty hand and right-click on a MailBox."}));
		languageKeys.put(path+"HasParcel", 
				new Language(new ISO639_2B[] {ISO639_2B.GER, ISO639_2B.ENG}, new Object[] {
						"<yellow>Du hast %amount% Parcel!",
						"<yellow>You have %amount% Parcel!"}));
		
	}
	
	private void initModifierValueEntryLanguage() //INFO:BonusMalusLanguages
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