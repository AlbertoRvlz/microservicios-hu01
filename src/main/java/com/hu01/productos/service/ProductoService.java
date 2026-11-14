package com.hu01.productos.service;

import com.hu01.productos.dto.ProductoRequest;
import com.hu01.productos.exception.DatoDuplicadoException;
import com.hu01.productos.exception.RecursoNoEncontradoException;
import com.hu01.productos.model.Producto;
import com.hu01.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Producto crear(ProductoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new DatoDuplicadoException("Ya existe un producto con el código " + request.codigo());
        }
        return repository.save(convertir(request, null));
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Producto obtenerPorId(String id) {
        return buscarPorId(id);
    }

    public Producto actualizar(String id, ProductoRequest request) {
        Producto actual = buscarPorId(id);
        if (!actual.getCodigo().equals(request.codigo()) && repository.existsByCodigo(request.codigo())) {
            throw new DatoDuplicadoException("Ya existe un producto con el código " + request.codigo());
        }
        return repository.save(convertir(request, id));
    }

    public void eliminar(String id) {
        repository.delete(buscarPorId(id));
    }

    private Producto buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con id " + id));
    }

    private Producto convertir(ProductoRequest request, String id) {
        return new Producto(id, request.codigo(), request.nombre(), request.descripcion(), request.precio(),
                request.stock());
    }
}
