/*
 * Copyright (C) 2025 The Android Open Source Project
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
package androidx.compose.remote.creation.profile

import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.creation.CreationDisplayInfo
import androidx.compose.remote.creation.RemoteComposeWriter

/**
 * Represent a RemoteCompose profile
 *
 * A profile can be set when creating documents and will allow to validate the creation. A
 * profile encapsulates:
 * - api level for the operations
 * - a platform services implementation
 * - a RemoteComposeWriter instance
 */
open class Profile(
    val apiLevel: Int,
    val operationsProfiles: Int,
    val platform: RcPlatformServices,
    val factory: RemoteComposeWriterFactory,
    private val supportedOperationsSupplier: (() -> Set<Int>)? = null,
) {
    constructor(
        apiLevel: Int,
        operationsProfiles: Int,
        platform: RcPlatformServices,
        supportedOperations: () -> Set<Int>,
        factory: RemoteComposeWriterFactory,
    ) : this(apiLevel, operationsProfiles, platform, factory, supportedOperations)

    fun create(
        creationDisplayInfo: CreationDisplayInfo,
        writerCallback: Any?,
    ): RemoteComposeWriter {
        return factory.create(creationDisplayInfo, this, writerCallback)
    }

    val profileFactory: RemoteComposeWriterFactory get() = factory

    fun getSupportedOperations(): Set<Int>? {
        return supportedOperationsSupplier?.invoke()
    }
}
