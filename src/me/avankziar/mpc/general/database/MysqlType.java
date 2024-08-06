package me.avankziar.mpc.general.database;

import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.general.objects.IgnoreSender;
import me.avankziar.mpc.general.objects.MailBox;
import me.avankziar.mpc.general.objects.PMail;
import me.avankziar.mpc.general.objects.PlayerData;

public enum MysqlType
{
	PLAYERDATA("mpcPlayerData", new PlayerData(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " player_uuid char(36) NOT NULL UNIQUE,"
			+ " player_name text);"),
	EMAIL("mpcEMail", new EMail(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " subject_matter text,"
			+ " message_content text,"
			+ " mail_owner char(36) NOT NULL,"
			+ " mail_sender text,"
			+ " mail_receiver char(36) NOT NULL,"
			+ " has_receiver_readed boolean,"
			+ " sending_date bigint);"),
	IGNORE_SENDER("mpcIgnoreSender", new IgnoreSender(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " mail_receiver char(36) NOT NULL,"
			+ " mail_sender char(36) NOT NULL);"),
	PMAIL("mpcPMail", new PMail(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " subject_matter text,"
			+ " message_content text,"
			+ " mail_owner char(36) NOT NULL,"
			+ " mail_sender text,"
			+ " mail_receiver char(36) NOT NULL,"
			+ " has_receiver_readed boolean,"
			+ " sending_date bigint,"
			+ " will_be_delivered boolean);"),
	MAILBOX("mpcMailBox", new MailBox(), ServerType.SPIGOT,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " box_owner text,"
			+ " box_server text,"
			+ " box_world text,"
			+ " box_x int,"
			+ " box_y int,"
			+ " box_z int,"
			+ " can_be_used_for_sending boolean);")
	;
	
	private MysqlType(String tableName, Object object, ServerType usedOnServer, String setupQuery)
	{
		this.tableName = tableName;
		this.object = object;
		this.usedOnServer = usedOnServer;
		this.setupQuery = setupQuery.replace("%%tablename%%", tableName);
	}
	
	private final String tableName;
	private final Object object;
	private final ServerType usedOnServer;
	private final String setupQuery;

	public String getValue()
	{
		return tableName;
	}
	
	public Object getObject()
	{
		return object;
	}
	
	public ServerType getUsedOnServer()
	{
		return usedOnServer;
	}
	
	public String getSetupQuery()
	{
		return setupQuery;
	}
}