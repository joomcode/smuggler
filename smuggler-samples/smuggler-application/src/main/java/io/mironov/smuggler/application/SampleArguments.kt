package io.mironov.smuggler.application

import io.mironov.smuggler.AutoParcelable
import io.mironov.smuggler.library.Message
import io.mironov.smuggler.library.User

data class SampleArguments(
    val user: User,
    val message: Message
) : AutoParcelable
