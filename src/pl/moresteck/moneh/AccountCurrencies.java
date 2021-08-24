package pl.moresteck.moneh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCurrencies implements Comparable<AccountCurrencies> {
	private ConcurrentHashMap<String, Integer> currencies = new ConcurrentHashMap<>();
	private String username;

	public AccountCurrencies(String values) {
		if (values != null)
			this.currencies.putAll(textToMap(values));
	}

	public AccountCurrencies(String username, String values) {
		if (values != null)
			this.currencies.putAll(textToMap(values));

		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public Map<String, Integer> getCurrenciesMap() {
		return this.currencies;
	}

	public int getAmountOfCurrency(String currency) {
		return this.currencies.containsKey(currency) ? this.currencies.get(currency) : -1;
	}

	public void addToCurrencyAmount(String currency, int add) {
		this.setAmountOfCurrency(currency, add + getAmountOfCurrency(currency));
	}

	public void setAmountOfCurrency(String currency, int set) {
		int value = getAmountOfCurrency(currency);
		if (value != -1) {
			this.currencies.remove(currency);
		}

		this.currencies.put(currency, set);
	}

	public void addToCurrenciesAmounts(String add) {
		HashMap<String, Integer> curtoadd = textToMap(add);

		for (String key : curtoadd.keySet()) {
			int value = getAmountOfCurrency(key);

			if (value != -1) {
				this.currencies.remove(key);
			} else {
				value = 0;
			}

			value += curtoadd.get(key);
			this.currencies.put(key, value);
		}
	}

	public void removeFromCurrencyAmount(String remove) {
		HashMap<String, Integer> curtorem = textToMap(remove);

		for (String key : curtorem.keySet()) {
			int value = getAmountOfCurrency(key);

			if (value != -1) {
				this.currencies.remove(key);
			} else {
				value = 0;
			}

			value -= curtorem.get(key);
			this.currencies.put(key, value);
		}
	}

	public Double getWeight() {
		double totalValue = 0.0D;

		Map<String, Integer> currenciesMap = this.getCurrenciesMap();
		for (String key2 : currenciesMap.keySet()) {
			totalValue += Moneh.getConfig().getCurrencyWeight(key2) * currenciesMap.get(key2);
		}

		return totalValue;
	}

	public String toText() {
		return getInTextFormat(this.getCurrenciesMap());
	}

	public static String getInTextFormat(Map<String, Integer> currencies) {
		String res = "";

		for (String key : currencies.keySet()) {
			res += key + Integer.toString(currencies.get(key)) + ";";
		}

		return res.substring(0, res.length()-1);
	}

	public static HashMap<String, Integer> textToMap(String values) {
		HashMap<String, Integer> currencyMap = new HashMap<>();
		String[] currencies = values.split(";");

		for (int i = 0; i < currencies.length; i++) {
			String currency = Character.toString(currencies[i].charAt(0));
			Integer value = Integer.parseInt(currencies[i].substring(1));

			currencyMap.put(currency, value);
		}

		return currencyMap;
	}

	@Override
	public int compareTo(AccountCurrencies o) {
		return this.getWeight().compareTo(o.getWeight());
	}
}
