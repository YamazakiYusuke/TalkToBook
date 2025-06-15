package com.example.talktobook.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseUseCase<in P, R> {

    suspend operator fun invoke(parameters: P): Result<R> = try {
        withContext(Dispatchers.IO) {
            execute(parameters)
        }
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): Result<R>
}

abstract class BaseUseCaseNoParams<R> {

    suspend operator fun invoke(): Result<R> = try {
        withContext(Dispatchers.IO) {
            execute()
        }
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): Result<R>
}