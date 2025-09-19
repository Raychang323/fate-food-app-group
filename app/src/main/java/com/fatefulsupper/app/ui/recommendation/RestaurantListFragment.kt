package com.fatefulsupper.app.ui.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatefulsupper.app.R
import com.fatefulsupper.app.data.model.Restaurant // Ensure this is imported

class RestaurantListFragment : Fragment() {

    private lateinit var viewModel: RestaurantListViewModel
    private lateinit var restaurantListAdapter: RestaurantListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var listTitleTextView: TextView
    private lateinit var emptyListTextView: TextView // For showing message when list is empty

    private val navArgs: RestaurantListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant_list, container, false)
        viewModel = ViewModelProvider(this).get(RestaurantListViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerView_restaurant_list)
        listTitleTextView = view.findViewById(R.id.textView_restaurant_list_title)
        emptyListTextView = view.findViewById(R.id.textView_empty_list_message) 
        
        setupRecyclerView()
        observeViewModel()
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val filterType = navArgs.listFilterType
        val likedIds = navArgs.likedRestaurantIds
        val aiRestaurantsList = navArgs.aiGeneratedRestaurants?.toList()

        viewModel.loadRestaurants(filterType, likedIds, aiRestaurantsList)

        when (filterType) {
            "liked" -> listTitleTextView.text = "我喜歡的餐廳"
            "ai_generated" -> listTitleTextView.text = "AI 為您推薦"
            else -> listTitleTextView.text = "餐廳列表"
        }
    }

    private fun setupRecyclerView() {
        restaurantListAdapter = RestaurantListAdapter { restaurant ->
            // Pass both restaurantId and the full restaurant object
            val action = RestaurantListFragmentDirections.actionRestaurantListFragmentToRestaurantDetailsFragment(
                restaurantId = restaurant.id,
                selectedRestaurantFull = restaurant // Pass the full Restaurant object here
            )
            findNavController().navigate(action)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = restaurantListAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            restaurants?.let {
                restaurantListAdapter.submitList(it)
                if (it.isEmpty()) {
                    emptyListTextView.isVisible = true
                    recyclerView.isVisible = false
                    when (navArgs.listFilterType) {
                        "liked" -> emptyListTextView.text = "您尚未收藏任何喜歡的餐廳。"
                        "ai_generated" -> emptyListTextView.text = "AI 推薦列表為空，請稍後再試。"
                        else -> emptyListTextView.text = "目前沒有餐廳可顯示。"
                    }
                } else {
                    emptyListTextView.isVisible = false
                    recyclerView.isVisible = true
                }
            }
        }
    }
}
