package me.avankziar.mpc.general.database;

import me.avankziar.mpc.general.objects.EMail;
import me.avankziar.mpc.general.objects.IgnoreSender;

public enum MysqlType
{
	EMAIL("mpcEMail", new EMail(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " subject_matter text,"
			+ " message_content text,"
			+ " mail_owner char(36) NOT NULL UNIQUE,"
			+ " mail_sender text,"
			+ " mail_receiver char(36) NOT NULL UNIQUE,"
			+ " has_receiver_readed boolean,"
			+ " sending_date bigint);"),
	IGNORE_SENDER("mpcEMail", new IgnoreSender(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " mail_receiver char(36) NOT NULL UNIQUE,"
			+ " mail_sender char(36) NOT NULL UNIQUE);");
	
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