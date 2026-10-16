package jass.com.agua.config;

import jass.com.agua.entity.*;
import jass.com.agua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TipoServicioService tipoServicioService;
    private final RolService rolService;
    private final PermisoService permisoService;
    private final RolPermisoService rolPermisoService;
    private final TarifaService tarifaService;

    @Override
    public void run(String... args) {

        crearTipoServicio(
                "DOMESTICO",
                "Servicio de agua para vivienda familiar"
        );

        crearTipoServicio(
                "COMERCIAL",
                "Servicio de agua para establecimientos comerciales"
        );

        crearTarifa(
                "DOMESTICO HASTA 20 M3",
                "DOMESTICO",
                new BigDecimal("0.00"),
                new BigDecimal("20.00"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO
        );

        crearTarifa(
                "DOMESTICO MAYOR A 20 M3",
                "DOMESTICO",
                new BigDecimal("20.01"),
                null,
                BigDecimal.ZERO,
                new BigDecimal("0.67")
        );

        crearTarifa(
                "COMERCIAL HASTA 20 M3",
                "COMERCIAL",
                new BigDecimal("0.00"),
                new BigDecimal("20.00"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO
        );

        crearTarifa(
                "COMERCIAL MAYOR A 20 M3",
                "COMERCIAL",
                new BigDecimal("20.01"),
                null,
                BigDecimal.ZERO,
                new BigDecimal("1.80")
        );

        crearRol(
                "PRESIDENTA",
                "Acceso administrativo y supervision general"
        );

        crearRol(
                "SECRETARIA",
                "Gestion administrativa, pagos, caja y asociados"
        );

        crearRol(
                "OPERADOR",
                "Registro de lecturas y operaciones de campo"
        );

        // ASOCIADOS
        crearPermiso("ASOCIADOS_VER", "ASOCIADOS",
                "Consultar asociados e inmuebles");

        crearPermiso("ASOCIADOS_CREAR", "ASOCIADOS",
                "Registrar asociados e inmuebles");

        crearPermiso("ASOCIADOS_EDITAR", "ASOCIADOS",
                "Actualizar asociados e inmuebles");

        crearPermiso("TITULARIDAD_CAMBIAR", "ASOCIADOS",
                "Registrar cambios de titularidad");

        // SERVICIOS
        crearPermiso("SERVICIOS_VER", "SERVICIOS",
                "Consultar servicios de agua");

        crearPermiso("SERVICIOS_CREAR", "SERVICIOS",
                "Registrar servicios de agua");

        crearPermiso("SERVICIOS_EDITAR", "SERVICIOS",
                "Actualizar servicios de agua");

        crearPermiso("MEDIDORES_GESTIONAR", "SERVICIOS",
                "Registrar y administrar medidores");

        // LECTURAS
        crearPermiso("LECTURAS_VER", "LECTURAS",
                "Consultar lecturas e historial de consumo");

        crearPermiso("LECTURAS_REGISTRAR", "LECTURAS",
                "Registrar lecturas de medidores");

        crearPermiso("LECTURAS_EDITAR", "LECTURAS",
                "Corregir lecturas registradas");

        crearPermiso("ASIGNACIONES_GESTIONAR", "LECTURAS",
                "Gestionar asignaciones de operadores");

        // FACTURACION
        crearPermiso("FACTURACION_VER", "FACTURACION",
                "Consultar recibos y facturacion");

        crearPermiso("FACTURACION_GENERAR", "FACTURACION",
                "Procesar lecturas y generar recibos");

        crearPermiso("TARIFAS_GESTIONAR", "FACTURACION",
                "Registrar y modificar tarifas");

        // PAGOS
        crearPermiso("PAGOS_VER", "PAGOS",
                "Consultar pagos realizados");

        crearPermiso("PAGOS_REGISTRAR", "PAGOS",
                "Registrar pagos de asociados");

        crearPermiso("MOROSIDAD_VER", "PAGOS",
                "Consultar asociados morosos");

        // CORTES
        crearPermiso("CORTES_VER", "CORTES",
                "Consultar ordenes de corte");

        crearPermiso("CORTES_GENERAR", "CORTES",
                "Generar ordenes de corte");

        crearPermiso("SUSPENSIONES_REGISTRAR", "CORTES",
                "Registrar suspensiones del servicio");

        crearPermiso("RECONEXIONES_REGISTRAR", "CORTES",
                "Registrar reconexiones del servicio");

        // CAJA
        crearPermiso("CAJA_VER", "CAJA",
                "Consultar caja y movimientos");

        crearPermiso("CAJA_MOVIMIENTOS", "CAJA",
                "Registrar ingresos y egresos");

        crearPermiso("CAJA_CERRAR", "CAJA",
                "Realizar cierres de caja");

        // REPORTES
        crearPermiso("REPORTES_VER", "REPORTES",
                "Consultar reportes");

        crearPermiso("REPORTES_EXPORTAR", "REPORTES",
                "Exportar reportes en Excel o PDF");

        // PROVEEDORES
        crearPermiso("PROVEEDORES_VER", "PROVEEDORES",
                "Consultar proveedores");

        crearPermiso("PROVEEDORES_GESTIONAR", "PROVEEDORES",
                "Registrar y actualizar proveedores");

        crearPermiso("REQUERIMIENTOS_GESTIONAR", "PROVEEDORES",
                "Registrar y gestionar requerimientos");

        // INVENTARIO
        crearPermiso("INVENTARIO_VER", "INVENTARIO",
                "Consultar materiales y stock");

        crearPermiso("INVENTARIO_GESTIONAR", "INVENTARIO",
                "Registrar materiales, entradas y salidas");

        // SEGURIDAD
        crearPermiso("USUARIOS_GESTIONAR", "SEGURIDAD",
                "Administrar usuarios y asignar roles");

        crearPermiso("ROLES_GESTIONAR", "SEGURIDAD",
                "Administrar roles y permisos");

        crearPermiso("AUDITORIA_VER", "SEGURIDAD",
                "Consultar auditoria del sistema");

        asignarPermisosPresidenta();
        asignarPermisosSecretaria();
        asignarPermisosOperador();
    }

    private void asignarPermisosPresidenta() {

        String[] permisos = {

                "ASOCIADOS_VER",
                "ASOCIADOS_CREAR",
                "ASOCIADOS_EDITAR",
                "TITULARIDAD_CAMBIAR",

                "SERVICIOS_VER",
                "SERVICIOS_CREAR",
                "SERVICIOS_EDITAR",
                "MEDIDORES_GESTIONAR",

                "LECTURAS_VER",
                "LECTURAS_REGISTRAR",
                "LECTURAS_EDITAR",
                "ASIGNACIONES_GESTIONAR",

                "FACTURACION_VER",
                "FACTURACION_GENERAR",
                "TARIFAS_GESTIONAR",

                "PAGOS_VER",
                "PAGOS_REGISTRAR",
                "MOROSIDAD_VER",

                "CORTES_VER",
                "CORTES_GENERAR",
                "SUSPENSIONES_REGISTRAR",
                "RECONEXIONES_REGISTRAR",

                "CAJA_VER",
                "CAJA_MOVIMIENTOS",
                "CAJA_CERRAR",

                "REPORTES_VER",
                "REPORTES_EXPORTAR",

                "PROVEEDORES_VER",
                "PROVEEDORES_GESTIONAR",
                "REQUERIMIENTOS_GESTIONAR",

                "INVENTARIO_VER",
                "INVENTARIO_GESTIONAR",

                "USUARIOS_GESTIONAR",
                "ROLES_GESTIONAR",
                "AUDITORIA_VER"
        };

        asignarPermisos("PRESIDENTA", permisos);
    }

    private void asignarPermisosSecretaria() {

        String[] permisos = {

                // Asociados
                "ASOCIADOS_VER",
                "ASOCIADOS_CREAR",
                "ASOCIADOS_EDITAR",
                "TITULARIDAD_CAMBIAR",

                // Servicios
                "SERVICIOS_VER",
                "SERVICIOS_CREAR",
                "SERVICIOS_EDITAR",
                "MEDIDORES_GESTIONAR",

                // Lecturas - consulta administrativa
                "LECTURAS_VER",

                // Facturacion
                "FACTURACION_VER",
                "FACTURACION_GENERAR",

                // Pagos y morosidad
                "PAGOS_VER",
                "PAGOS_REGISTRAR",
                "MOROSIDAD_VER",

                // Cortes
                "CORTES_VER",
                "CORTES_GENERAR",
                "SUSPENSIONES_REGISTRAR",
                "RECONEXIONES_REGISTRAR",

                // Caja
                "CAJA_VER",
                "CAJA_MOVIMIENTOS",
                "CAJA_CERRAR",

                // Reportes
                "REPORTES_VER",
                "REPORTES_EXPORTAR",

                // Proveedores
                "PROVEEDORES_VER",
                "PROVEEDORES_GESTIONAR",
                "REQUERIMIENTOS_GESTIONAR",

                // Inventario
                "INVENTARIO_VER",
                "INVENTARIO_GESTIONAR"
        };

        asignarPermisos("SECRETARIA", permisos);
    }

    private void asignarPermisosOperador() {

        String[] permisos = {

                // Información necesaria para el recorrido
                "ASOCIADOS_VER",
                "SERVICIOS_VER",

                // Trabajo principal
                "LECTURAS_VER",
                "LECTURAS_REGISTRAR"
        };

        asignarPermisos("OPERADOR", permisos);
    }

    private void asignarPermisos(
            String nombreRol,
            String[] nombresPermisos
    ) {

        Rol rol = rolService.obtenerPorNombre(nombreRol);

        for (String nombrePermiso : nombresPermisos) {

            Permiso permiso =
                    permisoService.obtenerPorNombre(nombrePermiso);

            if (!rolPermisoService.tienePermiso(
                    rol.getIdRol(),
                    permiso.getIdPermiso()
            )) {

                rolPermisoService.asignar(
                        rol.getIdRol(),
                        permiso.getIdPermiso()
                );
            }
        }
    }

    private void crearTipoServicio(
            String nombre,
            String descripcion
    ) {

        if (!tipoServicioService.existeNombre(nombre)) {

            TipoServicio tipo = TipoServicio.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .estado("ACTIVO")
                    .build();

            tipoServicioService.registrar(tipo);
        }
    }

    private void crearRol(
            String nombre,
            String descripcion
    ) {

        if (!rolService.existeNombre(nombre)) {

            Rol rol = Rol.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .build();

            rolService.guardar(rol);
        }
    }

    private void crearPermiso(
            String nombre,
            String modulo,
            String descripcion
    ) {

        if (!permisoService.existeNombre(nombre)) {

            Permiso permiso = Permiso.builder()
                    .nombre(nombre)
                    .modulo(modulo)
                    .descripcion(descripcion)
                    .build();

            permisoService.guardar(permiso);
        }
    }

    private void crearTarifa(
            String nombre,
            String nombreTipoServicio,
            BigDecimal consumoMinimo,
            BigDecimal consumoMaximo,
            BigDecimal importeFijo,
            BigDecimal precioM3
    ) {

        if (tarifaService.existeNombre(nombre)) {
            return;
        }

        TipoServicio tipoServicio =
                tipoServicioService.obtenerPorNombre(
                        nombreTipoServicio
                );

        Tarifa tarifa = Tarifa.builder()
                .tipoServicio(tipoServicio)
                .nombre(nombre)
                .consumoMinimo(consumoMinimo)
                .consumoMaximo(consumoMaximo)
                .importeFijo(importeFijo)
                .precioM3(precioM3)
                .fechaInicio(null)
                .fechaFin(null)
                .estado("ACTIVO")
                .build();

        tarifaService.registrar(tarifa);
    }
}