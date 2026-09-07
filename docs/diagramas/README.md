# Diagramas de la implementación

Cada diagrama se entrega en PNG, SVG y fuente PlantUML editable.

- [Clases principales](clases.svg): entidades, carrito y servicios de venta/sesión.
- [Casos de uso](casos-uso.svg): administrador, cajero y funcionalidades reales.
- [Entidad-relación](entidad-relacion.svg): las seis tablas y sus relaciones.
- [Arquitectura](arquitectura.svg): interfaz, servicios, persistencia y base local.

Los diagramas se renderizaron con PlantUML 1.2025.4 y su motor Smetana. Para regenerarlos con el JAR de PlantUML:

```text
java -jar plantuml.jar -tsvg -charset UTF-8 "docs/diagramas/*.puml"
java -jar plantuml.jar -tpng -charset UTF-8 "docs/diagramas/*.puml"
```

No se necesita PlantUML para ejecutar la aplicación. Los diagramas Mermaid de `02-diseno.md` conservan la vista del diseño previo.
