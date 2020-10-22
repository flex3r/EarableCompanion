package edu.teco.earablecompanion.utils

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow

class ViewEventFlow<T> : Flow<T> {

    private val eventSender = BroadcastChannel<T>(1) // TODO replace with SharedFlow in coroutines 1.4
    private val eventReceiver = eventSender.asFlow()

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) = eventReceiver.collect(collector)

    fun postEvent(event: T) {
        eventSender.offer(event)
    }
}