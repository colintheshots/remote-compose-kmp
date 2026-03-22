package androidx.compose.remote.core.operations.layout.managers
import androidx.compose.remote.core.Operation; import androidx.compose.remote.core.Operations; import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport; import androidx.compose.remote.core.WireBuffer; import androidx.compose.remote.core.operations.layout.Component
open class TextLayout : LayoutManager, VariableSupport {
    constructor(parent: Component?, cid: Int, aid: Int, x: Float, y: Float, w: Float, h: Float) : super(parent, cid, aid, x, y, w, h)
    override fun registerListening(context: RemoteContext) {}
    override fun updateVariables(context: RemoteContext) {}
    override fun toString(): String = "TEXTLAYOUT [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "TEXTLAYOUT"
    override fun write(buffer: WireBuffer) {}
    companion object { fun name(): String = "TextLayout"; fun id(): Int = Operations.LAYOUT_TEXT
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, textId: Int, color: Int, fontSize: Float,
                  fontStyle: Int, fontWeight: Float, fontFamilyId: Int, flagsAndTextAlign: Int,
                  overflow: Int, maxLines: Int) {
            buffer.start(Operations.LAYOUT_TEXT); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(textId)
            buffer.writeInt(color); buffer.writeFloat(fontSize); buffer.writeInt(fontStyle); buffer.writeFloat(fontWeight)
            buffer.writeInt(fontFamilyId); buffer.writeInt(flagsAndTextAlign); buffer.writeInt(overflow); buffer.writeInt(maxLines) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) {} }
}
