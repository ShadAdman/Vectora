package org.shad.adman.vectora.caching

// The cache contract moved to dependency-free vectora-core so vectora-search
// no longer needs this module (whose Realm compiler plugin currently fails
// under Kotlin 2.4.x). This alias keeps existing implementations compiling.
typealias VectoraCache = org.shad.adman.vectora.core.cache.VectoraCache
