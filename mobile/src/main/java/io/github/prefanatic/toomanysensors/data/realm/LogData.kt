package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject

public open class LogData: RealmObject() {
    public open var sensorId: Int = -1
    public open var sensorName: String = "Unknown"
    public open var notes: String = ""
    public open var entries: RealmList<LogValue> = RealmList()
}