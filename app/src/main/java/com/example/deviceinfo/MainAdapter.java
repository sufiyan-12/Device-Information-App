package com.example.deviceinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private ArrayList<Model> list;
    private Context context;

    public MainAdapter(Context context, ArrayList<Model> arrayList) {
        super();
        this.context = context;
        this.list = arrayList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        Model model = list.get(position);
        holder.titleTV.setText(model.title);
        holder.descriptionTV.setText(model.description);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateList(ArrayList<Model> newList){
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
    class MainViewHolder extends RecyclerView.ViewHolder {
        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        TextView titleTV = itemView.findViewById(R.id.title);
        TextView descriptionTV = itemView.findViewById(R.id.description);
    }
}
