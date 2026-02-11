package cc.mafien0.whoAmI.listeners

import cc.mafien0.whoAmI.game.GamePlayer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ChatListener : Listener {
    
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val message = (event.message() as? TextComponent)?.content() ?: return
        
        // Try to handle as game input
        if (GamePlayer.handleChatInput(player, message)) {
            event.isCancelled = true
        }
    }
}