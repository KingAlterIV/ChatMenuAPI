package me.tom.sparse.spigot.chat.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerChatInterceptor implements Listener {

    private Map<UUID, Boolean> paused = new ConcurrentHashMap<>();
    private Map<UUID, Queue<WrappedChatComponent>> messageQueue = new ConcurrentHashMap<>();
    private Map<UUID, Queue<WrappedChatComponent>> allowedMessages = new ConcurrentHashMap<>();

    public PlayerChatInterceptor(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.CHAT) {

            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerChat chat = new WrapperPlayServerChat(event.getPacket());
                if (chat.getChatType() != EnumWrappers.ChatType.CHAT) return;

                boolean allowed = isAllowed(event.getPlayer(), chat.getMessage());
                boolean paused = isPaused(event.getPlayer());
                if (!paused || !allowed) {
                    BaseComponent[] spigot = chat.getHandle().getSpecificModifier(BaseComponent[].class).read(0);
                    if (spigot == null) return;
                    WrappedChatComponent msg = WrappedChatComponent.fromJson(ComponentSerializer.toString(spigot));

                    Queue<WrappedChatComponent> queue = messageQueue.getOrDefault(event.getPlayer().getUniqueId(), new ConcurrentLinkedQueue<>());
                    while (queue.size() > 20) {
                        queue.remove();
                    }

                    queue.add(msg);
                }

                if (paused && !allowed) {
                    event.setCancelled(true);
                }
            }
        });
    }

    /**
     * Sends a message to the player associated with this, regardless of chat being paused.
     *
     * @param message the message to send
     */
    public void sendMessage(Player player, BaseComponent... message) {
        if (isPaused(player)) {
            allowedMessages.getOrDefault(player.getUniqueId(), new ConcurrentLinkedQueue<>()).add(WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));
        }
        player.spigot().sendMessage(message);
    }

    public boolean isPaused(Player player) {
        return paused.getOrDefault(player.getUniqueId(), false);
    }

    public void pause(Player player) {
        if (isPaused(player)) return;
        paused.put(player.getUniqueId(), true);
        System.out.println("Pausing chat for " + player.getName());
    }

    public void resume(Player player) {
        if (!isPaused(player)) return;
        paused.put(player.getUniqueId(), true);
        System.out.println("Resuming chat for " + player.getName());

        int i = 0;
        for (WrappedChatComponent components : messageQueue.getOrDefault(player.getUniqueId(), new ConcurrentLinkedQueue<>())) {
            WrapperPlayServerChat chat = new WrapperPlayServerChat();
            chat.setMessage(components);
            chat.setChatType(EnumWrappers.ChatType.CHAT);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat.getHandle());
                i++;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        while (i < 20) {
            i++;
            player.sendMessage("");
        }
    }

    public boolean isAllowed(Player player, WrappedChatComponent message) {
        return !isPaused(player) || allowedMessages.getOrDefault(player.getUniqueId(), new ConcurrentLinkedQueue<>()).remove(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        paused.remove(e.getPlayer().getUniqueId());
        messageQueue.remove(e.getPlayer().getUniqueId());
    }

    public void disable() {
        paused.clear();
        messageQueue.clear();
        allowedMessages.clear();
    }
}
