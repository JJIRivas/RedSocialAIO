package com.example.redsocialaio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Adaptador que conecta la lista de toots con el RecyclerView para mostrarlos
public class TootAdapter extends RecyclerView.Adapter<TootAdapter.TootViewHolder> {

    private List<Toot> toots;

    public TootAdapter(List<Toot> toots) {
        this.toots = toots;
    }

    @NonNull
    @Override
    public TootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos un layout simple para mostrar el texto del toot, esto crea o "infla" la vista individual para cada item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TootViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TootViewHolder holder, int position) {
        Toot toot = toots.get(position);
        // Convertimos el contenido HTML a texto simple para mostrarlo, "llena" los items, con los datos del post
        holder.textView.setText(android.text.Html.fromHtml(toot.getContent()));
    }

    @Override
    public int getItemCount() {
        //dice cuantos items estan en la lista
        return toots.size();
    }

    static class TootViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TootViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
