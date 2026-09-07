package com.comidasrapidas.config;

import com.comidasrapidas.service.DemoDataService;
import com.comidasrapidas.service.DemoDataSummary;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import static org.mockito.Mockito.*;

class DemoDataLoaderTest {
    @Test
    void delegaLaCargaAlServicio() {
        DemoDataService servicio = mock(DemoDataService.class);
        when(servicio.cargar("ClaveDanielPrueba", "ClaveJuanPrueba"))
                .thenReturn(new DemoDataSummary(2, 5, 12, 5, 3));

        new DemoDataLoader(servicio, "ClaveDanielPrueba", "ClaveJuanPrueba")
                .run(new DefaultApplicationArguments());

        verify(servicio).cargar("ClaveDanielPrueba", "ClaveJuanPrueba");
    }
}
