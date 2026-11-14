package com.hu01.productos.repository;

import com.hu01.productos.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    boolean existsByCodigo(String codigo);
}
