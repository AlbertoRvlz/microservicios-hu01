package com.hu01.productos;

import com.hu01.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository repository;

    @BeforeEach
    void limpiarProductos() {
        repository.deleteAll();
    }

    @Test
    void crearConsultarActualizarYEliminarProducto() throws Exception {
        String creado = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("P-01", "Mouse")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo", is("P-01")))
                .andReturn().getResponse().getContentAsString();
        String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(creado).get("id").asText();

        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Mouse")));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo", is("P-01")));
        mockMvc.perform(put("/api/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("P-01", "Mouse inalámbrico")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Mouse inalámbrico")));
        mockMvc.perform(delete("/api/productos/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void respondeErroresEstructuradosParaValidacionNoEncontradoYDuplicado() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"","nombre":"","precio":0,"stock":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.ruta", is("/api/productos")));

        mockMvc.perform(get("/api/productos/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("P-DUP", "Primero")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("P-DUP", "Duplicado")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void actualizarYEliminarProductoInexistenteDevuelvenNoEncontrado() throws Exception {
        mockMvc.perform(put("/api/productos/no-existe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("P-01", "Mouse")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/productos/no-existe")).andExpect(status().isNotFound());
    }

    private String productoJson(String codigo, String nombre) {
        return """
                {"codigo":"%s","nombre":"%s","descripcion":"Producto de prueba","precio":25.50,"stock":5}
                """.formatted(codigo, nombre);
    }
}
