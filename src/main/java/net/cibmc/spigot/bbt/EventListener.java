package net.cibmc.spigot.bbt;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Boat;
import org.bukkit.TreeSpecies;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.block.BlockState;

public class EventListener implements Listener {
    App app;
    final String BBT_SIGN_STRING = "[BBT_MC]";

    EventListener(App app) {
        this.app = app;
    }

    @EventHandler
	public void onSignChange(SignChangeEvent event){
        Player player = event.getPlayer();
        if (player == null) return;
		if (!event.getLine(0).equals(BBT_SIGN_STRING)) return;
		if (!event.getPlayer().hasPermission("bbt.admin")){
			event.getBlock().breakNaturally();
			event.getPlayer().sendMessage(ChatColor.RED + "[BBT] Wait a minute! But it's seems you don't have permission to build a BBT Boat Station!");
			return;
		}

		event.setLine(1, "------------");
        event.setLine(2, "Please sub-click");
        event.setLine(3, "to rent a boat!");
		event.getPlayer().sendMessage(ChatColor.GREEN + "[BBT] You built BBT Boat Station!");
	}

    @EventHandler
	public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if (player == null) return;
		Block blc = event.getBlock();
        Block searchedBlc = null;
        Outer:
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block checkingBlc = blc.getLocation().add(x, y, z).getBlock();
                    if (!isSign(checkingBlc)) continue;
                    Sign sign = (Sign)checkingBlc.getState();
                    if (!sign.getLine(0).equalsIgnoreCase(BBT_SIGN_STRING)) continue;
                    searchedBlc = checkingBlc;
                    break Outer;
                }
            }
        }

        if (searchedBlc == null) return;
        
		if(!player.hasPermission("bbt.admin")){
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "[BBT] " + ChatColor.WHITE + "You don't have permission to break a BBT boat station!");
		}
	}

    @EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if (player == null) return;
		Block blc = event.getBlock();
        Block searchedBlc = null;
        Outer:
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block checkingBlc = blc.getLocation().add(x, y, z).getBlock();
                    if (!isSign(checkingBlc)) continue;
                    Sign sign = (Sign)checkingBlc.getState();
                    if (!sign.getLine(0).equalsIgnoreCase(BBT_SIGN_STRING)) continue;
                    searchedBlc = checkingBlc;
                    break Outer;
                }
            }
        }

        if (searchedBlc == null) return;
        
		if(!player.hasPermission("bbt.admin")){
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "[BBT] " + ChatColor.WHITE + "You don't have permission to place a block around BBT boat station!");
		}
	}

    @EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Block blc = event.getClickedBlock();
        if (!isSign(blc)) {
            return;
        }
        Sign sign = (Sign)blc.getState();
		if (!sign.getLine(0).equalsIgnoreCase(BBT_SIGN_STRING))  {
            return;
        };
        event.setCancelled(true);

        Block searchedBlc = null;

        Outer:
        for (int y = -3; y <= 1; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Block checkingBlc = blc.getLocation().add(x, y, z).getBlock();
                    if (
                        checkingBlc.getType() == Material.WATER &&
                        checkingBlc.getLocation().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                        checkingBlc.getLocation().add(0, 2, 0).getBlock().getType() == Material.AIR
                    ) {
                        searchedBlc = checkingBlc;
                        break Outer;
                    }
                }
            }
        }

        if (searchedBlc == null) {
            player.sendMessage(ChatColor.RED + "[BBT] " + ChatColor.WHITE + "Sorry! But there is no safty location to ride a boat around here...");
            return;
        }

        Boat boat = (Boat)player.getWorld().spawnEntity(searchedBlc.getLocation().add(0.5, 0.5, 0.5), EntityType.BOAT);
        boat.setWoodType(TreeSpecies.GENERIC);
        boat.addPassenger(player);
        this.app.isHighSpeedMode.put(boat, Boolean.FALSE);
    }

    @EventHandler
    public void onVehicleExitEvent(VehicleExitEvent event) {
        Entity e = event.getExited();
        if (!(e instanceof Player)) {
            return;
        }

        Vehicle v = event.getVehicle();
        if (!(v instanceof Boat)) {
            return;
        }
        Boat boat = (Boat)v;
        Boolean flag = this.app.isHighSpeedMode.get(boat);
        if (flag == null) return;

        boat.remove();
        this.app.isHighSpeedMode.remove(boat);
    }

    private static boolean isSign(Block blc) {
        BlockState bs = blc.getState();
		return (bs instanceof Sign);
    }
}
