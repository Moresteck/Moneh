package pl.moresteck.moneh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {

	public ConcurrentHashMap<String, String> holdings = new ConcurrentHashMap<>();

	public void addHoldings(String player, String add) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		currencies.addToCurrenciesAmounts(add);

		String text = currencies.toText();
		this.holdings.put(player.toLowerCase(), text);
	}

	public void removeHoldings(String player, String remove) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		currencies.removeFromCurrencyAmount(remove);

		String text = currencies.toText();
		this.holdings.put(player.toLowerCase(), text);
	}

	public String getHoldings(String player) {
		return this.holdings.get(player.toLowerCase());
	}

	public boolean hasAtLeast(String player, AccountCurrencies curr) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.get(player.toLowerCase()));

		Map<String, Integer> map = curr.getCurrenciesMap();
		for (String key : map.keySet()) {
			if (currencies.getAmountOfCurrency(key) < map.get(key)) {
				return false;
			}
		}
		return true;
	}

	public void setHoldings(String player, AccountCurrencies curr, boolean force) {
		AccountCurrencies accur = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		if (force) {
			this.holdings.put(player.toLowerCase(), curr.toText());
		} else {
			Map<String, Integer> map = curr.getCurrenciesMap();
			for (String key : map.keySet()) {
				accur.setAmountOfCurrency(key, curr.getAmountOfCurrency(key));
			}

			this.holdings.put(player.toLowerCase(), AccountCurrencies.getInTextFormat(accur.getCurrenciesMap()));
		}
	}

	public void resetHoldings(String player) {
		this.holdings.remove(player.toLowerCase());
	}

	public ArrayList<AccountCurrencies> getTop() {
		ArrayList<AccountCurrencies> topList = new ArrayList<>();

		for (String key : this.holdings.keySet()) {
			String rawHoldings = this.getHoldings(key);

			AccountCurrencies currencies = new AccountCurrencies(key, rawHoldings);
			topList.add(currencies);
		}

		Collections.sort(topList, (v1, v2) -> {
			return v1.compareTo(v2) * -1;
		});

		return topList;
	}
}
