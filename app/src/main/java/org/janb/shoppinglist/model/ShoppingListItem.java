package org.janb.shoppinglist.model;

import android.util.Log;

public class ShoppingListItem {

    private String itemTitle;
    private int itemCount;
    private Boolean checked;
    public Boolean isImportant;

    public Boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        this.checked = !this.checked;
    }

    public void toggleImportant() {
        this.isImportant = !this.isImportant;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getItemCountString() {
        return String.valueOf(itemCount);
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

 public ShoppingListItem(String title, int count){
        this.itemTitle = title;
        this.itemCount = count;
        this.checked = false;
    }
}
