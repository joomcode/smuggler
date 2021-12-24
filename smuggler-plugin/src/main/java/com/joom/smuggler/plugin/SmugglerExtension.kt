package com.joom.smuggler.plugin

import java.io.File

open class SmugglerExtension {
  open var cacheable: Boolean = false
  open var incremental: Boolean = true
  open var bootClasspath: List<File> = emptyList()
}
