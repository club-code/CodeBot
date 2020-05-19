import org.mozilla.javascript.*


internal class TimedFactory : ContextFactory() {
    private inner class MyContext : Context(this) {
        var startTime: Long = 0
    }

    companion object {
        init {
            initGlobal(TimedFactory())
        }
    }

    override fun makeContext(): Context {
        val cx = MyContext()
        // Make Rhino runtime to call observeInstructionCount
        // each 10000 bytecode instructions
        cx.instructionObserverThreshold = 10000
        return cx
    }

    override fun hasFeature(cx: Context?, featureIndex: Int): Boolean {
        // Turn on maximum compatibility with MSIE scripts
        when (featureIndex) {
            Context.FEATURE_NON_ECMA_GET_YEAR -> return true
            Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME -> return true
            Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER -> return true
            Context.FEATURE_PARENT_PROTO_PROPERTIES -> return false
        }
        return super.hasFeature(cx, featureIndex)
    }

    override fun observeInstructionCount(cx: Context, instructionCount: Int) {
        val mcx = cx as MyContext
        val currentTime = System.currentTimeMillis()
        if (currentTime - mcx.startTime > 100) {
            // More then 10 seconds from Context creation time:
            // it is time to stop the script.
            // Throw Error instance to ensure that script will never
            // get control back through catch or finally.
            throw Error()
        }
    }

    override fun doTopCall(
        callable: Callable?,
        cx: Context, scope: Scriptable?,
        thisObj: Scriptable?, args: Array<Any?>?
    ): Any {
        val mcx = cx as MyContext
        mcx.startTime = System.currentTimeMillis()
        return super.doTopCall(callable, cx, scope, thisObj, args)
    }
}