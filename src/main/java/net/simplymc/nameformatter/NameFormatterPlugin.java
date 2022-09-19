package net.simplymc.nameformatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class NameFormatterPlugin extends JavaPlugin implements Listener {
    LuckPerms LPApi = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.LPApi = provider.getProvider();
        }

        getServer().getPluginManager().registerEvents(this, this);
        this.LPApi.getEventBus().subscribe(this, UserDataRecalculateEvent.class, (e) -> setPlayerNameColor(e.getUser().getUniqueId()));
        this.LPApi.getEventBus().subscribe(this, GroupDataRecalculateEvent.class, (e) -> {
            NodeMatcher<InheritanceNode> matcher = NodeMatcher.key(InheritanceNode.builder(e.getGroup().getName()).build());
            this.LPApi.getUserManager().searchAll(matcher).thenAcceptAsync((users) -> {
                for (UUID id : users.keySet()) {
                    setPlayerNameColor(id);
                }
            });
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setPlayerNameColor(event.getPlayer().getUniqueId());
    }

    private void setPlayerNameColor(UUID uuid) {
        Player player = getServer().getPlayer(uuid);
        if (player == null) return;
        CachedMetaData meta = this.LPApi.getPlayerAdapter(Player.class).getMetaData(player);

        String nameColor = meta.getMetaValue("name-color");
        if (nameColor == null) nameColor = "#ffffff";

        Component coloredName = Component.text().content(player.getName()).color(TextColor.fromCSSHexString(nameColor)).build();

        player.displayName(coloredName);
        player.playerListName(coloredName);
    }

}
