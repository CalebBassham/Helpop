package me.calebbassham.helpop

import org.bukkit.ChatColor

fun String.translateAlternateColorCodes(altColorChar: Char = '&'): String = ChatColor.translateAlternateColorCodes(altColorChar, this)