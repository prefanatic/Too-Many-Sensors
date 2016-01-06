package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject

public open class LogValue: RealmObject() {
    public open var timeStamp: Long = 0
    public open var values: RealmList<RealmFloatWrapper> = RealmList()
}