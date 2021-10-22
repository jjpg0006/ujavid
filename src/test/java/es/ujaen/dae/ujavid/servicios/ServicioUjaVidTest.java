/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.ujaen.dae.ujavid.servicios;

import es.ujaen.dae.ujavid.entidades.ContactoCercano;
import es.ujaen.dae.ujavid.entidades.Rastreador;
import es.ujaen.dae.ujavid.entidades.Usuario;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author Venza
 */
@SpringBootTest(classes = es.ujaen.dae.ujavid.app.UjaVidApp.class)
public class ServicioUjaVidTest {

    @Autowired
    ServicioUjaVid servicioUjaVid;

    @Test
    public void testAccesoServicioUjaCoin() {
        Assertions.assertThat(servicioUjaVid).isNotNull();
    }

    /**
     * Comprueba que no podemos dar de alta a un usuario en nuestro servicio
     * usando un email incorrecto
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testAltaUsuarioInvalido() {
        // Cliente con e-mail incorrecto!!!
        Usuario usuario = new Usuario(
                "4524",
                "nuevaclave");

        Assertions.assertThatThrownBy(() -> {
            servicioUjaVid.altaUsuario(usuario);
        })
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)

    /**
     * Comprueba que no podemos dar de alta a un rastreador en nuestro servicio
     * usando un email incorrecto
     */
    public void testAltaRastreadorInvalido() {
        // Rastreador con e-mail incorrecto!!!
        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Antonio",
                "Venzala",
                "Campaña",
                "6603760",
                "contraseña1");

