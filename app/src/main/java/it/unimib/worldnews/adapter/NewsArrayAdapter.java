package it.unimib.worldnews.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;

/**
 * Custom adapter that extends ArrayAdapter to show an array of News.
 */
public class NewsArrayAdapter extends ArrayAdapter<News> {

    private final int layout;
    private final News[] newsArray;

    public NewsArrayAdapter(@NonNull Context context, int layout, @NonNull News[] newsArray) {
        super(context, layout, newsArray);
        this.layout = layout;
        this.newsArray = newsArray;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(layout, parent, false);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textview_title);
        TextView textViewAuthor = convertView.findViewById(R.id.textview_author);
        Button buttonDelete = convertView.findViewById(R.id.button_delete);

        textViewTitle.setText(newsArray[position].getTitle());
        textViewAuthor.setText(newsArray[position].getAuthor());

        // Example to hide a widget at runtime
        buttonDelete.setVisibility(View.GONE);

        return convertView;
    }
}
