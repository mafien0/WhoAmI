package cc.mafien0.whoAmI

import cc.mafien0.whoAmI.commands.Commands
import cc.mafien0.whoAmI.config.Config
import cc.mafien0.whoAmI.listeners.ChatListener
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
        Config.init(this)
        CommandAPI.onEnable()
        server.pluginManager.registerEvents(ChatListener, this)
    }

    override fun onDisable() {
        log.info("Disabled WhoAmI")
        CommandAPI.onDisable()
    }
}
