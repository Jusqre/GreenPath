package com.example.tmapgreentest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmapgreentest.R;
import com.example.tmapgreentest.models.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> datas;

    public PostAdapter(List<Post> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post data = datas.get(position);
        holder.nicname.setText("작성자 : "+data.getNicname());
        holder.title.setText(data.getTitle());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView contents;
        private TextView nicname;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.item_post_title);
            nicname = itemView.findViewById(R.id.item_post_nicname);
        }
    }
}
