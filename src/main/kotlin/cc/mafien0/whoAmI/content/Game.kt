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
    var players: MutableList<Player> = mutableListOf()
    val playerInputs: MutableList<String> = mutableListOf()

    fun spawnTextDisplay(player: Player, text: String = "Placeholder Text") {
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
    }

    fun joinPlayer(player: Player) {
        log.info("Trying to join player: $player")

        if (players.size >= 4) {
            player.sendMessage(Component.text("The game is full", NamedTextColor.RED))
            return
        }
        if (players.contains(player)) {
            player.sendMessage(Component.text("You are already in the game", NamedTextColor.RED))
            return
        }

        player.sendMessage(Component.text("You joined the game", NamedTextColor.GREEN))
        players.add(player)
        log.info("player $player joined, players: $players")
    }

    fun leavePlayer(player: Player) {
        if (players.remove(player)) {
            player.sendMessage(Component.text("You left the game", NamedTextColor.YELLOW))
            log.info("player left: $player, players: $players")
        } else {
            player.sendMessage(Component.text("You are not in the game", NamedTextColor.RED))
        }
    }

    fun start() {
        log.info("Starting game")

        players.shuffle()
        playerInputs.clear()
        players.forEach { player ->
            PlayerControl.requestInput(player) { input ->
                playerInputs.add(input)
                player.sendMessage(Component.text("Input accepted", NamedTextColor.GREEN))
                log.info("Player ${player.name} entered: $input")
            }
        }

        playerInputs.addFirst(playerInputs.last())
        playerInputs.removeLast()

    }
}