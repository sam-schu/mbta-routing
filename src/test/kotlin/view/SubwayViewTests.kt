package view

import model.MbtaSubwayModel
import model.MockSubwayModel
import model.MutableSubwayModel
import model.Route
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader
import kotlin.test.assertEquals

/**
 * Includes tests for SubwayView.kt. These tests do not rely on data from the
 * true MBTA API.
 */
class SubwayViewTests {
    private lateinit var basicModel: MutableSubwayModel
    private lateinit var emptyInput: StringReader
    private lateinit var output: StringBuilder
    private lateinit var basicView: SubwayView

    @BeforeEach
    fun setUp() {
        basicModel = MbtaSubwayModel()
        emptyInput = StringReader("")
        output = StringBuilder()
        basicView = TextualSubwayView(basicModel, emptyInput, output)
    }

    @Test
    fun testRenderString() {
        assertEquals("", output.toString())

        basicView.renderString("")

        assertEquals("", output.toString())

        basicView.renderString("hello")

        assertEquals("hello", output.toString())

        basicView.renderString(" world\nhow are you?")

        assertEquals("hello world\nhow are you?", output.toString())
    }

    @Test
    fun testRenderStringLn() {
        assertEquals("", output.toString())

        basicView.renderStringLn()

        assertEquals("\n", output.toString())

        basicView.renderStringLn("")

        assertEquals("\n\n", output.toString())

        basicView.renderStringLn("hello")

        assertEquals("\n\nhello\n", output.toString())

        basicView.renderStringLn("world\nhow are you?")

        assertEquals("\n\nhello\nworld\nhow are you?\n", output.toString())

        basicView.renderStringLn()

        assertEquals("\n\nhello\nworld\nhow are you?\n\n", output.toString())
    }

