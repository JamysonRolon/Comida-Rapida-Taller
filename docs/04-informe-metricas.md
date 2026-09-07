# Informe de métricas y evaluación de calidad

Fecha de ejecución: 6 de septiembre de 2026.

Estado: implementación y verificación automatizada realizadas. Pendientes: capturas de la interfaz de SourceMonitor, aceptación con un empleado y prueba manual con la red externa desconectada. Esos pendientes no se presentan como resultados aprobados.

## 1. Introducción

Este proyecto académico evalúa una aplicación pequeña de escritorio mediante pruebas, análisis estático y medición local de tiempos. La necesidad de información es determinar si los casos de negocio implementados funcionan en los escenarios ensayados y si la estructura del código satisface los objetivos internos de mantenibilidad.

El análisis y el diseño se documentaron antes de implementar. Los requisitos y la matriz de trazabilidad se encuentran en [01-analisis.md](01-analisis.md) y [03-plan-calidad.md](03-plan-calidad.md).

## 2. Descripción de la aplicación

Gestión de un establecimiento de comidas rápidas con dos roles: administrador y cajero. Incluye cuentas de empleados, CRUD mediante desactivación de clientes/productos, categorías, stock por producto, ventas en efectivo, consulta diaria y comprobantes internos. El cliente puede no registrarse. La venta guarda precios unitarios históricos.

La aplicación no contiene servidor HTTP, REST, web, DIAN, medios de pago electrónicos ni inventario de ingredientes.

## 3. Tecnologías y entorno medido

| Elemento | Versión o condición verificada |
| --- | --- |
| Java | Oracle JDK 24.0.2; compilación con release 21 |
| JavaFX | 21.0.8 |
| Maven | 3.9.11 |
| Spring Boot | 3.5.16 |
| Persistencia | Spring Data JPA e Hibernate gestionados por Spring Boot |
| Base de integración/desempeño | PostgreSQL 16, imagen local postgres:16-alpine |
| Base local de ejecución | PostgreSQL 18.4, puerto local 55434 |
| Pruebas | JUnit y Mockito, dependencias gestionadas por Spring Boot |
| SourceMonitor | Ejecutable 3.5.16.62 |
| Sistema | Windows x64 |
| Procesador | Intel Core i7-6500U, 2 núcleos, 4 procesadores lógicos |
| Memoria física reportada | 8.491.270.144 bytes |

