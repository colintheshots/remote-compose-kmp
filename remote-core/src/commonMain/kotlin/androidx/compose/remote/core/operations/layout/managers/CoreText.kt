package androidx.compose.remote.core.operations.layout.managers
import androidx.compose.remote.core.Operation; import androidx.compose.remote.core.Operations; import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport; import androidx.compose.remote.core.WireBuffer; import androidx.compose.remote.core.operations.layout.Component
open class CoreText : LayoutManager, VariableSupport {
    constructor(parent: Component?, cid: Int, aid: Int, x: Float, y: Float, w: Float, h: Float) : super(parent, cid, aid, x, y, w, h)
    override fun registerListening(context: RemoteContext) {}
    override fun updateVariables(context: RemoteContext) {}
    override fun toString(): String = "CORETEXT [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "CORETEXT"
    override fun write(buffer: WireBuffer) {}
    companion object {
        const val TEXT_ALIGN_LEFT = 1
        const val TEXT_ALIGN_RIGHT = 2
        const val TEXT_ALIGN_CENTER = 3
        const val TEXT_ALIGN_JUSTIFY = 4
        const val TEXT_ALIGN_START = 5
        const val TEXT_ALIGN_END = 6

        const val OVERFLOW_CLIP = 1
        const val OVERFLOW_VISIBLE = 2
        const val OVERFLOW_ELLIPSIS = 3
        const val OVERFLOW_START_ELLIPSIS = 4
        const val OVERFLOW_MIDDLE_ELLIPSIS = 5

        fun name(): String = "CoreText"; fun id(): Int = Operations.CORE_TEXT
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, textId: Int, color: Int, colorId: Int,
                  fontSize: Float, fontStyle: Int, fontWeight: Float, fontFamilyId: Int,
                  textAlign: Int, overflow: Int, maxLines: Int, letterSpacing: Float,
                  lineHeightAdd: Float, lineHeightMultiplier: Float, lineBreakStrategy: Int,
                  hyphenationFrequency: Int, justificationMode: Int, underline: Boolean,
                  strikethrough: Boolean, fontAxis: IntArray?, fontAxisValues: FloatArray?,
                  autosize: Boolean, flags: Int) {
            buffer.start(Operations.CORE_TEXT); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(textId)
            buffer.writeInt(color); buffer.writeInt(colorId); buffer.writeFloat(fontSize); buffer.writeInt(fontStyle)
            buffer.writeFloat(fontWeight); buffer.writeInt(fontFamilyId); buffer.writeInt(textAlign); buffer.writeInt(overflow)
            buffer.writeInt(maxLines); buffer.writeFloat(letterSpacing); buffer.writeFloat(lineHeightAdd)
            buffer.writeFloat(lineHeightMultiplier); buffer.writeInt(lineBreakStrategy)
            buffer.writeInt(hyphenationFrequency); buffer.writeInt(justificationMode)
            buffer.writeByte(if (underline) 1 else 0); buffer.writeByte(if (strikethrough) 1 else 0)
            if (fontAxis != null) { buffer.writeInt(fontAxis.size); for (a in fontAxis) buffer.writeInt(a) } else buffer.writeInt(0)
            if (fontAxisValues != null) { buffer.writeInt(fontAxisValues.size); for (v in fontAxisValues) buffer.writeFloat(v) } else buffer.writeInt(0)
            buffer.writeByte(if (autosize) 1 else 0); buffer.writeInt(flags) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) {} }
}
