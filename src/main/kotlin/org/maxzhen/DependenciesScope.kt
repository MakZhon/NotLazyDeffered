package org.example.org.maxzhen

import kotlinx.coroutines.*

interface DependenciesScope {
    val dependenciesTasks: List<Deferred<*>>
    fun <T> executeAllAsync(block: suspend CoroutineScope.() -> T): Deferred<T>
}

fun <T> CoroutineScope.dependentAsync(
    dependenciesTasks: List<Deferred<*>>,
    block: DependenciesScope.() -> Deferred<T>
): Deferred<T> = block(DefaultDependenciesScope(this, dependenciesTasks))

private class DefaultDependenciesScope(
    private val scope: CoroutineScope,
    override val dependenciesTasks: List<Deferred<*>> = emptyList()
) : DependenciesScope {

    override fun <T> executeAllAsync(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return scope.async(start = CoroutineStart.LAZY) {
            dependenciesTasks.forEach {def ->
                def.invokeOnCompletion { error ->
                    if (error != null) {
                        this.cancel("Error execute in dependencies")
                    }
                }
            }
            block()
        }
    }
}