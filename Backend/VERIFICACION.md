# Verificación realizada

Fecha: 16 de octubre de 2026.

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
