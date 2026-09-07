# Plan de calidad y medición

Estado inicial: criterios definidos antes del código; resultados aún no medidos.

## Recorrido

Necesidad de calidad → representar la calidad → medir → interpretar → establecer/comprobar criterios de aceptación → evaluar → aceptar o mejorar. Los criterios se fijan antes de medir para evitar adaptarlos al resultado.

El vínculo de cada requisito es: característica → medida → indicador → criterio → evaluación. Los siguientes usos son la adaptación académica del proyecto, no una transcripción normativa ni una declaración de certificación.

## Referencias y aplicación prevista

| Referencia solicitada | Aplicación en el proyecto |
| --- | --- |
| ISO/IEC 25002:2024 | Relacionar modelos de calidad con medidas, requisitos y evaluación. |
| ISO/IEC 25010:2023 | Priorizar adecuación funcional, interacción, fiabilidad, seguridad, mantenibilidad y eficiencia de desempeño. |
| ISO/IEC 25019:2023 | Evaluar tareas con un empleado en el contexto del local; registrar ayuda necesaria, finalización y comprensión de mensajes. |
| ISO/IEC 25020:2019 | Organizar el marco de medición y sus fuentes de evidencia. |
| ISO/IEC 25023:2016 | Referencia para medidas del producto; las métricas de SourceMonitor no se presentan como equivalentes automáticos de medidas ISO. |
| ISO/IEC/IEEE 15939:2017 | Vincular cada medida a una necesidad de información y una decisión. |
| ISO/IEC 25030:2019 | Formular requisitos comprobables y condiciones de aceptación. |
| ISO/IEC 25040:2024 | Planificar evaluación, obtener evidencia, comparar criterios, mejorar y volver a evaluar. |

Fuentes oficiales verificadas para el alcance conceptual: [ISO/IEC 25002](https://www.iso.org/standard/78175.html), [ISO/IEC 25010](https://www.iso.org/standard/78176.html), [ISO/IEC 25019](https://www.iso.org/standard/78177.html). Las fichas públicas no sustituyen el texto íntegro de las normas. Las ediciones se mantienen conforme al encargo académico.

Compatibilidad, flexibilidad y safety (seguridad operacional) tienen menor prioridad en esta versión de un solo establecimiento: se documentan límites de entorno y riesgos de datos, sin añadir funciones artificiales.

## Matriz de trazabilidad

| Necesidad / requisito | Característica | Medida / obtención | Indicador / aceptación | Decisión |
| --- | --- | --- | --- | --- |
| RF04, RF05, RF06 | Adecuación funcional | Casos CRUD aprobados / ejecutados, JUnit y Mockito | 100 % obligatorios | Corregir cualquier fallo |
| RF07 a RF10 | Adecuación funcional | Casos monetarios y cliente opcional aprobados | 100 % | Corregir cálculos |
| RNF02 | Fiabilidad | Ventas parciales y stock negativo en integración PostgreSQL | 0 en los escenarios ejecutados | Corregir transacciones |
| RF01 a RF03 | Seguridad | Casos de acceso rechazado, permisos y hashes | 100 % de casos previstos | Corregir autorización |
| RNF04 | Interacción / calidad en uso | Tareas completadas sin ayuda en guion con empleado | Todas las tareas esenciales, mensajes comprendidos | Ajustar interfaz y repetir |
| RNF05 | Mantenibilidad | Complejidad, sentencias y profundidad, SourceMonitor | Objetivos de tabla inferior | Revisar métodos identificados |
| RNF06 | Eficiencia | Duración de 30 operaciones tras 5 de calentamiento; p95 por operación | <= 1 segundo, datos y equipo declarados | Revisar consultas |
| RNF08 | Operación local | Venta y consulta con red externa desconectada | Ambas completadas con PostgreSQL local | Revisar dependencias |

## Métricas de SourceMonitor

Ámbito primario: exclusivamente `src/main/java`, recursivo, lenguaje Java. Pruebas, dependencias y código generado se excluyen. Comparar checkpoints con la misma versión, archivos, opciones y definición de complejidad. Exportar TODOS los campos que produzca la herramienta, incluso los no enumerados aquí, a XML/CSV original. Registrar advertencias de sintaxis.

| Métrica | Resultado inicial | Significado y uso | Criterio del proyecto |
| --- | --- | --- | --- |
| Líneas de código | Pendiente | Tamaño; contextualizar con responsabilidades | Informativo |
| Sentencias | Pendiente | Volumen ejecutable | Informativo |
| Porcentaje de ramificación | Pendiente | Peso de decisiones; revisar concentración | Preferiblemente <= 30 % |
| Llamadas a métodos | Pendiente | Colaboración entre métodos | Informativo |
| Porcentaje de comentarios | Pendiente | Contexto documental, sin inflar comentarios | Informativo |
| Clases e interfaces | Pendiente | Tamaño estructural | Informativo |
| Métodos promedio por clase | Pendiente | Distribución de responsabilidades | <= 15 |
| Sentencias promedio por método | Pendiente | Tamaño de las operaciones | <= 20 |
| Complejidad máxima por método | Pendiente | Identificar caminos que requieren revisión | <= 10 |
| Complejidad promedio | Pendiente | Complejidad global | <= 4 |
| Profundidad máxima de bloques | Pendiente | Peor anidamiento | <= 4 |
| Profundidad promedio de bloques | Pendiente | Anidamiento global | <= 2 |

Los límites anteriores proceden del encargo. No son límites oficiales de ISO ni de SourceMonitor. Una métrica aprobada no demuestra por sí sola mantenibilidad, seguridad ni ausencia de defectos.

## Evaluación en uso pendiente

Contexto: empleado del establecimiento, computador local, teclado y ratón, base de datos de prueba, sin ayuda durante cada tarea. Registrar fecha, rol, equipo, tiempo y observaciones, sin publicar datos personales reales.

1. Iniciar sesión como cajero.
2. Consultar un producto y registrar un cliente.
3. Vender dos productos sin cliente registrado, comprobar total y comprobante.
4. Intentar una cantidad superior al stock y explicar el mensaje.
5. Consultar las ventas de hoy y recuperar un comprobante.
6. Como administrador, corregir un producto y desactivar un cliente.

Por tarea: completada sí/no, ayuda sí/no, errores, duración, mensaje comprendido sí/no. No rellenar resultados sin observación de una persona.

## Etapas y evidencia

1. Análisis: `01-analisis.md`.
2. Diseño: `02-diseno.md` y diagramas.
3. Base de datos: script DDL de seis tablas.
4. Proyecto Maven y arranque JavaFX/Spring.
5. Categorías, clientes, productos y empleados.
6. Venta transaccional y comprobante.
7. JUnit, Mockito e integración PostgreSQL.
8. Primer checkpoint SourceMonitor.
9. Refactorización motivada por los resultados y revisión.
10. Segundo checkpoint, comparación y documento final.

Si SourceMonitor no está disponible, se informa la limitación, se conserva la tabla pendiente y se entrega el procedimiento reproducible. Otras herramientas no se hacen pasar por SourceMonitor.
