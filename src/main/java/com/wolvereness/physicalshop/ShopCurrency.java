package com.wolvereness.physicalshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import com.wolvereness.physicalshop.config.MaterialConfig;
import com.wolvereness.physicalshop.exception.InvalidMaterialException;

/**
 * Non-material currency for shops, using Vault
 */
public class ShopCurrency extends ShopMaterial {
	private Economy econ;

	public ShopCurrency(final PhysicalShop plugin, String econName) {
		super(new ItemStack(0, 0));
		econName = econName.toLowerCase();
		ServicesManager servicesManager = Bukkit.getServer().getServicesManager();
		RegisteredServiceProvider<Economy> economyProvider = null;
		try {
			if ("vault".equals(econName)) {
				economyProvider = servicesManager.getRegistration(Economy.class);
				if (economyProvider != null) {
					econ = economyProvider.getProvider();
				}
			} else {
				for (RegisteredServiceProvider<Economy> provider :
						servicesManager.getRegistrations(Economy.class)) {
					Economy thisEcon = provider.getProvider();
					if (thisEcon != null && thisEcon.getName().toLowerCase().contains(econName)) {
						econ = thisEcon;
						break;
					}
				}
			}
			if (econ == null) {
				plugin.getLogger().severe("Economy service '" + econName + "' was not found.");
			}
		} catch(NoClassDefFoundError e) {
			plugin.getLogger().severe("Vault not found.");
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
		return econ == null ? -1 : -econ.hashCode();
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
		return resp.transactionSuccess();
	}

	@Override
	public boolean takeVirtual(final String playerName, final int amount) {
		if (amount == 0) return true;
		EconomyResponse resp = econ.withdrawPlayer(playerName, (double) amount);
		return resp.transactionSuccess();
	}
}
