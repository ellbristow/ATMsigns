package me.ellbristow.ATMsigns;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        Action action = event.getAction();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (action.equals(Action.RIGHT_CLICK_AIR) && player.getItemInHand().getTypeId() < 256) {
            block = player.getTargetBlock(null, 5);
            if (block != null && (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN_POST))) {
                action = Action.RIGHT_CLICK_BLOCK;
            }
        } else {
            if (event.isCancelled())
                return;
        }
        if (action.equals(Action.RIGHT_CLICK_BLOCK) && (block.getType().name().equals("SIGN_POST") || block.getType().name().equals("WALL_SIGN"))) {
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).equals(ChatColor.WHITE + "=== ATM ===")) {
                // Player right-clicked an ATM sign
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
        } else if (action.equals(Action.LEFT_CLICK_BLOCK) && (block.getType().name().equals("SIGN_POST") || block.getType().name().equals("WALL_SIGN"))) {
            Sign sign = (Sign) block.getState();
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
