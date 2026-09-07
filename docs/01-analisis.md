# Análisis y requisitos

Estado: especificación previa a la implementación. Fuente: solicitud adjunta del usuario y aclaración sobre administración de empleados.

## Alcance

Aplicación académica de escritorio para un establecimiento de comidas rápidas. JavaFX presenta la interfaz; Spring Boot configura servicios y persistencia; PostgreSQL almacena los datos. Las operaciones de negocio funcionan con una base de datos local sin Internet. La descarga inicial de dependencias requiere conectividad.

Se mantienen todas las exclusiones de la solicitud: sin web, REST, microservicios, DIAN, pagos electrónicos, domicilios, mesas, caja, proveedores, compras, recetas, ingredientes, promociones, fidelización ni múltiples sedes.

## Actores y permisos

| Operación | Administrador | Cajero |
| --- | --- | --- |
| Iniciar y cerrar sesión | Sí | Sí |
| Crear empleados, cambiar su contraseña y desactivarlos | Sí | No |
| Crear, consultar, actualizar y desactivar categorías | Sí | Solo consultar |
| Crear, consultar, actualizar y desactivar productos | Sí | Solo consultar |
| Crear y consultar clientes | Sí | Sí |
| Actualizar y desactivar clientes | Sí | No |
| Registrar y consultar ventas, total diario y comprobantes | Sí | Sí |

El cliente es una entidad del negocio, no un actor autenticado. La configuración inicial crea un administrador cuando no existen usuarios. No se distribuyen contraseñas predefinidas.

## Requisitos funcionales definitivos

| ID | Requisito y criterio funcional |
| --- | --- |
| RF01 | Autenticar únicamente usuarios activos; rechazar credenciales incorrectas con un mensaje comprensible. |
| RF02 | Aplicar permisos tanto en los controles de la interfaz como en los servicios. Cerrar sesión descarta el carrito. |
| RF03 | Configurar el administrador inicial; permitirle crear empleados, cambiar contraseñas y desactivar otros empleados. |
| RF04 | Registrar, listar, consultar por documento, actualizar y desactivar clientes conservando sus ventas. |
| RF05 | Crear, listar, actualizar y desactivar categorías con nombre único. |
| RF06 | Crear, listar, consultar, actualizar y desactivar productos; registrar precio, categoría y stock. |
| RF07 | Formar un carrito con uno o varios productos y cantidades enteras positivas; permitir retirar líneas. |
| RF08 | Mostrar precio unitario, subtotal y total calculados con BigDecimal. |
| RF09 | Confirmar una venta en efectivo con cliente opcional y usuario de la sesión. |
| RF10 | Almacenar venta y detalles y descontar stock en una transacción; rechazar cantidades superiores al stock disponible. |
| RF11 | Consultar ventas por fecha y sumar el total vendido del día en America/Bogota. |
| RF12 | Ver y guardar un comprobante interno de texto con número, fecha, empleado, cliente opcional, productos, cantidades, precios, subtotales y total. |
| RF13 | Solicitar confirmación al registrar ventas, desactivar registros y cambiar contraseñas. |

## Reglas de negocio

1. Documento único por cliente, incluso si está inactivo. Tipo y número de documento y nombre obligatorios. Apellido, teléfono y correo admiten vacío; el correo suministrado se valida.
2. Los nombres de categorías se comparan sin distinción entre mayúsculas y minúsculas y sin espacios exteriores. La desactivación de una categoría con productos activos se rechaza hasta desactivarlos o trasladarlos.
3. Producto con nombre obligatorio, precio positivo con hasta dos decimales, stock entero no negativo y una categoría activa al crearlo o modificarlo.
4. Productos y clientes se desactivan, no se borran físicamente. Las referencias históricas se conservan.
5. Disponibilidad de venta: producto activo con stock suficiente; cantidades enteras mayores que cero. Si el mismo producto se agrega dos veces, se acumula su cantidad.
6. Subtotal = cantidad × precio unitario; total = suma de subtotales. Sin impuestos, descuentos ni otros métodos de pago añadidos al alcance.
7. El precio de la venta se toma del catálogo al confirmar; si cambió desde la cotización, se solicita actualizar el carrito antes de confirmar de nuevo. El precio unitario del detalle permanece almacenado.
8. Confirmación atómica: todas las líneas se aceptan o no se registra ninguna parte de la venta. Bloqueo de productos en orden de ID para gestionar compras concurrentes.
9. Una venta puede no tener cliente; si lo tiene, debe estar activo al confirmar. El empleado se obtiene de la sesión, no de un campo editable.
10. Ventas confirmadas no se editan ni se anulan en esta versión. Los cambios posteriores del catálogo no cambian importes históricos.
11. Las contraseñas se almacenan mediante hash con sal. Nombre de usuario único, normalizado a minúsculas. Contraseña de al menos 10 caracteres, máximo 72 bytes UTF-8 para BCrypt.
12. El administrador no puede desactivar su propia cuenta. En la configuración inicial debe crear un administrador; los nuevos empleados pueden tener cualquiera de los dos roles.

