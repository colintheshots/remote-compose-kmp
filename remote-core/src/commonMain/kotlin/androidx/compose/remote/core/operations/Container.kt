package androidx.compose.remote.core.operations

import androidx.compose.remote.core.Operation

/** Interface for operations that contain a list of child operations */
interface Container {
    fun getList(): ArrayList<Operation>
}
