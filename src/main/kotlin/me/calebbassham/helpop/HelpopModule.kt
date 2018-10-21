package me.calebbassham.helpop

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class HelpopModule(val config: Config = HelpopModule.DefaultConfig(), private val plugin: JavaPlugin) : Listener {

    private val id = AtomicInteger(1)

    private val helpops = HashMap<Int, Helpop>()

    fun createHelpop(message: String, sender: Player): Helpop {
        val helpopId = id.getAndIncrement()
        val helpop = Helpop(helpopId, message, sender.uniqueId)

        helpops[helpopId] = helpop

        return helpop
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {

        val iter = helpops.iterator()

        while (iter.hasNext()) {
            val helpop = iter.next().value

            if (helpop.asker?.uniqueId != e.player.uniqueId) continue

            helpop.sendAnswerToAsker()

            helpop.delete()
        }

    }

    inner class Helpop(val id: Int, val message: String, asker: UUID) {

        val askerUniqueId = asker

        val asker: Player?
            get() = Bukkit.getPlayer(askerUniqueId)


        var answer: String? = null

        var answererUniqueId: UUID? = null
        val answerer: CommandSender?
            get() {
                if (answererUniqueId == null) {
                    if (answer == null) return null
                    return Bukkit.getConsoleSender()
                }

                return Bukkit.getPlayer(answererUniqueId)
            }

        fun isAnswered() = answer != null

        fun sendQuestion() {
            sendQuestionToAsker()
            sendQuestionToReceivers()
        }

        private fun sendQuestionToAsker() {
            asker?.sendMessage("${config.prefix} ${config.messageColor}$message")
        }

        private fun sendQuestionToReceivers() {
            for (receiver in config.receivers) {
                if (receiver == asker) continue
                receiver.sendMessage("${config.prefix} ${bracket("&6#$id".translateAlternateColorCodes(), config.symbolColor)} ${config.playerNameColor}${asker?.displayName}${config.symbolColor}: ${config.messageColor}$message")
            }
        }

        fun delete() {
            helpops.remove(id)
        }

        fun answer(answer: String, answerer: CommandSender) {
            this.answer = answer
            this.answererUniqueId = (answerer as? Player)?.uniqueId

            if (asker?.isOnline == true) {
                sendAnswerToAsker()
                delete()
            }

            sendAnswerToAnswerer()
            sendAnswerToReceivers()
        }

        fun sendAnswerToAsker() {
            // TODO - Better message format which includes what the question was. Likely multiple lines.

            val answerer = answerer

            val answererName = if (answerer is Player) answerer.displayName else answerer?.name
            asker?.sendMessage("${config.prefix} ${config.messageColor}$answer ${config.playerNameColor}—$answererName")
        }

        private fun sendAnswerToAnswerer() {
            val answerer = answerer

            val answererName = if (answerer is Player) answerer.displayName else answerer?.name
            answerer?.sendMessage("${config.prefix} ${config.messageColor}$answer ${config.playerNameColor}—$answererName")
        }

        private fun sendAnswerToReceivers() {
            // TODO - Better message format which includes what the question was. Likely multiple lines.
            val answerer = answerer

            for (receiver in config.receivers) {
                if (receiver == asker) continue
                if (receiver == answerer) continue

                val answererName = if (answerer is Player) answerer.displayName else answerer?.name
                receiver.sendMessage("${config.prefix} ${config.messageColor}$answer ${config.playerNameColor}—$answererName")
            }
        }

    }

    /* Command */

    inner class Cmd : CommandExecutor {

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            sender as? Player ?: run {
                sender.sendMessage("${config.prefix} ${config.messageColor}Only players can use this command.")
                return true
            }

            val message = args.joinToString(" ")

            if (message.isBlank()) {
                sender.sendMessage("${config.prefix} ${config.messageColor}You can't sendQuestion a blank helpop.")
                return true
            }

            val helpop = createHelpop(message, sender)

            helpop.sendQuestion()

            return true
        }

    }

    @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
    inner class AnswerCmd : CommandExecutor {

        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (args.size < 1) {
                return false // TODO - sendQuestion proper usage message
            }

            val id = args[0].toIntOrNull() ?: run {
                sender.sendMessage("${config.prefix} ${config.messageColor}Invalid helpop id.")
                return true
            }

            if (id < 1) {
                sender.sendMessage("${config.prefix} ${config.messageColor}Invalid helpop id.")
                return true
            }

            if (id >= this@HelpopModule.id.get()) {
                sender.sendMessage("${config.prefix} ${config.messageColor}No helpop with that id.")
                return true
            }

            val helpop = helpops[id] ?: run {
                sender.sendMessage("${config.prefix} ${config.messageColor}That helpop has already been answered.")
                return true
            }

            if (helpop.isAnswered()) {
                sender.sendMessage("${config.prefix} ${config.messageColor}That helpop has already been answered.")
                return true
            }

            val answer = args.drop(1).joinToString(" ")

            if (answer.isBlank()) {
                sender.sendMessage("${config.prefix} ${config.messageColor}You can't send a blank answer.")
                return true
            }

            helpop.answer(answer, sender)

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
        override val prefix = bracket("&6Help-OP", "&8").translateAlternateColorCodes()
        override val messageColor = "&7".translateAlternateColorCodes()
        override val playerNameColor = "&e".translateAlternateColorCodes()
        override val symbolColor = "&8".translateAlternateColorCodes()

        override val receivers: Array<Player>
            get() = Bukkit.getOnlinePlayers().filter { it.hasPermission("helpop.receive") }.toTypedArray()
    }

}