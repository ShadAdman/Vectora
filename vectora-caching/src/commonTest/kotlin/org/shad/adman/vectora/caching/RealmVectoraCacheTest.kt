package org.shad.adman.vectora.caching

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.coroutines.test.runTest
import org.shad.adman.vectora.caching.model.RealmIndexedItem
import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.Vector
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RealmVectoraCacheTest {

    private lateinit var realm: Realm
    private lateinit var cache: RealmVectoraCache

    @BeforeTest
    fun setUp() {
        val config = RealmConfiguration.Builder(schema = setOf(RealmIndexedItem::class))
            .name("test.realm")
            .inMemory()
            .build()
        realm = Realm.open(config)
        cache = RealmVectoraCache(realm)
    }

    @AfterTest
    fun tearDown() {
        if (this::realm.isInitialized) {
            realm.close()
        }
    }

    @Test
    fun testSaveAndLoadItems() = runTest {
        val items = listOf(
            IndexedItem(id = "1", item = "Product 1", vector = Vector(floatArrayOf(1.0f, 2.0f))),
            IndexedItem(id = "2", item = "Product 2", vector = Vector(floatArrayOf(3.0f, 4.0f)))
        )

        cache.saveItems(items) { it }
        val loadedItems = cache.loadItems { it }

        assertEquals(2, loadedItems.size)
        assertEquals("1", loadedItems[0].id)
        assertEquals("Product 1", loadedItems[0].item)
        assertEquals(1.0f, loadedItems[0].vector.values[0])
        assertEquals(2.0f, loadedItems[0].vector.values[1])
    }

    @Test
    fun testClear() = runTest {
        val items = listOf(
            IndexedItem(id = "1", item = "Product 1", vector = Vector(floatArrayOf(1.0f, 2.0f)))
        )

        cache.saveItems(items) { it }
        cache.clear()
        val loadedItems = cache.loadItems { it }

        assertEquals(0, loadedItems.size)
    }
}
