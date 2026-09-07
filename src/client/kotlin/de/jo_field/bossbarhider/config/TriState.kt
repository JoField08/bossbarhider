package de.jo_field.bossbarhider.config

enum class TriState(val displayName: String) {
    ANY("Any"),
    REQUIRE("Must have"),
    FORBID("Must not have");

    fun next(): TriState = entries[(ordinal + 1) % entries.size]

    /** Returns true if the actual boolean [value] satisfies this condition. */
    fun matches(value: Boolean): Boolean = when (this) {
        ANY -> true
        REQUIRE -> value
        FORBID -> !value
    }
}
