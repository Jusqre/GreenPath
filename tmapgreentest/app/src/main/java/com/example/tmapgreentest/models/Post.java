package com.example.tmapgreentest.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post {

    private String documentId;
    private String nicname;
    private String title;
    private String contents;
    @ServerTimestamp
    private Date date;

    public Post() {}

    public Post(String documentId, String nicname, String title, String contents) {
        this.documentId = documentId;
        this.nicname = nicname;
        this.title = title;
        this.contents = contents;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getNicname() {
        return nicname;
    }

    public void setNicname(String nicname) {
        this.nicname = nicname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Post{" +
                "documentId='" + documentId + '\'' +
                ", nicname='" + nicname + '\'' +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", date=" + date +
                '}';
    }
}
