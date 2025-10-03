package com.fatefulsupper.app.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R

class LoadingFragment : Fragment() {

    private lateinit var viewModel: LazyModeViewModel
    private val TAG = "LoadingFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use activityViewModels() or ViewModelProvider directly for shared ViewModel
        // Assuming LazyModeViewModel is scoped to the NavGraph or Activity
        viewModel = ViewModelProvider(requireActivity())[LazyModeViewModel::class.java]

        Log.d(TAG, "onViewCreated: Observing navigateToResultsEvent.")

        // We observe navigateToResultsEvent which is triggered when recommendations are ready.
        // The actual data (restaurants) will be picked up by ActionChoiceFragment from the ViewModel.
        viewModel.navigateToResultsEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Log.d(TAG, "navigateToResultsEvent observed, navigating to ActionChoiceFragment.")
                // Navigate to ActionChoiceFragment. Data will be fetched from ViewModel there.
                findNavController().navigate(R.id.action_loadingFragment_to_actionChoiceFragment)
            }
        }
    }
}