    @Test
    fun testRenderAllSubwayRoutes() {
        // Route data has not been loaded
        val model = MockSubwayModel()
        var view = TextualSubwayView(model, emptyInput, output)

        assertThrows<IllegalStateException> { view.renderAllSubwayRoutes() }

        model.loadRouteData()

        view.renderAllSubwayRoutes()
        assertEquals(
            "<none>\n",
            output.toString()
        )

        view.renderAllSubwayRoutes()
        assertEquals(
            "<none>\n<none>\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(listOf(
            Route("id1", "Red")
        ), dataLoaded = true), emptyInput, output)

        view.renderAllSubwayRoutes()
        assertEquals(
            "Red\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(listOf(
            Route("id1", "Red"),
            Route("id2", "Blue")
        ), dataLoaded = true), emptyInput, output)

        view.renderAllSubwayRoutes()
        assertEquals(
            "Red and Blue\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(listOf(
            Route("id1", "Red"),
            Route("id2", "Blue"),
            Route("id3", "Green")
        ), dataLoaded = true), emptyInput, output)

        view.renderAllSubwayRoutes()
        assertEquals(
            "Red, Blue, and Green\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(listOf(
            Route("id1", "Red"),
            Route("id2", "Blue"),
            Route("id3", "Green"),
            Route("id4", "Bright Orange"),
            Route("id5", "Mysterious Route!")
        ), dataLoaded = true), emptyInput, output)

        view.renderAllSubwayRoutes()
        assertEquals(
            "Red, Blue, Green, Bright Orange, and Mysterious Route!\n",
            output.toString()
        )
    }

    @Test
    fun testRenderSubwayRouteWithMostStops() {
        // Route data has not been loaded
        val model = MockSubwayModel()
        var view = TextualSubwayView(model, emptyInput, output)

        assertThrows<IllegalStateException> { view.renderSubwayRouteWithMostStops() }

        model.loadRouteData()

        view.renderSubwayRouteWithMostStops()
        assertEquals(
            "The model has no subway routes.\n",
            output.toString()
        )

        view.renderSubwayRouteWithMostStops()
        assertEquals(
            "The model has no subway routes.\nThe model has no subway routes.\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(
            subwayRouteWithMostStops = Pair(
                Route("id", "Purple Line"), 7
            ), dataLoaded = true
        ), emptyInput, output)

        view.renderSubwayRouteWithMostStops()
        assertEquals(
            "The subway route with the most stops is: Purple Line\nThis route has 7 stops.\n",
            output.toString()
        )
    }

    @Test
    fun testRenderSubwayRouteWithFewestStops() {
        // Route data has not been loaded
        val model = MockSubwayModel()
        var view = TextualSubwayView(model, emptyInput, output)

        assertThrows<IllegalStateException> { view.renderSubwayRouteWithFewestStops() }

        model.loadRouteData()

        view.renderSubwayRouteWithFewestStops()
        assertEquals(
            "The model has no subway routes.\n",
            output.toString()
        )

        view.renderSubwayRouteWithFewestStops()
        assertEquals(
            "The model has no subway routes.\nThe model has no subway routes.\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(
            subwayRouteWithFewestStops = Pair(
                Route("id", "Purple Line"), 7
            ), dataLoaded = true
        ), emptyInput, output)

        view.renderSubwayRouteWithFewestStops()
        assertEquals(
            "The subway route with the fewest stops is: Purple Line\nThis route has 7 stops.\n",
            output.toString()
        )
    }

    @Test
    fun testRenderSubwayTransferStops() {
        // Route data has not been loaded
        val model = MockSubwayModel()
        var view = TextualSubwayView(model, emptyInput, output)

        assertThrows<IllegalStateException> { view.renderSubwayTransferStops() }

        model.loadRouteData()

        view.renderSubwayTransferStops()
        assertEquals(
            "There are no subway transfer stops.\n",
            output.toString()
        )

        view.renderSubwayTransferStops()
        assertEquals(
            "There are no subway transfer stops.\nThere are no subway transfer stops.\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(
            transferStops = mapOf(
                "Fake Stop" to listOf(
                    Route("id1", "Pink Line"),
                    Route("id2", "Yellow Line")
                )
            ), dataLoaded = true
        ), emptyInput, output)

        view.renderSubwayTransferStops()
        assertEquals(
            "The subway transfer stops, followed by the routes they connect, are:\n\n"
            + "Fake Stop: Pink Line and Yellow Line\n",
            output.toString()
        )

        output.clear()
        view = TextualSubwayView(MockSubwayModel(
            transferStops = mapOf(
                "Fake Stop" to listOf(
                    Route("id1", "Pink Line"),
                    Route("id2", "Yellow Line")
                ),
                "Northeastern University" to listOf(
                    Route("id3", "Boston Line"),
                    Route("id4", "Canada Line"),
                    Route("id5", "Ocean Line")
                )
            ), dataLoaded = true
        ), emptyInput, output)

        view.renderSubwayTransferStops()
        assertEquals(
            "The subway transfer stops, followed by the routes they connect, are:\n\n"
                    + "Fake Stop: Pink Line and Yellow Line\n"
                    + "Northeastern University: Boston Line, Canada Line, and Ocean Line\n",
            output.toString()
        )
    }

    @Test
    fun testInputLine() {
        var input = StringReader("")
        var view = TextualSubwayView(basicModel, input, output)

        assertThrows<NoSuchElementException> { view.inputLine() }

        input = StringReader("only input")
        view = TextualSubwayView(basicModel, input, output)

        assertEquals("only input", view.inputLine())
        assertThrows<NoSuchElementException> { view.inputLine() }

        input = StringReader("only input\n")
        view = TextualSubwayView(basicModel, input, output)

        assertEquals("only input", view.inputLine())
        assertThrows<NoSuchElementException> { view.inputLine() }

        input = StringReader("input1\ninput 2\n\n")
        view = TextualSubwayView(basicModel, input, output)

        assertEquals("input1", view.inputLine())
        assertEquals("input 2", view.inputLine())
        assertEquals("", view.inputLine())
        assertThrows<NoSuchElementException> { view.inputLine() }
    }
}