package androidx.compose.remote.core.operations.layout.managers
import androidx.compose.remote.core.Operation; import androidx.compose.remote.core.Operations; import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport; import androidx.compose.remote.core.WireBuffer; import androidx.compose.remote.core.operations.layout.Component
open class ImageLayout : LayoutManager, VariableSupport {
    constructor(parent: Component?, cid: Int, aid: Int, x: Float, y: Float, w: Float, h: Float) : super(parent, cid, aid, x, y, w, h)
    override fun registerListening(context: RemoteContext) {}
    override fun updateVariables(context: RemoteContext) {}
    override fun toString(): String = "IMAGELAYOUT [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "IMAGELAYOUT"
    override fun write(buffer: WireBuffer) {}
    companion object { fun name(): String = "ImageLayout"; fun id(): Int = Operations.LAYOUT_IMAGE
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, bitmapId: Int, scaleType: Int, alpha: Float) {
            buffer.start(Operations.LAYOUT_IMAGE); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(bitmapId); buffer.writeInt(scaleType); buffer.writeFloat(alpha) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) {} }
}
