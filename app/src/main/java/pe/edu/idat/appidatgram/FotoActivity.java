package pe.edu.idat.appidatgram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FotoActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private static final int CAMERA_REQUEST = 1888;
    //Es un codigo de solicitud, es una guia para que lo identifiquen
    //al momento de recibir la respuesta.
    private ImageView imgFoto;
    private Button btnCompartir;
    String mRutaFotoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("IdatGram");
        btnCompartir =findViewById(R.id.btnCompartir);
        imgFoto = findViewById(R.id.imgFoto);

        btnCompartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,
                menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        String mensaje ="";
        if(idItem == R.id.opcion_config){
            mensaje = "Click en la opción config.";
        }else if(idItem == R.id.opcion_camara){
            if(PermisoEscrituraAlmacenamiento()){
                IntencionTomarFoto();
            }else {
                requestStoragePermission();
            }
            //mensaje = "Click en la opción camara.";
        }
        //Toast.makeText(this, mensaje,
          //      Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    private boolean PermisoEscrituraAlmacenamiento(){
        int result = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
        boolean exito = false;
        if(result == PackageManager.PERMISSION_GRANTED){
            exito = true;
        }
        return exito;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode  == 0){
            //Si la respuesta fue cancelada.
            if(grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(FotoActivity.this,
                        "Permiso ACEPTADO, ahora usted puede escribir",
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(FotoActivity.this,
                        "Permiso RECHAZADO, usted no puede escribir",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File crearArchivoImagen() throws IOException{
        //Crear un archivo de imagen
        String timeStamp = new
                SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPGE_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mRutaFotoActual = image.getAbsolutePath();
        return image;
    }

    private void IntencionTomarFoto(){
        Intent takePictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())
                != null){
            //Existe una actividad de cámara para manejar el intent.
            //Crear un nuevo archivo con la foto.
            File photoFile = null;
            try {
                photoFile = crearArchivoImagen();
            }catch (IOException ex){

            }
            //Si el archivo fue creado con éxito
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "pe.edu.idat.appidatgram.provider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(takePictureIntent,
                        CAMERA_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        grabarFotoGaleria();
        mostrarFoto();
    }

    private void grabarFotoGaleria(){
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File archivo = new File(mRutaFotoActual);
        Uri contentUri = Uri.fromFile(archivo);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void mostrarFoto(){
        //Se obtiene las dimensiones del ImageView
        int targetW = imgFoto.getWidth();
        int targetH = imgFoto.getHeight();
        //Obtener las dimensiones del archivo BitMap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mRutaFotoActual, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        //Determinar cuanto vamos a escalar la imagen
        int scala = Math.min(photoW/targetW, photoH/targetH);
        //Decodificar el archivo de imagen en un mapa de bits para
        //llenar la vista respectiva.
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scala;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(mRutaFotoActual,
                bmOptions);
        imgFoto.setImageBitmap(bitmap);

    }


}
