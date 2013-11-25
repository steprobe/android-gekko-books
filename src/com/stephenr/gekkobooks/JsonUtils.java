package com.stephenr.gekkobooks;

import java.io.IOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.stephenr.gekkobooks.dao.generated.ItemDetails;
import com.stephenr.gekkobooks.dao.generated.ListItem;

public class JsonUtils {

    private static final String JSON_ITEMS_ID = "id";
    private static final String JSON_ITEMS_title = "title";
    private static final String JSON_ITEMS_LINK = "link";

    public static ListItem listItemFromJson(JsonReader reader) throws IOException {

        String itemId = "";
        String link = "";
        String title = "";

        reader.beginObject();
        while(reader.hasNext()) {

            String name = reader.nextName();
            if (name.equals(JSON_ITEMS_ID) && reader.peek() == JsonToken.STRING) {
                itemId = reader.nextString();
            }
            else if (name.equals(JSON_ITEMS_title) && reader.peek() == JsonToken.STRING) {
                title = reader.nextString();
            }
            else if (name.equals(JSON_ITEMS_LINK) && reader.peek() == JsonToken.STRING) {
                link = reader.nextString();
            }
            else {
                reader.skipValue();
            }
        }

        reader.endObject();

        return new ListItem(new Long(itemId), link, title, System.currentTimeMillis());
    }

    private static final String JSON_DETAILS_ID = "id";
    private static final String JSON_DETAILS_IMAGE = "image";
    private static final String JSON_DETAILS_TITLE = "title";
    private static final String JSON_DETAILS_AUTHOR = "author";
    private static final String JSON_DETAILS_PRICE = "price";

    public static ItemDetails detailsFromJson(JsonReader reader) throws IOException {

        String id = "";
        String image = "";
        String author = "";
        String title = "";
        Double price = 0.0;

        reader.beginObject();
        while(reader.hasNext()) {

            String name = reader.nextName();
            if (name.equals(JSON_DETAILS_ID) && reader.peek() == JsonToken.STRING) {
                id = reader.nextString();
            }
            else if (name.equals(JSON_DETAILS_IMAGE) && reader.peek() == JsonToken.STRING) {
                image = reader.nextString();
            }
            else if (name.equals(JSON_DETAILS_TITLE) && reader.peek() == JsonToken.STRING) {
                title = reader.nextString();
            }
            else if (name.equals(JSON_DETAILS_AUTHOR) && reader.peek() == JsonToken.STRING) {
                author = reader.nextString();
            }
            else if (name.equals(JSON_DETAILS_PRICE) && reader.peek() == JsonToken.NUMBER) {
                price = reader.nextDouble();
            }
            else {
                reader.skipValue();
            }
        }

        reader.endObject();

        return new ItemDetails(new Long(id), image, title, author, price, System.currentTimeMillis(), null);
    }
}
