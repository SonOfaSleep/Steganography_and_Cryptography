package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        when (val input: String = readLine()!!.lowercase()) {
            "exit" -> break
            "hide" -> hideMenu()
            "show" -> showMenu()
            else -> println("Wrong task: $input")
        }
    }
    println("Bye!")
}

fun hideMenu() {
    println("Input image file:")
    val inputFile = File(readLine()!!)
    println("Output image file:")
    val outputFile = File(readLine()!!)
    println("Message to hide:")
    val message = readLine()!!
    val pixelCountMessage = message.length * 8 + 24
    println("Password:")
    val password = readLine()!!

    try {
        val bufferedImage = ImageIO.read(inputFile)
        val pixelCountImage = bufferedImage.width * bufferedImage.height

        if (pixelCountMessage > pixelCountImage) {
            println("The input image is not large enough to hold this message.")
            return
        }
        ImageIO.write(hideMessage(bufferedImage, message, password), "PNG", outputFile)
        println("Message saved in $outputFile image.")
    }
    catch (e: Exception) {
        println(e.message)
        return
    }
}

fun hideMessage(bufferedImage: BufferedImage, message: String, password: String): BufferedImage {

    fun encryptBinary(message: String, password: String): String {
        val binaryMessageString = toBinary(message)
        val binaryPassword = toBinary(password)
        var encryptedBinaryString = ""

        var passwordIndex = 0
        for (char in binaryMessageString) {
            if (passwordIndex == binaryPassword.length) passwordIndex = 0
            encryptedBinaryString += char.digitToInt() xor binaryPassword[passwordIndex].digitToInt()
            passwordIndex++
        }
        encryptedBinaryString += "000000000000000000000011"
        return encryptedBinaryString
    }

    val encryptedBinaryString = encryptBinary(message, password)
    var encryptedBinaryStringIndex = 0
    mother@ for (y in 0 until bufferedImage.height) {
        for (x in 0 until bufferedImage.width) {
            if (encryptedBinaryStringIndex > encryptedBinaryString.lastIndex) break@mother

            val color = Color(bufferedImage.getRGB(x, y))
            val r = color.red
            val g = color.green
            var bluePixel = color.blue
            val bit = encryptedBinaryString[encryptedBinaryStringIndex]
            if (bluePixel % 2 == bit.digitToInt()) {
                encryptedBinaryStringIndex++
                continue
            } else if (bluePixel % 2 == 1 && bit.digitToInt() == 0) {
                bluePixel--
            } else {
                bluePixel++
            }
            val newColor = Color(r, g, bluePixel)
            bufferedImage.setRGB(x, y, newColor.rgb)
            encryptedBinaryStringIndex++
        }
    }
    return bufferedImage
}

fun showMenu() {
    println("Input image file:")
    val fileWithMessage = File(readLine()!!)
    println("Password:")
    val password = readLine()!!
    try {
        showMessage(bufferedImage = ImageIO.read(fileWithMessage), password)
    }
    catch (e: Exception) {
        println(e.message)
        return
    }
}

fun showMessage(bufferedImage: BufferedImage, password: String) {

    fun getMessageFromImage(bufferedImage: BufferedImage): String {
        var messageBinary = ""
        var iteration = 0
        mother@ for (y in 0 until bufferedImage.height) {
            for (x in 0 until bufferedImage.width) {
                val color = Color(bufferedImage.getRGB(x, y))
                messageBinary += color.blue % 2
                iteration++
                if (iteration > 24) {
                    val substring = messageBinary.subSequence(messageBinary.length - 24, messageBinary.length)
                    if (substring == "000000000000000000000011") break@mother
                }
            }
        }
        return messageBinary
    }

    fun decodeMessage(password: String): String {
        val messageBinary = getMessageFromImage(bufferedImage)
        val passwordBinary = toBinary(password)
        var decodedMessageBinary = ""

        var passwordIndex = 0
        for (char in messageBinary) {
            if (passwordIndex == passwordBinary.length) passwordIndex = 0
            decodedMessageBinary += char.digitToInt() xor passwordBinary[passwordIndex].digitToInt()
            passwordIndex++
        }
        val decodedMessageList = decodedMessageBinary.chunked(8).toMutableList()
        repeat(3) { decodedMessageList.removeLast() }
        return decodedMessageList.map { it.toInt(2).toChar() }.joinToString("")
    }
    println("Message:")
    println(decodeMessage(password))
}

fun toBinary(string: String): String {
    val binaryList = string.toByteArray().map { it.toString(2) }.toList()
    var binaryString = ""
    for (byte in binaryList) {
        if (byte.length < 8) binaryString += "0".repeat(8 - byte.length)
        binaryString += byte
    }
    return binaryString
}