package org.shad.adman.vectora.caching.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RealmIndexedItem : RealmObject {
    @PrimaryKey
    var id: String = ""
    var vectorValues: RealmList<Float> = realmListOf()
    var metadata: String = "" // Optional metadata or serialized item
}
