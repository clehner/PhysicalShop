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

/**
 * Non-material currency for shops, using Vault
 */
public class ShopCurrency extends ShopMaterial {
	private Economy econ;

	public ShopCurrency(final PhysicalShop plugin) {
		super(new ItemStack(0, 0));
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

	@Override
	public boolean giveVirtual(final String playerName, final int amount) {
		if (amount == 0) return true;
		EconomyResponse resp = econ.depositPlayer(playerName, (double) amount);
		return (resp.type == SUCCESS);
	}

	@Override
	public boolean takeVirtual(final String playerName, final int amount) {
		if (amount == 0) return true;
		EconomyResponse resp = econ.withdrawPlayer(playerName, (double) amount);
		return (resp.type == SUCCESS);
	}
}
