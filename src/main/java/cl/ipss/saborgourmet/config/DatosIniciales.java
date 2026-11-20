package cl.ipss.saborgourmet.config;

import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.repository.MesaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Mesas de ejemplo para probar la aplicación recién instalada. */
@Configuration
public class DatosIniciales {
    @Bean
    CommandLineRunner mesasDeEjemplo(MesaRepository repositorio) {
        return args -> {
            if (repositorio.count() == 0) {
                int[] capacidades = {2, 2, 4, 4, 6, 8};
                for (int i = 0; i < capacidades.length; i++) {
                    Mesa mesa = new Mesa(); mesa.setNumero(i + 1);
                    mesa.setCapacidad(capacidades[i]); repositorio.save(mesa);
                }
            }
        };
    }
}
