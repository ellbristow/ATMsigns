package me.ellbristow.ATMsigns;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class signListener implements Listener {
	
	public static ATMsigns plugin;
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public signListener (ATMsigns instance) {
		
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onSignChange (SignChangeEvent event) {
		SignChangeEvent sign = event;
		Player player = event.getPlayer();
		if ((sign.getLine(0).equalsIgnoreCase(ChatColor.WHITE + "=== ATM ==="))) {
			// Player trying to manually create sign (possible permission bypass)
			player.sendMessage(ChatColor.RED + "Please use [atm] to make ATM signs!");
			event.setCancelled(true);
			return;
		}
		if (sign.getLine(0).equalsIgnoreCase("[atm]") && (player.hasPermission("atmsigns.create"))) {
			sign.setLine(0, ChatColor.WHITE + "=== ATM ===");
			sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.BLACK + " Deposit      ");
			sign.setLine(2, "  Withdraw     ");
			sign.setLine(3, "  Balance      ");
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onBlockBreak (BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getTypeId() == 63 || block.getTypeId() == 68) {
			Player player = event.getPlayer();
			Sign sign = (Sign) block.getState();
			if (sign.getLine(1) == ChatColor.WHITE + "=== ATM ===" && !player.hasPermission("atmsigns.destroy")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to break ATM signs!");
				event.setCancelled(true);
				sign.setLine(1, sign.getLine(1));
				sign.update();
			 }
		}
	}
}
