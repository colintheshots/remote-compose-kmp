/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.compose.remote.core

import androidx.compose.remote.core.operations.Container
import androidx.compose.remote.core.serialize.MapSerializer
import androidx.compose.remote.core.serialize.Serializable

/**
 * PaintOperation interface, used for operations aimed at painting (while any operation _can_ paint,
 * this makes it a little more explicit)
 */
abstract class PaintOperation : Operation(), Serializable {

    override fun apply(context: RemoteContext) {
        if (context.getMode() == RemoteContext.ContextMode.PAINT) {
            val paintContext = context.getPaintContext()
            if (paintContext != null) {
                paint(paintContext)
            }
        } else {
            if (this is Container) {
                for (op in (this as Container).getList()) {
                    if (op.isDirty()) {
                        if (op is VariableSupport) {
                            (op as VariableSupport).updateVariables(context)
                        }
                        op.apply(context)
                    }
                }
            }
        }
    }

    override fun deepToString(indent: String): String {
        return indent + toString()
    }

    /** Paint the operation in the context */
    abstract fun paint(context: PaintContext)

    /**
     * Will return true if the operation is similar enough to the current one, in the context of an
     * animated transition.
     */
    open fun suitableForTransition(op: Operation): Boolean {
        return false
    }

    override fun serialize(serializer: MapSerializer) {
        // default no-op
    }

    companion object {
        /** Path or Bitmap need to be dereferenced */
        const val PTR_DEREFERENCE: Int = 0x1 shl 30

        /** Valid bits in Path or Bitmap */
        const val VALUE_MASK: Int = 0xFFFF
    }

    /**
     * Get the id from the context if needed
     *
     * @param id the id to get
     * @param context the context
     * @return the id dereferenced if needed
     */
    protected fun getId(id: Int, context: PaintContext): Int {
        var returnId = id and VALUE_MASK
        if ((id and PTR_DEREFERENCE) != 0) {
            returnId = context.getContext().getInteger(returnId)
        }
        return returnId
    }
}
