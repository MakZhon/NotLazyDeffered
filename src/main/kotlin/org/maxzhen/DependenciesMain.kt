package org.example.org.maxzhen

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

fun main() = runBlocking {

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

    measureTime {
        val res = dependentAsync(dependencies) {
            var sum = 0
            executeAllAsync {
                dependencies.forEach(Job::start)
                dependencies.forEach {
                    sum += it.await()
                }
                sum
            }
        }
        res.start()
        res.join()
    }.let { duration ->
        println("Execute time ${duration.inWholeSeconds}")
    }
}