package com.example.lab6_20211688;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.lab6_20211688.databinding.FragmentLaboratorio7Binding;
import com.google.firebase.storage.UploadTask;

public class Laboratorio7Fragment extends Fragment {
    private String nombreUltimaImagen = null;
    private FragmentLaboratorio7Binding binding;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri imagenUri = null;
    private CloudStorage cloudStorage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLaboratorio7Binding.inflate(inflater, container, false);
        cloudStorage = new CloudStorage();

        binding.btnSeleccionar.setOnClickListener(v -> seleccionarImagen());
        binding.btnSubir.setOnClickListener(v -> subirImagen());
        binding.btnDescargar.setOnClickListener(v -> mostrarSelectorDeImagen());

        return binding.getRoot();
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void subirImagen() {
        if (imagenUri != null) {
            String nombreIngresado = binding.editNombreImagen.getText().toString().trim();
            if (nombreIngresado.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese un nombre para la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            nombreUltimaImagen = nombreIngresado + ".jpg";
            UploadTask uploadTask = cloudStorage.subirImagen(nombreUltimaImagen, imagenUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                cloudStorage.obtenerReferencia(nombreUltimaImagen).getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Toast.makeText(getContext(), "Imagen subida con éxito:\n" + uri.toString(), Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Error al obtener URL", Toast.LENGTH_SHORT).show()
                        );
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        } else {
            Toast.makeText(getContext(), "Seleccione una imagen primero", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarSelectorDeImagen() {
        cloudStorage.listarImagenes(nombres -> {
            if (nombres.isEmpty()) {
                Toast.makeText(getContext(), "No hay imágenes para descargar", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] arrayNombres = nombres.toArray(new String[0]);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Selecciona una imagen")
                    .setItems(arrayNombres, (dialog, which) -> {
                        nombreUltimaImagen = arrayNombres[which];
                        descargarImagen();
                    })
                    .show();

        }, error -> Toast.makeText(getContext(), "Error al listar imágenes", Toast.LENGTH_SHORT).show());
    }

    private void descargarImagen() {
        if (nombreUltimaImagen == null) {
            Toast.makeText(getContext(), "Primero suba una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        cloudStorage.obtenerReferencia(nombreUltimaImagen).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(requireContext()).load(uri).into(binding.imgPreview);

                    DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setTitle("Descargando imagen");
                    request.setDescription(nombreUltimaImagen);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreUltimaImagen);
                    downloadManager.enqueue(request);

                    Toast.makeText(getContext(), "Descarga iniciada...", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener URL", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imagenUri = data.getData();
            binding.imgPreview.setImageURI(imagenUri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
