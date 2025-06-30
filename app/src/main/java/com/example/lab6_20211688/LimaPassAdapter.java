package com.example.lab6_20211688.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20211688.R;
import com.example.lab6_20211688.models.MovimientoLimaPass;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LimaPassAdapter extends RecyclerView.Adapter<LimaPassAdapter.LimaPassViewHolder> {

    private List<MovimientoLimaPass> listaMovimientos;
    private Context context;

    public LimaPassAdapter(List<MovimientoLimaPass> listaMovimientos, Context context) {
        this.listaMovimientos = listaMovimientos;
        this.context = context;
    }

    @NonNull
    @Override
    public LimaPassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento, parent, false);
        return new LimaPassViewHolder(vista);
    }

    public void setListaMovimientos(List<MovimientoLimaPass> nuevaLista) {
        this.listaMovimientos = nuevaLista;
        notifyDataSetChanged(); // actualiza el RecyclerView
    }

    @Override
    public void onBindViewHolder(@NonNull LimaPassViewHolder holder, int position) {
        MovimientoLimaPass mov = listaMovimientos.get(position);

        holder.idTarjeta.setText("ID Tarjeta: " + mov.getIdTarjeta());
        holder.fecha.setText("Fecha: " + mov.getFecha());
        holder.estaciones.setText("De: " + mov.getParaderoEntrada() + " a " + mov.getParaderoSalida());
        holder.tiempo.setVisibility(View.VISIBLE);
        holder.tiempo.setText("Duración: " + mov.getTiempoViaje() + " min");


        // Botón Editar
        holder.btnEditar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_movimiento, null);

            TextView editIdTarjeta = dialogView.findViewById(R.id.editIdTarjeta);
            TextView editFecha = dialogView.findViewById(R.id.editFecha);
            TextView editEntrada = dialogView.findViewById(R.id.editEntrada);
            TextView editSalida = dialogView.findViewById(R.id.editSalida);
            Button btnGuardarCambios = dialogView.findViewById(R.id.btnEditarMovimiento);

            editIdTarjeta.setText(mov.getIdTarjeta());
            editFecha.setText(mov.getFecha());
            editEntrada.setText(mov.getParaderoEntrada());
            editSalida.setText(mov.getParaderoSalida());

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            btnGuardarCambios.setOnClickListener(v1 -> {
                String nuevoId = editIdTarjeta.getText().toString();
                String nuevaFecha = editFecha.getText().toString();
                String nuevoEntrada = editEntrada.getText().toString();
                String nuevoSalida = editSalida.getText().toString();

                FirebaseFirestore.getInstance()
                        .collection("movimientos-limapass")
                        .document(mov.getId())
                        .update(
                                "idTarjeta", nuevoId,
                                "fecha", nuevaFecha,
                                "paraderoEntrada", nuevoEntrada,
                                "paraderoSalida", nuevoSalida
                        )
                        .addOnSuccessListener(unused -> {
                            mov.setIdTarjeta(nuevoId);
                            mov.setFecha(nuevaFecha);
                            mov.setParaderoEntrada(nuevoEntrada);
                            mov.setParaderoSalida(nuevoSalida);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
            });

            dialog.show();
        });

        // Botón Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar Movimiento")
                    .setMessage("¿Estás seguro de eliminar este movimiento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("movimientos-limapass")
                                .document(mov.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    listaMovimientos.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaMovimientos.size();
    }

    public static class LimaPassViewHolder extends RecyclerView.ViewHolder {
        TextView idTarjeta, fecha, estaciones, tiempo;
        Button btnEditar, btnEliminar;

        public LimaPassViewHolder(@NonNull View itemView) {
            super(itemView);
            idTarjeta = itemView.findViewById(R.id.textViewIdTarjeta);
            fecha = itemView.findViewById(R.id.textViewFecha);
            estaciones = itemView.findViewById(R.id.textViewEstaciones);
            tiempo = itemView.findViewById(R.id.textViewTiempo);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
