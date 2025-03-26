package com.example.Farmer;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class FilteredArrayAdapter<T> extends ArrayAdapter<T> implements Filterable {

    private List<T> items;
    private List<T> filteredItems;

    public FilteredArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.filteredItems = new ArrayList<>(objects); // Ensure proper initialization
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                String query = constraint.toString().toLowerCase();

                List<T> filteredList = new ArrayList<>();
                for (T item : items) {
                    String itemName = item.toString().toLowerCase();
                    if (itemName.contains(query)) {
                        filteredList.add(item);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems = (List<T>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        if (filteredItems == null) {
            return 0; // Ensure a valid count even if filteredItems is null
        }
        return filteredItems.size();
    }

    @Override
    public T getItem(int position) {
        if (filteredItems == null || position < 0 || position >= filteredItems.size()) {
            return null;
        }
        return filteredItems.get(position);
    }
}
