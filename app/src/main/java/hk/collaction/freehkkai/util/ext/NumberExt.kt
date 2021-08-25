package hk.collaction.freehkkai.util.ext

import android.content.res.Resources
import android.util.TypedValue
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

fun Float.dp2px(): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()

fun Float.px2dp(): Float {
    val scale = Resources.getSystem().displayMetrics.density
    return (this / scale + 0.5F)
}

fun Int.px2dp() = this.toFloat().px2dp().toInt()

fun Float.px2sp(): Float {
    val fontScale = Resources.getSystem().displayMetrics.density
    return (this / fontScale + 0.5F)
}

fun Int.px2sp() = this.toFloat().px2sp().toInt()

fun Float.roundTo(n: Int): Float {
    return this.toDouble().roundTo(n).toFloat()
}

fun Double.roundTo(n: Int): Double {
    if (this.isNaN()) return 0.0

    return try {
        BigDecimal(this).setScale(n, RoundingMode.HALF_EVEN).toDouble()
    } catch (e: NumberFormatException) {
        this.roundToInt().toDouble()
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)