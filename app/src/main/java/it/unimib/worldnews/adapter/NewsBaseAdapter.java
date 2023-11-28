package it.unimib.worldnews.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.DateTimeUtil;

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
                    inflate(R.layout.news_list_item, parent, false);
        }

        TextView textViewTitle = convertView.findViewById(R.id.textview_title);
        TextView textViewDate = convertView.findViewById(R.id.textview_date);
        ImageView imageViewFavoriteNews = convertView.findViewById(R.id.imageview_favorite_news);

        imageViewFavoriteNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewFavoriteNews.setImageDrawable(
                        AppCompatResources.getDrawable(parent.getContext(),
                                R.drawable.ic_baseline_favorite_border_24));
            }
        });

        textViewTitle.setText(newsList.get(position).getTitle());
        textViewDate.setText(DateTimeUtil.getDate(newsList.get(position).getDate()));

        return convertView;
    }
}
