package me.ellbristow.ATMsigns;

import java.text.DecimalFormat;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ATMsigns extends JavaPlugin {

    private Economy economy;
    private final SignListener blockListener = new SignListener();
    private final PlayerListener playerListener = new PlayerListener(this);
    private int item;
    private String itemName;
    private double currency;
    private int altItem1;
    private String altItem1Name;
    private double altItem1Curr;
    private int altItem2;
    private String altItem2Name;
    private double altItem2Curr;
    private double depositFee;
    private double withdrawFee;
    private boolean percentFee;
    private boolean feeToOwner;

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        if (initEconomy()) {
            getLogger().info("hooked in to Vault.");
            pm.registerEvents(blockListener, this);
            pm.registerEvents(playerListener, this);

            // writes all unset default options to disk and creates the config directory if not present
            getConfig().options().copyDefaults(true);
            saveConfig();

            item = getConfig().getInt("item", 266);
            itemName = getConfig().getString("item_name", "default");
            if (itemName.equalsIgnoreCase("default")) {
                itemName = Material.getMaterial(item).toString().replace("_", " ");
            }
            currency = getConfig().getDouble("currency", 1);
            altItem1 = getConfig().getInt("alt_item1", 999);
            if (altItem1 != 999) {
                altItem1Name = getConfig().getString("alt_item1_name", "default");
                if (altItem1Name.equalsIgnoreCase("default")) {
                    altItem1Name = Material.getMaterial(altItem1).toString().replace("_", " ");
                }
                altItem1Curr = getConfig().getDouble("alt_item1_curr", 0);
            }
            altItem2 = getConfig().getInt("alt_item2", 999);
            if (altItem2 != 999) {
                altItem2Name = getConfig().getString("alt_item2_name", "default");
                if (altItem2Name.equalsIgnoreCase("default")) {
                    altItem2Name = Material.getMaterial(altItem2).toString().replace("_", " ");
                }
                altItem2Curr = getConfig().getDouble("alt_item2_curr", 0);
            }
            depositFee = getConfig().getDouble("deposit_fee", 0);
            withdrawFee = getConfig().getDouble("withdraw_fee", 0);
            percentFee = getConfig().getBoolean("percentage_fee", false);
            feeToOwner = getConfig().getBoolean("fee_to_owner", true);

            boolean checkFailed = false;
            if (Material.getMaterial(item) == null) {
                getLogger().log(Level.SEVERE, "Item ID {0} not found!", item);
                getLogger().severe("will be disabled!");
                checkFailed = true;
            }
            if (altItem1 != 999 && Material.getMaterial(altItem1) == null) {
                getLogger().log(Level.SEVERE, "Item ID {0} not found!", altItem1);
                getLogger().severe("will be disabled!");
                checkFailed = true;
            }
            if (altItem2 != 999 && Material.getMaterial(altItem2) == null) {
                getLogger().log(Level.SEVERE, "Item ID {0} not found!", altItem2);
                getLogger().severe("will be disabled!");
                checkFailed = true;
            }
            if (checkFailed) {
                pm.disablePlugin(this);
            }
        } else {
            getLogger().severe("failed to link to Vault!");
            getLogger().severe("(Vault did not find an economy plugin)");
            pm.disablePlugin(this);
        }
    }

    public boolean initEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public void depositItem(Player player, String owner) {
        owner = ChatColor.stripColor(owner);
        if (player.getItemInHand().getTypeId() == item || player.getItemInHand().getTypeId() == altItem1 || player.getItemInHand().getTypeId() == altItem2) {
            // Correct item in hand
            int depItem = player.getItemInHand().getTypeId();
            int count = player.getItemInHand().getAmount();
            player.setItemInHand(new ItemStack(Material.AIR, 0));
            double total = 0;
            if (depItem == item) {
                economy.depositPlayer(player.getName(), currency * count);
                player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + itemName + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) currency * count).replace(".00", ""));
                total = currency * count;
            } else if (depItem == altItem1) {
                economy.depositPlayer(player.getName(), altItem1Curr * count);
                player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + altItem1Name + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem1Curr * count).replace(".00", ""));
                total = altItem1Curr * count;
            } else if (depItem == altItem2) {
                economy.depositPlayer(player.getName(), altItem2Curr * count);
                player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + altItem2Name + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem2Curr * count).replace(".00", ""));
                total = altItem2Curr * count;
            }
            if (depositFee != 0 && !owner.equals(player.getName())) {
                double fee;
                if (percentFee) {
                    fee = roundTwoDecimals(total / 100 * depositFee);
                } else {
                    fee = depositFee;
                }
                economy.withdrawPlayer(player.getName(), fee);
                player.sendMessage(ChatColor.GOLD + "Deposit Fee: " + ChatColor.WHITE + economy.format(fee));
                if (feeToOwner && !owner.equalsIgnoreCase("Server")) {
                    economy.depositPlayer(owner, fee);
                    if (getServer().getOfflinePlayer(owner).isOnline()) {
                        Player target = getServer().getPlayer(owner);
                        target.sendMessage(ChatColor.GOLD + "ATM fee of " + ChatColor.WHITE + economy.format(fee) + ChatColor.GOLD + " received from " + ChatColor.WHITE + player.getName());
                    }
                }
            }
        } else {
            // Wrong item in hand
            String alternatives = "";
            if (altItem1 != 999) {
                if (altItem2 != 999) {
                    alternatives += ChatColor.RED + ", " + ChatColor.WHITE;
                } else {
                    alternatives += ChatColor.RED + " or " + ChatColor.WHITE;
                }
                alternatives += altItem1Name;
            }
            if (altItem2 != 999) {
                alternatives += ChatColor.RED + " or " + ChatColor.WHITE + altItem2Name;
            }
            player.sendMessage(ChatColor.RED + "You can only deposit " + ChatColor.WHITE + itemName + alternatives + ChatColor.RED + "!");
        }
    }

    public void withdrawItem(Player player, String owner) {
        double fee = 0;
        int quant = 1;
        if (player.isSneaking()) {
            quant = 10;
        }
        if (withdrawFee != 0 && !owner.equals(player.getName())) {
            if (percentFee) {
                fee = roundTwoDecimals(quant * currency / 100 * withdrawFee);
            } else {
                fee = quant * withdrawFee;
            }
        }
        if (economy.has(player.getName(), (quant * currency) + fee)) {
            // Enough in account
            if (player.getItemInHand().getTypeId() == 0 || (player.getItemInHand().getTypeId() == item && player.getItemInHand().getAmount() < player.getItemInHand().getType().getMaxStackSize())) {
                player.setItemInHand(new ItemStack(item, player.getItemInHand().getAmount() + 1));
                economy.withdrawPlayer(player.getName(), (quant * currency) + fee);
                player.sendMessage(ChatColor.GREEN + "Withdrawn: " + ChatColor.GOLD + quant + " " + itemName + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) (quant * currency)).replace(".00", ""));
                if (fee != 0) {
                    player.sendMessage(ChatColor.GOLD + "Withdrawl Fee: " + ChatColor.WHITE + economy.format(fee));
                    if (feeToOwner && !owner.equalsIgnoreCase("Server")) {
                        economy.depositPlayer(owner, fee);
                        if (getServer().getOfflinePlayer(owner).isOnline()) {
                            getServer().getPlayer(owner).sendMessage(ChatColor.GOLD + "ATM fee of " + ChatColor.WHITE + fee + ChatColor.GOLD + " received from " + ChatColor.WHITE + player.getName());
                        }
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must have a free hand to withdraw " + ChatColor.WHITE + itemName + ChatColor.RED + "!");
            }
        } else {
            // Not enough in account
            player.sendMessage(ChatColor.RED + "Your balance is too low to withdraw " + quant + " " + ChatColor.WHITE + itemName + ChatColor.RED + "!");
            if (fee != 0) {
                player.sendMessage(ChatColor.RED + "Total required: " + ChatColor.WHITE + (quant * currency) + ChatColor.RED + " Fee: " + ChatColor.WHITE + fee + ChatColor.RED + " Total: " + ChatColor.WHITE + economy.format(((quant * currency) + fee)));
            }
        }
    }

    public void getBalance(Player player) {
        player.sendMessage(ChatColor.GREEN + "Balance: " + economy.format((double) economy.getBalance(player.getName())).replace(".00", ""));
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    public void onDisable() {
    }
}
