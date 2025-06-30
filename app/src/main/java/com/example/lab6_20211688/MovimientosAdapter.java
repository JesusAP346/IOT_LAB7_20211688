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
import com.example.lab6_20211688.models.MovimientoLinea1;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MovimientosAdapter extends RecyclerView.Adapter<MovimientosAdapter.MovimientoViewHolder> {

    private List<MovimientoLinea1> listaMovimientos;
    private Context context;

    public MovimientosAdapter(List<MovimientoLinea1> listaMovimientos, Context context) {
        this.listaMovimientos = listaMovimientos;
        this.context = context;
    }

    @NonNull
    @Override
    public MovimientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento, parent, false);
        return new MovimientoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimientoViewHolder holder, int position) {
        MovimientoLinea1 mov = listaMovimientos.get(position);

        holder.idTarjeta.setText("ID Tarjeta: " + mov.getIdTarjeta());
        holder.fecha.setText("Fecha: " + mov.getFechaTexto()); // Mostrar fecha legible
        holder.estaciones.setText("De: " + mov.getEstacionEntrada() + " a " + mov.getEstacionSalida());
        holder.tiempo.setText("Duración: " + mov.getTiempoViaje() + " min");

        holder.btnEditar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_movimiento, null);

            TextView editIdTarjeta = dialogView.findViewById(R.id.editIdTarjeta);
            TextView editFecha = dialogView.findViewById(R.id.editFecha);
            TextView editEntrada = dialogView.findViewById(R.id.editEntrada);
            TextView editSalida = dialogView.findViewById(R.id.editSalida);
            TextView editTiempo = dialogView.findViewById(R.id.editTiempo);
            Button btnEditarMovimiento = dialogView.findViewById(R.id.btnEditarMovimiento);

            editIdTarjeta.setText(mov.getIdTarjeta());
            editFecha.setText(mov.getFechaTexto()); // Mostrar como texto
            editEntrada.setText(mov.getEstacionEntrada());
            editSalida.setText(mov.getEstacionSalida());
            editTiempo.setText(String.valueOf(mov.getTiempoViaje()));

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            btnEditarMovimiento.setOnClickListener(view -> {
                String nuevoIdTarjeta = editIdTarjeta.getText().toString();
                String nuevaFechaTexto = editFecha.getText().toString(); // solo se edita el texto
                String nuevaEntrada = editEntrada.getText().toString();
                String nuevaSalida = editSalida.getText().toString();
                int nuevoTiempo = Integer.parseInt(editTiempo.getText().toString());

                FirebaseFirestore.getInstance()
                        .collection("movimientos-linea1")
                        .document(mov.getId())
                        .update(
                                "idTarjeta", nuevoIdTarjeta,
                                "fechaTexto", nuevaFechaTexto,
                                "estacionEntrada", nuevaEntrada,
                                "estacionSalida", nuevaSalida,
                                "tiempoViaje", nuevoTiempo
                        )
                        .addOnSuccessListener(unused -> {
                            mov.setIdTarjeta(nuevoIdTarjeta);
                            mov.setFechaTexto(nuevaFechaTexto); // actualizar solo el texto
                            mov.setEstacionEntrada(nuevaEntrada);
                            mov.setEstacionSalida(nuevaSalida);
                            mov.setTiempoViaje(nuevoTiempo);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Movimiento actualizado", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
            });

            dialog.show();
        });

        holder.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar")
                    .setMessage("¿Deseas eliminar este movimiento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("movimientos-linea1")
                                .document(mov.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    listaMovimientos.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show();
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

    public static class MovimientoViewHolder extends RecyclerView.ViewHolder {
        TextView idTarjeta, fecha, estaciones, tiempo;
        Button btnEditar, btnEliminar;

        public MovimientoViewHolder(@NonNull View itemView) {
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
