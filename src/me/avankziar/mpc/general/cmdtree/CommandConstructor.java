package me.avankziar.mpc.general.cmdtree;

import java.util.ArrayList;

public class CommandConstructor extends BaseConstructor
{
	public ArrayList<ArgumentConstructor> subcommands;
    public ArrayList<String> tablist;

	public CommandConstructor(CommandSuggest.Type cst, String path, boolean canConsoleAccess, boolean putUpCmdPermToValueEntrySystem,
    		ArgumentConstructor...argumentConstructors)
    {
		super(
				getYamlHandling().getCommandString(path+".Name"),
				path,
				getYamlHandling().getCommandString(path+".Permission"),
				getYamlHandling().getCommandString(path+".Suggestion"),
				getYamlHandling().getCommandString(path+".CommandString"),
				getYamlHandling().getCommandString(path+".HelpInfo"),
				canConsoleAccess,
				putUpCmdPermToValueEntrySystem);
        this.subcommands = new ArrayList<>();
        this.tablist = new ArrayList<>();
        CommandSuggest.set(cst, this);
        for(ArgumentConstructor ac : argumentConstructors)
        {
        	CommandSuggest.set(ac.suggestType, ac);
        	this.subcommands.add(ac);
        	this.tablist.add(ac.getName());
        }
        getCommandTree().add(this);
    }
	
	public static CommandConstructor getCommandFromPath(String commandpath)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getPath().equalsIgnoreCase(commandpath))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}
	
	public static CommandConstructor getCommandFromCommandString(String command)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getName().equalsIgnoreCase(command))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}
}