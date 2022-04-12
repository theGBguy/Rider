package com.example.rider.ui.nav_fragments

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchRequestTask

private class PlacesAutoCompleteAdapter(context: Context, textViewResourceId: Int) :
    ArrayAdapter<String>(context, textViewResourceId), Filterable {

    private var resultList: ArrayList<String>? = null
    private var searchEngine: SearchEngine = MapboxSearchSdk.getSearchEngine()
    private lateinit var searchRequestTask: SearchRequestTask

    override fun getCount(): Int {
        return resultList!!.size
    }

    override fun getItem(index: Int): String {
        return resultList!![index]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    // Retrieve the autocomplete results.
//                    searchRequestTask = searchEngine.search(
//                        constraint.trim().toString(),
//                        SearchOptions(limit = 5),
//                        searchCallback
//                    )
//                    resultList = autocomplete(constraint.toString())

                    // Assign the data to the FilterResults
                    filterResults.values = resultList
                    filterResults.count = resultList!!.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}