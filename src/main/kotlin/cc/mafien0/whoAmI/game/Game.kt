package cc.mafien0.whoAmI.game

import cc.mafien0.whoAmI.config.Config
import dev.geco.gsit.api.GSitAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("WhoAmI Game")

object Game {

    // In-game variables
    private val playerInputs: MutableList<String> = mutableListOf()
    private var inputsReceived = 0

    /**
     * Spawns a floating text display above the player's current location.
     *
     * @param player The player above whose location the text display will spawn.
     * @param coords The location where the text display will be spawned.
     * @param text The text to be displayed. Defaults to "Placeholder Text" if not provided.
     * @param tag The scoreboard tag to add to the entity. Defaults to "gameText".
     * @return The TextDisplay entity itself, for later use.
     */
    fun spawnTextDisplay(
        player: Player,
        coords: Location,
        text: String = "Placeholder Text",
        tag: String = "gameText"
    ): TextDisplay {
        val world = player.world
        val display = world.spawn(coords, TextDisplay::class.java) { entity ->
            entity.text(Component.text(text, NamedTextColor.WHITE))
            entity.billboard = Display.Billboard.CENTER
            entity.backgroundColor = Color.fromARGB(70, 0, 0, 0)
            entity.transformation = Transformation(
                Vector3f(),
                AxisAngle4f(),
                Vector3f(2f, 2f, 2f),
                AxisAngle4f()
            )
            entity.addScoreboardTag(tag)
        }
        return display
    }

    /**
     * Removes all entities in the given world that have the specified scoreboard tag.
     *
     * @param world The world in which the entities will be checked and removed.
     * @param tag The scoreboard tag used to identify entities for removal. Defaults to "gameTag".
     */
    fun killByTag(world: World, tag: String = "gameText") {
        world.entities.forEach { entity ->
            if (entity.scoreboardTags.contains(tag)) entity.remove()
        }
    }

    /**
     * Rotates the elements in the given mutable list by moving the last element
     * to the front and shifting the remaining elements to the right.
     *
     * @param list The mutable list of strings to be rotated.
     */
    private fun rotateList(list: MutableList<String>) {
        if (list.size < 2) return
        list.addFirst(list.last())
        list.removeLast()
    }

    /**
     * Prepares the game for execution by resetting variables, shuffling players, and running checks.
     *
     * @param player The player who initiated the game.
     * @return True if all the checks passed, false otherwise.
     */
    private fun preRun(player: Player): Boolean {
        log.info("PreRun started")

        // Reset variables
        inputsReceived = 0
        val totalPlayers = GamePlayer.count()

        playerInputs.clear()
        repeat(totalPlayers) { playerInputs.add("") }

        // Get all players as a mutable list and shuffle
        val gamePlayers = GamePlayer.getAll().toMutableList()
        gamePlayers.shuffle()

        // Checks
        if (!GamePlayer.hasEnoughPlayers()) {
            player.sendMessage(Component.text("Not enough players (${totalPlayers}/${GamePlayer.maxPlayers})", NamedTextColor.RED))
            log.info("Player ${player.name} tried to run the game, but not enough players")
            return false
        }

        // If everything checks out, continue
        log.info("Player ${player.name} successfully started the game")
        return true
    }

