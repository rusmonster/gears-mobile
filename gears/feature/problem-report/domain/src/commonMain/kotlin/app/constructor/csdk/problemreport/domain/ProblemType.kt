@file:Suppress("ktlint:constructor:missing-enum-entry-kdoc")

package app.constructor.csdk.problemreport.domain

/** Categories a user can assign to a problem report. */
enum class ProblemType(val index: Int) {
    BUG(0),
    UI(1),
    PERFORMANCE(2),
    ACCOUNT(3),
    CONTENT(4),
    OTHER(5),
    ;

    companion object {
        /** Returns the type for [index], or `null` when no category maps to it. */
        fun fromIndexOrNull(index: Int): ProblemType? = entries.firstOrNull { it.index == index }
    }
}
