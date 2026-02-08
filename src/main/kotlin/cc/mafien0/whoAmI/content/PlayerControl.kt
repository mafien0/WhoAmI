package cc.mafien0.whoAmI.content

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object PlayerControl : Listener {
    private val awaitingInput = mutableMapOf<Player, (String) -> Unit>()

    fun requestInput(player: Player, callback: (String) -> Unit) {
        player.sendMessage(Component.text("Input:", NamedTextColor.BLUE))
        awaitingInput[player] = callback
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val callback = awaitingInput[player] ?: return

        event.isCancelled = true
        val message = (event.message() as? TextComponent)?.content() ?: return

        awaitingInput.remove(player)
        callback(message)
    }
}