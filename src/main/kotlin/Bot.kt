import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.mozilla.javascript.Context


const val MAX_LENGTH = 2000
const val MAX_TIME = 100

fun main(args: Array<String>){
    JDABuilder.createDefault(args[0])
        .addEventListeners(Bot())
        .build()
}

class Bot: ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent){
        val msg = event.message
        if(msg.contentRaw.contains("!bf")){
            val arguments = msg.contentRaw.split(" ")
            val brainfuck = arguments[1]
            val input = if(arguments.size > 2) arguments[2] else ""
            val channel = event.channel
            val result = stringToBrainfuck(brainfuck, input).take(MAX_LENGTH)
            channel.sendMessage(result)
                .queue()
        }
        if(msg.contentRaw.contains("!js")){
            val arguments = msg.contentRaw.split(" ")
            stringToJS(arguments[1])
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
        while(i in content.indices){
            when(content[i]){
                '>'->ptr++
                '<'->ptr--
                '+'->tape[ptr]++
                '-'->tape[ptr]--
                '.'->result.append(tape[ptr].toChar())
                ','->if(inputPtr < input.length){
                    tape[ptr] = input[inputPtr].toByte()
                    inputPtr++
                }
                '['->if (tape[ptr] == 0.toByte()) {
                        i++
                        while (count > 0 || content[i] != ']')
                        {
                            if (content[i] == '[')
                                count++
                            else if (content[i] == ']')
                                count--
                            i++
                        }
                    }
                ']'->
                    if (tape[ptr] != 0.toByte())
                    {
                        i--
                        while (count > 0 || content[i] != '[')
                        {
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
            if(System.currentTimeMillis() - time > MAX_TIME || result.length >= MAX_LENGTH)
                return result.toString()
        }
        return result.toString()
    }

    fun stringToJS(content: String): String{
        val ctx = Context.enter()
        val scope = ctx.initSafeStandardObjects()
        ctx.optimizationLevel = 9
        return try {
            val script = ctx.compileString(content, "my_script_id", 1, null)
            val o = script.exec(ctx, scope)
            Context.toString(o)
        } catch (e: Exception){
            e.message ?: ""
        } finally {
            Context.exit()
        }
    }
}