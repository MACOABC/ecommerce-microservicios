# Verificación realizada

Fecha: 16 de octubre de 2026.

La revisión funcional más reciente se encuentra en [REVISION_PAGINA.md](REVISION_PAGINA.md): 71 comprobaciones funcionales, revisión visual y correcciones del proxy y de errores HTTP. Las secciones siguientes conservan el historial de las verificaciones iniciales.

## Entorno

- Windows / PowerShell.
- JDK Temurin 25.0.4.1, compilación con destino Java 21.
- Maven 3.9.11.
- Node.js 24.20.0 / npm 11.19.0.
- PostgreSQL 17.6 temporal, en tres bases separadas.
- Cuatro servicios ejecutados localmente y frontend servido por Vite.

## Resultados

| Comprobación manual | Resultado |
| --- | --- |
| Compilación y empaquetado de los cuatro módulos Maven | Correcto |
| TypeScript estricto y build de Vite | Correcto |
| Auditoría npm al instalar dependencias | 0 vulnerabilidades reportadas |
| Ejecución de los tres scripts SQL | Correcta |
| Catálogo a través del gateway | Seis productos iniciales |
| Compra aprobada | Total calculado en servidor y stock descontado |
| Repetición del mismo UUID y cuerpo | Mismo resultado, un único descuento |
| Reutilización del UUID con otros datos | HTTP 409 |
| Dos compras concurrentes sobre una sola unidad | Una aprobada y otra HTTP 409; stock final cero |
| Compra de varios productos con stock insuficiente en uno | HTTP 409; ningún descuento parcial |
| Pago rechazado y repetición | Stock restituido una única vez |
| Creación repetida de guía | Mismo envío para la misma orden |
| Avance PREPARANDO a ENTREGADO | Cuatro eventos de seguimiento |
| Repetición del último estado | Sin evento duplicado |
| Creación, actualización y baja de producto | Correctas; baja lógica con HTTP 404 posterior |
| Datos de producto inválidos | HTTP 400 |
| Acceso a /internal desde gateway | HTTP 404 |
| Compra completa en navegador | Carrito, pago aprobado, orden y guía consultada |
| git diff --check | Sin errores de espacios |

No se incluyen suites ni archivos de pruebas en el repositorio.

## Límite de la verificación

La simulación de una interrupción del catálogo fue bloqueada por la revisión automática de ejecución y no se completó. La implementación conserva la intención de compra y operaciones remotas idempotentes, pero la recuperación ante caída de proceso queda pendiente de comprobar en ejecución.

No se verificó un entorno de producción ni una pasarela de pagos real. El alcance es local y académico.

## Actualización de Docker y despliegue

Fecha: 16 de octubre de 2026. Se incorporaron cinco Dockerfiles multietapa, dos archivos Compose, el proxy Nginx, contextos de build filtrados y la plantilla de variables de entorno.

| Comprobación | Resultado |
| --- | --- |
| Compose completo con CLI oficial 5.6.0, `config --quiet` | Válido; ocho servicios |
| Compose de desarrollo, `config --quiet` | Válido; tres instancias PostgreSQL |
| Interpolación mediante la invocación documentada y valores ficticios | Correcta en ambos Compose |
| Contraseñas vacías en la plantilla | La configuración falla antes del arranque |
| Rutas de montaje de los tres SQL | Existen y se resuelven correctamente en ambos modos |
| Bases del despliegue completo | Sin puertos publicados al equipo |
| Healthchecks de PostgreSQL | Variables escapadas para expansión dentro del contenedor; comprobación TCP |
| Imágenes Maven, Temurin, Node, Nginx y PostgreSQL | Etiquetas consultadas y existentes en Docker Hub |
| Configuración Nginx con `nginx -t`, distribución oficial Windows 1.30.6 | Sintaxis correcta |
| Contextos de build | Backend incluye padre y módulos; frontend incluye lockfile y fuentes |
| Secretos de ejecución | Plantilla vacía; `.env` ignorado en Git y en contextos Docker |

El equipo no dispone de Docker Engine ni Docker Desktop. La validación de Compose usa el ejecutable oficial independiente y no inicia contenedores. El análisis de Nginx verifica la sintaxis con un binario nativo; no equivale a ejecutar su imagen Linux.

Quedan pendientes `docker compose build --pull`, el arranque completo, la ejecución de los SQL dentro de sus imágenes, las comprobaciones de salud en contenedores y el flujo de compra a través de Nginx. Los resultados previos de la aplicación local no certifican ese despliegue. No se añadieron suites ni archivos de pruebas.
