package xyz.mackan.Slabbo.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.mackan.Slabbo.Slabbo;
import xyz.mackan.Slabbo.abstractions.SlabboAPI;
import xyz.mackan.Slabbo.abstractions.SlabboItemAPI;
import xyz.mackan.Slabbo.types.MetaKey;
import xyz.mackan.Slabbo.types.SlabType;

import java.util.*;

public class ItemUtil {

	private static SlabboAPI api = Bukkit.getServicesManager().getRegistration(SlabboAPI.class).getProvider();
	private static SlabboItemAPI itemAPI = Bukkit.getServicesManager().getRegistration(SlabboItemAPI.class).getProvider();

	public static double getSlabYOffset (Location location) {
		Block block = location.getBlock();

		if (block == null) return 0;

		SlabType slabType = api.getSlabType(block);

		if (slabType == SlabType.NONE) {
			block.setType(itemAPI.getDefaultSlab().getType());

			slabType = SlabType.BOTTOM;
		}

		if (slabType == SlabType.BOTTOM) {
			return 0.5;
		} else {
			return 1.0;
		}
	}

	public static void setEntityToShopItem (Item item, Location location) {
		api.setNoPickup(item, 1);
		api.setNoDespawn(item, 1);
		api.setNoMerge(item, 1);
		api.setShopLocation(item, location);
	}

	public static void dropShopItem (Location location, ItemStack item, int quantity) {
		Location dropLocation = location.clone();

		dropLocation.add(0.5, getSlabYOffset(location), 0.5);

		ItemStack clonedItem = item.clone();
		ItemMeta meta = clonedItem.getItemMeta();

		String displayType = Slabbo.getInstance().getConfig().getString("itemdisplay", "quantity");

		if (displayType.equalsIgnoreCase("quantity")) {
			if (quantity < 1) quantity = 1;
			if (quantity > 64) quantity = 64;

			clonedItem.setAmount(quantity);
		} else {
			clonedItem.setAmount(64);
		}

		meta.setDisplayName("Slabbo Item "+ShopUtil.locationToString(location));

		clonedItem.setItemMeta(meta);

		Item itemEnt = location.getWorld().dropItem(dropLocation, clonedItem);

		setEntityToShopItem(itemEnt, location);

		api.setGravity(itemEnt, false);

		itemEnt.setVelocity(itemEnt.getVelocity().zero());

		itemEnt.teleport(dropLocation);
	}

	public static List<Item> findSlabboItemsInWorld (World world) {
		Collection<Entity> worldEntities = world.getEntities();

		List<Item> shopItems = new ArrayList<Item>();

		for (Entity entity : worldEntities) {
			boolean isItem = api.isItem(entity);

			if (!isItem) continue;

			Item item = (Item) entity;

			if (!api.isSlabboItem(item)) continue;;

			shopItems.add(item);
		}

		return shopItems;
	}

	public static List<Item> findShopItems (Location location) {
		Collection<Entity> nearbyEntites = api.getNearbyEntities(location, 0.5, 2, 0.5);

		String locationString = ShopUtil.locationToString(location);

		List<Item> shopItems = new ArrayList<Item>();

		for (Entity entity : nearbyEntites) {
			boolean isItem = api.isItem(entity);

			if (!isItem) continue;

			Item item = (Item) entity;

			if (!api.isSlabboItem(item)) continue;

			String itemLocationString = api.getShopLocation(item);

			if (itemLocationString == null || itemLocationString.equals("")) continue;

			if (itemLocationString.equals(locationString)) {
				shopItems.add(item);
			}
		}

		return shopItems;
	}

	public static void removeShopItemsAtLocation (Location location) {
		List<Item> shopItems = findShopItems(location);

		for (Item shopItem : shopItems) {
			shopItem.remove();
		}
	}

	public static void removeShopItems (World world) {
		List<Item> shopItems = findSlabboItemsInWorld(world);

		for (Item shopItem : shopItems) {
			shopItem.remove();
		}
	}


}
