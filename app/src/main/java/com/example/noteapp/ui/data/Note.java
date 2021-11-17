package com.example.noteapp.ui.data;

public class Note {
    final String title;
    final String content;

    Note( String aTitle, String aContent) {
        this.title = aTitle;
        this.content = aContent;
    }

    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
}
