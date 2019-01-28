package com.example.android.findbooks;

import android.graphics.Bitmap;

public class Book {

    private String mBookName;

    private String mAuthorName;

    private Bitmap mThumbnail;

    public Book(String bookName, String authorName, Bitmap thumbnail) {
        mBookName = bookName;
        mAuthorName = authorName;
        mThumbnail = thumbnail;
    }

    public String getBookName() {
        return mBookName;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }
}
