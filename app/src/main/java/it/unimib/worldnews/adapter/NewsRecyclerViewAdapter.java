package it.unimib.worldnews.adapter;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;
import it.unimib.worldnews.util.DateTimeUtil;

/**
 * Custom adapter that extends RecyclerView.Adapter to show an ArrayList of News
 * with a RecyclerView.
 */
public class NewsRecyclerViewAdapter extends
        RecyclerView.Adapter<NewsRecyclerViewAdapter.NewViewHolder> {

    /**
     * Interface to associate a click listener with
     * a RecyclerView item.
     */
    public interface OnItemClickListener {
        void onNewsItemClick(News news);
        void onFavoriteButtonPressed(int position);
    }

    private final List<News> newsList;
    private final Application application;
    private final OnItemClickListener onItemClickListener;

    public NewsRecyclerViewAdapter(List<News> newsList, Application application,
                                   OnItemClickListener onItemClickListener) {
        this.newsList = newsList;
        this.application = application;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public NewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.news_list_item, parent, false);

        return new NewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewViewHolder holder, int position) {
        holder.bind(newsList.get(position));
    }

    @Override
    public int getItemCount() {
        if (newsList != null) {
            return newsList.size();
        }
        return 0;
    }

    /**
     * Custom ViewHolder to bind data to the RecyclerView items.
     */
    public class NewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView textViewTitle;
        private final TextView textViewDate;
        private final ImageView imageViewFavoriteNews;

        public NewViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textview_title);
            textViewDate = itemView.findViewById(R.id.textview_date);
            imageViewFavoriteNews = itemView.findViewById(R.id.imageview_favorite_news);
            itemView.setOnClickListener(this);
            imageViewFavoriteNews.setOnClickListener(this);
        }

        public void bind(News news) {
            textViewTitle.setText(news.getTitle());
            textViewDate.setText(DateTimeUtil.getDate(news.getDate()));
            setImageViewFavoriteNews(newsList.get(getAdapterPosition()).isFavorite());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.imageview_favorite_news) {
                setImageViewFavoriteNews(!newsList.get(getAdapterPosition()).isFavorite());
                onItemClickListener.onFavoriteButtonPressed(getAdapterPosition());
            } else {
                onItemClickListener.onNewsItemClick(newsList.get(getAdapterPosition()));
            }
        }

        private void setImageViewFavoriteNews(boolean isFavorite) {
            if (isFavorite) {
                imageViewFavoriteNews.setImageDrawable(
                        AppCompatResources.getDrawable(application,
                                R.drawable.ic_baseline_favorite_24));
                imageViewFavoriteNews.setColorFilter(
                        ContextCompat.getColor(
                                imageViewFavoriteNews.getContext(),
                                R.color.red_500)
                );
            } else {
                imageViewFavoriteNews.setImageDrawable(
                        AppCompatResources.getDrawable(application,
                                R.drawable.ic_baseline_favorite_border_24));
                imageViewFavoriteNews.setColorFilter(
                        ContextCompat.getColor(
                                imageViewFavoriteNews.getContext(),
                                R.color.black)
                );
            }
        }
    }
}
