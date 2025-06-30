package com.example.lab6_20211688;
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CloudStorage {

    private final StorageReference storageRef;

    public CloudStorage() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public UploadTask subirImagen(String nombreArchivo, Uri uri) {
        StorageReference archivoRef = storageRef.child("imagenes/" + nombreArchivo);
        return archivoRef.putFile(uri);
    }

    public StorageReference obtenerReferencia(String nombreArchivo) {
        return storageRef.child("imagenes/" + nombreArchivo);
    }

    public void listarImagenes(Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        storageRef.child("imagenes").listAll()
                .addOnSuccessListener(listResult -> {
                    List<String> nombres = new ArrayList<>();
                    for (StorageReference item : listResult.getItems()) {
                        nombres.add(item.getName());
                    }
                    onSuccess.accept(nombres);
                })
                .addOnFailureListener(onError::accept);
    }

}