Las versiones concretas de dependencias transitivas se pueden consultar con `mvn dependency:tree`. Referencias de configuración: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html) y [OpenJFX](https://openjfx.io/openjfx-docs/maven).

## 4. SourceMonitor y procedimiento

SourceMonitor analiza código Java y exporta métricas de archivos y métodos; permite conservar checkpoints. Su mantenedor documenta esas funciones en [SourceMonitor](https://www.derpaul.net/SourceMonitor/) y en el [repositorio informativo](https://github.com/SourceMonitor/SM-Info).

Se descargó el ejecutable desde el enlace del mantenedor y se ejecutó mediante `/C` con un archivo XML. El instalador no completó la instalación en la ruta solicitada; el ejecutable de la distribución ZIP sí ejecutó el análisis.

Configuración común:

- Directorio: `src/main/java`, inclusión recursiva de `*.java`.
- Lenguaje Java; lectura UTF-8.
- 46 archivos en los dos primeros checkpoints y 49 archivos en el tercero y el cuarto.
- Complejidad modificada desactivada.
- Líneas en blanco incluidas; sin omitir cabeceras/pies.
- Sin `src/test/java`, dependencias, herramientas ni archivos generados.
- Exportación de resumen y detalle con métricas de métodos.

El archivo binario de SourceMonitor se conserva solo en el entorno local porque incorpora la ruta absoluta del equipo. El script `medir-sourcemonitor.ps1` permite regenerarlo. Las exportaciones publicadas conservan los valores y separadores decimales entregados por la herramienta; únicamente se sustituyó el directorio absoluto por `src/main/java` para no publicar la ruta personal.

## 5. Evidencia original y métricas utilizadas

| Evidencia | Contenido |
| --- | --- |
| [01-resumen.xml](metricas/01-resumen.xml) | Métricas globales del primer checkpoint |
| [01-inicial.xml](metricas/01-inicial.xml) | Métricas iniciales de cada archivo y método |
| [01-fuentes.zip](metricas/01-fuentes.zip) | Copia de las fuentes analizadas antes de los ajustes |
| [02-resumen.xml](metricas/02-resumen.xml) | Métricas globales del segundo checkpoint |
| [02-detalle.xml](metricas/02-detalle.xml) | Métricas finales de cada archivo y método |
| [03-resumen.xml](metricas/03-resumen.xml) | Métricas globales tras añadir los datos ficticios |
| [03-detalle.xml](metricas/03-detalle.xml) | Métricas tras añadir datos ficticios |
| [04-resumen.xml](metricas/04-resumen.xml) | Métricas globales de la revisión previa a publicación |
| [04-detalle.xml](metricas/04-detalle.xml) | Métricas actuales de cada archivo y método |
| [comparacion.csv](metricas/comparacion.csv) | Tabla derivada de los cuatro XML de resumen, sin recalcular métricas |
| [fuentes-finales-sha256.csv](metricas/fuentes-finales-sha256.csv) | Huellas de los archivos de producción de la entrega |

Los XML contienen los 15 campos de métricas de Java producidos por esta ejecución, además de las distribuciones de profundidad y las métricas de métodos. Los nombres y líneas del método más complejo/bloque más profundo se conservan en el detalle por archivo; los campos de línea globales vacíos se registran como no aplicables, sin inventar una línea de proyecto.

## 6. Resultados y criterios

Los objetivos son del proyecto. [ISO/IEC 25023:2016](https://www.iso.org/standard/35747.html) deja la determinación de rangos de aceptación al contexto del producto y sus necesidades.

| Métrica | Inicial | Ajustes UI | Actual | Criterio | Interpretación |
| --- | ---: | ---: | ---: | --- | --- |
| Líneas | 2.421 | 2.435 | 2.631 | Informativo | El aumento actual corresponde al cargador ficticio y su configuración segura |
| Sentencias | 1.663 | 1.674 | 1.812 | Informativo | Volumen de implementación |
| Sentencias de ramificación | 2,3 % | 2,3 % | 2,2 % | Preferiblemente <= 30 % | Dentro del objetivo |
| Llamadas a métodos | 674 | 680 | 763 | Informativo | Colaboración y composición, no complejidad por sí solas |
| Líneas con comentarios | 0,0 % | 0,0 % | 0,0 % | Informativo | Valor redondeado por la herramienta; no se añadieron comentarios para inflarlo |
| Clases e interfaces | 46 | 46 | 48 | Informativo | Se añadieron el servicio y el resumen de carga; SourceMonitor contó 49 archivos |
| Métodos por clase | 6,00 | 6,04 | 6,12 | <= 15 | Dentro del objetivo promedio |
| Sentencias promedio por método | 2,94 | 2,95 | 3,03 | <= 20 | Dentro del objetivo promedio |
| Complejidad máxima | 7 | 7 | 7 | <= 10 | Dentro del objetivo; método de clasificación de errores |
| Complejidad promedio | 1,24 | 1,25 | 1,24 | <= 4 | Dentro del objetivo |
| Profundidad máxima | 4 | 4 | 4 | <= 4 | En el límite del objetivo; revisar futuras ampliaciones |
| Profundidad promedio | 1,33 | 1,34 | 1,33 | <= 2 | Dentro del objetivo |

El método de mayor complejidad reportado en los cuatro checkpoints es `Dialogos.mensaje()`. En el detalle actual no hay métodos con complejidad superior a 10, más de 20 sentencias ni profundidad superior a 4. El mayor número de sentencias por método reportado es 17, en `ClienteController.vista()`.

## 7. Relación con calidad y normas seleccionadas

| Referencia | Aplicación académica y evidencia |
| --- | --- |
| [ISO/IEC 25002:2024](https://www.iso.org/standard/78175.html) | Relación entre modelo, requisitos, medida y evaluación, explicitada en el plan |
| [ISO/IEC 25010:2023](https://www.iso.org/standard/78176.html) | Selección de características del producto; pruebas funcionales, seguridad, fiabilidad, métricas y tiempos |
| [ISO/IEC 25019:2023](https://www.iso.org/standard/78177.html) | Contexto y tareas de calidad en uso; guion preparado, observación con empleado pendiente |
| [ISO/IEC 25020:2019](https://www.iso.org/standard/72117.html) | Definición de fuentes, método, resultados y aplicación de las mediciones |
| [ISO/IEC 25023:2016](https://www.iso.org/standard/35747.html) | Referencia para medir calidad del producto, sin equiparar automáticamente métricas estructurales con cumplimiento de una norma |
| [ISO/IEC/IEEE 15939:2017](https://www.iso.org/standard/71197.html) | Necesidad de información: dificultad de mantenimiento; indicadores: complejidad y profundidad; decisión: aceptar estructura y revisar puntos señalados |
| [ISO/IEC 25030:2019](https://www.iso.org/standard/72116.html) | Requisitos comprobables, condiciones y objetivos establecidos antes de medir |
| [ISO/IEC 25040:2024](https://www.iso.org/standard/83467.html) | Evaluación planificada, obtención de evidencia, ajustes y comparación de checkpoints |

Se consultaron las fichas públicas oficiales. No se realizó una auditoría de todos los requisitos del texto completo de las normas y no se declara certificación ISO. Compatibilidad, flexibilidad y seguridad operacional tienen menor prioridad para el alcance local definido; no motivaron funcionalidades artificiales.

## 8. Pruebas y trazabilidad ejecutada

Última ejecución completa: `mvn verify`, con las variables de integración, desempeño y renderizado habilitadas. Resultado de Maven: `BUILD SUCCESS`.

| Grupo | Pruebas | Fallos / errores / omitidas | Evidencia |
| --- | ---: | --- | --- |
| Clientes | 10 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.ClienteServiceTest.txt) |
| Productos | 10 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.ProductoServiceTest.txt) |
| Ventas | 12 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.VentaServiceTest.txt) |
| Carrito | 5 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.CarritoServiceTest.txt) |
| Categorías | 5 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.CategoriaServiceTest.txt) |
| Seguridad | 10 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.SeguridadServiceTest.txt) |
| Comprobante | 1 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.ComprobanteServiceTest.txt) |
| Carga ficticia | 2 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.DemoDataServiceTest.txt) |
| Activación de carga ficticia | 1 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.config.DemoDataLoaderTest.txt) |
| Integración PostgreSQL | 9 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.PostgresIT.txt) |
| Renderizado JavaFX | 1 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.VisualIT.txt) |
| Desempeño | 1 | 0 / 0 / 0 | [Reporte](evidencia/com.comidasrapidas.service.DesempenoIT.txt) |
| **Total** | **67** | **0 / 0 / 0** | 56 unitarias y 11 de integración/renderizado/desempeño |

