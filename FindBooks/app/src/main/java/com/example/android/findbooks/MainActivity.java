package com.example.android.findbooks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mTextEntered;
    private ImageButton mSearchButton;
    private ProgressBar mProgressBar;
    private TextView mMessage;

    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchButton = (ImageButton) findViewById(R.id.searchButton);
        mTextEntered = (EditText) findViewById(R.id.searchBox);
        mMessage = (TextView) findViewById(R.id.message);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mBookAdapter = new BookAdapter(this, new ArrayList<Book>());

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View v = getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                mBookAdapter.clear();

                if (mTextEntered.getText().toString().trim().matches("")) {
                    mMessage.setVisibility(View.VISIBLE);
                    mMessage.setText("Please Enter Something In The Search Box");
                } else {
                    if (isNetworkConnected()) {
                        BookAsyncTask task = new BookAsyncTask();
                        task.execute();
                    } else {
                        mMessage.setVisibility(View.VISIBLE);
                        mMessage.setText("Your Device Is Not Connected To The Internet");
                    }
                }
            }
        });

        ListView bookListView = (ListView) findViewById(R.id.list);

        bookListView.setAdapter(mBookAdapter);

    }

    public boolean isNetworkConnected() { //check network connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) //IMPORTANT as u can't call info.isConnected() if info is NULL. Will throw null point exception. So first check it's null or not
            return true;
        else
            return false;
    }

    public String getText() {
        return mTextEntered.getText().toString();
    }

    private class BookAsyncTask extends AsyncTask<Void, Void, List<Book>> {

        @Override
        protected void onPreExecute() {
            mMessage.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Book> doInBackground(Void... voids) {
            List<Book> result = QueryUtils.fetchBookData(getText());
            return result;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            mProgressBar.setVisibility(View.INVISIBLE);

            if (books == null) {
                mMessage.setVisibility(View.VISIBLE);
                mMessage.setText("No Books Found.");
            } else {
                mMessage.setVisibility(View.GONE);
                mBookAdapter.addAll(books);
            }
        }
    }

}