    /**
     * First step: Request player inputs, rotate the list, and proceed to the next step.
     *
     * @param player The player who initiated the game.
     * @return True if successful, false otherwise.
     */
    fun firstStep(player: Player): Boolean {
        val gamePlayers = GamePlayer.getAll()
        val totalPlayers = gamePlayers.size

        // Request inputs from all players
        gamePlayers.forEachIndexed { index, gamePlayer ->
            gamePlayer.requestInput("Name a minecraft item:") { input ->
                // Store the input
                playerInputs[index] = input
                inputsReceived++

                Bukkit.broadcast(
                    Component.text(
                        "${gamePlayer.playerName} submitted their input: $inputsReceived/$totalPlayers",
                        NamedTextColor.AQUA
                    )
                )
                log.info("Player ${gamePlayer.playerName} entered: $input")

                // When all inputs are received, proceed to next step
                if (inputsReceived >= totalPlayers) {
                    // Rotate list so each player gets someone else's text
                    rotateList(playerInputs)

                    // Update assigned texts after rotation
                    gamePlayers.forEachIndexed { idx, gp ->
                        gp.assignedText = playerInputs[idx]
                    }

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
     * Second step: Sit players in their places, spawn text displays, and hide them from respective players.
     *
     * @param player The player who initiated the game.
     * @return True if successful, false otherwise.
     */
    fun secondStep(player: Player): Boolean {
        // Get plugin instance
        val plugin = Bukkit.getPluginManager().getPlugin("whoAmI")
        if (plugin == null) {
            log.error("Plugin instance is null!")
            return false
        }

        // Switch to main thread for entity spawning
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val gamePlayers = GamePlayer.getAll()

            gamePlayers.forEachIndexed { index, gamePlayer ->
                // Load positions from the config
                val coords = Config.getPosition(index)?.block?.location
                if (coords == null) {
                    player.sendMessage(
                        Component.text(
                            "Position $index not configured! Use /addposition ${index + 1}",
                            NamedTextColor.RED
                        )
                    )
                    log.error("Position $index is not configured in config")
                    return@Runnable
                }

                // Sit player on their seat using GSit API
                GSitAPI.createSeat(coords.block, gamePlayer.player)
                log.info("Sitting player ${gamePlayer.playerName} on coords $coords")

                // Spawn text display centered on the block
                val text = gamePlayer.assignedText ?: "Error: No text assigned"
                val display = spawnTextDisplay(
                    gamePlayer.player,
                    coords.clone().add(0.5, 3.0, 0.5),
                    text
                )
                gamePlayer.textDisplay = display
                log.info("Spawned text display for ${gamePlayer.playerName}")

                // Hide text display from the player
                gamePlayer.player.hideEntity(plugin, gamePlayer.textDisplay!!)
                log.info("Hidden text display from ${gamePlayer.playerName}")
            }

            Bukkit.broadcast(Component.text("Game started!", NamedTextColor.GREEN))
            // TODO: Add sound
        })

        return true
    }

    /**
     * Checks if the game is currently running.
     *
     * @return True if the game is running, false otherwise.
     */
    fun isRunning(): Boolean {
        // TODO
        return true
    }

    /**
     * Starts the game.
     *
     * @param player The player who initiated the start command.
     * @return True, if the game successfully started, false otherwise.
     */
    fun start(player: Player): Boolean {
        log.info("Starting game")

        // Pre-run settings and checks
        if (!preRun(player)) {
            log.info("PreRun failed, game not started")
            return false
        }

        // Start first step
        log.info("Starting first step")
        if (!firstStep(player)) {
            log.info("FirstStep failed, game not started")
            return false
        }

        // Next steps are run in the first step because it's async
        return true
    }

    /**
     * Stops the game if it is currently running.
     *
     * @param player The player who initiated the stop command.
     * @return True, if the game was successfully stopped, false otherwise.
     */
    fun stop(player: Player): Boolean {
        if (isRunning() || Config.isDebug()) {
            // Kill all game entities
            killByTag(player.world, "gameText")

            // Clean up all game players
            GamePlayer.clearAll()

            // Reset game state
            playerInputs.clear()
            inputsReceived = 0

            Bukkit.broadcast(Component.text("Game stopped!", NamedTextColor.YELLOW))
            log.info("Game stopped by ${player.name}")
            return true
        } else {
            log.info("Tried to stop the game, but the game is not running")
            player.sendMessage(Component.text("The game is not running!", NamedTextColor.RED))
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
            stop(player)
        }
        start(player)
        return true
    }
}