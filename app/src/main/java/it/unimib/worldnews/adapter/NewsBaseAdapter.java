package it.unimib.worldnews.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;

/**
 * Custom adapter that extends BaseAdapter to show a list of News.
 */
public class NewsBaseAdapter extends BaseAdapter {

    private final List<News> newsList;

    public NewsBaseAdapter(List<News> newsList) {
        this.newsList = newsList;
    }

    @Override
    public int getCount() {
        if (newsList != null) {
            return newsList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.fav_news_list_item, parent, false);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textview_title);
        TextView textViewAuthor = convertView.findViewById(R.id.textview_author);
        Button buttonDelete = convertView.findViewById(R.id.button_delete);

        textViewTitle.setText(newsList.get(position).getTitle());
        textViewAuthor.setText(newsList.get(position).getAuthor());

        buttonDelete.setOnClickListener(v -> {
            newsList.remove(position);
            // Call this method to refresh the UI and update the content of ListView
            notifyDataSetChanged();
        });

        return convertView;
    }
}
