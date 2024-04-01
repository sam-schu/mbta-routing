package view

import model.SubwayModel
import java.util.Scanner

/**
 * Enables information to be input and output, including the output of data from
 * a subway model.
 */
interface SubwayView {
    /**
     * Outputs the given string.
     */
    fun renderString(msg: String)

    /**
     * Outputs the given string, with a line break following it.
     */
    fun renderStringLn(msg: String = "")

    /**
     * Outputs the names of the subway routes stored by the view's associated
     * subway model. The route names are formatted into a list for display, with
     * a line break following it.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     */
    fun renderAllSubwayRoutes()

    /**
     * Blocks if necessary to read a line of input, and then returns the
     * inputted string (without the line separator character at the end if
     * originally present).
     *
     * @throws NoSuchElementException if the view's input source is unable to
     * provide more input.
     */
    fun inputLine(): String
}

/**
 * Enables text to be input from a Readable and output from an Appendable,
 * including the output of data from a subway model.
 *
 * @param model the subway model from which data should be obtained for display.
 * @param readable the Readable source from which input should be read.
 * @param output the Appendable to which textual output should be appended.
 */
class TextualSubwayView(
    private val model: SubwayModel,
    readable: Readable,
    private val output: Appendable
) : SubwayView {
    private val input: Scanner = Scanner(readable)

    override fun renderString(msg: String) {
        output.append(msg)
    }

    override fun renderStringLn(msg: String) {
        output.append(msg).append('\n')
    }

    override fun renderAllSubwayRoutes() {
        val routeNames = model.getSubwayRoutes().map { it.name }
        renderStringLn(formatList(routeNames))
    }

    override fun inputLine(): String {
        return input.nextLine()
    }

    /*
    Returns a String representation of the given list, separating items with
    commas when necessary and providing a final "and". "<none>" is returned
    if the given list is empty.

    Examples:
    [] -> "<none>"
    ["red"] -> "red"
    ["red", "blue"] -> "red and blue"
    ["red", "blue", "green"] -> "red, blue, and green"
     */
    private fun formatList(items: List<String>): String {
        if (items.isEmpty()) {
            return "<none>"
        }

        val outputBuilder = StringBuilder()
        val numItems = items.size

        items.forEachIndexed { i, str ->
            outputBuilder.append(str)
            if (i <= numItems - 3) {
                outputBuilder.append(", ")
            } else if (i == numItems - 2) {
                outputBuilder.append(
                    if (numItems > 2) {
                        ", and "
                    } else {
                        " and "
                    }
                )
            }
        }
        return outputBuilder.toString()
    }
}