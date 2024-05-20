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

fun main() {
    runBlocking {
        val firstTask = async {
            delay(7.seconds)
            println("Finish execute Task 1")
        }

        val secondTask = async {
            delay(4.seconds)
            println("Finish execute Task 2")
        }

        val thirdTask = async {
            delay(6.seconds)
            println("Finish execute Task 3")
        }

        val dependencies = listOf(firstTask, secondTask, thirdTask)

        measureTime {
            val mainTask = async {
                dependencies.forEach{ dependency ->
                    dependency.await()
                }.let { println("All dependencies were executed") }
            }

            deferredParallelExecution(
                mainTask,
                dependencies
            ).await()
        }.let { duration -> println("Execute time ${duration.inWholeSeconds}") }
    }
}