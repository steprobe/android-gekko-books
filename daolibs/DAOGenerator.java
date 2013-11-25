package com.stephenr.gekkobooks.dao.generator;

import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class DAOGenerator {

    // see: http://greendao-orm.com/documentation/how-to-get-started/

    private static final String LIST_ITEM = "ListItem";
    private static final String LIST_ITEM_LINK = "link";
    private static final String LIST_ITEM_TITLE = "title";
    private static final String LIST_ITEM_TIMSESTAMP = "timestamp";

    private static final String ITEM_DETAIL = "ItemDetails";
    private static final String DETAIL_IMAGE = "image";
    private static final String DETAIL_TITLE = "title";
    private static final String DETAIL_AUTHOR = "author";
    private static final String DETAIL_PRICE = "price";
    private static final String DETAIL_TIMSESTAMP = "timestamp";
    private static final String DETAIL_IMAGEFILE = "imagefile";
    
    public static void main(String[] args) throws Exception {

        Schema schema = new Schema(1, "com.stephenr.gekkobooks.dao.generated");
        addEntities(schema);

        try {
            new DaoGenerator().generateAll(schema, "./src");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error generating DAO");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating DAO");
        }
    }

    private static void addEntities(Schema schema) {

        Entity listItem = schema.addEntity(LIST_ITEM);
        listItem.addIdProperty();
        listItem.addStringProperty(LIST_ITEM_LINK);
        listItem.addStringProperty(LIST_ITEM_TITLE);
        listItem.addLongProperty(LIST_ITEM_TIMSESTAMP);

        Entity itemDetails = schema.addEntity(ITEM_DETAIL);
        itemDetails.addIdProperty();
        itemDetails.addStringProperty(DETAIL_IMAGE);
        itemDetails.addStringProperty(DETAIL_TITLE);
        itemDetails.addStringProperty(DETAIL_AUTHOR);
        itemDetails.addDoubleProperty(DETAIL_PRICE);
        itemDetails.addLongProperty(DETAIL_TIMSESTAMP);
        itemDetails.addStringProperty(DETAIL_IMAGEFILE);
    }
}

