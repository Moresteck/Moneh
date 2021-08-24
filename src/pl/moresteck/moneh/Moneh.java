package pl.moresteck.moneh;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.server.InventoryPlayer;

public class Moneh extends JavaPlugin {
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final File bankfile = new File(this.getDataFolder(), "plugins/Moneh/bank.json");
	private final File configfile = new File(this.getDataFolder(), "plugins/Moneh/config.json");

	private static Bank bank = new Bank();
	private static Config config = new Config();

	public void onEnable() {
		this.bankfile.getParentFile().mkdirs();
		this.loadConfig();
		this.loadBank();
	}

	public void loadBank() {
		try {
			bank = gson.fromJson(new String(Files.readAllBytes(this.bankfile.toPath()), "UTF-8"), Bank.class);
		} catch (Throwable t) {
			System.out.println("[Moneh] Failed to load bank data");
			t.printStackTrace();
		}
	}

	public void loadConfig() {
		try {
			config = gson.fromJson(new String(Files.readAllBytes(this.configfile.toPath()), "UTF-8"), Config.class);
		} catch (Throwable t) {
			System.out.println("[Moneh] Failed to load config");
			t.printStackTrace();
			config.defaultConfig();
			this.saveConfig();
		}
	}

	public void saveBank() {
		String json = gson.toJson(bank);
		try {
			Files.write(this.bankfile.toPath(), json.getBytes("UTF-8"));
		} catch (Throwable t) {
			System.out.println("[Moneh] Failed to save bank data! Dumping it in logs instead:");
			System.out.println(json);
			t.printStackTrace();
		}
	}

	public void saveConfig() {
		String json = gson.toJson(config);
		try {
			Files.write(this.configfile.toPath(), json.getBytes("UTF-8"));
		} catch (Throwable t) {
			System.out.println("[Moneh] Failed to save configuration! Dumping it in logs instead:");
			System.out.println(json);
			t.printStackTrace();
		}
	}

	public void onDisable() {
		this.saveBank();
		this.saveConfig();
	}

	public static Bank getBank() {
		return bank;
	}

