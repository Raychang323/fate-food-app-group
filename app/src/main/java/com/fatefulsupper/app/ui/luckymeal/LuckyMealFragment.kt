package com.fatefulsupper.app.ui.luckymeal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatefulsupper.app.R

class LuckyMealFragment : Fragment() {

    private lateinit var viewModel: LuckyMealViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lucky_meal, container, false)
        viewModel = ViewModelProvider(this).get(LuckyMealViewModel::class.java)

        val buttonAccept = view.findViewById<Button>(R.id.button_accept_lucky_meal)
        val buttonDecline = view.findViewById<Button>(R.id.button_decline_lucky_meal)

        // TODO: Observe viewModel.luckyMeal to display the meal's photo and info (n29)
        // viewModel.luckyMeal.observe(viewLifecycleOwner) { meal ->
        //     meal?.let {
        //         // Display meal photo, name, etc.
        //     }
        // }

        buttonAccept.setOnClickListener {
            // TODO: Replace with actual latitude, longitude, and name from the lucky meal data
            val dummyLatitude = 1.0f
            val dummyLongitude = 1.0f
            val destinationName = "Lucky Meal Spot"
            val action = LuckyMealFragmentDirections.actionLuckyMealToMap( // Updated action ID
                dummyLatitude,
                dummyLongitude,
                destinationName
            )
            findNavController().navigate(action)
        }

        buttonDecline.setOnClickListener {
            findNavController().navigate(R.id.action_luckyMealFragment_to_connoisseurCheckFragment)
        }

        return view
    }
}
