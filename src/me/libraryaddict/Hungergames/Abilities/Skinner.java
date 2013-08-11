package me.libraryaddict.Hungergames.Abilities;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.libraryaddict.Hungergames.Events.GameStartEvent;
import me.libraryaddict.Hungergames.Interfaces.Disableable;
import me.libraryaddict.Hungergames.Types.AbilityListener;
import me.libraryaddict.Hungergames.Types.Gamer;
import me.libraryaddict.Hungergames.Types.HungergamesApi;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import me.libraryaddict.disguise.Events.DisguisedEvent;
import me.libraryaddict.disguise.Events.UndisguisedEvent;

public class Skinner extends AbilityListener implements Disableable {
    public int chanceInOneOfSkinning = 3;
    private boolean disable = true;
    public String skinName = "Default";
    public boolean skinUsersOfKit = true;

    public Skinner() throws Exception {
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") == null)
            throw new Exception(String.format(HungergamesApi.getTranslationManager().getLoggerDependencyNotFound(),
                    "Plugin LibsDisguises"));
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null)
            throw new Exception(String.format(HungergamesApi.getTranslationManager().getLoggerDependencyNotFound(),
                    "Plugin ProtocolLib"));
    }

    private boolean isSkinned(Entity entity) {
        Disguise disguise = DisguiseAPI.getDisguise(entity);
        if (disguise != null) {
            if (disguise.isPlayerDisguise() && ((PlayerDisguise) disguise).getName().equals(skinName)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (hasAbility((Player) event.getDamager())
                    && (this.chanceInOneOfSkinning <= 0 || new Random().nextInt(this.chanceInOneOfSkinning) == 0)) {
                this.disable = false;
                DisguiseAPI.disguiseToAll(event.getEntity(), new PlayerDisguise(this.skinName));
            }
        }
    }

    @EventHandler
    public void onDisguise(DisguisedEvent event) {
        if (isSkinned(event.getDisguised()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        if (skinUsersOfKit)
            for (Player p : getMyPlayers()) {
                DisguiseAPI.disguiseToAll(p, new PlayerDisguise(this.skinName));
            }
    }

    @EventHandler
    public void onUnDisguise(UndisguisedEvent event) {
        Gamer gamer = HungergamesApi.getPlayerManager().getGamer(event.getDisguised());
        if (gamer != null && gamer.isAlive()) {
            if (isSkinned(event.getDisguised()))
                event.setCancelled(true);
        }
    }

    @Override
    public void unregisterPlayer(Player player) {
        myPlayers.remove(player.getName());
        if (disable && HungergamesApi.getHungergames().currentTime >= 0 && this instanceof Disableable && myPlayers.size() == 0) {
            HandlerList.unregisterAll(this);
        }
    }
}