Los casos PostgreSQL comprueban persistencia completa, reversión cuando falla una línea posterior, dos ventas simultáneas de una sola unidad, restricciones SQL, conservación del historial, precios históricos, límites diarios de Bogotá, hashes/permisos y configuración inicial única. Las pruebas de renderizado producen imágenes de siete pantallas y comprueban que termina su carga; no prueban todas las interacciones manuales.

## 9. Eficiencia de desempeño

Base inicial: 1.000 productos y 1.000 clientes ficticios. Se realizaron cinco operaciones de calentamiento y treinta mediciones por operación. Cada medición incluye el servicio y PostgreSQL local en contenedor; no incluye tiempo humano ni pintado de la pantalla. Las inserciones aumentan el número de filas durante el ensayo. p95: posición 29 de 30 duraciones ordenadas (nearest rank).

| Operación | p95 (ms) | Máximo (ms) | Objetivo p95 |
| --- | ---: | ---: | --- |
| Consultar 1.000 productos | 59,565 | 69,591 | <= 1.000 ms |
| Consultar 1.000 clientes | 27,907 | 30,849 | <= 1.000 ms |
| Registrar cliente | 29,848 | 34,975 | <= 1.000 ms |
| Actualizar cliente | 13,594 | 13,855 | <= 1.000 ms |
| Desactivar cliente | 12,103 | 14,343 | <= 1.000 ms |
| Registrar producto | 14,456 | 15,019 | <= 1.000 ms |
| Actualizar producto | 19,277 | 22,231 | <= 1.000 ms |
| Desactivar producto | 32,416 | 49,451 | <= 1.000 ms |

Fuente: [rendimiento.csv](evidencia/rendimiento.csv), producido por `DesempenoIT`. Los ocho p95 cumplen el objetivo en este ensayo. No se extrapola el resultado a otras máquinas, mayores volúmenes ni condiciones de red diferentes.

## 10. Capturas y evidencia visual

