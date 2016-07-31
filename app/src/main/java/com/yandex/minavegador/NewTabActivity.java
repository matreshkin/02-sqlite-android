package com.yandex.minavegador;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URI;
import java.util.Locale;

public class NewTabActivity extends AppCompatActivity {

    public static final String RESULT_URL = "RESULT_URL";
    public static final String RESULT_QUERY = "RESULT_QUERY";
    private EditText mEdit;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tab);
        mList = (ListView) findViewById(R.id.list);
        mEdit = (EditText) findViewById(R.id.edit);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSuggestions();
            }
        });
        mEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    onEnter();
                    return true;
                }
                return false;
            }
        });
    }

    private void onEnter() {
        Bundle data = new Bundle();
        String text = getTrimmed();
        String textWithHttp = withHttp(text);
        if (isWebUrl(text)) {
            data.putString(RESULT_URL, text);
        } else if (isWebUrl(textWithHttp)) {
            data.putString(RESULT_URL, textWithHttp);
        } else {
            data.putString(RESULT_QUERY, text);
        }
        Intent intent = new Intent();
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean isWebUrl(String text) {
        if (!text.contains(".") || text.contains(" ")) {
            return false;
        }
        try {
            URI uri = URI.create(text);
            return uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static String withHttp(String text) {
        String lowered = text.toLowerCase(Locale.ENGLISH);
        if (lowered.startsWith("http://") || lowered.startsWith("https://")) {
            return text;
        }
        return "http://" + text;
    }

    private void updateSuggestions() {
        String[] arr = getTrimmed().split(" ");
        // TODO
    }

    @NonNull
    private String getTrimmed() {
        return mEdit.getText().toString().trim();
    }

    private void onSuggestionsUpdated(Cursor cursor) {
        mList.setAdapter(new CursorAdapter(getBaseContext(), cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return getLayoutInflater().inflate(R.layout.item_suggest, parent);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView text = (TextView) view.findViewById(R.id.text);
                text.setText(cursor.getString(cursor.getColumnCount() - 1));
            }
        });
    }
}