        Assertions.assertThatThrownBy(() -> {
            servicioUjaVid.altaRastreador(rastreador);
        })
                .isInstanceOf(ConstraintViolationException.class);
    }

    /**
     * Comprueba que podemos dar de alta a un usuario en nuestro servicio usando
     * credenciales correctas
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testAltaYLoginUsuario() {
        Usuario usuario = new Usuario(
                "660376093",
                "nuevaclave");

        servicioUjaVid.altaUsuario(usuario);
        Optional<Usuario> usuarioLogin = servicioUjaVid.loginUsuario(usuario.getNumTelefono(), "nuevaclave");

        Assertions.assertThat(usuarioLogin.isPresent()).isTrue();
        Assertions.assertThat(usuario.getF_alta()).isEqualTo(LocalDate.now());
        Assertions.assertThat(usuarioLogin.get()).isEqualTo(usuario);
    }

    /**
     * Comprueba que podemos dar de alta a un rastreador en nuestro servicio
     * usando credenciales correctas
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testAltaYLoginRastreador() {
        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Antonio",
                "Venzala",
                "Campaña",
                "660376093",
                "contraseña1");

        servicioUjaVid.altaRastreador(rastreador);
        UUID rastreadorLogin = servicioUjaVid.loginRastreador(rastreador.getDni(), "contraseña1");

        Assertions.assertThat(rastreadorLogin).isNotNull();
        Assertions.assertThat(rastreadorLogin).isEqualTo(rastreador.getUuid());
    }

    /**
     * Valida que podemos notificar como positivo a un usuario registrado
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testNotificarPos() {

        String contrasena = "contraseña1";

        Usuario usuario = new Usuario(
                "660376093",
                "nuevaclave");
        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Antonio",
                "Venzala",
                "Campaña",
                "660376093",
                contrasena);
        servicioUjaVid.altaUsuario(usuario);
        servicioUjaVid.altaRastreador(rastreador);

        servicioUjaVid.notificarPos(usuario.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);
        Assertions.assertThat(usuario.isPositivo()).isTrue();
        Assertions.assertThat(rastreador.getNUM_TOTAL_NOTIFICADOS()).isEqualTo(1);

    }

    /**
     * Valida que podemos añadir a los usuarios un contacto con otra persona
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testContactoCercano() {
        Usuario usuario1 = new Usuario(
                "656764549",
                "nuevaclave");

        Usuario usuario2 = new Usuario(
                "699699699",
                "nuevaclave");
        Usuario usuario3 = new Usuario(
                "699699692",
                "nuevaclave3");

        servicioUjaVid.altaUsuario(usuario1);
        servicioUjaVid.altaUsuario(usuario2);
        servicioUjaVid.altaUsuario(usuario3);
        //Si creamos 2 contactos iguales pero con horas diferentes, se sobreescriben
        ContactoCercano contacto0 = new ContactoCercano(LocalDateTime.now(),
                usuario2, 4, 2);
        ContactoCercano contacto1 = new ContactoCercano(LocalDateTime.now(),
                usuario2, 4, 2);
        ContactoCercano contacto2 = new ContactoCercano(LocalDateTime.now(),
                usuario3, 4, 2);

        List<ContactoCercano> contactos = new ArrayList<>();
        contactos.add(contacto0);
        contactos.add(contacto1);
        contactos.add(contacto2);

        servicioUjaVid.addContactoCercano(contactos, usuario1.getUuid());
        Assertions.assertThat(usuario1.getListadoContactos().size()).isEqualTo(2);
    }

    /**
     * Valida nuestra función metaheurística, donde se comprueba que se obtiene
     * una lista ordenada para una determinada persona de los contactos que ha
     * tenido recientemente
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testMetaheuristica() {

        String contrasena = "contraseña1";

        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Juan Jose",
                "Peiro",
                "Garrido",
                "612121211",
                contrasena);

        Usuario usuario1 = new Usuario(
                "656764549",
                "nuerrrfve");

        Usuario usuario2 = new Usuario(
                "699699699",
                "nrerfre");

        Usuario usuario3 = new Usuario(
                "670670670",
                "nurfrerfeve");

        Usuario usuario4 = new Usuario(
                "660376093",
                "efrerfree");

        servicioUjaVid.altaRastreador(rastreador);
        servicioUjaVid.altaUsuario(usuario1);
        servicioUjaVid.altaUsuario(usuario2);
        servicioUjaVid.altaUsuario(usuario3);
        servicioUjaVid.altaUsuario(usuario4);

        ContactoCercano contacto0 = new ContactoCercano(LocalDateTime.now(),
                usuario2, 4, 2);

        ContactoCercano contacto1 = new ContactoCercano(LocalDateTime.now(),
                usuario3, 2, 4);

        ContactoCercano contacto2 = new ContactoCercano(LocalDateTime.now(),
                usuario4, 1, 3);

        usuario1.addContactoCercano(contacto0);
        usuario1.addContactoCercano(contacto1);
        usuario1.addContactoCercano(contacto2);

        servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);

        List<ContactoCercano> lista = servicioUjaVid.verContactosCercanos(usuario1.getUuid());

        for (int i = 0; i < lista.size() - 1; i++) {
            Assertions.assertThat(lista.get(i).getRiesgo()).isGreaterThanOrEqualTo(lista.get(i + 1).getRiesgo());
        }
    }

    /**
     * Validación de que el estadístico de positivos actualmente y la función
     * notificar curación trabajan correctamente
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testNotificacionyPositivosActualmente() {
        String contrasena = "contraseña1";

        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Juan Jose",
                "Peiro",
                "Garrido",
                "612121211",
                contrasena);

        Usuario usuario1 = new Usuario(
                "656764549",
                "nuerrrfve");

        Usuario usuario2 = new Usuario(
                "699699699",
                "nrerfre");

        Usuario usuario3 = new Usuario(
                "670670670",
                "nurfrerfeve");

        servicioUjaVid.altaRastreador(rastreador);
        servicioUjaVid.altaUsuario(usuario1);
        servicioUjaVid.altaUsuario(usuario2);
        servicioUjaVid.altaUsuario(usuario3);

        servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario2.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario3.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);

        Assertions.assertThat(servicioUjaVid.positivos_actual()).isEqualTo(3);
        servicioUjaVid.notificarCuracion(usuario1.getUuid());
        Assertions.assertThat(servicioUjaVid.positivos_actual()).isEqualTo(2);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testPositivos15Dias() {
        String contrasena = "contraseña1";

        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Juan Jose",
                "Peiro",
                "Garrido",
                "612121211",
                contrasena);

        Usuario usuario1 = new Usuario(
                "656764549",
                "nuerrrfve");

        Usuario usuario2 = new Usuario(
                "699699699",
                "nrerfre");

        Usuario usuario3 = new Usuario(
                "670670670",
                "nurfrerfeve");

        servicioUjaVid.altaRastreador(rastreador);
        servicioUjaVid.altaUsuario(usuario1);
        servicioUjaVid.altaUsuario(usuario2);
        servicioUjaVid.altaUsuario(usuario3);

        servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now().minusDays(5), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario2.getUuid(), LocalDateTime.now().minusDays(40), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario3.getUuid(), LocalDateTime.now().minusDays(60), rastreador.getDni(), contrasena);

        Assertions.assertThat(servicioUjaVid.positivos15Dias()).isEqualTo(1);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testFechasInvalidas() {
        String contrasena = "contraseña1";

        Rastreador rastreador = new Rastreador(
                "77434825N",
                "Juan Jose",
                "Peiro",
                "Garrido",
                "612121211",
                contrasena);

        Usuario usuario1 = new Usuario(
                "656764549",
                "nuerrrfve");

        servicioUjaVid.altaRastreador(rastreador);
        servicioUjaVid.altaUsuario(usuario1);

        Assertions.assertThatThrownBy(() -> {
            servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now().plusDays(5), rastreador.getDni(), contrasena);
        })
                .isInstanceOf(ConstraintViolationException.class);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void testReportadosRastreador() {
        String contrasena="a";
        String dniRastreador="77434825N";
        Rastreador rastreador = new Rastreador(
                dniRastreador,
                "Juan Jose",
                "Peiro",
                "Garrido",
                "612121211",
                contrasena);

        Usuario usuario1 = new Usuario(
                "656764549",
                "nuerrrfve");

        Usuario usuario2 = new Usuario(
                "699699699",
                "nrerfre");

        Usuario usuario3 = new Usuario(
                "670670670",
                "nurfrerfeve");


        servicioUjaVid.altaRastreador(rastreador);
        servicioUjaVid.altaUsuario(usuario1);
        servicioUjaVid.altaUsuario(usuario2);
        servicioUjaVid.altaUsuario(usuario3);

        servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now().minusDays(60), rastreador.getDni(), contrasena);

        servicioUjaVid.notificarPos(usuario2.getUuid(), LocalDateTime.now().minusDays(40), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario3.getUuid(), LocalDateTime.now().minusDays(5), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarCuracion(usuario1.getUuid());
        servicioUjaVid.notificarCuracion(usuario2.getUuid());
        servicioUjaVid.notificarPos(usuario1.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);
        servicioUjaVid.notificarPos(usuario2.getUuid(), LocalDateTime.now(), rastreador.getDni(), contrasena);
        
        Assertions.assertThat(servicioUjaVid.positivosRastreador(dniRastreador)).isEqualTo(5);

    }
    
}
