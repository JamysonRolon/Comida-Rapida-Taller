# Comidas rápidas

Aplicación académica de escritorio en JavaFX, Spring Boot y PostgreSQL. Administra empleados, clientes, categorías, productos, stock y ventas en efectivo. Genera comprobantes internos de texto.

## Ejecutar en este equipo

Requisitos: JDK 21 o posterior compatible, Maven 3.6.3 o posterior y PostgreSQL. El desarrollo se verificó con JDK 24.0.2, Maven 3.9.11 y PostgreSQL 16 en las pruebas de integración.

Desde PowerShell, dentro de la carpeta del proyecto:

```powershell
.\scripts\preparar-postgres-local.ps1
.\scripts\iniciar.ps1
```

El primer script usa los binarios de PostgreSQL 18 instalados en este equipo. Crea un clúster exclusivo dentro de `.work/postgres-local`, escucha en `127.0.0.1:55434` y guarda una contraseña aleatoria de conexión en `.work/db-local.env`. No utiliza el servicio PostgreSQL existente. Si los binarios están en otra ruta:

```powershell
.\scripts\preparar-postgres-local.ps1 -PgBin 'C:\ruta\PostgreSQL\bin'
```

La aplicación crea las seis tablas al iniciar sobre una base vacía y valida sus correspondencias JPA. El script local activa `DEMO_DATA=true` y carga un conjunto ficticio idempotente. Si se ejecuta sin datos de demostración, la primera pantalla permite definir el administrador inicial.

## Credenciales de Acceso

### Usuario Administrador (Principal)
- **Usuario:** `pepito_admin`
- **Contraseña:** `PepitoAdmin123*`
- **Rol:** `ADMINISTRADOR` (Acceso completo: Gestión de Productos, Categorías, Empleados/Usuarios, Clientes, Exportación de Reportes a Excel y Ventas)

### Usuario Cajero (Demostración)
- **Usuario:** `daniel`
- **Rol:** `CAJERO` (Atención en punto de venta, registro de pedidos en carrito, facturación electrónica/POS, consulta de ventas)

## Datos ficticios incluidos

La carga de demostración crea cinco categorías, doce productos, cinco clientes y tres ventas de ejemplo. También crea estas cuentas:

| Nombre | Usuario | Rol |
| --- | --- | --- |
| Pepito Admin | `pepito_admin` | Administrador |
| Daniel | `daniel` | Cajero |
| Juan | `juan` | Administrador |

El script local genera contraseñas aleatorias para ambas cuentas y las conserva en `.work/db-local.env`, archivo excluido de Git. Para consultarlas en este equipo:

```powershell
Get-Content .work/db-local.env | Select-String '^DEMO_(DANIEL|JUAN)_PASSWORD='
```

Las contraseñas se guardan en PostgreSQL mediante BCrypt. La carga busca primero cada nombre de usuario, categoría, producto y documento, por lo que reiniciar la aplicación no duplica esos registros. Las ventas ficticias solo se crean cuando la tabla de ventas está vacía.

Para una base sin contenido ficticio, defina `DEMO_DATA=false`. Si activa la carga manualmente, también debe definir `DEMO_DANIEL_PASSWORD` y `DEMO_JUAN_PASSWORD` con valores de al menos ocho caracteres.

Después de descargar las dependencias y ejecutar una vez:

```powershell
.\scripts\iniciar.ps1 -SinInternet
```

Las operaciones principales necesitan PostgreSQL local en ejecución. Para detener únicamente el clúster del proyecto, cierre primero la aplicación:

```powershell
.\scripts\detener-postgres-local.ps1
```

## Usar una base PostgreSQL propia

Cree una base dedicada y un usuario propietario con permiso de crear sus tablas. Defina `DB_URL`, `DB_USER` y `DB_PASSWORD` en el entorno y ejecute `mvn javafx:run`. Ejemplo de URL: `jdbc:postgresql://localhost:5432/comidas_rapidas`. El script `iniciar.ps1` da prioridad a `.work/db-local.env` si existe.

El DDL está en [schema.sql](src/main/resources/schema.sql). Se utiliza `ddl-auto=validate`; no se modifican automáticamente estructuras existentes. Los scripts de instalación no reemplazan una estrategia de migraciones para futuras versiones.

## Guía paso a paso para poblar la base de datos

Existen dos métodos para ingresar información en el sistema:

### Método 1: Desde la interfaz de la aplicación (Recomendado)

Siga este orden para respetar la integridad referencial de los datos:

