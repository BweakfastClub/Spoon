package main

import kotlinx.coroutines.experimental.Job

data class Argument(
    val argument: String,
    val tag: String = "",
    val function: () -> Job
)