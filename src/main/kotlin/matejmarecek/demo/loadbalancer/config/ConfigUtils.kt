package matejmarecek.demo.loadbalancer.config

/**
 * Contains util functionality related to the Configuration.
 */

fun parseIntInLimit(toParse: String?, min: Int, max: Int, default: Int): Int {
    if (toParse == null) {
        return default
    }
    val parsedNumber = toParse.toIntOrNull() ?: default
    return if (parsedNumber in min..max) parsedNumber else default
}

fun parseLongInLimit(toParse: String?, min: Long, max: Long, default: Long): Long {
    if (toParse == null) {
        return default
    }
    val parsedNumber = toParse.toLongOrNull() ?: default
    return if (parsedNumber in min..max) parsedNumber else default
}