package cc.mafien0.whoAmI.content

import dev.geco.gsit.api.GSitAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

private val log = org.slf4j.LoggerFactory.getLogger("WhoAmI Game")
object Game {

    // Get the max value of players from the config
    val maxPlayers: Int
        get() = Config.getValue("config.max-players")?.toString()?.toIntOrNull() ?: 4

    // Pre-game variables
    var players: MutableList<Player> = mutableListOf()

    // in-game variables
    val playerInputs: MutableList<String> = mutableListOf()
    var inputsReceived = 0
    var totalPlayers = 0

    /**
     * Spawns a floating text display above the player's current location.
     *
     * @param player The player above whose location the text display will spawn.
     * @param text The text to be displayed. Defaults to "Placeholder Text" if not provided.
     * @return True if the text display was successfully spawned.
     */
    fun spawnTextDisplay(player: Player, coords: Location, text: String = "Placeholder Text"): Boolean {
        val world = player.world
        world.spawn(coords, TextDisplay::class.java) { entity ->
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
        if (players.size >= maxPlayers) { // Size check
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
        // Tries to remove player from list
        if (players.remove(player)) {
            // If successful, send feedback
            player.sendMessage(Component.text("You left the game", NamedTextColor.YELLOW))
            log.info("player left: $player, players: $players")
            return true
        } else {
            // If unsuccessful, send feedback too
            player.sendMessage(Component.text("You are not in the game", NamedTextColor.RED))
            return false
        }
    }

    /**
     * Rotates the elements in the given mutable list by moving the last element
     * to the front and shifting the remaining elements to the right.
     *
     * @param list The mutable list of strings to be rotated. The list must contain
     *             at least two elements; otherwise, no operation is performed.
     */
    fun rotateList(list: MutableList<String>) {
        if (list.size < 2) return // Do nothing if list is too small
        list.addFirst(list.last())
        list.removeLast()
    }

    /**
     * Actions:
     * - Reset variables
     * - Shuffle players
     * - Run checks
     * @param player The player who initiated the game
     * @return True, if all the checks passed, false otherwise
     */
    fun preRun(player: Player): Boolean {

        // Reset variables
        playerInputs.clear()
        inputsReceived = 0
        totalPlayers = players.size

        // Shuffle players
        players.shuffle()

        // Checks
        if (totalPlayers < maxPlayers) {
            player.sendMessage(Component.text("Not enough players", NamedTextColor.RED))
            log.info("Player $player tried to run the game, but not enough players")
            return false
        }
        if (totalPlayers > maxPlayers) {
            player.sendMessage(Component.text("Too many players", NamedTextColor.RED))
            log.info("Player $player tried to run the game, but too many players")
            return false
        }

        // If everything checks out, continue
        log.info("Player $player successfuly started the game")
        return true
    }

    /**
     * Actions:
     * - Request player inputs
     * - Rotate list
     * - Log status
     * - Run checks
     * @param player The player who initiated the game
     * @return True, if successful, false otherwise
     */
    fun firstStep(player: Player): Boolean {
        // Iterate through players and request inputs
        players.forEach { p ->
            PlayerControl.requestInput(p) { input ->
                playerInputs.add(input)
                p.sendMessage(Component.text("Input accepted", NamedTextColor.GREEN))
                log.info("Player ${p.name} entered: $input")

                // Count the inputs and continue when all players have submitted their inputs
                inputsReceived++
                if (inputsReceived >= totalPlayers) {
                    // Rotate list
                    rotateList(playerInputs)
                
                    // Log status
                    log.info("Player inputs: $playerInputs")
                    log.info("Total players: $totalPlayers")
                    log.info("Inputs received: $inputsReceived")

                    log.info("Starting second step")
                    secondStep(player)
                }
            }
        }

        return true
    }

    /**
     * Actions:
     * - Sit players on their places
     * - Spawn text displays in their positions
     * @param player The player who initiated the game
     * @return True, if successful, false otherwise
     */
    fun secondStep(player: Player): Boolean {
        // Switch to main thread for entity spawning
        val plugin = Bukkit.getPluginManager().getPlugin("whoAmI")
        Bukkit.getScheduler().runTask(plugin!!, Runnable {

            players.forEachIndexed { index, p ->
                // Load positions from the config
                val coords = Config.getPosition(index)?.block?.location
                if (coords == null) {
                    player.sendMessage(Component.text("Position $index not configured! Use /addposition $index", NamedTextColor.RED))
                    log.error("Position $index is not configured in config")
                    return@Runnable
                }
                log.info("Got coords")

                // Sit players on their places using GSit API
                GSitAPI.createSeat(coords.block, p)
                log.info("Sitting player $p on coords $coords")

                // Spawn text display centered on a block
                spawnTextDisplay(p, coords.clone().add(0.5, 3.0, 0.5), playerInputs[index])
                log.info("Spawned text display")
            }
        })
        // TODO: Add checks
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
            log.info("Tried to stop the game, but the game is not running")
            player.sendMessage(Component.text("The game is not running!", NamedTextColor.RED))
            return false
        }
    }

    /**
     * TODO
     */
    fun start(player: Player): Boolean {
        log.info("Starting game")

        // Pre run settings and checks
        if(!preRun(player)) {
            log.info("PreRun failed, game not started")
            return false
        }
        // Start steps
        log.info("Starting first step")
        if(!firstStep(player)) {
            log.info("FirstStep failed, game not started")
            return false
        }
        // Next steps are being run in the first step because first step is async, and we need to wait for it.
        return true
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
        start(player)
        return true
    }
}