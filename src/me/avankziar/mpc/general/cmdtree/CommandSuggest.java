package me.avankziar.mpc.general.cmdtree;

import java.util.LinkedHashMap;

public class CommandSuggest
{
	/**
	 * All Commands and their following arguments
	 */
	public enum Type 
	{
		MAIL,
		MAIL_IGNORE,
		MAIL_LISTIGNORE,
		
		EMAIL,
		EMAIL_READ,
		EMAIL_SEND,
		EMAIL_DELETE,
		EMAIL_OUTGOINGMAIL,
		
		EMAILS,
		EMAILS_OUTGOINGMAIL,
		
		PMAIL,
		PMAIL_WRITE,
		PMAIL_SEND,
		PMMAIL_OPEN,
		PMAIL_READ,
		PMAIL_SILENTOPEN,
		PMAIL_OUTGOINGMAIL
		;
	}
	
	public static LinkedHashMap<CommandSuggest.Type, BaseConstructor> map = new LinkedHashMap<>();
	
	public static void set(CommandSuggest.Type cst, BaseConstructor bc)
	{
		map.put(cst, bc);
	}
	
	public static BaseConstructor get(CommandSuggest.Type ces)
	{
		return map.get(ces);
	}
	
	public static String getCmdString(CommandSuggest.Type ces)
	{
		BaseConstructor bc = map.get(ces);
		return bc != null ? bc.getCommandString() : null;
	}
	
	
}
