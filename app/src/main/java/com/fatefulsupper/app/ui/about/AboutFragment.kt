package com.fatefulsupper.app.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fatefulsupper.app.R
// Potentially import BuildConfig if you want to display the version name programmatically
// import com.fatefulsupper.app.BuildConfig

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        // Example of how you might set the version name programmatically
        // val versionName = BuildConfig.VERSION_NAME
        // val textViewVersion: TextView = view.findViewById(R.id.textView_app_version_about)
        // textViewVersion.text = "版本 $versionName"

        return view
    }
}