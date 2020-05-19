import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TestBot {
    lateinit var bot: Bot

    @Before
    fun setUp(){
        bot = Bot()
    }

    @Test
    fun read(){
        assertEquals("Ale", this.bot.stringToBrainfuck(",[.[-],]", "Ale"))
        assertEquals("elA", this.bot.stringToBrainfuck(">,[>,]<[.<]", "Ale"))
        assertEquals("Conte est trop fort ! :heart:", this.bot.stringToBrainfuck("++++[++++>---<]>.+[--->+<]>+++.-.++++++.+++[->+++<]>.--[--->+<]>-.+[->+++<]>++.[--->+<]>----.+.[---->+<]>+++.---[->++++<]>.--.---.+.[------->++<]>.++[->+++<]>.+++++++++.+++.++.[---->+<]>+++.+.-.---[->++<]>.--[--->+<]>.---.----.--[--->+<]>---.++.[-->+<]>.", ""))
    }

    @Test
    fun js(){
        assertEquals("15", bot.stringToJS("function add(i,j){return i+j}; add(10,5);"))
    }
}