package com.quadrigasoftware

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Provides a standard logger for the core library.
 */
object CoreLogger {
    val log: Logger = LoggerFactory.getLogger("com.quadrigasoftware.core")
}

/**
 * Extension property to easily get a logger for any class.
 */
val Any.logger: Logger
    get() = LoggerFactory.getLogger(this.javaClass)
