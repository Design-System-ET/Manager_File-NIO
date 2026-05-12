# Manager_File-NIO

Java project for file persistence using NIO (New Input/Output) API.

## Requirements

- Java 26+

## Build

```bash
mvn compile
```

## Run

```bash
mvn compile exec:java -Dexec.mainClass="org.design_system.Main"
```

## Marco Teorico: Java NIO

### Que es Java NIO?

Java NIO (New Input/Output) es una API introduced en Java 1.4 que proporciona una forma alternativa de manejar operaciones de I/O. A diferencia del I/O tradicional basado en streams, NIO trabaja con buffers (canales) y permite operaciones no bloqueantes.

### Componentes Principales

#### 1. Channel
- Representa una conexion abierta hacia un dispositivo (archivo, socket)
- Permite lectura y escritura bidireccional
- Tipos principales:
  - `FileChannel`: para operaciones con archivos
  - `SocketChannel`: para conexiones de red
  - `ServerSocketChannel`: para servidores TCP

#### 2. Buffer
- Es un contenedor de datos primitivos
- Almacena datos temporalmente durante operaciones de I/O
- Tipos principales:
  - `ByteBuffer`: mas utilizado
  - `CharBuffer`, `IntBuffer`, `LongBuffer`, etc.

#### 3. Selector
- Permite manejar multiples canales con un solo thread
- Ideal para aplicaciones que manejan muchas conexiones simultaneas

### Diferencias entre I/O Tradicional y NIO

| Caracteristica | I/O Tradicional | NIO |
|----------------|-----------------|-----|
| Orientacion | Streams (flujo de bytes) | Buffers (bloques de datos) |
| Bloqueo | Bloqueante | Puede ser no bloqueante |
| Rendimiento | Menor para grandes archivos | Mejor para archivos grandes |
| Programacion | Mas simple | Mas complejo |

### Conceptos Clave de Buffer

- **Capacity**: tamanho maximo del buffer
- **Position**: posicion actual de lectura/escritura
- **Limit**: hasta donde se puede leer/escribir
- **flip()**: cambia el buffer de modo escritura a modo lectura
- **clear()**: resetea el buffer para nuevas escrituras
- **hasRemaining()**: verifica si hay datos por leer

### RandomAccessFile

Aunque no es estrictamente parte de NIO, `RandomAccessFile` permite acceso aleatorio a archivos:
- Puede leer y escribir en cualquier posicion
- Mode "rw" = lectura y escritura
- Mode "r" = solo lectura

---

## Arquitectura del Proyecto

### Interface: FilePersistanse

```java
public interface FilePersistanse {
    String write(final String data);
    String findAll();
}
```

La interfaz `FilePersistanse` define el contrato para la persistencia de archivos:

| Metodo | Descripcion |
|--------|-------------|
| `write(String data)` | Escribe datos en el archivo y retorna los datos escritos |
| `findAll()` | Lee y retorna todo el contenido del archivo |

### Implementacion: NIOFilePersistanse

```java
public class NIOFilePersistanse implements FilePersistanse
```

Implementa la interface usando la API NIO de Java.

---

## Metodos Utilizados

### Constructor: `NIOFilePersistanse(String fileName)`

```java
public NIOFilePersistanse(String fileName) throws IOException
```

**Descripcion**: Inicializa la clase y crea el directorio de almacenamiento si no existe.

**Parametros**:
- `fileName`: nombre del archivo donde se almacenaran los datos

**Ruta de almacenamiento**: `{user.dir}/managerFiles/NIO/{fileName}`

**Codigo clave**:
```java
var file = new File(currentDir + storedDir);
if (!file.exists() && !file.mkdirs()) {
    throw new IOException("Failed to create directory: " + file.getAbsolutePath());
}
```

---

### Metodo: `write(String data)`

```java
@Override
public String write(String data)
```

**Descripcion**: Escribe datos en el archivo usando `RandomAccessFile` con modo lectura/escritura ("rw").

**Caracteristicas**:
- Usa try-with-resources para manejo automatico de recursos
- Mueve el cursor al final del archivo con `seek()`
- Agrega los datos sin sobrescribir contenido existente
- Agrega salto de linea al final

**Codigo clave**:
```java
var file = new RandomAccessFile(new File(currentDir + storedDir + fileName), "rw");
file.seek(file.length());  // Mueve cursor al final
file.writeBytes(data);      // Escribe los datos
file.writeBytes(System.lineSeparator());  // Agrega nueva linea
```

**Retorno**: Devuelve los datos que fueron escritos.

---

### Metodo: `findAll()`

```java
@Override
public String findAll()
```

**Descripcion**: Lee todo el contenido del archivo usando NIO con `ByteBuffer` y `FileChannel`.

**Caracteristicas**:
- Obtiene el `FileChannel` desde `RandomAccessFile`
- Usa un `ByteBuffer` de 256 bytes para almacenar temporalmente los datos
- Usa el patron flip/clear para alternar entre escritura y lectura del buffer

**Codigo clave**:
```java
var channel = file.getChannel();
var buffer = ByteBuffer.allocate(256);

var bytesReader = channel.read(buffer);
while(bytesReader != -1) {
    buffer.flip();  // Cambia a modo lectura
    while(buffer.hasRemaining()) {
        content.append((char) buffer.get());
    }
    buffer.clear();  // Limpia para siguiente lectura
    bytesReader = channel.read(buffer);
}
```

**Retorno**: Cadena de texto con todo el contenido del archivo.

---

### Metodo: `clearFile()`

```java
public void clearFile()
```

**Descripcion**: Metodo adicional para limpiar el contenido del archivo.

**Caracteristicas**:
- Usa `FileOutputStream` para truncar el archivo
- El archivo queda vacio despues de ejecutarse

**Codigo clave**:
```java
try (OutputStream outputStream = new FileOutputStream(new File(currentDir + storedDir + fileName))) {
    System.out.println("File cleared successfully.");
}
```

---

## Flujo de Datos

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                            │
│  Crea una instancia de NIOFilePersistanse con "user.csv"    │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              NIOFilePersistanse.java                        │
│                                                             │
│  1. write("Maria Cabrera, Av Espana 409, 099789789")        │
│     ├── Crea RandomAccessFile en modo "rw"                  │
│     ├── seek() al final del archivo                        │
│     └── writeBytes() + salto de linea                      │
│                                                             │
│  2. findAll()                                              │
│     ├── Obtiene FileChannel                                │
│     ├── Lee con ByteBuffer (256 bytes)                     │
│     ├── flip() / clear() para cada lectura                 │
│     └── Retorna contenido completo como String             │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              managerFiles/NIO/user.csv                      │
│  Maria Cabrera, Av Espana 409, 099789789                    │
└─────────────────────────────────────────────────────────────┘
```

---

## Estructura del Proyecto

```
Manager_File-NIO/
├── pom.xml
├── README.md
├── LICENCE
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── design_system/
│                   ├── Main.java
│                   └── Persistense/
│                       ├── FilePersistanse.java (interface)
│                       └── NIOFilePersistanse.java (implementacion)
└── managerFiles/
    └── NIO/
        └── user.csv
```

---

## Licencia

MIT License - ver archivo `LICENCE`