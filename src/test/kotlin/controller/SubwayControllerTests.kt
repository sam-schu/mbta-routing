package controller

import model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import view.TextualSubwayView
import java.io.StringReader
import kotlin.test.assertEquals

/**
 * Includes tests for SubwayController.kt. These tests do not rely on data from
 * the true MBTA API.
 */
class SubwayControllerTests {
    private lateinit var emptyInput: StringReader
    private lateinit var output: StringBuilder

    private lateinit var threeRoutesModel: MutableSubwayModel

    @BeforeEach
    fun setUp() {
        emptyInput = StringReader("")
        output = StringBuilder()

        threeRoutesModel = MockSubwayModel(listOf(
            Route("id1", "Red"),
            Route("id2", "Blue"),
            Route("id3", "Green")
        ))
    }

    @Test
    fun testInitialDataLoadingFailure() {
        testWithMockServer(forbidden) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))
            val controller = MbtaSubwayController(
                model, TextualSubwayView(model, emptyInput, output), false
            )

            controller.start()

            assert("A fatal error occurred" in output.toString())
            assert("Welcome to the MBTA subway routing program!" !in output.toString())
        }
    }

    @Test
    fun testNoInput() {
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(threeRoutesModel, emptyInput, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("A fatal error occurred" in output.toString())
    }

    @Test
    fun testInvalidInput() {
        val input = StringReader("hi\nq\n")
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(threeRoutesModel, input, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("A fatal error occurred" !in output.toString())
        assert("The option entered was not recognized." in output.toString())
    }

    @Test
    fun testCommand1Success() {
        val input = StringReader("1\nq\n")
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(threeRoutesModel, input, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("Please enter one of the following options (before the colon):" in output.toString())
        assert("A fatal error occurred" !in output.toString())
        assert("The option entered was not recognized." !in output.toString())
        assert("The route data was successfully reloaded." in output.toString())
    }

    @Test
    fun testCommand1Failure() {
        testWithMockServer(singleRoute, rateLimited) { url ->
            val input = StringReader("1\nq\n")
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))
            val controller = MbtaSubwayController(
                model, TextualSubwayView(model, input, output), false
            )

            controller.start()

            assert("Welcome to the MBTA subway routing program!" in output.toString())
            assert(
                "Please enter one of the following options (before the colon):" in output.toString()
            )
            assert("A fatal error occurred" !in output.toString())
            assert("The option entered was not recognized." !in output.toString())
            assert("The route data was successfully reloaded." !in output.toString())
            assert(
                "An error occurred while attempting to load new route data." in output.toString()
            )
        }
    }

    @Test
    fun testCommand2Success() {
        val input = StringReader("  2            \n        q\n")
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(threeRoutesModel, input, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("Please enter one of the following options (before the colon):" in output.toString())
        assert("A fatal error occurred" !in output.toString())
        assert("The option entered was not recognized." !in output.toString())
        assert("The names of all MBTA subway routes are:" in output.toString())
        assert("Red, Blue, and Green" in output.toString())
    }

    @Test
    fun testCommand2Failure() {
        val input = StringReader("2\nq\n")
        val wrongModelForView = MockSubwayModel(listOf(Route("id", "Green")))
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(wrongModelForView, input, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("Please enter one of the following options (before the colon):" in output.toString())
        assert("A fatal error occurred" !in output.toString())
        assert("The option entered was not recognized." !in output.toString())
        assert("Green" !in output.toString())
        assert("An unexpected error occurred when attempting to access the\nroute data."
                in output.toString())
    }

    @Test
    fun testQuit() {
        val input = StringReader("q\n")
        val controller = MbtaSubwayController(
            threeRoutesModel, TextualSubwayView(threeRoutesModel, input, output), false
        )

        controller.start()

        assert("Welcome to the MBTA subway routing program!" in output.toString())
        assert("Please enter one of the following options (before the colon):" in output.toString())
        assert("A fatal error occurred" !in output.toString())
        assert("The option entered was not recognized." !in output.toString())
        assert("The program has terminated." in output.toString())
    }

    @Test
    fun testFullRun() {
        testWithMockServer(singleRoute, rateLimited, threeRoutes) { url ->
            val input = StringReader("h\n2\n1\n2\n1\n2\nQ\n")
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))
            val controller = MbtaSubwayController(
                model, TextualSubwayView(model, input, output), false
            )

            controller.start()

            assertEquals(
                """
                    Welcome to the MBTA subway routing program!
                    Subway route data has been successfully loaded.

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The option entered was not recognized.

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The names of all MBTA subway routes are:
                    Red Line

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    An error occurred while attempting to load new route data.
                    The previously loaded data has been retained.


                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The names of all MBTA subway routes are:
                    Red Line

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The route data was successfully reloaded.

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The names of all MBTA subway routes are:
                    Red Line, Mattapan Trolley, and Orange Line

                    Please enter one of the following options (before the colon):
                    1: Reload the data from the MBTA server.
                    2: List the names of all subway routes.
                    q: Quit the program.


                    The program has terminated.
                    
                """.trimIndent(),
                output.toString()
            )
        }
    }
}