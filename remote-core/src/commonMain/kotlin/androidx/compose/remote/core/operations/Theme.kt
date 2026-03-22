// Copyright (C) 2024 The Android Open Source Project. Apache License 2.0.
package androidx.compose.remote.core.operations
import androidx.compose.remote.core.*
class Theme(var mTheme: Int) : Operation(), RemoteComposeOperation {
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, mTheme) }
    override fun toString(): String = "SET_THEME $mTheme"
    override fun apply(context: RemoteContext) { context.setTheme(mTheme); markDirty() }
    override fun deepToString(indent: String): String = indent + toString()
    companion object {
        private val OP_CODE = Operations.THEME; const val SYSTEM = 0; const val UNSPECIFIED = -1; const val DARK = -2; const val LIGHT = -3
        fun name(): String = "Theme"; fun id(): Int = OP_CODE
        fun apply(buffer: WireBuffer, theme: Int) { buffer.start(OP_CODE); buffer.writeInt(theme) }
        fun read(buffer: WireBuffer, operations: MutableList<Operation>) { operations.add(Theme(buffer.readInt())) }
    }
}
