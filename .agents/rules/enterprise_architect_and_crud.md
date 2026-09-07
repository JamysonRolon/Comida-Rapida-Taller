# Directrices de Modelado Enterprise Architect y Patrones CRUD

## 1. Reglas para Enterprise Architect y Archivos .qea (SQLite)
1. **Verificación Obligatoria de Conectores:**
   - Tras ejecutar cualquier generación o modificación de diagramas, verificar en la tabla `t_connector` que todos los conectores hayan sido insertados con su GUID y tipo correspondiente.
   - Asegurar que `t_diagramlinks` esté limpio o contenga únicamente los enlaces válidos del diagrama para evitar que EA oculte líneas.
2. **Cero Nodos Aislados en Arquitectura de Servicios:**
   - Ningún servicio de negocio debe figurar sin dependencias en el diagrama de clases de servicios.
   - `VentaService` debe modelar dependencias explícitas (`«use»`) hacia `ClienteService`, `ProductoService`, `ComprobanteService` y `ReporteExcelService`.
   - `ProductoService` debe conectarse a `CategoriaService`.
3. **Sincronización en Caliente de Archivos .qea:**
   - Nunca usar copias directas de archivos del sistema operativo cuando EA pueda tener el archivo abierto.
   - Utilizar siempre `sqlite3` con `src.backup(dst)` para volcar el modelo hacia `PROYECTO_DE_PROGRAMACION/diagramas/` y `docs/diagramas/`.
4. **Ortografía y Reemplazos Sanitizados:**
   - Las correcciones ortográficas deben usar coincidencias de palabra completa para evitar corromper cadenas como "Diagramas" o "Datos".
   - Los conectores de estereotipo `«include»` deben tener `Name = NULL` para que EA renderice limpiamente la etiqueta.

## 2. Reglas para Controladores JavaFX y CRUD Completo
1. **Convención de Controles CRUD:**
   - Botón de guardado dinámico: "Guardar nuevo [entidad]" (creación) vs "Actualizar [entidad]" (edición).
   - Botón "Eliminar [entidad]" habilitado exclusivamente con fila seleccionada y con confirmación modal previa (`Dialogos.confirmar`).
   - Botón "Limpiar campos" para restablecer formulario, dropdowns y botones a su estado inicial.
   - Notificación modal de éxito con `Dialogos.informar(...)` tras cada operación.
2. **Integridad de Datos en Eliminación:**
   - Evaluar referencias foráneas antes de borrar:
     - Sin ventas: Borrado físico (`repository.delete()`).
     - Con ventas: Desactivación lógica (`activo = false`) y notificación clara de preservación de histórico (`DatosInvalidosException`).
3. **Filtros Reactivos:**
   - Usar `FilteredList` con listeners en campos de búsqueda.
   - Actualizar en tiempo real el contador y sumatoria de registros visibles.
