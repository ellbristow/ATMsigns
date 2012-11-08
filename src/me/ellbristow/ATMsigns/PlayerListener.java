package me.ellbristow.ATMsigns;

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

public class PlayerListener implements Listener {

    private ATMsigns plugin;

    public PlayerListener(ATMsigns instance) {
        plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.isCancelled()) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType().name().equals("SIGN_POST") || event.getClickedBlock().getType().name().equals("WALL_SIGN"))) {
                Block block = event.getClickedBlock();
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(ChatColor.WHITE + "=== ATM ===")) {
                    // Player right-clicked an ATM sign
                    Player player = event.getPlayer();
                    if (player.hasPermission("atmsigns.use")) {
                        if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Deposit")) {
                            // DEPOSITING
                            plugin.depositItem(player, sign.getLine(3));
                        } else if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Withdraw")) {
                            // WITHDRAWING
                            plugin.withdrawItem(player, sign.getLine(3));
                        } else if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Balance")) {
                            // BALANCE
                            plugin.getBalance(player);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use ATM signs!");
                    }
                    event.setCancelled(true);
                }
            } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && (event.getClickedBlock().getType().name().equals("SIGN_POST") || event.getClickedBlock().getType().name().equals("WALL_SIGN"))) {
                Block block = event.getClickedBlock();
                Sign sign = (Sign) block.getState();
                Player player = event.getPlayer();
                if (sign.getLine(0).equals(ChatColor.WHITE + "=== ATM ===")) {
                    // Player left-clicked an ATM sign
                    if (player.hasPermission("atmsigns.use")) {
                        if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Deposit")) {
                            // Deposit, switch to Withdraw
                            sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.WHITE + " Withdraw");
                            sign.setLine(2, ChatColor.BLACK + " Balance ");
                            sign.update();
                        } else if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Withdraw")) {
                            // Withdraw, switch to Balance
                            sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.WHITE + " Balance ");
                            sign.setLine(2, " Deposit ");
                            sign.update();
                        } else if (sign.getLine(1).startsWith(ChatColor.GREEN + ">" + ChatColor.WHITE + " Balance")) {
                            // Balance, switch to Deposit
                            sign.setLine(1, ChatColor.GREEN + ">" + ChatColor.WHITE + " Deposit ");
                            sign.setLine(2, " Withdraw");
                            sign.update();
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use ATM signs!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
