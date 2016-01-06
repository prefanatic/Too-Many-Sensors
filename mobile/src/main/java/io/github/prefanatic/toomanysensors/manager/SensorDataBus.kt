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