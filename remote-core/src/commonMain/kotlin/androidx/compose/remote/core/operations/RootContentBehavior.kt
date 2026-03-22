// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
class RootContentBehavior(var mScroll: Int, var mAlignment: Int, var mSizing: Int, var mMode: Int) : Operation(), RemoteComposeOperation {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mScroll, mAlignment, mSizing, mMode) }
    override fun toString(): String = "ROOT_CONTENT_BEHAVIOR scroll: $mScroll sizing: $mSizing mode: $mMode"
    override fun apply(context: RemoteContext) { context.setRootContentBehavior(mScroll, mAlignment, mSizing, mMode) }
    override fun deepToString(indent: String): String = toString()
    companion object {
        private val OP_CODE = Operations.ROOT_CONTENT_BEHAVIOR; const val NONE = 0; const val SCROLL_HORIZONTAL = 1; const val SCROLL_VERTICAL = 2
        const val SIZING_LAYOUT = 1; const val SIZING_SCALE = 2
        const val ALIGNMENT_TOP = 1; const val ALIGNMENT_VERTICAL_CENTER = 2; const val ALIGNMENT_BOTTOM = 4; const val ALIGNMENT_START = 16; const val ALIGNMENT_HORIZONTAL_CENTER = 32; const val ALIGNMENT_END = 64; const val ALIGNMENT_CENTER = ALIGNMENT_HORIZONTAL_CENTER + ALIGNMENT_VERTICAL_CENTER
        const val LAYOUT_HORIZONTAL_MATCH_PARENT = 1; const val LAYOUT_HORIZONTAL_WRAP_CONTENT = 2; const val LAYOUT_HORIZONTAL_FIXED = 4; const val LAYOUT_VERTICAL_MATCH_PARENT = 8; const val LAYOUT_VERTICAL_WRAP_CONTENT = 16; const val LAYOUT_VERTICAL_FIXED = 32; const val LAYOUT_MATCH_PARENT = LAYOUT_HORIZONTAL_MATCH_PARENT + LAYOUT_VERTICAL_MATCH_PARENT; const val LAYOUT_WRAP_CONTENT = LAYOUT_HORIZONTAL_WRAP_CONTENT + LAYOUT_VERTICAL_WRAP_CONTENT
        const val SCALE_INSIDE = 1; const val SCALE_FILL_WIDTH = 2; const val SCALE_FILL_HEIGHT = 3; const val SCALE_FIT = 4; const val SCALE_CROP = 5; const val SCALE_FILL_BOUNDS = 6
        fun name(): String = "RootContentBehavior"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, scroll: Int, alignment: Int, sizing: Int, mode: Int) { buffer.start(OP_CODE); buffer.writeInt(scroll); buffer.writeInt(alignment); buffer.writeInt(sizing); buffer.writeInt(mode) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(RootContentBehavior(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt())) }
    }
}
