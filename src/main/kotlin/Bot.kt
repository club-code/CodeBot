import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.mozilla.javascript.Context
import java.util.regex.Pattern


const val MAX_LENGTH = 2000
const val MAX_TIME = 100

fun main(args: Array<String>) {
    JDABuilder
            .createDefault(args[0])
            .addEventListeners(Bot())
            .build()
}

class Bot : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val channel = event.channel
        val msg = event.message
        val raw = msg.contentRaw

        val lines = raw.split('\n')
        val args = lines[0].split(" ")

        val codeBlockPattern = Pattern.compile("`{1,3}(?:([^`\\n]*)\\n)?([^`]*)`{1,3}")
        val matcher = codeBlockPattern.matcher(raw)

        if (raw.startsWith("!bf")) {
            if (matcher.find()) {
                val brainfuck = matcher.group(2)
                val input = if (args.size > 1) args[1] else ""
                val result = stringToBrainfuck(brainfuck, input).take(MAX_LENGTH)
                if (result.isNotEmpty())
                    channel.sendMessage(result)
                            .queue()
            }
        }
        if (msg.contentRaw.startsWith("!js")) {
            if (matcher.find()) {
                val js = matcher.group(2)
                val result = stringToJS(js)
                if (result.isNotEmpty())
                    channel.sendMessage(result)
                            .queue()
            }
        }
    }

    fun stringToBrainfuck(content: String, input: String): String {
        var ptr = 0
        val tape = ByteArray(10_000)
        val result = StringBuilder()
        var inputPtr = 0

        val time = System.currentTimeMillis()

        var i = 0
        var count = 0
        while (i in content.indices) {
            when (content[i]) {
                '>' -> ptr++
                '<' -> ptr--
                '+' -> tape[ptr]++
                '-' -> tape[ptr]--
                '.' -> result.append(tape[ptr].toChar())
                ',' -> if (inputPtr < input.length) {
                    tape[ptr] = input[inputPtr].toByte()
                    inputPtr++
                }
                '[' -> if (tape[ptr] == 0.toByte()) {
                    i++
                    while (count > 0 || content[i] != ']') {
                        if (content[i] == '[')
                            count++
                        else if (content[i] == ']')
                            count--
                        i++
                    }
                }
                ']' ->
                    if (tape[ptr] != 0.toByte()) {
                        i--
                        while (count > 0 || content[i] != '[') {
                            if (content[i] == ']')
                                count++
                            else if (content[i] == '[')
                                count--
                            i--
                        }
                        i--
                    }
            }
            i++
            if (System.currentTimeMillis() - time > MAX_TIME || result.length >= MAX_LENGTH)
                return result.toString()
        }
        return result.toString()
    }

    fun stringToJS(content: String): String {
        val ctx = TimedFactory().enterContext()
        ctx.languageVersion = Context.VERSION_ES6
        val scope = ctx.initSafeStandardObjects()
        val console = Console()
        scope.put(
                "console",
                scope,
                Context.toObject(console, scope)
        )
        ctx.optimizationLevel = 9
        return try {
            val script = ctx.compileString(content, "my_script_id", 1, null)
            val o = script.exec(ctx, scope)
            if (console.isEmpty())
                "Return: `" + Context.toString(o) + "`"
            else
                "Return: `" + Context.toString(o) + "`\nLogs:\n```\n" + console.toString() + "```"
        } catch (e: Exception) {
            e.message ?: ""
        } finally {
            Context.exit()
        }
    }
}

class Console {
    private val buffer = java.lang.StringBuilder()

    fun log(s: String) {
        buffer.append(s + '\n')
    }

    override fun toString() = buffer.toString()

    fun isEmpty() = buffer.isEmpty()
}
