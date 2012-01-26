package me.ellbristow.ATMsigns;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class playerListener implements Listener {

	public static ATMsigns plugin;
	public static Economy economy;
	
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public playerListener (ATMsigns instance, Economy eco) {
		economy = eco;
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onPlayerInteract (PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().name().equals("SIGN_POST") || event.getClickedBlock().getType().name().equals("WALL_SIGN"))) {
			Block block = event.getClickedBlock();
			Sign sign = (Sign) block.getState();
			if (sign.getLine(0).equals(ChatColor.WHITE + "=== ATM ===")) {
				// Player right-clicked an ATM sign
				Player player = event.getPlayer();
				if (player.hasPermission("atmsigns.use")) {
					if (sign.getLine(1).startsWith(ChatColor.GREEN + ">")) {
						// DEPOSITING
						ATMsigns.depositItem(player);
					}
					else if (sign.getLine(2).startsWith(ChatColor.GREEN + ">")) {
						// WITHDRAWING
						ATMsigns.withdrawItem(player);
					}
					else if (sign.getLine(3).startsWith(ChatColor.GREEN + ">")) {
						// BALANCE
						ATMsigns.getBalance(player);
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You do not have permission to use ATM signs!");
				}
				event.setCancelled(true);
			}
		}
		else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && (event.getClickedBlock().getType().name().equals("SIGN_POST") || event.getClickedBlock().getType().name().equals("WALL_SIGN"))) {
			Block block = event.getClickedBlock();
			Sign sign = (Sign) block.getState();
			Player player = event.getPlayer();
			if (sign.getLine(0).equals(ChatColor.WHITE + "=== ATM ===")) {
				// Player left-clicked an ATM sign
				if (player.hasPermission("atmsigns.use")) {
					if (sign.getLine(1).startsWith(ChatColor.GREEN + ">")) {
						// Deposit, switch to Withdraw
						sign.setLine(1, "  Deposit      ");
						sign.setLine(2, ChatColor.GREEN + ">" + ChatColor.BLACK + " Withdraw     ");
						sign.update();
					}
					else if (sign.getLine(2).startsWith(ChatColor.GREEN + ">")) {
						// Withdraw, switch to Balance
						sign.setLine(2, "  Withdraw     ");
						sign.setLine(3, ChatColor.GREEN + ">" + ChatColor.BLACK + " Balance      ");
						sign.update();
					}
					else if (sign.getLine(3).startsWith(ChatColor.GREEN + ">")) {
						// Balance, switch to Deposit
						sign.setLine(3, "  Balance      ");
						sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.BLACK + " Deposit      ");
						sign.update();
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You do not have permission to use ATM signs!");
					event.setCancelled(true);
				}
			}
		}
	}
}
