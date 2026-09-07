package de.jo_field.bossbarhider

import de.jo_field.bossbarhider.gui.RuleViewport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ViewportTest {
    @Test fun `deleting content or enlarging the window clamps stale scroll offsets`() {
        val viewport = RuleViewport()
        viewport.resize(1000, 200)
        viewport.scrollTo(800.0)
        viewport.resize(300, 200)
        assertEquals(100.0, viewport.offset)
        viewport.resize(300, 400)
        assertEquals(0.0, viewport.offset)
    }

    @Test fun `dragging maps track endpoints to complete list bounds`() {
        val viewport = RuleViewport()
        viewport.resize(1000, 200)
        viewport.dragThumb(160.0)
        assertEquals(800.0, viewport.offset)
        assertEquals(160, viewport.thumbTop)
        viewport.dragThumb(-100.0)
        assertEquals(0.0, viewport.offset)
    }

    @Test fun `zero height and empty content never create invalid scrollbar geometry`() {
        val viewport = RuleViewport()
        viewport.resize(0, 0)
        viewport.dragThumb(100.0)
        assertEquals(0.0, viewport.offset)
        assertEquals(0, viewport.thumbHeight)
        viewport.resize(100, 1)
        assertEquals(1, viewport.thumbHeight)
        viewport.dragThumb(100.0)
        assertEquals(0.0, viewport.offset)
    }
}
