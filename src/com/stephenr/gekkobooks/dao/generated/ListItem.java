package com.stephenr.gekkobooks.dao.generated;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table LIST_ITEM.
 */
public class ListItem {

    private Long id;
    private String link;
    private String title;
    private Long timestamp;

    public ListItem() {
    }

    public ListItem(Long id) {
        this.id = id;
    }

    public ListItem(Long id, String link, String title, Long timestamp) {
        this.id = id;
        this.link = link;
        this.title = title;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
