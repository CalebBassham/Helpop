package me.calebbassham.helpop

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger

class HelpopModule(val config: Config = HelpopModule.DefaultConfig()) {

    private val id = AtomicInteger(1)

    private val helpops = HashMap<Int, Helpop>()

    fun createHelpop(message: String, sender: Player): Helpop {
        val helpopId = id.getAndIncrement()
        val helpop = Helpop(helpopId, message, sender)

        helpops[helpopId] = helpop

        return helpop
    }

    inner class Helpop(val id: Int, val message: String, val sender: Player) {

        fun send() {
            sendToSender()
            sendToReceivers()
        }

        private fun sendToSender() {
            sender.sendMessage("${config.prefix} ${config.messageColor}$message")
        }

        private fun sendToReceivers() {
            for(receiver in config.receivers) {
                receiver.sendMessage("${config.prefix} ${config.playerNameColor}${sender.displayName}${config.symbolColor}: ${config.messageColor}$message")
            }
        }

        fun delete() {
            helpops.remove(id)
        }

    }

    /* Command */

    inner class Cmd : CommandExecutor {

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            sender as? Player ?: run {
                sender.sendMessage("Only players can use this command.")
                return true
            }

            val message = args.joinToString(" ")
            val helpop = createHelpop(message, sender)

            helpop.send()

            helpop.delete() // TODO - Remove when you can answer helpops.

            return true
        }

    }

    /* Config */

    interface Config {
        val prefix: String
        val playerNameColor: String
        val messageColor: String
        val symbolColor: String
        val receivers: Array<Player>
    }

    class DefaultConfig : Config {
        override val prefix = "&8[&6Help-OP&8]".translateAlternateColorCodes()
        override val messageColor = "&7".translateAlternateColorCodes()
        override val playerNameColor = "&e".translateAlternateColorCodes()
        override val symbolColor = "&8".translateAlternateColorCodes()

        override val receivers: Array<Player>
            get() = Bukkit.getOnlinePlayers().filter { it.hasPermission("helpop.receive") }.toTypedArray()
    }

}