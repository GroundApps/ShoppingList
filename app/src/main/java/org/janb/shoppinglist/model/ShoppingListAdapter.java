package org.janb.shoppinglist.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.janb.shoppinglist.R;
import org.janb.shoppinglist.fragments.ShoppingListFragment;

import java.util.List;

public class ShoppingListAdapter extends ArrayAdapter {
    private Context context;
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();

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
        View viewToUse;
        ShoppingListItem item = (ShoppingListItem)getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            viewToUse = mInflater.inflate(R.layout.list_row, parent, false);

            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id.row_item_title);
            holder.countText = (TextView)viewToUse.findViewById(R.id.row_item_count);
            holder.checkLine = viewToUse.findViewById(R.id.row_item_check);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

            holder.countText.setText(String.valueOf(item.getItemCount()));
        if(item.getItemCount() == 1){
            holder.countText.setText("");
        }
        holder.titleText.setText(item.getItemTitle());
        if(item.isChecked()) {
            //holder.titleText.setPaintFlags(holder.titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titleText.setText(item.getItemTitle());
            holder.checkLine.setVisibility(View.VISIBLE);
        }
        return viewToUse;
    }


}
