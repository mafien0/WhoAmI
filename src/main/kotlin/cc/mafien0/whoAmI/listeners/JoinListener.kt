package cc.mafien0.whoAmI.listeners

import cc.mafien0.whoAmI.game.GamePlayer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object JoinListener: Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // If the player is in the game
        val gamePlayer = GamePlayer.findByName(player.name) ?: return

        // Get plugin instance
        val plugin = Bukkit.getPluginManager().getPlugin("whoAmI") ?: return

        // Set player reference
        gamePlayer.player = player

        // Hide the player's text display
        gamePlayer.hideDisplay(plugin)
    }
}