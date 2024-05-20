package org.example

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private fun <T> CoroutineScope.executeAllDependencies(
    dependenciesTasks: List<Deferred<T>>,
    action: suspend Deferred<T>.() -> Unit
) = dependenciesTasks.forEach { dependency ->
    launch {
        dependency.action()
    }
}

fun <T> deferredParallelExecution(
    mainTask: Deferred<T>,
    dependenciesTasks: List<Deferred<T>>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<T> {
    CoroutineScope(coroutineContext).executeAllDependencies(
        dependenciesTasks,
        Deferred<T>::start
    )
    mainTask.start()
    return mainTask
}

fun testDefault() = runBlocking {
    val task1 = async(start = CoroutineStart.LAZY) {
        delay(7.seconds)
        println("Finish execute Task 1")
        7
    }

    val task2 = async(start = CoroutineStart.LAZY) {
        delay(4.seconds)
        println("Finish execute Task 2")
        4
    }

    val dependencies = listOf(task1, task2)
    val mainTask = async(start = CoroutineStart.LAZY) {
        dependencies.forEach { dependency ->
            println("Task value: ${dependency.await()}")
        }.let { println("All dependencies were executed") }
    }
    mainTask.await()
}

fun testParallel() = runBlocking {
    val task1 = async(start = CoroutineStart.LAZY) {
        delay(7.seconds)
        println("Finish execute Task 1")
        7
    }
    val task2 = async(start = CoroutineStart.LAZY) {
        delay(4.seconds)
        println("Finish execute Task 2")
        4
    }

    val dependencies = listOf(task1, task2)
    val mainTask = async(start = CoroutineStart.LAZY) {
        dependencies.forEach { dependency ->
            println("Task value: ${dependency.await()}")
        }.let { println("All dependencies were executed") }
    }

    deferredParallelExecution(
        mainTask,
        dependencies
    ).await()
}

fun main() {
    runBlocking {
        measureTime {
            testDefault()
        }.let { duration ->
            if (duration.inWholeSeconds.toInt() == 11) {
                println("Execute time ${duration.inWholeSeconds}")
            } else {
                println("Test 1 not passed")
            }
        }

        measureTime {
            testParallel()
        }.let { duration ->
            if (duration.inWholeSeconds.toInt() == 7) {
                println("Execute time ${duration.inWholeSeconds}")
            } else {
                println("Test 2 not passed")
            }
        }
    }
}