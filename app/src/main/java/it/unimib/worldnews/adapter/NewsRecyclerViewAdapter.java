package it.unimib.worldnews.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.worldnews.R;
import it.unimib.worldnews.model.News;

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
        void onDeleteButtonPressed(int position);
    }

    private final List<News> newsList;
    private final OnItemClickListener onItemClickListener;

    public NewsRecyclerViewAdapter(List<News> newsList, OnItemClickListener onItemClickListener) {
        this.newsList = newsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public NewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.fav_news_list_item, parent, false);

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
        private final TextView textViewAuthor;

        public NewViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textview_title);
            textViewAuthor = itemView.findViewById(R.id.textview_author);
            Button buttonDelete = itemView.findViewById(R.id.button_delete);
            itemView.setOnClickListener(this);
            buttonDelete.setOnClickListener(this);
        }

        public void bind(News news) {
            textViewTitle.setText(news.getTitle());
            textViewAuthor.setText(news.getAuthor());
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.button_delete) {
                newsList.remove(getAdapterPosition());
                // Call this method to refresh the UI after the deletion of an item
                // and update the content of RecyclerView
                notifyItemRemoved(getAdapterPosition());
                onItemClickListener.onDeleteButtonPressed(getAdapterPosition());
            } else {
                onItemClickListener.onNewsItemClick(newsList.get(getAdapterPosition()));
            }
        }
    }
}
