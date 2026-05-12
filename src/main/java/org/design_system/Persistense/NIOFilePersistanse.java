package org.design_system.Persistense;

import java.io.*;
import java.nio.ByteBuffer;

public class NIOFilePersistanse implements FilePersistanse{

    // obtenemos en la variable la ubicacion actual del proyecto usando la propiedad del sistema "user.dir"
    private final String currentDir = System.getProperty("user.dir");
    // Indica las subcarpetas que vamos a crear a partir de la ubicacion anterior
    private final String storedDir = File.separator + "managerFiles" + File.separator + "NIO" + File.separator;
    // el nombre que se le asignara al archivo fisico en el disco duro
    private final String fileName;


    public NIOFilePersistanse(String fileName) throws IOException {
        this.fileName = fileName;

        //intancia de File que obtiene el path de las sub carpetas
        var file = new File(currentDir + storedDir);
        //si no existe se crean
        if (!file.exists() && !file.mkdirs()) throw new IOException("Failed to create directory: " + file.getAbsolutePath());

    }

    // Metodo que permite escribir datos en el archivo usando NIO (New Input/Output)
    @Override
    public String write(String data) {

        // Bloque try-with-resources para manejar automaticamente el cierre del archivo
        try(
                // Crea un archivo de acceso aleatorio en modo lectura/escritura
                var file = new RandomAccessFile(new File(currentDir + storedDir + fileName), "rw");

                ){

            // Mueve el cursor al final del archivo para agregar datos sin sobrescribir
            file.seek(file.length());
            // Escribe los datos proporcionados en el archivo
            file.writeBytes(data);
            // Agrega una nueva linea al final del archivo
            file.writeBytes(System.lineSeparator());


        }catch(IOException e){
            e.printStackTrace();
        }

        // Devuelve los datos que fueron escritos al archivo
        return data;
    }

    // Metodo que permite leer todos los datos del archivo usando NIO
    @Override
    public String findAll() {
        // Crea un StringBuffer para almacenar el contenido leido del archivo
        var content = new StringBuffer();

        try(
                // Crea un archivo de acceso aleatorio en modo solo lectura
                var file = new RandomAccessFile(new File(currentDir + storedDir + fileName), "r");
                // Obtiene el canal de archivos para usar operaciones NIO
                var channel = file.getChannel();

                ){
            // Crea un buffer de bytes con capacidad de 256 bytes para leer datos
            var buffer = ByteBuffer.allocate(256);
            // Lee datos del canal y los almacena en el buffer, devuelve la cantidad de bytes leidos
            var bytesReader = channel.read(buffer);
            // Bucle que continua mientras haya bytes para leer (-1 indica fin de archivo)
            while(bytesReader != -1){
                // Prepara el buffer para ser ledo (cambia el modo de escritura a lectura)
                buffer.flip();
                // Bucle que lee cada byte restante del buffer
                while(buffer.hasRemaining()){
                    // Convierte cada byte a caracter y lo agrega al contenido
                    content.append((char) buffer.get());

                }

                // Limpia el buffer para escribir nuevos datos
                buffer.clear();
                // Continua leyendo mas bytes del canal
                bytesReader = channel.read(buffer);
            }


        }catch(IOException e){
            e.printStackTrace();
        }

        // Devuelve el contenido completo del archivo como una cadena de texto
        return content.toString();
    }

    public void clearFile(){
        // Metodo para limpiar el contenido del archivo usando un OutputStream
        try (OutputStream outputStream = new FileOutputStream(new File(currentDir + storedDir + fileName));)
        {
            System.out.println("File cleared successfully.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
