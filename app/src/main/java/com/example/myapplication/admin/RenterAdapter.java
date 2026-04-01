package com.example.myapplication.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class RenterAdapter extends RecyclerView.Adapter<RenterAdapter.ViewHolder> {

    private List<DocumentSnapshot> renterList;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(DocumentSnapshot doc);
    }

    public RenterAdapter(List<DocumentSnapshot> renterList, OnDeleteClickListener listener) {
        this.renterList = renterList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_renter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = renterList.get(position);

        holder.tvOrg.setText(doc.getString("orgName"));
        holder.tvOwner.setText("Owner: " + doc.getString("ownerName"));
        holder.tvLoc.setText("Location: " + doc.getString("location"));
        holder.tvEmail.setText("Email: " + doc.getString("email"));

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(doc));
    }

    @Override
    public int getItemCount() {
        return renterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrg, tvOwner, tvLoc, tvEmail;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrg = itemView.findViewById(R.id.tvOrgName);
            tvOwner = itemView.findViewById(R.id.tvOwnerName);
            tvLoc = itemView.findViewById(R.id.tvLocation);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnDelete = itemView.findViewById(R.id.btnDeleteRenter);
        }
    }
}