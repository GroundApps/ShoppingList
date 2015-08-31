package org.janb.shoppinglist.api;

import org.janb.shoppinglist.model.ShoppingListItem;

import java.util.ArrayList;

public class ResponseHelper {
    public int type;
    public String content;
    public ArrayList<ShoppingListItem> items;

    public ResponseHelper(int type, String content){
        this.type = type;
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public ArrayList<ShoppingListItem> getItems() {
        return items;
    }

}
