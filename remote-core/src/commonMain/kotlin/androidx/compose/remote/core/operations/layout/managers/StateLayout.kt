package androidx.compose.remote.core.operations.layout.managers
import androidx.compose.remote.core.Operation; import androidx.compose.remote.core.Operations; import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.VariableSupport; import androidx.compose.remote.core.WireBuffer; import androidx.compose.remote.core.operations.layout.Component
open class StateLayout : LayoutManager, VariableSupport {
    constructor(parent: Component?, cid: Int, aid: Int, x: Float, y: Float, w: Float, h: Float) : super(parent, cid, aid, x, y, w, h)
    override fun registerListening(context: RemoteContext) {}
    override fun updateVariables(context: RemoteContext) {}
    override fun toString(): String = "STATELAYOUT [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "STATELAYOUT"
    override fun write(buffer: WireBuffer) {}
    companion object { fun name(): String = "StateLayout"; fun id(): Int = Operations.LAYOUT_STATE
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, hp: Int, vp: Int, indexId: Int) {
            buffer.start(Operations.LAYOUT_STATE); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(hp); buffer.writeInt(vp); buffer.writeInt(indexId) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) {} }
}
