package it.unimib.worldnews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.DateTimeUtil;

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

        textViewTitle.setText(newsArray[position].getTitle());
        textViewDate.setText(DateTimeUtil.getDate(newsArray[position].getDate()));

        return convertView;
    }
}
