package it.unimib.worldnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;

/**
 * Custom adapter that extends ArrayAdapter to show an ArrayList of News.
 */
public class NewsListAdapter extends ArrayAdapter<News> {

    private final List<News> newsList;
    private final int layout;
    private final OnDeleteButtonClickListener onDeleteButtonClickListener;

    /**
     * Interface to associate a listener to other elements defined in the layout
     * chosen for the ListView item (e.g., a Button).
     */
    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(News news);
    }

    public NewsListAdapter(@NonNull Context context, int layout, @NonNull List<News> newsList,
                           OnDeleteButtonClickListener onDeleteButtonClickListener) {
        super(context, layout, newsList);
        this.layout = layout;
        this.newsList = newsList;
        this.onDeleteButtonClickListener = onDeleteButtonClickListener;
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

        textViewTitle.setText(newsList.get(position).getTitle());
        textViewAuthor.setText(newsList.get(position).getAuthor());

        buttonDelete.setOnClickListener(v -> {
            News news = newsList.get(position);
            newsList.remove(news);
            // Call this method to refresh the UI and update the content of ListView
            notifyDataSetChanged();
            onDeleteButtonClickListener.onDeleteButtonClick(news);
        });

        return convertView;
    }
}
