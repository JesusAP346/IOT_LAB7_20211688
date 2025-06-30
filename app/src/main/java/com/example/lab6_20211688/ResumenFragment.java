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

import com.example.lab6_20211688.databinding.FragmentResumenBinding;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResumenFragment extends Fragment {

    private FragmentResumenBinding binding;
    private FirebaseFirestore db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat mesAnioFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResumenBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        configurarDatePickers();
        binding.btnFiltrarResumen.setOnClickListener(v -> cargarDatos());

        return binding.getRoot();
    }

    private void configurarDatePickers() {
        binding.fechaInicioResumen.setOnClickListener(v -> mostrarDatePicker(binding.fechaInicioResumen));
        binding.fechaFinResumen.setOnClickListener(v -> mostrarDatePicker(binding.fechaFinResumen));
    }

    private void mostrarDatePicker(android.widget.EditText campoFecha) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            campoFecha.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void cargarDatos() {
        String inicioStr = binding.fechaInicioResumen.getText().toString().trim();
        String finStr = binding.fechaFinResumen.getText().toString().trim();

        if (inicioStr.isEmpty() || finStr.isEmpty()) {
            Toast.makeText(getContext(), "Seleccione ambas fechas", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date fechaInicio = sdf.parse(inicioStr);
            Date fechaFin = sdf.parse(finStr);

            if (fechaInicio == null || fechaFin == null) return;

            Map<String, Integer> conteoMesLinea1 = new HashMap<>();
            Map<String, Integer> conteoMesLimaPass = new HashMap<>();
            Map<String, Integer> conteoTarjetasLinea1 = new HashMap<>();
            Map<String, Integer> conteoTarjetasLimaPass = new HashMap<>();

            db.collection("movimientos-linea1").get().addOnSuccessListener(snapshot -> {
                for (QueryDocumentSnapshot doc : snapshot) {
                    Timestamp ts = doc.getTimestamp("fecha");
                    String idTarjeta = doc.getString("idTarjeta");

                    if (ts != null && idTarjeta != null) {
                        Date fechaDoc = ts.toDate();
                        if (!fechaDoc.before(fechaInicio) && !fechaDoc.after(fechaFin)) {
                            String claveMes = mesAnioFormat.format(fechaDoc);
                            conteoMesLinea1.put(claveMes, conteoMesLinea1.getOrDefault(claveMes, 0) + 1);
                            conteoTarjetasLinea1.put(idTarjeta, conteoTarjetasLinea1.getOrDefault(idTarjeta, 0) + 1);
                        }
                    }
                }

                db.collection("movimientos-limapass").get().addOnSuccessListener(snapshot2 -> {
                    for (QueryDocumentSnapshot doc : snapshot2) {
                        String fechaStr = doc.getString("fecha");
                        String idTarjeta = doc.getString("idTarjeta");

                        if (fechaStr != null && idTarjeta != null) {
                            try {
                                Date fechaDoc = sdf.parse(fechaStr);
                                if (fechaDoc != null && !fechaDoc.before(fechaInicio) && !fechaDoc.after(fechaFin)) {
                                    String claveMes = mesAnioFormat.format(fechaDoc);
                                    conteoMesLimaPass.put(claveMes, conteoMesLimaPass.getOrDefault(claveMes, 0) + 1);
                                    conteoTarjetasLimaPass.put(idTarjeta, conteoTarjetasLimaPass.getOrDefault(idTarjeta, 0) + 1);
                                }
                            } catch (ParseException ignored) {}
                        }
                    }

                    mostrarGraficoBarras(conteoMesLinea1, conteoMesLimaPass);
                    mostrarGraficoTorta(conteoTarjetasLinea1, conteoTarjetasLimaPass);
                });
            });

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Formato de fecha incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarGraficoBarras(Map<String, Integer> linea1, Map<String, Integer> limapass) {
        Set<String> meses = new TreeSet<>(linea1.keySet());
        meses.addAll(limapass.keySet());

        List<BarEntry> entriesLinea1 = new ArrayList<>();
        List<BarEntry> entriesLimaPass = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int i = 0;
        for (String mes : meses) {
            labels.add(mes);
            entriesLinea1.add(new BarEntry(i, linea1.getOrDefault(mes, 0)));
            entriesLimaPass.add(new BarEntry(i, limapass.getOrDefault(mes, 0)));
            i++;
        }

        BarDataSet set1 = new BarDataSet(entriesLinea1, "Línea 1");
        BarDataSet set2 = new BarDataSet(entriesLimaPass, "Lima Pass");
        set1.setColor(android.graphics.Color.parseColor("#03DAC5"));
        set2.setColor(android.graphics.Color.parseColor("#BB86FC"));

        BarData barData = new BarData(set1, set2);
        barData.setBarWidth(0.4f);

        binding.barChartResumen.setData(barData);
        binding.barChartResumen.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartResumen.getXAxis().setGranularity(1f);
        binding.barChartResumen.groupBars(0f, 0.1f, 0.05f);
        binding.barChartResumen.invalidate();
        binding.barChartResumen.setDescription(new Description());
    }

    private void mostrarGraficoTorta(Map<String, Integer> linea1, Map<String, Integer> limapass) {
        List<PieEntry> entradas = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : linea1.entrySet()) {
            entradas.add(new PieEntry(entry.getValue(), entry.getKey() + " (Tren)"));
        }
        for (Map.Entry<String, Integer> entry : limapass.entrySet()) {
            entradas.add(new PieEntry(entry.getValue(), entry.getKey() + " (Bus)"));
        }

        PieDataSet dataSet = new PieDataSet(entradas, "Uso de Tarjetas");
        dataSet.setColors(
                new int[]{
                        android.graphics.Color.parseColor("#018786"),
                        android.graphics.Color.parseColor("#6200EE"),
                        android.graphics.Color.parseColor("#03DAC5")
                }
        );

        PieData data = new PieData(dataSet);
        binding.pieChartResumen.setData(data);
        binding.pieChartResumen.invalidate();
        binding.pieChartResumen.setDescription(new Description());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
