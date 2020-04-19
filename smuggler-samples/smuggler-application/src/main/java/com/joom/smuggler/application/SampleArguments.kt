package com.joom.smuggler.application

import com.joom.smuggler.AutoParcelable
import com.joom.smuggler.library.Message
import com.joom.smuggler.library.User

data class SampleArguments(
  val user: User,
  val message: Message
) : AutoParcelable