	public static Config getConfig() {
		return config;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("moneh")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player p = (Player) sender;

					p.sendMessage("Your" + ChatColor.DARK_GREEN + " holdings" + ChatColor.WHITE + ":");

					ArrayList<AccountCurrencies> order = Moneh.getConfig().getCurrenciesInWeightOrder(new AccountCurrencies(bank.getHoldings(p.getName())));
					for (AccountCurrencies accur : order) {
						String key = accur.getCurrenciesMap().keySet().iterator().next();
						p.sendMessage(" " + config.getCurrencyColor(key) + "(" + key + ") " + config.getCurrencyAlias(key) + ChatColor.GRAY + ": " + ChatColor.WHITE + Integer.toString(accur.getCurrenciesMap().get(key)));
					}
				} else {
					CommandHelp.HELP.showHelp(sender);
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("send")) {
					CommandHelp.PAY.showHelp(sender);
				} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					CommandHelp.HELP.showHelp(sender);
				} else if (args[0].equalsIgnoreCase("top")) {
					CommandHelp.TOP.showHelp(sender);
				} else {
					String holdings = bank.getHoldings(args[0]);
					if (holdings == null) {
						sender.sendMessage(ChatColor.RED + "There is no bank account for username \"" + args[0] + "\".");
					} else {
						sender.sendMessage(ChatColor.DARK_GREEN + "Holdings of " + ChatColor.WHITE + args[0] + ":");

						ArrayList<AccountCurrencies> order = Moneh.getConfig().getCurrenciesInWeightOrder(new AccountCurrencies(holdings));
						for (AccountCurrencies accur : order) {
							String key = accur.getCurrenciesMap().keySet().iterator().next();
							sender.sendMessage(" " + config.getCurrencyColor(key) + "(" + key + ") " + config.getCurrencyAlias(key) + ChatColor.GRAY + ": " + ChatColor.WHITE + Integer.toString(accur.getCurrenciesMap().get(key)));
						}
					}
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("send")) {
					CommandHelp.PAY.showHelp(sender);
				} else if (args[0].equalsIgnoreCase("top")) {

					int page = -1;
					try {
						page = Integer.parseInt(args[1]);
					} catch (Throwable t) {
						sender.sendMessage(ChatColor.RED + "\"" + args[1] + "\" is not an integer");
						CommandHelp.TOP.showHelp(sender);
						return true;
					}

					ArrayList<AccountCurrencies> top = bank.getTop();
					if (top.size() < page*10-10) {
						sender.sendMessage(ChatColor.RED + "Page " + args[1] + " does not exist");
						return true;
					}

					int max = page*10;
					if (top.size() < max) {
						max = top.size();
					}

					sender.sendMessage(ChatColor.DARK_GREEN + "Top players " + ChatColor.WHITE + "(page " + page + " / " + ((int)(max/10)+1) + ")" + ChatColor.GRAY + ":");

					for (int i = page*10-10; i < max; i++) {
						AccountCurrencies acc = top.get(i);

						// 1 currency per AccountCurrencies object
						ArrayList<AccountCurrencies> order = Moneh.getConfig().getCurrenciesInWeightOrder(acc);

						String assemble = "";
						for (AccountCurrencies accur : order) {
							String key = accur.getCurrenciesMap().keySet().iterator().next();
							assemble += " " + config.getCurrencyColor(key) + key + accur.getAmountOfCurrency(key) + ChatColor.GRAY + ",";
						}

						if (assemble.length() == 0) {
							assemble = " -";
						} else {
							assemble = assemble.substring(0, assemble.length()-1);
						}

						sender.sendMessage((i+1) + ". " + ChatColor.DARK_GREEN + acc.getUsername() + ChatColor.GRAY + ":" + assemble);
					}
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("send")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;

						AccountCurrencies toadd = new AccountCurrencies(args[2].toUpperCase());
						if (bank.hasAtLeast(p.getName(), toadd)) {

							AccountCurrencies to = new AccountCurrencies(bank.getHoldings(args[1]));
							if (to.getCurrenciesMap().isEmpty()) {
								p.sendMessage(ChatColor.RED + "Player \"" + args[1] + "\" has no bank account.");
								p.sendMessage(ChatColor.YELLOW + "To create a bank account, one must deposit some currency.");
							} else {
								bank.removeHoldings(p.getName(), args[2].toUpperCase());
								bank.addHoldings(args[1], args[2].toUpperCase());

								p.sendMessage(ChatColor.GREEN + "Transaction of " + ChatColor.WHITE + args[2].toUpperCase() + ChatColor.GREEN + " to " + ChatColor.WHITE  + args[1] + ChatColor.GREEN + " has been successful.");

								Player target = Bukkit.getServer().getPlayer(args[1]);
								if (target != null && target.isOnline()) {
									target.sendMessage(ChatColor.GREEN + "You've received " + ChatColor.WHITE + args[2].toUpperCase() + ChatColor.GREEN + " from " + ChatColor.WHITE + p.getName());
								}
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You are not a player lol");
					}
				} else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("forceset")) {
					boolean force = args[0].equalsIgnoreCase("forceset");
					if ((!sender.hasPermission("moneh.admin.set") && !sender.isOp())
							|| (force && !sender.hasPermission("moneh.admin.forceset") && !sender.isOp())) {

						sender.sendMessage(ChatColor.RED + "No permission!");
					} else {
						bank.setHoldings(args[1], new AccountCurrencies(args[2].toUpperCase()), force);

						sender.sendMessage(ChatColor.YELLOW + (force ? "Force-set" : "Set") + " holdings of " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " to " + ChatColor.WHITE  + args[2].toUpperCase());

						Player target = Bukkit.getServer().getPlayer(args[1]);
						if (target != null && target.isOnline()) {
							String cause = "*CONSOLE*";
							if (sender instanceof Player) {
								cause = ((Player)sender).getName();
							}

							target.sendMessage(ChatColor.YELLOW + "Your holdings have been " + (force ? "force-" : "") + "set to " + ChatColor.WHITE + args[2].toUpperCase() + ChatColor.YELLOW + " by " + ChatColor.WHITE + cause);
						}
					}
				} else if (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("put")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						PlayerInventory inv = p.getInventory();

						int amount = -1;
						try {
							amount = Integer.parseInt(args[2]);
						} catch (Throwable t) {
							p.sendMessage(ChatColor.RED + "\"" + args[2] + "\" is not an integer");
							return true;
						}

						if (amount <= 0) {
							p.sendMessage(ChatColor.RED + "\"" + amount + "\" is not a natural number");
						} else {
							ItemStack required = config.getCurrencyItem(args[1].toUpperCase()).toItemStack(amount);
							if (InventoryWorkaround.containsItem(inv, true, required)) {
								InventoryWorkaround.removeItem(inv, true, required);

								bank.addHoldings(p.getName().toLowerCase(), args[1].toUpperCase() + amount);
								this.saveBank();

								p.sendMessage(ChatColor.GREEN + "Successfully deposited " + ChatColor.WHITE + amount + " " + required.getType().name() + ChatColor.GREEN + " to your bank!");
							} else {
								p.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + amount + " " + required.getType().name() + ChatColor.RED + " to deposit!");
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You are not a player lol");
					}
				} else if (args[0].equalsIgnoreCase("withdraw") || args[0].equalsIgnoreCase("take")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						PlayerInventory inv = p.getInventory();
						InventoryPlayer inv2 = ((CraftInventoryPlayer)inv).getInventory();

						int amount = -1;
						try {
							amount = Integer.parseInt(args[2]);
						} catch (Throwable t) {
							p.sendMessage(ChatColor.RED + "\"" + args[2] + "\" is not an integer");
							return true;
						}

						if (amount <= 0) {
							p.sendMessage(ChatColor.RED + "\"" + amount + "\" is not a natural number");
						} else {
							MaterialData data = config.getCurrencyItem(args[1].toUpperCase());
							net.minecraft.server.ItemStack is = new net.minecraft.server.ItemStack(data.getItemTypeId(), amount, data.getData());

							if (bank.hasAtLeast(p.getName(), new AccountCurrencies(args[1].toUpperCase() + amount)) && inv2.canHold(is) > 0) {

								InventoryWorkaround.addItem(inv, true, data.toItemStack(amount));
								((CraftPlayer)p).updateInventory();

								bank.removeHoldings(p.getName().toLowerCase(), args[1].toUpperCase() + amount);
								this.saveBank();

								p.sendMessage(ChatColor.GREEN + "Successfully withdrawed " + ChatColor.WHITE + amount + " " + data.getItemType().name() + ChatColor.GREEN + " from bank!");
							} else {
								p.sendMessage(ChatColor.RED + "You do not have " + ChatColor.WHITE + amount + " " + data.getItemType().name() + ChatColor.RED + " to withdraw!");
							}
						}
					} else {
						sender.sendMessage(ChatColor.RED + "You are not a player lol");
					}
				}
			} else {
				CommandHelp.HELP.showHelp(sender);
			}
		}
		return true;
	}

	public enum CommandHelp {
		PAY,
		TOP,
		HELP;

		public void showHelp(CommandSender sender) {
			if (this == PAY) {

				sender.sendMessage(ChatColor.DARK_GREEN + "Usage" + ChatColor.WHITE + ":");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "pay" + ChatColor.GRAY + " <recipient> <amount of currency>");
				sender.sendMessage(ChatColor.DARK_GREEN + "Example" + ChatColor.WHITE + ":");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "pay" + ChatColor.GRAY + " Moresteck W20");

			} else if (this == TOP) {

				sender.sendMessage(ChatColor.DARK_GREEN + "Usage" + ChatColor.WHITE + ":");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "top" + ChatColor.GRAY + " <page>");
				sender.sendMessage(ChatColor.DARK_GREEN + "Example" + ChatColor.WHITE + ":");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "top" + ChatColor.GRAY + " 1");

			} else if (this == HELP) {

				sender.sendMessage(ChatColor.DARK_GREEN + "Command help for " + ChatColor.WHITE + "Moneh:");
				sender.sendMessage("[]" + ChatColor.DARK_GREEN + " optional   " + ChatColor.WHITE + "<>" + ChatColor.DARK_GREEN + " required");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "top" + ChatColor.GRAY + " <page>");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "pay" + ChatColor.GRAY + " <recipient> <amount of currency>");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "deposit" + ChatColor.GRAY + " <currency> <amount of currency>");
				sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "withdraw" + ChatColor.GRAY + " <currency> <amount of currency>");

				if (sender.hasPermission("moneh.admin.set") || sender.isOp()) {
					sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "set" + ChatColor.GRAY + " <player> <currencies> - sets only the specified currencies");
				}

				if (sender.hasPermission("moneh.admin.forceset") || sender.isOp()) {
					sender.sendMessage(" /moneh " + ChatColor.DARK_GREEN + "forceset" + ChatColor.GRAY + " <player> <holdings> - sets all of holdings");
				}
			}
		}
	}
}
