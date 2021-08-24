package pl.moresteck.moneh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class Config {

	private HashMap<String, String> currency_color = new HashMap<>();
	private HashMap<String, String> currency_alias = new HashMap<>();
	private HashMap<String, Double> currency_weight = new HashMap<>();
	private HashMap<String, MaterialData> currency_item = new HashMap<>();

	public void defaultConfig() {
		currency_color.put("W", "f");
		currency_color.put("G", "6");
		currency_color.put("L", "1");
		currency_color.put("D", "b");

		currency_alias.put("L", "Lapis");
		currency_alias.put("G", "Gold");
		currency_alias.put("D", "Diamond");
		currency_alias.put("W", "Wood");

		currency_weight.put("W", 1.0);
		currency_weight.put("G", 30.0);
		currency_weight.put("D", 100.0);
		currency_weight.put("L", 10.0);

		currency_item.put("L", new MaterialData(Material.INK_SACK, DyeColor.BLUE.getData()));
		currency_item.put("G", new MaterialData(Material.GOLD_INGOT));
		currency_item.put("D", new MaterialData(Material.DIAMOND));
		currency_item.put("W", new MaterialData(Material.LOG));
	}

	public ChatColor getCurrencyColor(String currency) {
		if (!this.currency_color.containsKey(currency))
			return null;

		return ChatColor.getByCode(Integer.parseInt(this.currency_color.get(currency).toUpperCase(), 16));
	}

	public double getCurrencyWeight(String currency) {
		if (!this.currency_weight.containsKey(currency))
			return 0.0;

		return this.currency_weight.get(currency);
	}

	public String getCurrencyAlias(String currency) {
		if (!this.currency_alias.containsKey(currency))
			return null;

		return this.currency_alias.get(currency);
	}

	public MaterialData getCurrencyItem(String currency) {
		if (!this.currency_item.containsKey(currency))
			return null;

		return this.currency_item.get(currency);
	}

	public ArrayList<AccountCurrencies> getCurrenciesInWeightOrder(AccountCurrencies acc) {
		ArrayList<AccountCurrencies> cur = new ArrayList<>();

		for (String key : acc.getCurrenciesMap().keySet()) {
			AccountCurrencies acc2 = new AccountCurrencies(key + acc.getCurrenciesMap().get(key));
			cur.add(acc2);
		}

		Collections.sort(cur, (v1, v2) -> {
			return v1.compareTo(v2) * -1;
		});

		return cur;
	}
}
