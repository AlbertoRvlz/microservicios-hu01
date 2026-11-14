package com.hu01.productos.service;

import com.hu01.productos.dto.ProductoRequest;
import com.hu01.productos.exception.DatoDuplicadoException;
import com.hu01.productos.exception.RecursoNoEncontradoException;
import com.hu01.productos.model.Producto;
import com.hu01.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoService service;

    private ProductoRequest request;

    @BeforeEach
    void setUp() {
        request = new ProductoRequest("P-100", "Teclado", "Mecánico", new BigDecimal("49.90"), 10);
    }

    @Test
    void crearProductoCuandoCodigoNoExiste() {
        when(repository.existsByCodigo("P-100")).thenReturn(false);
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = service.crear(request);

        assertEquals("P-100", resultado.getCodigo());
        assertEquals("Teclado", resultado.getNombre());
        verify(repository).save(any(Producto.class));
    }

    @Test
    void crearLanzaErrorCuandoCodigoExiste() {
        when(repository.existsByCodigo("P-100")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> service.crear(request));
        verify(repository, never()).save(any(Producto.class));
    }

    @Test
    void obtenerProductoCuandoExiste() {
        Producto producto = new Producto("1", "P-100", "Teclado", "Mecánico", new BigDecimal("49.90"), 10);
        when(repository.findById("1")).thenReturn(Optional.of(producto));

        assertEquals(producto, service.obtenerPorId("1"));
    }

    @Test
    void obtenerLanzaErrorCuandoNoExiste() {
        when(repository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId("inexistente"));
    }

    @Test
    void actualizarConservaIdYActualizaDatos() {
        Producto actual = new Producto("1", "P-100", "Anterior", null, BigDecimal.ONE, 1);
        when(repository.findById("1")).thenReturn(Optional.of(actual));
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto resultado = service.actualizar("1", request);

        assertEquals("1", resultado.getId());
        assertEquals("Teclado", resultado.getNombre());
    }

    @Test
    void actualizarRechazaCodigoUsadoPorOtroProducto() {
        Producto actual = new Producto("1", "P-OLD", "Anterior", null, BigDecimal.ONE, 1);
        when(repository.findById("1")).thenReturn(Optional.of(actual));
        when(repository.existsByCodigo("P-100")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> service.actualizar("1", request));
    }

    @Test
    void listarDevuelveProductos() {
        List<Producto> productos = List.of(
                new Producto("1", "P-100", "Teclado", null, BigDecimal.ONE, 0));
        when(repository.findAll()).thenReturn(productos);

        assertEquals(productos, service.listar());
    }

    @Test
    void eliminarProductoExistente() {
        Producto producto = new Producto("1", "P-100", "Teclado", null, BigDecimal.ONE, 0);
        when(repository.findById("1")).thenReturn(Optional.of(producto));

        service.eliminar("1");

        verify(repository).delete(producto);
    }
}
