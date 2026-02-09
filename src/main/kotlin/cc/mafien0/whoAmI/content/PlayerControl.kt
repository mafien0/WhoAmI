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

    /**
     * Prompts the player to provide input and stores the callback function to handle the input once it is received.
     *
     * @param player The player from whom input is requested.
     * @param callback A lambda function that processes the input string provided by the player.
     */
    fun requestInput(player: Player, callback: (String) -> Unit) {
        player.sendMessage(Component.text("Input:", NamedTextColor.BLUE))
        awaitingInput[player] = callback
    }

    /**
     * Handles the AsyncChatEvent for capturing player chat input and processing it as part of
     * awaiting input callbacks.
     *
     * @param event The chat event triggered asynchronously when a player sends a chat message. This parameter
     *              contains information about the player, the message content, and allows controlling the
     *              event's behavior (e.g., canceling it).
     */
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