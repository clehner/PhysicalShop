package com.wolvereness.physicalshop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.wolvereness.physicalshop.exception.InvalidExchangeException;

/**
 *
 */
public class InventoryHelpers {
	private static boolean add(final Inventory inventory, final ItemStack stack) {
		if (stack == null) return true;
		int left = stack.getAmount();
		final int maxStackSize = stack.getType().getMaxStackSize();

		loop: for (int pass = 0; pass < 2; ++pass) {
			final ItemStack[] contents = inventory.getContents();

			for (int i = 0; i < contents.length; ++i) {
				if (left == 0) {
					break loop;
				}

				final ItemStack s = contents[i];

				if (s == null) {
					if (pass == 0) {
						continue;
					}
				} else {
					if (	(s.getType() != stack.getType())
							|| (s.getDurability() != stack.getDurability())
							|| (s.hasItemMeta())
							) {
						continue;
					}
				}

				final int size = s == null ? 0 : s.getAmount();
				final int newSize = Math.min(maxStackSize, size + left);

				stack.setAmount(newSize);

				inventory.setItem(i, stack);

				left -= newSize - size;
			}
		}

		return left == 0;
	}

	/**
	 * This will add and remove material from the specified inventory,
	 * and from the specified player if it is a virtual material.
	 * If it fails, it resets inventories, undoes charges, and throws an error.
	 * @param String name of the player who owns this inventory
	 * @param inventory inventory to use
	 * @param addMaterial material to add
	 * @param addAmount amount of material to add
	 * @param removeMaterial material to remove
	 * @param removeAmount amount of material to remove
	 * @throws InvalidExchangeException if the inventory cannot support the stack or add, or not enough in inventory to remove the stack
	 */
	public static void exchange(
	        final String playerName,
	        final Inventory inventory,
			final ShopMaterial addMaterial,
			final int addAmount,
			final ShopMaterial removeMaterial,
			final int removeAmount)
			throws InvalidExchangeException {
		final ShopItemStack[] oldItems = InventoryHelpers.getItems(inventory);

		if (removeAmount != 0) {
			final ItemStack removeStack = removeMaterial.getStack(removeAmount);
			if (!InventoryHelpers.remove(inventory, removeStack) ||
					!removeMaterial.takeVirtual(playerName, removeAmount)) {
				InventoryHelpers.setItems(inventory, oldItems);
				throw new InvalidExchangeException(
						InvalidExchangeException.Type.REMOVE);
			}
		}

		if (addAmount != 0) {
			final ItemStack addStack = addMaterial.getStack(addAmount);
			if (!InventoryHelpers.add(inventory, addStack) ||
					!addMaterial.giveVirtual(playerName, addAmount)) {
				removeMaterial.giveVirtual(playerName, removeAmount);
				InventoryHelpers.setItems(inventory, oldItems);
				throw new InvalidExchangeException(
						InvalidExchangeException.Type.ADD);
			}
		}
	}

	/**
	 * Finds how much of given material is in given inventory
	 * @param inventory the inventory to consider
	 * @param material the material to consider
	 * @return the amount of material in said inventory
	 */
	public static int getCount(final Inventory inventory, final ShopMaterial material) {
		int amount = 0;

		for (final ItemStack i : inventory.getContents()) {
			if (	(i != null)
					&& (i.getType() == material.getMaterial())
					&& (i.getDurability() == material.getDurability())
					&& !i.hasItemMeta()) {
				amount += i.getAmount();
			}
		}

		return amount;
	}

	/**
	 * Makes a set of shop item stacks to represent this inventory
	 * @param inventory the inventory to consider
	 * @return a set of shop item stacks
	 */
	public static ShopItemStack[] getItems(final Inventory inventory) {
		final ItemStack[] contents = inventory.getContents();
		final ShopItemStack[] items = new ShopItemStack[contents.length];

		for (int i = 0; i < items.length; ++i) {
			final ItemStack stack = contents[i];
			items[i] = stack == null ? null : new ShopItemStack(stack);
		}

		return items;
	}

	private static boolean remove(final Inventory inventory, final ItemStack stack) {
		if (stack == null) return true;
		int left = stack.getAmount();
		final ItemStack[] contents = inventory.getContents();

		for (int i = 0; i < contents.length; ++i) {
			if (left == 0) {
				break;
			}

			final ItemStack s = contents[i];

			if (	(s == null)
					|| (s.getType() != stack.getType())
					|| (s.getDurability() != stack.getDurability())
					|| (s.hasItemMeta())) {
				continue;
			}

			final int size = s.getAmount();
			final int newSize = size - Math.min(size, left);

			if (newSize == 0) {
				inventory.setItem(i, null);
			} else {
				s.setAmount(newSize);
				inventory.setItem(i, s);
			}

			left -= size - newSize;
		}

		return left == 0;
	}

	/**
	 * This changes slot in this inventory to given array
	 * @param inventory the inventory to consider
	 * @param items the items to overwrite the inventory with
	 */
	public static void setItems(final Inventory inventory, final ShopItemStack[] items) {
		for (int i = 0; i < items.length; ++i) {
			final ShopItemStack stack = items[i];
			inventory.setItem(i, stack == null ? null : stack.getStack());
		}
	}

}
