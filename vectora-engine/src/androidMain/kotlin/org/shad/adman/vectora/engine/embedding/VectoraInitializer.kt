package org.shad.adman.vectora.engine.embedding

import android.content.Context
import androidx.startup.Initializer

internal var appContext: Context? = null

class VectoraInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        appContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
