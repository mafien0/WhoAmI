package cc.mafien0.whoAmI

import cc.mafien0.whoAmI.content.Commands
import cc.mafien0.whoAmI.content.Config
import cc.mafien0.whoAmI.content.PlayerControl
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("WhoAmI")
class WhoAmI : JavaPlugin() {

    override fun onLoad() {
        log.info("Loaded WhoAmI")
        CommandAPI.onLoad(CommandAPIPaperConfig(this).verboseOutput(true))
        Commands().load()
    }

    override fun onEnable() {
        log.info("Enabled WhoAmI")
        CommandAPI.onEnable()
        Config.init(this)
        server.pluginManager.registerEvents(PlayerControl, this)
    }

    override fun onDisable() {
        log.info("Disabled WhoAmI")
        CommandAPI.onDisable()
    }
}
