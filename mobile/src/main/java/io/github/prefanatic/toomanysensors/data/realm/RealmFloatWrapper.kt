package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmObject

public open class RealmFloatWrapper: RealmObject()  {
    public open var value: Float? = null
}