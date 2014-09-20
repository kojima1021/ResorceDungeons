package com.thekarura.bukkit.plugin.resorcedungeons.command;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thekarura.bukkit.plugin.resorcedungeons.ResorceDungeons;
import com.thekarura.bukkit.plugin.resorcedungeons.util.MessageFormats;

/**
 * リロード関連を扱います
 * 
 * @author karura
 */
public class ReloadCommand implements CommandExecutor {
	
	public static final Logger log = ResorceDungeons.log;
	private static final String logPrefix = ResorceDungeons.logPrefix;
	private static final String msgPrefix = ResorceDungeons.msgPrefix;
	
	private ResorceDungeons instance = ResorceDungeons.getInstance();
	
	public ReloadCommand(ResorceDungeons plugin){
		this.instance = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		MessageFormats format = new MessageFormats(instance);
		
		if (args.length == 0){
			
			sender.sendMessage(format.MessageFormat(instance.getConfig().getString("message.reload.no_args"), sender.getName()));
			return false;
			
		}
		
		if (args[0].equalsIgnoreCase("Reload")){
			
			instance.reloadConfig();
			sender.sendMessage(format.MessageFormat(instance.getConfig().getString("message.reload.reload"), sender.getName()));
			
		}
		
		if (args[0].equalsIgnoreCase("Disable")){
			
			if (!(sender instanceof Player)){
				log.info(logPrefix+"Plguinを停止しました。Reload時に戻ります。");
				instance.getPluginLoader().disablePlugin(instance);
				return true;
			} else {
				sender.sendMessage(format.MessageFormat(instance.getConfig().getString("message.reload.disable_console"), sender.getName()));
			}
			
		}
		
		return false;
	}

}
