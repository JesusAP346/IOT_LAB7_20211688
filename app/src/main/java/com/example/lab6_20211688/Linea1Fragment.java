package com.example.lab6_20211688;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lab6_20211688.adapters.MovimientosAdapter;
import com.example.lab6_20211688.databinding.FragmentLinea1Binding;
import com.example.lab6_20211688.models.MovimientoLinea1;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class Linea1Fragment extends Fragment {

    private FragmentLinea1Binding binding;
    private FirebaseFirestore db;
    private List<MovimientoLinea1> listaMovimientos;
    private MovimientosAdapter adapter;

    public Linea1Fragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLinea1Binding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        binding.editTextFecha.setOnClickListener(v -> showDatePicker(dateStr -> binding.editTextFecha.setText(dateStr)));
        binding.editTextFechaInicio.setOnClickListener(v -> showDatePicker(dateStr -> binding.editTextFechaInicio.setText(dateStr)));
        binding.editTextFechaFin.setOnClickListener(v -> showDatePicker(dateStr -> binding.editTextFechaFin.setText(dateStr)));

        binding.recyclerViewMovimientos.setLayoutManager(new LinearLayoutManager(getContext()));
        listaMovimientos = new ArrayList<>();
        adapter = new MovimientosAdapter(listaMovimientos, getContext());
        binding.recyclerViewMovimientos.setAdapter(adapter);

        cargarMovimientos();

        binding.btnGuardarMovimiento.setOnClickListener(view -> {
            String idTarjeta = binding.layoutIdTarjeta.getEditText().getText().toString().trim();
            String fechaTexto = binding.layoutFecha.getEditText().getText().toString().trim();
            String entrada = binding.layoutEntrada.getEditText().getText().toString().trim();
            String salida = binding.layoutSalida.getEditText().getText().toString().trim();
            String tiempoStr = binding.layoutTiempo.getEditText().getText().toString().trim();

            if (idTarjeta.isEmpty() || fechaTexto.isEmpty() || entrada.isEmpty() || salida.isEmpty() || tiempoStr.isEmpty()) {
                Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            int tiempoViaje;
            try {
                tiempoViaje = Integer.parseInt(tiempoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tiempo inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date fechaDate = new SimpleDateFormat("dd/MM/yyyy").parse(fechaTexto);
                Timestamp fechaTimestamp = new Timestamp(fechaDate);

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                HashMap<String, Object> movMap = new HashMap<>();
                movMap.put("uid", uid);
                movMap.put("idTarjeta", idTarjeta);
                movMap.put("fecha", fechaTimestamp);
                movMap.put("fechaTexto", fechaTexto);
                movMap.put("estacionEntrada", entrada);
                movMap.put("estacionSalida", salida);
                movMap.put("tiempoViaje", tiempoViaje);

                db.collection("movimientos-linea1")
                        .add(movMap)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(getContext(), "Movimiento guardado exitosamente", Toast.LENGTH_SHORT).show();
                            Log.d("msg-test", "ID generado: " + docRef.getId());
                            limpiarFormulario();
                            cargarMovimientos();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al guardar movimiento", Toast.LENGTH_SHORT).show();
                            Log.e("msg-test", "Error al guardar", e);
                        });

            } catch (Exception e) {
                Toast.makeText(getContext(), "Formato de fecha inválido (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnFiltrarFechas.setOnClickListener(v -> filtrarPorFecha());

        return binding.getRoot();
    }

    private interface DatePickerCallback {
        void onDateSelected(String dateStr);
    }

    private void showDatePicker(DatePickerCallback callback) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    callback.onDateSelected(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void cargarMovimientos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("movimientos-linea1")
                .whereEqualTo("uid", uid)
                //.orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> mostrarLista(querySnapshot))
                .addOnFailureListener(e -> Log.e("msg-test", "Error al cargar movimientos", e));
    }

    private void filtrarPorFecha() {
        String fechaInicioStr = binding.layoutFechaInicio.getEditText().getText().toString().trim();
        String fechaFinStr = binding.layoutFechaFin.getEditText().getText().toString().trim();

        try {
            Date inicio = new SimpleDateFormat("dd/MM/yyyy").parse(fechaInicioStr);
            Date fin = new SimpleDateFormat("dd/MM/yyyy").parse(fechaFinStr);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("movimientos-linea1")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        listaMovimientos.clear();
                        for (var doc : querySnapshot) {
                            MovimientoLinea1 mov = doc.toObject(MovimientoLinea1.class);
                            mov.setId(doc.getId());

                            // Convertimos Timestamp a Date aquí
                            if (mov.getFecha() != null) {
                                Date fechaMov = mov.getFecha().toDate();
                                if (!fechaMov.before(inicio) && !fechaMov.after(fin)) {
                                    listaMovimientos.add(mov);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error al filtrar", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(getContext(), "Formato inválido (usa dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
        }
    }


    private void mostrarLista(QuerySnapshot querySnapshot) {
        listaMovimientos.clear();
        for (var document : querySnapshot) {
            MovimientoLinea1 mov = document.toObject(MovimientoLinea1.class);
            mov.setId(document.getId());
            listaMovimientos.add(mov);
        }
        adapter.notifyDataSetChanged();
    }

    private void limpiarFormulario() {
        binding.layoutIdTarjeta.getEditText().setText("");
        binding.layoutFecha.getEditText().setText("");
        binding.layoutEntrada.getEditText().setText("");
        binding.layoutSalida.getEditText().setText("");
        binding.layoutTiempo.getEditText().setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}