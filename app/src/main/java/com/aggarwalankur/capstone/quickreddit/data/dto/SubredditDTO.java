package com.aggarwalankur.capstone.quickreddit.data.dto;

import java.io.Serializable;

/**
 * Created by Ankur on 18-Oct-2016.
 */
public class SubredditDTO implements Serializable{
    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