1. **Iniciar sesión:**
   - Ingrese con el usuario administrador: `pepito_admin` y contraseña `PepitoAdmin123*`.
2. **Crear Categorías (Menú lateral -> Categorías):**
   - Ingrese el nombre de la categoría (por ejemplo: `Hamburguesas`, `Bebidas`, `Acompañamientos`, `Combos`).
   - Haga clic en **Guardar categoría**.
   - Use el botón **Limpiar campos** para ingresar la siguiente categoría.
3. **Crear Productos (Menú lateral -> Productos):**
   - Complete los campos:
     - **Nombre:** Nombre del producto (ej: `Hamburguesa Especial Doble`).
     - **Descripción:** Ingredientes o detalle (ej: `Doble carne artesanal, queso cheddar y tocineta`).
     - **Precio:** Valor unitario en pesos sin puntos de miles (ej: `22000` o `22000,50`).
     - **Stock:** Unidades disponibles en inventario (ej: `50`).
     - **Categoría:** Seleccione la categoría creada en el paso anterior.
   - Haga clic en **Guardar producto**.
4. **Registrar Clientes (Menú lateral -> Clientes):**
   - Seleccione el **Tipo de documento** en el desplegable (`Cédula de ciudadanía` o `NIT`).
   - Ingrese:
     - **Documento:** Número de identificación (ej: `1098765432` o `900123456-1`).
     - **Nombre y Apellido:** Nombres del cliente o razón social.
     - **Teléfono y Correo:** Datos de contacto para la factura electrónica.
   - Haga clic en **Guardar cliente**.
5. **Crear Empleados / Cajeros (Menú lateral -> Empleados):**
   - Digite el nombre completo, el nombre de usuario (en minúsculas), la contraseña y seleccione el rol `CAJERO`.
   - Haga clic en **Crear empleado**.
6. **Registrar Ventas (Menú lateral -> Nueva venta):**
   - Seleccione el producto del catálogo y la cantidad a vender.
   - Haga clic en **Agregar producto** para sumar al carrito.
   - Si la venta es con **Factura Electrónica**, seleccione el cliente registrado en el combo. Si es consumidor final, use el botón **Venta POS sin cliente**.
   - Haga clic en **Confirmar venta en efectivo** y en la ventana emergente elija **Descargar comprobante en PDF**.

---

### Método 2: Inserción directa mediante SQL (psql o pgAdmin)

Si prefiere cargar datos directamente en PostgreSQL, ejecute las siguientes instrucciones en orden:

```sql
-- 1. Crear base de datos (si aún no existe)
CREATE DATABASE comidas_rapidas;

-- 2. Conectarse a la base de datos y ejecutar el esquema DDL de schema.sql
-- (Las tablas se crean automáticamente al iniciar la app si la base está vacía).

-- 3. Insertar Categorías
INSERT INTO categoria (nombre, activo) VALUES 
('Hamburguesas', true),
('Bebidas', true),
('Papas y Entradas', true),
('Combos', true);

-- 4. Insertar Productos vinculados a sus categorías
INSERT INTO producto (nombre, descripcion, precio, stock, activo, categoria_id) VALUES 
('Hamburguesa Clásica', 'Carne 150g, lechuga, tomate y queso', 16000.00, 40, true, 1),
('Hamburguesa Especial', 'Doble carne, tocineta, queso cheddar y salsa especial', 24000.00, 30, true, 1),
('Gaseosa 400ml', 'Bebida gaseosa en botella', 4500.00, 80, true, 2),
('Jugo Natural', 'Jugo en agua o leche sabores variados', 6000.00, 25, true, 2),
('Papas a la Francesa', 'Porción de 200g crocantes con sal marina', 7000.00, 50, true, 3);

-- 5. Insertar Clientes (para Facturación Electrónica)
INSERT INTO cliente (tipo_documento, numero_documento, nombre, apellido, telefono, correo, activo) VALUES 
('Cédula de ciudadanía', '1098765432', 'Carlos', 'Gómez', '3101234567', 'carlos.gomez@correo.com', true),
('NIT', '901234567-8', 'Inversiones', 'Alimentos SAS', '3209876543', 'contacto@alimentos.com', true);
```

## Uso básico

1. Configure el administrador y entre con sus credenciales.
2. Cree categorías y productos con precio y stock.
3. En **Empleados**, cree las cuentas de cajero que necesite.
4. En **Nueva venta**, seleccione productos y cantidades. El cliente puede quedar vacío.
5. Revise el total, confirme la venta en efectivo y guarde el comprobante si lo necesita.
6. En **Ventas realizadas**, elija una fecha para consultar su total y recuperar comprobantes.

