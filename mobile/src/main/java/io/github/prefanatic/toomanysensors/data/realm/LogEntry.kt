package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class LogEntry : RealmObject() {
    public open var dateCollected: Long = 0
    public open var lengthOfCollection: Long = 0
    public open var category: String = "Unknown"
    public open var name: String = "My Record Event"
    public open var notes: String = ""
    public open var data: RealmList<LogData> = RealmList()
}