Las reglas de precisión, normalización, categoría inactiva y protección de la cuenta propia son decisiones de diseño de esta implementación, no requisitos atribuidos a ISO.

## Casos de uso

| ID | Actor | Flujo principal | Alternativas |
| --- | --- | --- | --- |
| CU01 | Empleado | Introducir credenciales, validar cuenta, abrir menú según rol | Credenciales inválidas o usuario inactivo: permanecer en acceso |
| CU02 | Administrador | Registrar empleado o seleccionar uno y cambiar contraseña/desactivar | Username duplicado, contraseña inválida, desactivación propia |
| CU03 | Ambos; edición solo administrador | Consultar clientes, registrar datos, validar y guardar | Documento duplicado, correo inválido, registro inexistente |
| CU04 | Administrador | Crear/editar categoría, confirmar desactivación | Nombre duplicado, categoría con productos activos |
| CU05 | Administrador | Crear/editar producto, asignar categoría y stock, guardar | Precio inválido, stock negativo, categoría inactiva |
| CU06 | Ambos | Elegir producto, cantidad y cliente opcional; revisar total; confirmar; ver comprobante | Carrito vacío, falta de stock, precio cambiado, fallo de persistencia: no guardar venta parcial |
| CU07 | Ambos | Elegir fecha, consultar ventas y total, seleccionar venta y abrir comprobante | Fecha sin ventas: lista vacía y total cero |
| CU08 | Ambos | Cerrar sesión | Carrito local descartado |

## Requisitos no funcionales y aceptación

| ID | Característica priorizada | Requisito verificable | Evidencia prevista |
| --- | --- | --- | --- |
| RNF01 | Adecuación funcional | Todos los casos obligatorios se completan; todas las pruebas automatizadas pasan | JUnit, Mockito e integración PostgreSQL |
| RNF02 | Fiabilidad | Venta y descuento atómicos; stock almacenado >= 0 incluso con dos ventas simultáneas | Pruebas de rollback, concurrencia y restricciones SQL |
| RNF03 | Seguridad | Sin acceso a servicios protegidos sin sesión; permisos por rol; hashes de contraseña | Pruebas de autenticación y autorización |
| RNF04 | Capacidad de interacción | Etiquetas en español, mensajes sin trazas técnicas y confirmaciones; venta sin ayuda en prueba con usuario | Guion de aceptación y observación pendiente |
| RNF05 | Mantenibilidad | Separar presentación, negocio y persistencia; alcanzar objetivos SourceMonitor de la matriz de calidad | Checkpoints y revisión de código |
| RNF06 | Eficiencia de desempeño | Objetivo del proyecto: p95 <= 1 segundo para consultas y CRUD con 1.000 productos y 1.000 clientes, tras calentamiento, en el equipo de evaluación | Medición local repetible, hardware y tamaño registrados |
| RNF07 | Interacción y fiabilidad | Consultas y escrituras fuera del hilo gráfico; botón de acción bloqueado durante la operación | Prueba visual y revisión del ejecutor de tareas |
| RNF08 | Operación local | Venta y consultas disponibles sin Internet con PostgreSQL local en ejecución | Prueba manual sin red externa |
| RNF09 | Entrega | Código, diagramas, pruebas y documento de métricas en ZIP <= 15 MB sin target, IDE ni datos/credenciales locales | Revisión del ZIP |

No se declara cumplimiento integral ni certificación ISO. Los objetivos cuantitativos pertenecen al proyecto.
