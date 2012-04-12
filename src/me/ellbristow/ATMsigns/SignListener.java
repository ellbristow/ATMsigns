package me.ellbristow.ATMsigns;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {
	
	private ATMsigns plugin;
	
	private static final Logger logger = Logger.getLogger("Minecraft");
	
	public SignListener (ATMsigns instance) {
		plugin = instance;
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onSignChange (SignChangeEvent event) {
		if (!event.isCancelled()) {
			SignChangeEvent sign = event;
			Player player = event.getPlayer();
			if ((sign.getLine(0).equalsIgnoreCase(ChatColor.WHITE + "=== ATM ==="))) {
				// Player trying to manually create sign (possible permission bypass)
				player.sendMessage(ChatColor.RED + "Please use [atm] to make ATM signs!");
				event.setCancelled(true);
				return;
			}
			if (sign.getLine(0).equalsIgnoreCase("[atm]") && (player.hasPermission("atmsigns.create"))) {
				if (sign.getLine(3).isEmpty()) {
					sign.setLine(3, ChatColor.GRAY + player.getName());
				}
				else if (!sign.getLine(3).equalsIgnoreCase(player.getName())) {
					OfflinePlayer target = plugin.getServer().getOfflinePlayer(sign.getLine(3).toString());
					if (target == null || !target.hasPlayedBefore()) {
						player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + sign.getLine(3).toString() + ChatColor.RED + " not found!");
						return;
					}
					sign.setLine(3, ChatColor.GRAY + target.getName());
				}
				else {
					sign.setLine(3, ChatColor.GRAY + player.getName());
				}
				sign.setLine(0, ChatColor.WHITE + "=== ATM ===");
				sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.WHITE + " Deposit ");
				sign.setLine(2, " Withdraw");
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onBlockBreak (BlockBreakEvent event) {
		if (!event.isCancelled()) {
			Block block = event.getBlock();
			if (block.getTypeId() == 63 || block.getTypeId() == 68) {
				Player player = event.getPlayer();
				Sign sign = (Sign) block.getState();
				if ((ChatColor.WHITE + "=== ATM ===").equals(sign.getLine(0)) && !player.hasPermission("atmsigns.break")) {
					if (!sign.getLine(3).equalsIgnoreCase(player.getName())) {
						player.sendMessage(ChatColor.RED + "You can't break that ATM sign, it's not yours!");
						event.setCancelled(true);
						sign.setLine(1, sign.getLine(1));
						sign.update();
					}
				}
			}
		}
	}
}
