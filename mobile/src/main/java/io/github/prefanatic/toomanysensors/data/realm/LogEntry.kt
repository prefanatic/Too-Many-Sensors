package io.github.prefanatic.toomanysensors.data.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

public open class LogEntry : RealmObject() {
    // dateCollected is based off time, and it seems REALLY UNLIKELY that we have two entries at the same time - that would be magical!
    @PrimaryKey public open var dateCollected: Long = 0

    public open var lengthOfCollection: Long = 0
    public open var category: String = "Unknown"
    public open var name: String = "My Record Event"
    public open var notes: String = ""
    public open var data: RealmList<LogData> = RealmList()
}