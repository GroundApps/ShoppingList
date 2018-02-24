package org.janb.shoppinglist.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
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
    Boolean hideChecked;

    public ShoppingListAdapter(Context context, List<ShoppingListItem> items, Boolean hideChecked) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        this.hideChecked = hideChecked;
    }

    private class ViewHolder{
        TextView titleText;
        TextView countText;
        View checkLine;
    }

    public void setHideChecked(Boolean hideChecked) {
        this.hideChecked = hideChecked;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View viewToUse = null;
        ShoppingListItem item = (ShoppingListItem)getItem(position);
        Log.d("GET VIEW", "position:" + position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (item.isChecked() && hideChecked) {
            viewToUse = mInflater.inflate(R.layout.list_row_hidden, parent, false);
        } else if (true || convertView == null) {  // FIXME: what was the intended behaviour here?
            holder = new ViewHolder();
            int rowType = 0;
            switch (rowType) {
                case TYPE_ITEM:
                    viewToUse = mInflater.inflate(R.layout.list_row, parent, false);
                    holder.titleText = (TextView) viewToUse.findViewById(R.id.row_item_title);
                    holder.countText = (TextView) viewToUse.findViewById(R.id.row_item_count);
                    holder.checkLine = viewToUse.findViewById(R.id.row_item_check);
                    Log.d("GET VIEW ITEM", position + ": " + item.getItemTitle());
                    viewToUse.setTag(holder);
                    holder.countText.setText(String.valueOf(item.getItemCount()));
                    if (item.getItemCount() == 1) {
                        holder.countText.setText("");
                    }
                    String title = item.getItemTitle();
                    if (item.isChecked()) {
                        holder.checkLine.setVisibility(View.VISIBLE);
                    }
                    if (item.getItemTitle().contains("***")) {
                        holder.titleText.setTypeface(null, Typeface.BOLD);
                    } else {
                        title = title.replaceAll("^\\d{1,3}\\. ", "");
                    }
                    holder.titleText.setText(title);
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
