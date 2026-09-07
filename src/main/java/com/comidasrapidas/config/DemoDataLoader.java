package com.comidasrapidas.config;

import com.comidasrapidas.service.DemoDataService;
import com.comidasrapidas.service.DemoDataSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(DemoDataLoader.class);
    private final DemoDataService datos;
    private final String claveDaniel;
    private final String claveJuan;

    public DemoDataLoader(DemoDataService datos,
                          @Value("${app.demo-data.daniel-password}") String claveDaniel,
                          @Value("${app.demo-data.juan-password}") String claveJuan) {
        this.datos = datos;
        this.claveDaniel = claveDaniel;
        this.claveJuan = claveJuan;
    }

    @Override
    public void run(ApplicationArguments args) {
        DemoDataSummary resumen = datos.cargar(claveDaniel, claveJuan);
        LOG.warn("Datos ficticios disponibles: {} empleados, {} categorías, {} productos, {} clientes, {} ventas nuevas",
                resumen.empleados(), resumen.categorias(), resumen.productos(), resumen.clientes(),
                resumen.ventasCreadas());
    }
}