Las capturas siguientes corresponden al renderizado real de JavaFX con datos ficticios: [venta](capturas/01-venta.png), [productos](capturas/02-productos.png), [clientes](capturas/03-clientes.png), [categorías](capturas/04-categorias.png), [empleados](capturas/05-empleados.png), [historial](capturas/06-historial.png) y [acceso](capturas/07-login.png).

**Capturas de SourceMonitor pendientes.** El ejecutor de control de Windows devolvió, en los tres intentos incluyendo reinicio de sesión de herramientas: `Computer Use native pipe is unavailable: failed to connect native pipe ... (os error 2)`. No se obtuvieron capturas de su GUI y no se sustituyen por imágenes inventadas. Se incluyen los XML generados por SourceMonitor con la ruta local anonimizada.

Para completar esta evidencia manualmente: abrir `comidas-rapidas.smproj`, capturar la comparación de ambos checkpoints, el resumen final y el detalle de `Dialogos.java`; guardar las imágenes en `docs/capturas/sourcemonitor/` con nombres y versión visibles.

## 11. Problemas encontrados

1. El script PowerShell de pruebas trató advertencias nativas de Maven como errores terminantes. Se ajustó el manejo de salida y se repitió `verify` con resultado satisfactorio.
2. La instalación de SourceMonitor no completó la ruta solicitada. El análisis se ejecutó con el binario distribuido en ZIP por el mantenedor.
3. El control gráfico de Windows estuvo indisponible, por lo que las capturas de SourceMonitor siguen pendientes.
4. La revisión visual mostró nombres de producto truncados y acceso alineado arriba. Se amplió la columna de producto, se añadieron ayudas con el texto completo y se centró el formulario de acceso.
5. La revisión de actualización de clientes detectó que se restablecía la lista ignorando el filtro visible. Se reutiliza una lista filtrada para conservar el criterio.
6. El arranque local de PostgreSQL mediante `Start-Process -Wait` permanecía esperando al árbol de procesos del servidor. Se cambió a esperar únicamente la salida de `pg_ctl` mediante `WaitForExit` con límite de tiempo; se repitió el arranque.

## 12. Refactorizaciones y comparación

No se detectaron incumplimientos de los umbrales estructurales en el primer checkpoint. No se realizó una refactorización artificial para reducir cifras.

Se centralizó la presentación del texto completo de las celdas en `Controles.celdaConAyuda()`, se conservó el filtro de clientes y se ajustó el acceso. Después se añadió una carga ficticia opcional e idempotente. Tras estos cambios, las 67 pruebas pasaron. La revisión previa a publicación retiró credenciales del código y creó un cuarto checkpoint con las mismas opciones sobre los 49 archivos actuales.

Las pequeñas subidas de líneas, sentencias, llamadas y complejidad promedio corresponden a estas mejoras de interacción. La complejidad máxima se mantuvo en 7. La profundidad máxima permanece en el límite 4 del proyecto y debe seguir observándose si se amplían los controladores.

## 13. Decisión de evaluación

| Aspecto | Decisión sustentada |
| --- | --- |
| Adecuación funcional automatizada | Aprobada para los escenarios ejecutados |
| Fiabilidad transaccional y stock | Aprobada para los escenarios de rollback y concurrencia ejecutados |
| Seguridad básica | Aprobada para autenticación, hashes y permisos ensayados |
| Mantenibilidad estructural | Objetivos SourceMonitor alcanzados en los cuatro checkpoints |
| Eficiencia de servicios | Objetivo alcanzado en las ocho operaciones medidas |
| Renderizado | Siete pantallas generadas y revisadas |
| Calidad en uso | Pendiente de prueba con empleado |
| Capturas GUI de SourceMonitor | Pendientes por indisponibilidad del control de ventanas |
| Arranque Maven sin conexión | Verificado mediante el script con -SinInternet, que usa mvn -o javafx:run |
| Operación con red externa desconectada | Pendiente de prueba manual; la base de ejecución es local |

## 14. Conclusiones

La evidencia obtenida muestra una implementación que supera las pruebas automatizadas ejecutadas y alcanza los objetivos estructurales y de tiempo definidos para el ensayo local. Los resultados no equivalen a ausencia de defectos ni a cumplimiento integral de ISO.

La aceptación final del proyecto académico requiere adjuntar las capturas GUI de SourceMonitor y completar el guion con un empleado, incluida la comprobación manual sin red externa. El estado de esos puntos permanece explícitamente pendiente.
