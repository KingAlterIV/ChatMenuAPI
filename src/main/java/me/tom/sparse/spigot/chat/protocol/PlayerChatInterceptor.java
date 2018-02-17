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
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

                BaseComponent[] spigot = chat.getHandle().getSpecificModifier(BaseComponent[].class).read(0);
                WrappedChatComponent msg;
                if (spigot != null) {
                    msg = WrappedChatComponent.fromJson(ComponentSerializer.toString(spigot));
                } else {
                    msg = chat.getMessage();
                }

                boolean allowed = isAllowed(event.getPlayer(), msg);
                boolean paused = isPaused(event.getPlayer());
                if (!paused || !allowed) {
                    Queue<WrappedChatComponent> queue = messageQueue.computeIfAbsent(event.getPlayer().getUniqueId(), (uuid) -> new ConcurrentLinkedQueue<>());
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
            allowedMessages.computeIfAbsent(player.getUniqueId(), (uuid) -> new ConcurrentLinkedQueue<>()).add(WrappedChatComponent.fromJson(ComponentSerializer.toString(message)));
        }
        player.spigot().sendMessage(message);
    }

    public boolean isPaused(Player player) {
        return paused.getOrDefault(player.getUniqueId(), false);
    }

    public void pause(Player player) {
        if (isPaused(player)) return;
        paused.put(player.getUniqueId(), true);
    }

    public void resume(Player player) {
        if (!isPaused(player)) return;

        paused.put(player.getUniqueId(), false);

        int i = 0;
        // copy so that we don't catch new messages
        Queue<WrappedChatComponent> queuedMessages = new ConcurrentLinkedQueue<>(messageQueue.getOrDefault(player.getUniqueId(), new ConcurrentLinkedQueue<>()));
        while (i < 20 - queuedMessages.size()) {
            i++;
            player.sendMessage(" ");
        }

        for (WrappedChatComponent components : queuedMessages) {
            WrapperPlayServerChat chat = new WrapperPlayServerChat();
            chat.setMessage(components);
            chat.setChatType(EnumWrappers.ChatType.CHAT);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat.getHandle());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        allowedMessages.remove(player.getUniqueId());
        messageQueue.remove(player.getUniqueId());
    }

    public boolean isAllowed(Player player, WrappedChatComponent message) {
        return !isPaused(player) || allowedMessages.computeIfAbsent(player.getUniqueId(), (uuid) -> new ConcurrentLinkedQueue<>()).remove(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        paused.remove(e.getPlayer().getUniqueId());
        messageQueue.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (isPaused(e.getPlayer())) e.setCancelled(true);
    }

    public void disable() {
        paused.clear();
        messageQueue.clear();
        allowedMessages.clear();
    }
}
