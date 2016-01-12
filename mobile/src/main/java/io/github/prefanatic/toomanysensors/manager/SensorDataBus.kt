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

package io.github.prefanatic.toomanysensors.manager

import io.github.prefanatic.toomanysensors.data.dto.SensorData
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

object  SensorDataBus {
    private val subject = SerializedSubject<SensorData, SensorData>(PublishSubject.create())

    public fun post(data: SensorData) = subject.onNext(data)
    public fun asObservable(): Observable<SensorData> = subject.asObservable()
}