package net.akehurst.omg.uml.v2_5_1.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import net.akehurst.kotlin.compose.layout.graph.CompoundGraphLayoutView
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutViewState
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull

/** Renders each demo diagram to a PNG (in build/demo-diagrams), to look at the result without a window. */
class render_DemoDiagrams {

    @Test
    fun render_all() {
        val out = File(System.getProperty("java.io.tmpdir"), "demo-diagrams").also { it.mkdirs() }
        DemoDiagrams.examples.forEach { ex ->
            val shown = DemoDiagramView.make(ex)
            assertNotNull(shown.state, "${ex.title}: ${shown.problems}")
            ImageComposeScene(1600, 1500, Density(1.25f)) {
                MaterialTheme {
                    CompoundGraphLayoutView(state = shown.state!!, layoutKey = 0, viewState = GraphLayoutViewState(), updateView = { _, _ -> })
                }
            }.use { scene ->
                // a few frames, so that measuring, layout and the drawing of the laid out graph all happen
                var img = scene.render(0)
                for (i in 1..10) img = scene.render(i * 100_000_000L)
                val f = File(out, ex.title.replace(' ', '_') + ".png")
                f.writeBytes(img.encodeToData(EncodedImageFormat.PNG)!!.bytes)
                println("wrote $f")
            }
        }
    }
}
