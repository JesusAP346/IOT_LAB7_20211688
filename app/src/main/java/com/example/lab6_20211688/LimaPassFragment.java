package com.example.lab6_20211688;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab6_20211688.adapters.LimaPassAdapter;
import com.example.lab6_20211688.databinding.FragmentLimaPassBinding;
import com.example.lab6_20211688.models.MovimientoLimaPass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LimaPassFragment extends Fragment {

    private FragmentLimaPassBinding binding;
    private FirebaseFirestore db;
    private ArrayList<MovimientoLimaPass> listaMovimientos;
    private LimaPassAdapter adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public LimaPassFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLimaPassBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        listaMovimientos = new ArrayList<>();
        adapter = new LimaPassAdapter(listaMovimientos, getContext());
        binding.recyclerViewMovimientosLp.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewMovimientosLp.setAdapter(adapter);

        cargarMovimientos();

        binding.btnGuardarLimaPass.setOnClickListener(view -> guardarMovimiento());
        binding.btnFiltrarFechasLp.setOnClickListener(view -> filtrarPorFechas());

        configurarPickers();

        return binding.getRoot();
    }

    private void cargarMovimientos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("movimientos-limapass")
                .whereEqualTo("uid", uid)
                //.orderBy("fechaServer", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("msg-limapass", "Error al leer", error);
                        return;
                    }

                    listaMovimientos.clear();
                    for (var doc : value) {
                        MovimientoLimaPass mov = doc.toObject(MovimientoLimaPass.class);
                        mov.setId(doc.getId());
                        listaMovimientos.add(mov);
                    }
                    adapter.setListaMovimientos(listaMovimientos);
                    adapter.notifyDataSetChanged();
                });
    }

    private void guardarMovimiento() {
        String idTarjeta = binding.layoutIdTarjetaLp.getEditText().getText().toString().trim();
        String fecha = binding.layoutFechaLp.getEditText().getText().toString().trim();
        String entrada = binding.layoutParaderoEntrada.getEditText().getText().toString().trim();
        String salida = binding.layoutParaderoSalida.getEditText().getText().toString().trim();
        String tiempoStr = binding.layoutTiempoLp.getEditText().getText().toString().trim();

        if (idTarjeta.isEmpty() || fecha.isEmpty() || entrada.isEmpty() || salida.isEmpty() || tiempoStr.isEmpty()) {
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

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        HashMap<String, Object> mov = new HashMap<>();
        mov.put("uid", uid);  // ✅ Se guarda el UID del usuario
        mov.put("idTarjeta", idTarjeta);
        mov.put("fecha", fecha);
        mov.put("paraderoEntrada", entrada);
        mov.put("paraderoSalida", salida);
        mov.put("tiempoViaje", tiempoViaje);
        mov.put("fechaServer", FieldValue.serverTimestamp());

        db.collection("movimientos-limapass")
                .add(mov)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Movimiento guardado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d("msg-limapass", "ID: " + documentReference.getId());
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show();
                    Log.e("msg-limapass", "Fallo al guardar", e);
                });
    }

    private void filtrarPorFechas() {
        String fechaInicioStr = binding.editTextFechaInicioLp.getText().toString().trim();
        String fechaFinStr = binding.editTextFechaFinLp.getText().toString().trim();

        if (fechaInicioStr.isEmpty() || fechaFinStr.isEmpty()) {
            Toast.makeText(getContext(), "Seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date fechaInicio = sdf.parse(fechaInicioStr);
            Date fechaFin = sdf.parse(fechaFinStr);

            ArrayList<MovimientoLimaPass> filtrados = new ArrayList<>();
            for (MovimientoLimaPass mov : listaMovimientos) {
                String fechaTexto = mov.getFecha();
                if (fechaTexto != null && !fechaTexto.isEmpty()) {
                    try {
                        Date fechaMov = sdf.parse(fechaTexto);
                        if (!fechaMov.before(fechaInicio) && !fechaMov.after(fechaFin)) {
                            filtrados.add(mov);
                        }
                    } catch (ParseException e) {
                        Log.w("msg-limapass", "Fecha inválida en un documento: " + fechaTexto);
                    }
                }
            }
            adapter.setListaMovimientos(filtrados);
            adapter.notifyDataSetChanged();

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            Log.e("msg-limapass", "Error al parsear fechas de filtro", e);
        }
    }


    private void configurarPickers() {
        binding.editTextFechaLp.setOnClickListener(v -> mostrarDatePicker(binding.editTextFechaLp));
        binding.editTextFechaInicioLp.setOnClickListener(v -> mostrarDatePicker(binding.editTextFechaInicioLp));
        binding.editTextFechaFinLp.setOnClickListener(v -> mostrarDatePicker(binding.editTextFechaFinLp));
    }

    private void mostrarDatePicker(com.google.android.material.textfield.TextInputEditText campoFecha) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            campoFecha.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void limpiarFormulario() {
        binding.layoutIdTarjetaLp.getEditText().setText("");
        binding.layoutFechaLp.getEditText().setText("");
        binding.layoutParaderoEntrada.getEditText().setText("");
        binding.layoutParaderoSalida.getEditText().setText("");
        binding.layoutTiempoLp.getEditText().setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
