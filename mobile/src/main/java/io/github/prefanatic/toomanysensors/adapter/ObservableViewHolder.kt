package io.github.prefanatic.toomanysensors.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.jakewharton.rxbinding.view.RxView
import rx.Observable
import rx.Subscription

open class ObservableViewHolder : RecyclerView.ViewHolder {
    private val clickObservable: Observable<View>

    constructor(itemView: View?) : super(itemView) {
        clickObservable = RxView.clicks(itemView).map { itemView }
    }

    public fun getClickObservable() = clickObservable
}