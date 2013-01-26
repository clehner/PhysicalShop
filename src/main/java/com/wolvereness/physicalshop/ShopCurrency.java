package com.wolvereness.physicalshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import static net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS;

import com.wolvereness.physicalshop.config.MaterialConfig;
import com.wolvereness.physicalshop.exception.InvalidMaterialException;
import static com.wolvereness.physicalshop.config.ConfigOptions.SERVER_SHOP;

/**
 * Non-material currency for shops, using Vault
 */
public class ShopCurrency extends ShopMaterial {
	private Economy econ;
	final String serverShop;

	public ShopCurrency(final PhysicalShop plugin) {
		super(new ItemStack(0, 0));
		serverShop = plugin.getConfig().getString(SERVER_SHOP);
		RegisteredServiceProvider<Economy> economyProvider = null;
		try {
			economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		} catch(NoClassDefFoundError e) {}
		if (economyProvider == null) {
			plugin.getLogger().severe("Vault not found.");
		} else {
			econ = economyProvider.getProvider();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ShopCurrency)) return false;
		if (econ != ((ShopCurrency)obj).econ) return false;
		return true;
	}

	@Override
	public ItemStack getStack(final int amount) {
		return null;
	}

	@Override
	public int hashCode() {
		return -1;
	}

	@Override
	public String toString() {
		return econ.currencyNamePlural();
	}

	@Override
	public String toString(final MaterialConfig materialConfig) {
		return toString();
	}

	/**
	 * @param fromPlayer the player to debit
	 * @param toPlayer the player to credit
	 * @param amount the amount of money to transfer
	 * @return success or failure
	 */
	public boolean transfer(String fromPlayer, String toPlayer, final int amount) {
		if (fromPlayer.equalsIgnoreCase(toPlayer)) return true;

		EconomyResponse resp;
		if (!fromPlayer.equalsIgnoreCase(serverShop)) {
			resp = econ.withdrawPlayer(fromPlayer, (double) amount);
			if (resp.type != SUCCESS) return false;
		}
		if (!toPlayer.equalsIgnoreCase(serverShop)) {
			resp = econ.depositPlayer(toPlayer, (double) amount);
			if (resp.type != SUCCESS) return false;
		}
		return true;
	}

	/**
	 * @param playerName name of the player to check
	 * @param amount the minimum balance to check for
	 * @return whether the player has the given amount of money
	 */
	public boolean has(String playerName, final int amount) {
		return playerName.equalsIgnoreCase(serverShop) ||
			econ.has(playerName, (double) amount);
	}
}
