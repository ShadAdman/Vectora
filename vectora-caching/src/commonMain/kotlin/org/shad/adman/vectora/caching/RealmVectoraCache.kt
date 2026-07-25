package org.shad.adman.vectora.caching

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import org.shad.adman.vectora.caching.model.RealmIndexedItem
import org.shad.adman.vectora.core.model.IndexedItem
import org.shad.adman.vectora.core.model.Vector

class RealmVectoraCache(
    private val realm: Realm
) : VectoraCache {

    companion object {
        fun create(): RealmVectoraCache {
            val config = RealmConfiguration.Builder(schema = setOf(RealmIndexedItem::class))
                .name("vectora_cache.realm")
                .build()
            return RealmVectoraCache(Realm.open(config))
        }
    }

    override suspend fun <T> saveItems(items: List<IndexedItem<T>>, serializer: (T) -> String) {
        realm.write {
            items.forEach { item ->
                copyToRealm(RealmIndexedItem().apply {
                    id = item.id
                    vectorValues = item.vector.values.toList().toRealmList()
                    metadata = serializer(item.item)
                })
            }
        }
    }

    override suspend fun <T> loadItems(deserializer: (String) -> T): List<IndexedItem<T>> {
        return realm.query<RealmIndexedItem>().find().map { realmItem ->
            IndexedItem(
                id = realmItem.id,
                item = deserializer(realmItem.metadata),
                vector = Vector(realmItem.vectorValues.toFloatArray())
            )
        }
    }

    override suspend fun clear() {
        realm.write {
            delete(query<RealmIndexedItem>())
        }
    }
}
