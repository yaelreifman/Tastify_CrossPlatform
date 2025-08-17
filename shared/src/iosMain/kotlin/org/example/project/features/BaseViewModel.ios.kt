package org.example.project.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

actual open class BaseViewModel actual constructor() {
    actual val scope: CoroutineScope= CoroutineScope(Dispatchers.IO)

    fun clear(){
        scope.cancel()
    }
}