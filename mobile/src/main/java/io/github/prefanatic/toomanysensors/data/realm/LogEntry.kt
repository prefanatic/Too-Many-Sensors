/*
 * Copyright 2015-2016 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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