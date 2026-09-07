# Guion de aceptación manual

Estado: pendiente de ejecución por un empleado. No registrar aprobaciones ni tiempos sin observar la tarea.

Usar datos ficticios y una base separada de cualquier operación real. Registrar fecha, evaluador, rol y equipo.

| Caso | Tarea | Resultado esperado | Completada | Ayuda | Tiempo | Observaciones |
| --- | --- | --- | --- | --- | --- | --- |
| AM01 | Configurar el primer administrador e iniciar sesión | Cuenta creada, menú de administrador | Pendiente | Pendiente | Pendiente | |
| AM02 | Crear cajero y entrar con su cuenta | No muestra gestión de empleados ni edición del catálogo | Pendiente | Pendiente | Pendiente | |
| AM03 | Registrar cliente con documento nuevo | Aparece en consulta | Pendiente | Pendiente | Pendiente | |
| AM04 | Repetir documento y probar correo incorrecto | Mensajes comprensibles y sin registro duplicado | Pendiente | Pendiente | Pendiente | |
| AM05 | Editar y desactivar cliente como administrador | Datos actualizados; cliente inactivo conservado | Pendiente | Pendiente | Pendiente | |
| AM06 | Crear categoría y producto con stock 5 | Producto visible con su precio y categoría | Pendiente | Pendiente | Pendiente | |
| AM07 | Introducir precio cero y stock negativo | Validación antes de guardar | Pendiente | Pendiente | Pendiente | |
| AM08 | Vender dos productos sin cliente registrado | Total correcto, comprobante y descuento exacto | Pendiente | Pendiente | Pendiente | |
| AM09 | Solicitar más unidades que las disponibles | Venta rechazada; usuario comprende el motivo | Pendiente | Pendiente | Pendiente | |
| AM10 | Guardar comprobante y abrirlo como texto | Número, fecha, cantidades, importes y total legibles | Pendiente | Pendiente | Pendiente | |
| AM11 | Consultar ventas de hoy y un día sin ventas | Total correcto; cero y lista vacía en día sin ventas | Pendiente | Pendiente | Pendiente | |
| AM12 | Cerrar y volver a abrir la aplicación | Registros y ventas conservados | Pendiente | Pendiente | Pendiente | |
| AM13 | Cambiar contraseña de cajero y desactivarlo | Clave anterior rechazada; cuenta inactiva rechazada | Pendiente | Pendiente | Pendiente | |
| AM14 | Vender y consultar con red externa desconectada y PostgreSQL local activo | Ambas tareas completas | Pendiente | Pendiente | Pendiente | |
| AM15 | Cerrar sesión con carrito | Confirmación y descarte al aceptar | Pendiente | Pendiente | Pendiente | |

Criterio de calidad en uso del proyecto: todas las tareas esenciales completadas sin ayuda y mensajes comprendidos. Si una falla, describir el problema, ajustar y repetir esa tarea. No atribuir esta lista ni sus umbrales como texto normativo de ISO.

Capturas SourceMonitor pendientes: comparación de checkpoints, resumen final y método de mayor complejidad. El procedimiento y la causa del pendiente están en [el informe](04-informe-metricas.md#10-capturas-y-evidencia-visual).
