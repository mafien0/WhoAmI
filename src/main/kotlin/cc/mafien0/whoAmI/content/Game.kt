package cc.mafien0.whoAmI.content

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

private val log = org.slf4j.LoggerFactory.getLogger("WhoAmI Game")
object Game {
    var players: MutableList<Player> = mutableListOf() // Define a list of players
    val playerInputs: MutableList<String> = mutableListOf() // Define a list of player inputs
    val maxPlayers = Config.getValue("max-players") // Get the max players value from config

    /**
     * Spawns a floating text display above the player's current location.
     *
     * @param player The player above whose location the text display will spawn.
     * @param text The text to be displayed. Defaults to "Placeholder Text" if not provided.
     * @return True if the text display was successfully spawned.
     */
    fun spawnTextDisplay(player: Player, text: String = "Placeholder Text"): Boolean {
        val world = player.world
        val spawnLocation = player.location.add(0.0, 3.0, 0.0)
        world.spawn(spawnLocation, TextDisplay::class.java) { entity ->
            entity.text(Component.text(text, NamedTextColor.WHITE))
            entity.billboard = Display.Billboard.CENTER
            entity.backgroundColor = Color.fromARGB(70, 0, 0, 0)
            entity.transformation = Transformation(
                Vector3f(),
                AxisAngle4f(),
                Vector3f(2f, 2f, 2f),
                AxisAngle4f()
            )
        }
        return true
    }

    /**
     * Attempts to add a player to the game. Ensures that the player is not already in the game
     * and that the maximum number of players has not been reached.
     *
     * @param player The player attempting to join the game.
     * @return `true` if the player successfully joined the game, `false` if the player could not join
     * due to being already in the game or because the game is full.
     */
    fun joinPlayer(player: Player): Boolean {
        log.info("Trying to join player: $player")

        // Checks
        if (players.size >= 4) { // Size check
            player.sendMessage(Component.text("The game is full", NamedTextColor.RED))
            return false
        }
        if (players.contains(player)) { // Is player already in game?
            player.sendMessage(Component.text("You are already in the game", NamedTextColor.RED))
            return false
        }

        player.sendMessage(Component.text("You joined the game", NamedTextColor.GREEN))
        players.add(player)
        log.info("player $player joined, players: $players")
        return true
    }

    /**
     * Removes a player from the game if they are currently part of it.
     *
     * @param player The player attempting to leave the game.
     * @return True if the player was successfully removed from the game,
     *         otherwise false if the player was not part of the game.
     */
    fun leavePlayer(player: Player): Boolean {
        if (players.remove(player)) {
            player.sendMessage(Component.text("You left the game", NamedTextColor.YELLOW))
            log.info("player left: $player, players: $players")
            return true
        // Else, burn his family alive
        } else {
            player.sendMessage(Component.text("You are not in the game", NamedTextColor.RED))
            return false
        }
    }

    // TODO
    fun start(): Boolean {
        log.info("Starting game")

        // Shuffle players, ask each one for an input
        players.shuffle()
        playerInputs.clear()
        players.forEach { player ->
            PlayerControl.requestInput(player) { input ->
                playerInputs.add(input)
                player.sendMessage(Component.text("Input accepted", NamedTextColor.GREEN))
                log.info("Player ${player.name} entered: $input")
            }
        }

        // Rotate the list
        playerInputs.addFirst(playerInputs.last())
        playerInputs.removeLast()

        return true
    }

    // TODO
    fun isRunning(): Boolean {
        return true
    }

    /**
     * Stops the game if it is currently running. Provides feedback to the player
     * whether the operation was successful.
     *
     * @param player The player who initiated the stop command.
     * @return True, if the game was successfully stopped, false otherwise.
     */
    fun stop(player: Player): Boolean {
        if (isRunning()) {
            // TODO
            return true
        } else {
            log.info("Game is not running!")
            player.sendMessage(Component.text("Game is not running!", NamedTextColor.RED))
            return false
        }
    }

    /**
     * Restarts the game. Stops the game if it is currently running and starts it again.
     *
     * @param player The player who initiated the restart.
     * @return True, if the game successfully restarted, false otherwise.
     */
    fun restart(player: Player): Boolean {
        // If the game is running, stop it
        if (isRunning()) {
            stop(player) // Player gets feedback
        }
        start()
        return true
    }
}