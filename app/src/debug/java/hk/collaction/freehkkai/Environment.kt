package hk.collaction.freehkkai

enum class Environment(private val debug: Boolean, private val beta: Boolean) {
    CONFIG(true, false);

    fun isBeta(): Boolean {
        return beta
    }

    fun isDebug(): Boolean {
        return debug
    }

}