Un cajero puede consultar productos/categorías, registrar y consultar clientes, vender y consultar ventas. La edición/desactivación de clientes, productos y categorías y la gestión de empleados requieren administrador, también en los servicios.

Para editar un cliente, producto o categoría, seleccione su fila y guarde los cambios. **Nuevo / limpiar** permite crear otro. La desactivación conserva las ventas asociadas. La versión no incluye reactivación ni anulación de ventas.

Si el precio cambia desde que se agregó al carrito, quite esa línea, actualice el catálogo y agréguela de nuevo. Si falla una operación de persistencia, consulte las ventas antes de repetir el cobro, porque un fallo de conexión puede impedir recibir la confirmación de una transacción ya guardada.

## Pruebas

```powershell
mvn test
```

Las pruebas unitarias utilizan JUnit y Mockito. Las pruebas de integración requieren una base **exclusiva** denominada `comidas_pruebas` y vacían sus seis tablas antes de cada escenario.

```powershell
$env:TEST_DB_PASSWORD = 'contraseña de su base de pruebas'
.\scripts\probar-integracion.ps1 -Url 'jdbc:postgresql://localhost:55432/comidas_pruebas' -Usuario comidas_test
```

Si existe `.work/postgres-test.env`, el script lee su `POSTGRES_PASSWORD`. Para ejecutar además el ensayo de desempeño y la prueba de renderizado JavaFX:

```powershell
$env:RUN_PERF_IT = 'true'
$env:RUN_FX_IT = 'true'
.\scripts\probar-integracion.ps1
```

El renderizado necesita una sesión gráfica. Produce siete PNG en `target/capturas`. El ensayo de desempeño produce `target/rendimiento.csv`. Sin las variables de habilitación, Maven omite las pruebas optativas de integración, renderizado y desempeño; una ejecución omitida no cuenta como evidencia aprobada.

## Documentación y entrega

- [Requisitos, reglas, actores y casos de uso](docs/01-analisis.md).
- [Arquitectura, clases y modelo entidad-relación](docs/02-diseno.md).
- [Plan de calidad, trazabilidad y criterios](docs/03-plan-calidad.md).
- [Informe de métricas y evaluación](docs/04-informe-metricas.md).
- [Guion de aceptación manual](docs/05-aceptacion-manual.md).
- [Diagramas](docs/diagramas/).
- [Exportaciones reales de SourceMonitor](docs/metricas/).
- **Archivo de Enterprise Architect:** [`diagramas/diagramadeprueba.qea`](diagramas/diagramadeprueba.qea) (y copia en [`docs/diagramas/diagramadeprueba.qea`](docs/diagramas/diagramadeprueba.qea)), que contiene los 3 Diagramas de Clases y los 5 Diagramas de Casos de Uso organizados por módulos funcionales.

Para generar otro checkpoint:

```powershell
.\scripts\medir-sourcemonitor.ps1 -Ejecutable 'C:\ruta\SourceMonitor.exe' -Checkpoint '03-revision' -Prefijo '03'
```

Mantenga lenguaje Java, mismo conjunto de fuentes y mismas opciones de complejidad. No incluya `target`, pruebas ni dependencias en el análisis de producción.

El ZIP de entrega contiene código, scripts, diagramas, documentación y evidencia. Excluye `.work`, credenciales, base local, `target`, herramientas descargadas y configuración del IDE.

Para regenerar el ZIP: `.\scripts\empaquetar.ps1`.

En este equipo, la base de integración creada para la verificación está en el contenedor `comidas-rapidas-pruebas`. Si está detenido, ejecute `docker start comidas-rapidas-pruebas` antes de las pruebas de integración. Docker se utilizó únicamente para aislar las pruebas; el arranque normal usa el PostgreSQL local indicado arriba.

## Límites documentados

Comprobante interno, sin DIAN ni facturación electrónica. Solo efectivo, sin cálculo de cambio ni múltiples medios. Sin ingredientes, recetas, compras, caja, domicilios o interfaces web. Al recuperar comprobantes se muestran nombres actuales del catálogo/cliente y precios históricos del detalle.

La prueba de renderizado no reemplaza la aceptación con un empleado real. Las limitaciones y tareas pendientes de evidencia se indican en el informe.

Referencias técnicas: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [OpenJFX](https://openjfx.io/openjfx-docs/maven) y [SourceMonitor](https://www.derpaul.net/SourceMonitor/).
