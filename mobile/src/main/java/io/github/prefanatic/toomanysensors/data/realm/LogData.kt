package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class LogData: RealmObject() {
    public open var sensorId: Int? = null
    public open var dateCollected: Long? = null
    public open var lengthOfCollection: Long? = null
    public open var category: String? = null
    public open var notes: String? = null
    public open var entries: RealmList<LogValue>? = null
}