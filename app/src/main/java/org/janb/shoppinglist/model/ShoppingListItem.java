package org.janb.shoppinglist.model;

/**
 * Created by Office on 09.07.2015.
 */
public class ShoppingListItem {

    private String itemTitle;
    private int itemCount;

    public String getItemTitle() {
        return itemTitle;
    }

    public int getItemCount() {
        return itemCount;
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
    }
}
