package cc.mafien0.whoAmI.game

import cc.mafien0.whoAmI.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("WhoAmI GamePlayer")

class GamePlayer(val player: Player) {
    val playerName: String = player.name
    var assignedText: String? = null
    var textDisplay: TextDisplay? = null
    
    // Input callback for this specific player
    internal var inputCallback: ((String) -> Unit)? = null

    companion object {
        // Private list of GamePlayers in the game
        private val players: MutableList<GamePlayer> = mutableListOf()

        // Max players allowed in the game
        var maxPlayers: Int = Config.getMaxPlayers()

        /**
         * Creates a new GamePlayer instance and adds them to the game.
         *
         * @param player The player attempting to join the game.
         * @return The created GamePlayer instance if successful, null otherwise.
         */
        fun join(player: Player): GamePlayer? {
            log.info("Trying to join player: ${player.name}")

            // Check if game is full
            if (players.size >= maxPlayers) {
                player.sendMessage(Component.text("The game is full", NamedTextColor.RED))
                log.info("Player ${player.name} tried to join, but the game is full")
                return null
            }

            // Check if player is already in the game
            if (findByPlayer(player) != null) {
                player.sendMessage(Component.text("You are already in the game", NamedTextColor.RED))
                log.info("Player ${player.name} tried to join, but they are already in the game")
                return null
            }

            // Create new GamePlayer and add to list
            val gamePlayer = GamePlayer(player)
            players.add(gamePlayer)

            Bukkit.broadcast(
                Component.text(
                    "${player.name} joined the game, players: ${players.size}/$maxPlayers",
                    NamedTextColor.AQUA
                )
            )
            log.info("Player ${player.name} joined, total players: ${players.size}")
            return gamePlayer
        }

        /**
         * Removes a player from the game and cleans up their GamePlayer instance.
         *
         * @param player The player attempting to leave the game.
         * @return True if the player was successfully removed, false otherwise.
         */
        fun leave(player: Player): Boolean {
            val gamePlayer = findByPlayer(player)

            if (gamePlayer != null) {
                // Clean up the GamePlayer
                gamePlayer.cleanup()

                // Remove from list
                players.remove(gamePlayer)

                Bukkit.broadcast(
                    Component.text(
                        "${player.name} left the game, players: ${players.size}/$maxPlayers",
                        NamedTextColor.AQUA
                    )
                )
                log.info("Player ${player.name} left, remaining players: ${players.size}")
                return true
            } else {
                player.sendMessage(Component.text("You are not in the game", NamedTextColor.RED))
                return false
            }
        }

        /**
         * Finds a GamePlayer by their Bukkit Player instance.
         *
         * @param player The Bukkit Player to search for.
         * @return The GamePlayer instance if found, null otherwise.
         */
        fun findByPlayer(player: Player): GamePlayer? {
            return players.find { it.player == player }
        }

        /**
         * Finds a GamePlayer by their player name.
         *
         * @param name The player name to search for.
         * @return The GamePlayer instance if found, null otherwise.
         */
        fun findByName(name: String): GamePlayer? {
            return players.find { it.playerName == name }
        }

        /**
         * Gets all GamePlayers currently in the game.
         *
         * @return A read-only list of GamePlayers.
         */
        fun getAll(): List<GamePlayer> {
            return players.toList()
        }

        /**
         * Gets the count of players currently in the game.
         */
        fun count(): Int {
            return players.size
        }

        /**
         * Checks if the game has enough players to start.
         */
        fun hasEnoughPlayers(): Boolean {
            return players.size == maxPlayers
        }

        /**
         * Clears all players from the game.
         */
        fun clearAll() {
            players.forEach { it.cleanup() }
            players.clear()
            log.info("All players cleared from the game")
        }

        /**
         * Handles chat input for any player who has a pending input request.
         * Should be called from the chat event listener.
         *
         * @param player The player who sent the chat message
         * @param message The message content
         * @return True if the message was consumed as input, false otherwise
         */
        fun handleChatInput(player: Player, message: String): Boolean {
            val gamePlayer = findByPlayer(player)
            val callback = gamePlayer?.inputCallback

            if (callback != null) {
                gamePlayer.inputCallback = null
                callback(message)
                return true
            }
            return false
        }
    }

    /**
     * Requests input from this player.
     *
     * @param callback Function to call when the player provides input
     */
    fun requestInput(inputText: String = "Input:", callback: (String) -> Unit) {
        player.sendMessage(Component.text(inputText, NamedTextColor.BLUE))
        inputCallback = callback
    }

    /**
     * Cleans up resources associated with this GamePlayer.
     * Called when the player leaves the game.
     */
    private fun cleanup() {
        // Remove text display if it exists
        textDisplay?.remove()
        textDisplay = null
        assignedText = null
        inputCallback = null
        log.info("Cleaned up GamePlayer for $playerName")
    }
}