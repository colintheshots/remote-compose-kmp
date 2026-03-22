package androidx.compose.remote.core.operations.layout.managers
import androidx.compose.remote.core.Operation; import androidx.compose.remote.core.Operations; import androidx.compose.remote.core.WireBuffer
import androidx.compose.remote.core.operations.layout.Component
open class RowLayout(parent: Component?, componentId: Int, animationId: Int, x: Float, y: Float, width: Float, height: Float,
    var mHorizontalPositioning: Int, var mVerticalPositioning: Int, var mSpacedBy: Float = 0f
) : LayoutManager(parent, componentId, animationId, x, y, width, height) {
    constructor(parent: Component?, cid: Int, aid: Int, hp: Int, vp: Int, sb: Float) : this(parent, cid, aid, 0f, 0f, 0f, 0f, hp, vp, sb)
    override fun toString(): String = "ROW [$componentId:$mAnimationId]"
    override fun getSerializedName(): String = "ROW"
    override fun write(buffer: WireBuffer) { Companion.apply(buffer, componentId, mAnimationId, mHorizontalPositioning, mVerticalPositioning, mSpacedBy) }
    companion object {
        const val START=1; const val CENTER=2; const val END=3; const val TOP=4; const val BOTTOM=5
        const val SPACE_BETWEEN=6; const val SPACE_EVENLY=7; const val SPACE_AROUND=8
        fun name(): String = "RowLayout"; fun id(): Int = Operations.LAYOUT_ROW
        fun apply(buffer: WireBuffer, cid: Int, aid: Int, hp: Int, vp: Int, sb: Float) {
            buffer.start(Operations.LAYOUT_ROW); buffer.writeInt(cid); buffer.writeInt(aid); buffer.writeInt(hp); buffer.writeInt(vp); buffer.writeFloat(sb) }
        fun read(buffer: WireBuffer, ops: MutableList<Operation>) {
            ops.add(RowLayout(null, buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readFloat())) }
    }
}
