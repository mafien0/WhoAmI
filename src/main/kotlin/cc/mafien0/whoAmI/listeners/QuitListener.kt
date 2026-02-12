package cc.mafien0.whoAmI.listeners

import cc.mafien0.whoAmI.game.Game
import cc.mafien0.whoAmI.game.GamePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object QuitListener: Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // If the game is running, don't allow players to leave'
        if (Game.isRunning) return

        val player = event.player
        GamePlayer.leave(player)
    }
}
