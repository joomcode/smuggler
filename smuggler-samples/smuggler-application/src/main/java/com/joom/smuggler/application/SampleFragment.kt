@file:Suppress("deprecation")
package com.joom.smuggler.application

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SampleFragment : Fragment() {
  companion object {
    fun newInstance(args: SampleArguments): SampleFragment {
      return SampleFragment().apply {
        arguments = Bundle(1).apply {
          putParcelable("arguments", args)
        }
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.sample_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val text = view.findViewById<TextView>(R.id.text)
    val args = arguments.getParcelable<SampleArguments>("arguments")

    text.text = args?.message?.text ?: "<null>"
  }
}
