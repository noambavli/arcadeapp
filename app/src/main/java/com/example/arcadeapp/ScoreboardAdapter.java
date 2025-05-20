package com.example.arcadeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ScoreViewHolder> {
    private List<ScoreboardItem> scores;

    public ScoreboardAdapter(List<ScoreboardItem> scores) {
        this.scores = scores;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scoreboard, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        ScoreboardItem score = scores.get(position);
        holder.rankText.setText(String.valueOf(position + 1));
        holder.playerNameText.setText(score.getUsername());
        holder.scoreText.setText(String.valueOf(score.getScore()));

        // Alternate between blue and yellow backgrounds
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.scoreboard_item_blue);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.scoreboard_item_yellow);
        }
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView rankText;
        TextView playerNameText;
        TextView scoreText;

        ScoreViewHolder(View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            playerNameText = itemView.findViewById(R.id.playerNameText);
            scoreText = itemView.findViewById(R.id.scoreText);
        }
    }
} 