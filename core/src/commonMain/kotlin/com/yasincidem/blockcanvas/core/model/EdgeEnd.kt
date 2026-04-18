package com.yasincidem.blockcanvas.core.model

public sealed interface EdgeEnd {
    public object None : EdgeEnd

    public data class Arrow(
        val size: Float = 8f,
        val filled: Boolean = true,
    ) : EdgeEnd

    public data class Circle(
        val radius: Float = 4f,
        val filled: Boolean = true,
    ) : EdgeEnd

    public data class Diamond(
        val size: Float = 6f,
        val filled: Boolean = true,
    ) : EdgeEnd
}
