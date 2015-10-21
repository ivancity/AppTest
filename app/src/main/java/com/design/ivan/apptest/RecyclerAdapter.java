package com.design.ivan.apptest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ivanm on 10/13/15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecViewHolder>{

    OnItemClickListener clickListener;

    List<String> categoryNames;

    public RecyclerAdapter(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }

    @Override
    public RecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_item, parent, false);
        RecViewHolder viewHolder = new RecViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecViewHolder holder, int position) {
        holder.textCategoryName.setText(categoryNames.get(position));
    }

    @Override
    public int getItemCount() {
        return categoryNames == null ? 0 : categoryNames.size();
    }

    class RecViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtImage;
        TextView textCategoryName;

        public RecViewHolder(View itemView) {
            super(itemView);

            //TODO: change this line to a ImageView instead of TextView and find correct R.id
            //txtImage = (TextView) itemView.findViewById(R.id.txt_img);
            textCategoryName = (TextView) itemView.findViewById(R.id.txt_category_name);

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    /**
     * This interface will allow us to implement the click listener call back to the
     * FragmentCategory and open a new Activity.
     */
    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}
