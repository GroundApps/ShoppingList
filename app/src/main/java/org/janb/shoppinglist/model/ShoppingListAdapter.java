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

public class ShoppingListAdapter extends ArrayAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private Context context;

    public ShoppingListAdapter(Context context, List<ShoppingListItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    private class ViewHolder{
        TextView titleText;
        TextView countText;
        View checkLine;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View viewToUse = null;
        ShoppingListItem item = (ShoppingListItem)getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            holder = new ViewHolder();
            int rowType = 0;
            switch (rowType) {
                case TYPE_ITEM:
                    viewToUse = mInflater.inflate(R.layout.list_row, parent, false);


                    holder.titleText = (TextView) viewToUse.findViewById(R.id.row_item_title);
                    holder.countText = (TextView) viewToUse.findViewById(R.id.row_item_count);
                    holder.checkLine = viewToUse.findViewById(R.id.row_item_check);
                    viewToUse.setTag(holder);
                    holder.countText.setText(String.valueOf(item.getItemCount()));
                    if (item.getItemCount() == 1) {
                        holder.countText.setText("");
                    }
                    holder.titleText.setText(item.getItemTitle());
                    if (item.isChecked()) {
                        holder.titleText.setText(item.getItemTitle());
                        holder.checkLine.setVisibility(View.VISIBLE);
                    }
            break;
            case TYPE_SEPARATOR:
                convertView = mInflater.inflate(R.layout.list_category, parent, false);
                holder.titleText = (TextView) convertView.findViewById(R.id.textSeparator);
                break;

        }
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }



        return viewToUse;
    }


}
