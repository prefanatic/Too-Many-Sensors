package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class LogData: RealmObject() {
    public open var sensorId: Int = -1
    public open var dateCollected: Long = 0
    public open var lengthOfCollection: Long? = null
    public open var category: String = "Unknown"
    public open var notes: String = ""
    public open var entries: RealmList<LogValue> = RealmList()
}