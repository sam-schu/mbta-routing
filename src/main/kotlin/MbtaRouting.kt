import controller.MbtaSubwayController
import controller.SubwayController
import model.MbtaSubwayModel
import model.MutableSubwayModel
import view.SubwayView
import view.TextualSubwayView
import java.io.InputStreamReader

/**
 * Runs the MBTA subway routing program, using a connection to the true MBTA
 * API.
 */
fun main() {
    val model: MutableSubwayModel = MbtaSubwayModel()
    val view: SubwayView = TextualSubwayView(model, InputStreamReader(System.`in`), System.out)
    val controller: SubwayController = MbtaSubwayController(model, view)

    controller.start()
}