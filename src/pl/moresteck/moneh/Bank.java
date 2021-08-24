package pl.moresteck.moneh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {

	public ConcurrentHashMap<String, String> holdings = new ConcurrentHashMap<>();

	public Bank() {
		try {
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void addHoldings(String player, String add) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		currencies.addCurrencyValue(add);
		String text = AccountCurrencies.getInTextFormat(currencies.getCurrenciesMap());
		this.holdings.put(player.toLowerCase(), text);
	}

	public void removeHoldings(String player, String remove) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		currencies.removeCurrencyValue(remove);
		String text = AccountCurrencies.getInTextFormat(currencies.getCurrenciesMap());
		this.holdings.put(player.toLowerCase(), text);
	}

	public String getHoldings(String player) {
		return this.holdings.get(player.toLowerCase());
	}

	public boolean hasAtLeast(String player, AccountCurrencies curr) {
		AccountCurrencies currencies = new AccountCurrencies(this.holdings.get(player.toLowerCase()));
		Map<String, Integer> map = curr.getCurrenciesMap();
		for (String key : map.keySet()) {
			if (currencies.getCurrencyValue(key) < map.get(key)) {
				return false;
			}
		}
		return true;
	}

	public void set(String player, AccountCurrencies curr, boolean force) {
		AccountCurrencies accur = new AccountCurrencies(this.holdings.remove(player.toLowerCase()));
		if (force) {
			this.holdings.put(player.toLowerCase(), AccountCurrencies.getInTextFormat(curr.getCurrenciesMap()));
		} else {
			Map<String, Integer> map = curr.getCurrenciesMap();
			for (String key : map.keySet()) {
				accur.setCurrencyValue(key, curr.getCurrencyValue(key));
			}
			this.holdings.put(player.toLowerCase(), AccountCurrencies.getInTextFormat(accur.getCurrenciesMap()));
		}
	}

	public void resetAccount(String player) {
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
