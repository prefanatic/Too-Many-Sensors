package io.github.prefanatic.toomanysensors.data.realm

import io.github.prefanatic.toomanysensors.data.TEAL
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class Category : RealmObject() {
    @PrimaryKey
    public open var name: String = "Unknown"

    public open var color: Int = TEAL
}