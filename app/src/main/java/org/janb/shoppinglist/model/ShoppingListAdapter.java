package org.janb.shoppinglist.model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.janb.shoppinglist.R;

import java.util.List;

/**
 * Created by Office on 09.07.2015.
 */
public class ShoppingListAdapter extends ArrayAdapter {
    private Context context;
    private boolean useList = true;

    public ShoppingListAdapter(Context context, List<ShoppingListItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder{
        TextView titleText;
        TextView countText;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ShoppingListItem item = (ShoppingListItem)getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            viewToUse = mInflater.inflate(R.layout.list_row, null);

            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id.row_item_title);
            holder.countText = (TextView)viewToUse.findViewById(R.id.row_item_count);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.countText.setText(String.valueOf(item.getItemCount()));
        holder.titleText.setText(item.getItemTitle());

        return viewToUse;
    }


}
