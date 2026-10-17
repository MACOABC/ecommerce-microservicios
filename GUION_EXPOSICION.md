# Guion de exposición · Merca

**Seis integrantes · Aproximadamente 2 a 3 minutos por integrante · Duración total estimada: 15 a 18 minutos.**

Las intervenciones tienen entre 277 y 297 palabras. A unas 130 palabras por minuto, más las acciones de pantalla y las transiciones, la participación queda equilibrada. Ensayen una vez con cronómetro y realicen las acciones mientras explican o durante pausas breves. Los tiempos son orientativos.

## Caso práctico que se demostrará

Una persona compra una unidad de Auriculares Studio, comprueba que baja el stock, consulta la guía y observa un cambio de estado del envío. Después se repite la compra con un pago rechazado para verificar que el inventario final se conserva.

El pago y el despacho son simulados; se usan datos ficticios. Si ese artículo no tiene al menos dos unidades antes de empezar, elijan otro producto disponible y ajusten el nombre y la variable de identificación del anexo.

## Reparto

| Integrante | Tema principal | Evidencia en pantalla | Palabras del texto |
| --- | --- | --- | --- |
| 1 | Caso práctico, alcance y decisiones de implementación | Página y estructura del proyecto | 277 |
| 2 | Diagrama de arquitectura y arranque real | Diagrama, arranque y servicios activos | 279 |
| 3 | Catálogo, interfaz y carrito | Catálogo, cantidades y bolsa | 297 |
| 4 | Compra aprobada e interacción entre órdenes y productos | Orden pagada y comparación de stock | 296 |
| 5 | Logística, guía y seguimiento en vivo | Guía y actualización de seguimiento | 290 |
| 6 | Pago rechazado, evidencia del estilo y cierre | Pago rechazado e inventario conservado | 296 |

## Preparación antes de grabar

1. Actualicen el proyecto y ensayen en el mismo equipo que usarán en la grabación. Configuren `.env` antes de compartir pantalla; no muestren sus contraseñas.
2. Elijan un modo de ejecución y descríbanlo con precisión. El texto principal del integrante 2 supone Docker Compose; al final se incluye una variante para ejecución nativa.
3. Para Docker, abran Docker Desktop y construyan las imágenes previamente con `docker compose build`. Así la grabación puede mostrar el arranque real sin dedicar minutos a descargar dependencias. No es necesario borrar los volúmenes.
4. Tengan abiertas la terminal en la raíz del proyecto, el README con los diagramas, el navegador y, opcionalmente, el código de los controladores y servicios.
5. Usen una ventana privada nueva para que la demostración no empiece con el carrito o una compra pendiente de otra sesión. Los pedidos antiguos en las bases pueden permanecer; comparen cantidades antes y después, no saldos fijos.
6. Conserven la misma ventana de PowerShell durante toda la demostración: las variables de los comandos del anexo se reutilizan.
7. Verifiquen que el artículo elegido tenga al menos dos unidades disponibles. Practiquen la copia del código completo del pedido para evitar perder tiempo con un UUID incompleto.

**Durante la grabación deben verse acciones y resultados reales:** inicio o comprobación del sistema, interacción con la tienda, orden creada, cambio de stock y actualización del envío. Los diagramas apoyan la explicación; no reemplazan la ejecución. Los logs de arranque no prueban por sí solos todas las llamadas internas, y la pestaña Network del navegador sólo muestra las solicitudes del frontend. Relacionen las peticiones visibles con los cambios en los datos y el código que las implementa.

Los bloques “Acción en vivo” son indicaciones de puesta en escena, no texto para leer.

## Integrante 1 · Caso práctico, alcance y decisiones de implementación

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** Mostrar la página de inicio y, durante unos segundos, las tres carpetas principales del proyecto. No iniciar todavía una compra.

### Texto para decir

Buenas tardes. Nuestro proyecto se llama Merca y consiste en una tienda virtual desarrollada con arquitectura de microservicios. Elegimos un caso concreto: una persona consulta productos, agrega un artículo a su carrito, confirma una compra y revisa el estado de su envío. También contemplamos un pago rechazado para mostrar cómo se recupera el inventario.

Acotamos el proyecto a catálogo, órdenes y logística. El pago es una simulación; no cobramos dinero ni usamos tarjetas reales. Tampoco integramos una empresa de transporte: registramos y actualizamos el estado del envío dentro de nuestra aplicación.

La implementación se organizó en dos etapas. Primero construimos la solución funcional: las bases de datos, los servicios del backend y la interfaz. Después incorporamos Docker, Nginx y Docker Compose para preparar un entorno que pueda ejecutarse de forma consistente en otros equipos.

Ordenamos el código en tres carpetas principales. Base de Datos contiene los scripts que crean las tablas y cargan los ejemplos iniciales. Backend contiene cuatro aplicaciones Java: productos, órdenes, envíos y el gateway. Frontend contiene la interfaz de la tienda.

Elegimos Java con Spring Boot porque facilita desarrollar APIs HTTP y separar la lógica de negocio de la atención de solicitudes. Para acceder a los datos usamos Spring JDBC, que permite trabajar con consultas SQL explícitas, y PostgreSQL para guardar la información.

La interfaz utiliza HTML, CSS y TypeScript. Esto nos permitió implementar las pantallas necesarias sin incorporar un framework complejo. Vite se encarga de preparar los archivos del frontend.

En la demostración seguiremos una misma compra de principio a fin. Así podremos relacionar lo que ve el usuario con las operaciones que realizan los componentes. Ahora explicaremos cómo se conectan esas partes.

## Integrante 2 · Diagrama de arquitectura y arranque real

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** Mostrar el diagrama del README. Ejecutar docker compose up -d --wait --wait-timeout 240 y docker compose ps con las imágenes ya construidas. Abrir http://localhost:8088. Si se usa ejecución nativa, emplear la variante indicada al final.

### Texto para decir

Este diagrama representa la arquitectura que implementamos. A la izquierda está el navegador, donde se ejecutan los archivos HTML, CSS y TypeScript. En el despliegue con Docker, esos archivos son entregados por Nginx. Desde el equipo entramos por el puerto 8088, que se dirige al puerto 80 del contenedor.

Las solicitudes de datos utilizan la ruta api. Nginx las reenvía al API Gateway, desarrollado con Spring Cloud Gateway. Su responsabilidad es dirigir cada petición al servicio correspondiente; las reglas de compra no se ejecutan en el gateway.

Tenemos tres servicios de negocio. Productos administra el catálogo y el inventario. Órdenes coordina la compra y el pago simulado. Envíos administra la guía y los cambios de estado de la entrega.

Cada uno utiliza una base de datos propia. Productos no modifica directamente las tablas de órdenes, ni envíos consulta esas tablas mediante una conexión SQL. Cuando necesita información de otro dominio, utiliza su API.

Las flechas entre servicios representan llamadas HTTP síncronas, con datos en formato JSON. Por ejemplo, órdenes solicita una reserva a productos. Para crear un envío, logística consulta a órdenes y verifica que la compra esté pagada. Son operaciones diferentes; las flechas en ambos sentidos no representan un ciclo infinito.

Ahora ejecutamos el sistema. Docker Compose organiza ocho servicios: los tres de negocio, el gateway, el frontend y tres instancias PostgreSQL. Las comprobaciones de salud permiten esperar a que las dependencias estén disponibles.

Los Dockerfiles compilan la aplicación en una etapa y ejecutan el resultado en otra. Docker facilita el empaquetado, pero la arquitectura de microservicios se evidencia en la separación de responsabilidades, procesos y datos.

Con el sistema funcionando, pasaremos al catálogo para iniciar la compra.

## Integrante 3 · Catálogo, interfaz y carrito

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** En el catálogo, filtrar Tecnología, buscar Auriculares y mostrar su stock. Guardar el stock inicial con el comando del anexo. Agregar un auricular, subir a dos unidades y volver a una. Abrir Mi bolsa sin confirmar.

### Texto para decir

Comenzamos desde la experiencia del comprador. En el catálogo podemos ver el nombre, la descripción, el precio y la cantidad disponible de cada producto. La información procede del servicio de productos; no es solamente contenido fijo dibujado en la página.

Vamos a filtrar la categoría Tecnología y a buscar Auriculares. Esto permite encontrar el artículo dentro de la selección cargada. El frontend hace el filtrado visible sobre los datos que recibió; la API también admite consultas de búsqueda.

Antes de comprar, observamos el stock disponible. Conservaremos ese valor para compararlo después de confirmar la orden. Lo importante será comprobar que la cantidad cambia en el servidor, no únicamente en la pantalla.

Agregamos una unidad a la bolsa. Después aumentamos la cantidad a dos y la reducimos nuevamente a una. Podemos observar que el contador y el total del carrito cambian con esas acciones. También es posible retirar un producto.

El carrito se guarda en el navegador. Por eso puede mantenerse después de recargar la página. En esta etapa todavía no existe una orden pagada ni se ha reservado inventario: únicamente estamos preparando los artículos que se enviarán al backend.

Esta distinción es importante. El navegador facilita la interacción, pero no decide si hay stock suficiente ni cuál será el precio definitivo de la compra. Esa validación corresponde a los servicios del servidor.

El servicio de productos también tiene operaciones para crear, actualizar y desactivar artículos, además de administrar categorías. Esas operaciones están disponibles mediante su API; la interfaz que estamos mostrando está orientada al comprador.

La aplicación impide agregar productos agotados y limita las cantidades seleccionadas. Sin embargo, el servidor vuelve a comprobar el inventario, porque otra persona podría comprar antes que nosotros.

Dejamos una unidad en la bolsa y continuamos con el registro de la orden.

## Integrante 4 · Compra aprobada e interacción entre órdenes y productos

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** En Mi bolsa, usar datos ficticios, elegir Aprobar pago y confirmar una sola vez. Mostrar PAGADA y el UUID. Consultar el stock actual con el comando del anexo y compararlo con el inicial. Dejar Mis pedidos abierto.

### Texto para decir

Ahora vamos a completar la compra. Ingresamos un nombre, un correo y una dirección de ejemplo. Seleccionamos aprobar el pago simulado y confirmamos una sola vez.

En este momento el navegador genera un identificador único para la compra y envía los productos y sus cantidades. La solicitud pasa por Nginx y el gateway hasta llegar al servicio de órdenes.

Órdenes guarda primero la intención de compra en su propia base. Después se comunica directamente con el servicio de productos para solicitar una reserva de inventario.

Productos comprueba que los artículos existan, estén activos y tengan unidades suficientes. La reserva se realiza dentro de una transacción local de su base de datos. Si uno de los artículos no tiene stock, no se completa un descuento parcial del conjunto.

También se utilizan bloqueos sobre el inventario para coordinar solicitudes que podrían ocurrir al mismo tiempo. La última unidad disponible no debe venderse dos veces.

Los precios usados para calcular el total vienen del servicio de productos. El backend no acepta como precio definitivo un monto enviado por el navegador. Con esos valores, órdenes calcula el total y registra el resultado del pago simulado.

En nuestro ejemplo el resultado es aprobado y la orden aparece como pagada. Podemos ver su identificador y el detalle de la compra. Comprobamos además que el stock del producto se redujo en una unidad.

Esta operación muestra una interacción real entre dos servicios y sus bases de datos. Órdenes coordina la compra, pero productos conserva la responsabilidad sobre el inventario.

El identificador permite reconocer la misma operación si hace falta reintentar. Repetirla con los mismos datos no debe volver a descontar stock.

Una vez confirmada la orden, el frontend también solicita preparar su envío. A continuación mostraremos cómo logística genera y consulta esa información.

## Integrante 5 · Logística, guía y seguimiento en vivo

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** En el pedido pagado, pulsar Generar o consultar guía y mostrar PREPARANDO. Usar el UUID que dejó copiado el integrante 4. Ejecutar los comandos del anexo para recuperar la misma guía y pasar a DESPACHADO. Pulsar Rastrear envío y mostrar los dos eventos.

### Texto para decir

Continuamos con la misma compra. Desde Mis pedidos elegimos generar o consultar la guía. La interfaz nos lleva al seguimiento y muestra el código del envío.

Aunque veamos un botón para generar la guía, la aplicación ya puede haberla solicitado automáticamente después de aprobar la compra. Por eso esta operación también permite recuperar una guía existente.

La petición entra por el gateway y llega a órdenes, que se comunica con el servicio de envíos. Logística recibe el identificador de la orden y consulta al servicio de órdenes para verificar que realmente esté pagada.

Esta comprobación evita que se prepare un despacho solamente porque el navegador lo solicite. La decisión se valida en el backend utilizando la información del servicio responsable de la compra.

Después, envíos registra su guía, el destinatario, la dirección y el estado inicial, que es preparando. También guarda un evento con la fecha del registro en su propia base de datos.

Cada orden puede tener un solo envío. Vamos a solicitar nuevamente la guía y comprobar que devuelve el mismo identificador. Así mostramos que repetir esa operación no crea despachos duplicados.

Para demostrar un cambio real, actualizaremos el estado a despachado mediante la API. La interfaz actual es de consulta para el comprador; la actualización logística se realiza desde una operación del servidor, no desde un botón administrativo de la página.

Ahora volvemos al navegador y pulsamos Rastrear envío. Aparece el nuevo estado y podemos observar dos eventos: preparando y despachado.

Los estados posteriores son en tránsito y entregado. El servicio exige avanzar en orden y no agrega otro evento si se repite el mismo estado.

Así conectamos una compra pagada con su seguimiento, utilizando un servicio de logística separado que conserva sus propios datos e historial.

## Integrante 6 · Pago rechazado, evidencia del estilo y cierre

**Tiempo estimado:** 2 a 3 minutos, incluyendo la acción en pantalla.

**Acción en vivo:** Guardar el stock actual. Agregar de nuevo una unidad del mismo producto, completar datos ficticios y elegir Rechazar pago. Mostrar RECHAZADA, el carrito conservado y la igualdad del stock antes/después. Volver brevemente al diagrama para el cierre.

### Texto para decir

Para terminar, mostraremos un segundo resultado de la compra: el pago rechazado. Tomamos el stock actual, agregamos una unidad del mismo producto y seleccionamos rechazar el pago simulado.

Al confirmar, la aplicación procesa la reserva y luego registra el rechazo. El servicio de órdenes solicita a productos liberar las unidades que se habían reservado.

En la pantalla aparece la orden rechazada y el carrito permanece disponible. Al comparar el inventario antes y después de este intento, vemos que la cantidad final es la misma. Tampoco corresponde crear un envío para una orden que no está pagada.

Esta demostración nos permite justificar por qué el proyecto utiliza microservicios. No se trata solamente de tener varias carpetas o de usar contenedores.

Tenemos aplicaciones ejecutables separadas, con configuración y puertos propios. Cada servicio concentra una responsabilidad del negocio y administra su base de datos. Las colaboraciones se realizan mediante contratos HTTP, en lugar de acceder directamente a las tablas de otro servicio.

Además, cada aplicación dispone de su propio Dockerfile y puede compilarse como un artefacto independiente. El gateway centraliza la entrada desde la interfaz, mientras los servicios se comunican directamente cuando necesitan colaborar.

Esta separación también tiene un costo: una compra depende de la disponibilidad del catálogo y se deben considerar fallos de comunicación. Por eso conservamos el identificador de la operación y usamos reservas que se pueden reintentar sin duplicar descuentos.

Las transacciones son locales a cada base. No afirmamos que exista una única transacción distribuida ni un sistema automático de conciliación.

Nuestro alcance es académico: el pago y la entrega son simulados, y todavía no incorporamos autenticación ni permisos de administración.

Con esta ejecución demostramos una compra aprobada, su seguimiento y un pago rechazado. Los cambios observados evidencian la colaboración real entre catálogo, órdenes y envíos.

## Comandos de apoyo para la demostración

Usen una misma ventana de PowerShell desde la raíz del proyecto. Elijan de antemano a la persona que manejará teclado y ratón en cada turno.

### Integrante 2 · Arranque con Docker

Con las imágenes construidas antes de grabar:

```powershell
docker compose up -d --wait --wait-timeout 240
docker compose ps
```

Muestren los ocho servicios y su estado real; esperen a que estén disponibles antes de pasar al catálogo. Abran `http://localhost:8088`. Si configuraron otro puerto público, ajusten también la variable `$base` de los siguientes comandos.

### Integrante 3 · Registrar stock antes de la compra

```powershell
$base = 'http://localhost:8088'
$productoId = 1
$producto = Invoke-RestMethod -Uri "$base/api/productos/$productoId"
$stockInicial = $producto.stock
$producto | Select-Object id, nombre, precio, stock
```

El producto 1 corresponde a Auriculares Studio en los datos iniciales. Comparen el identificador con el artículo que realmente agreguen al carrito.

### Integrante 4 · Comparar el stock después del pago aprobado

Desde Mis pedidos, copien el UUID de la orden nueva y péguenlo cuando la terminal lo solicite:

```powershell
$idOrden = Read-Host 'Pega el codigo completo del pedido PAGADO'
$stockDespuesCompra = (Invoke-RestMethod -Uri "$base/api/productos/$productoId").stock
[pscustomobject]@{
    StockAntes = $stockInicial
    StockDespues = $stockDespuesCompra
    UnidadesDescontadas = $stockInicial - $stockDespuesCompra
}
```

Con una unidad comprada, la diferencia esperada es 1. No escriban un número corto de pedido: se utiliza el UUID completo que muestra la página.

### Integrante 5 · Consultar la misma guía y avanzar el estado

La variable `$idOrden` ya debe contener el UUID copiado por el integrante 4.

```powershell
$envio = Invoke-RestMethod -Uri "$base/api/ordenes/$idOrden/envio" -Method Post
$envioRepetido = Invoke-RestMethod -Uri "$base/api/ordenes/$idOrden/envio" -Method Post
[pscustomobject]@{
    Guia = $envio.guia
    Estado = $envio.estado
    MismoEnvio = ($envio.id -eq $envioRepetido.id)
}
```

`MismoEnvio` debe ser `True`. Después actualicen un estado:

```powershell
$cambio = @{ estado = 'DESPACHADO' } | ConvertTo-Json
Invoke-RestMethod -Uri "$base/api/envios/$($envio.id)/estado" -Method Patch -ContentType 'application/json' -Body $cambio |
    Select-Object guia, estado, actualizado
```

Vuelvan al seguimiento y pulsen **Rastrear envío** para mostrar el evento nuevo. Realicen esta operación sobre la guía de la compra recién creada, que debe estar en PREPARANDO. No usen la guía histórica de ejemplo ni salten directamente a ENTREGADO.

### Integrante 6 · Comparar el stock de la compra rechazada

Antes de confirmar la segunda compra:

```powershell
$stockAntesRechazo = (Invoke-RestMethod -Uri "$base/api/productos/$productoId").stock
```

En la página agreguen una unidad, completen datos ficticios y elijan **Rechazar pago**. Cuando aparezca RECHAZADA:

```powershell
$stockDespuesRechazo = (Invoke-RestMethod -Uri "$base/api/productos/$productoId").stock
[pscustomobject]@{
    StockAntes = $stockAntesRechazo
    StockDespues = $stockDespuesRechazo
    StockConservado = ($stockAntesRechazo -eq $stockDespuesRechazo)
}
```

`StockConservado` debe ser `True`. Para que la comparación sea clara, eviten otras compras simultáneas del mismo artículo durante la grabación.

## Variante del integrante 2 si se graba sin Docker

En esta PC la revisión anterior se realizó con Java, PostgreSQL y Nginx nativos. No se debe presentar esa ejecución como si fueran ocho contenedores.

El diagrama del README representa el despliegue completo definido en Compose. Para una ejecución nativa, aclaren que los servicios Java son procesos separados y que las tres bases pueden residir en una misma instalación PostgreSQL. Mantengan la explicación de responsabilidades y de comunicación HTTP.

Sustituyan el párrafo que comienza “Ahora ejecutamos el sistema” por:

> Ahora mostramos la ejecución nativa. Están funcionando las cuatro aplicaciones Java en procesos diferentes y Nginx sirve la interfaz en el puerto 8088. Tenemos tres bases PostgreSQL separadas. Consultamos la salud de cada servicio y abrimos la tienda. Docker Compose está incluido como otra forma de despliegue, pero esta grabación muestra la aplicación ejecutándose directamente en el equipo.

Y sustituyan la frase siguiente sobre Dockerfiles por:

> Los Dockerfiles incluidos permiten compilar y empaquetar cada aplicación para un despliegue con contenedores. La evidencia del estilo arquitectónico que mostraremos ahora es la separación de procesos, responsabilidades y datos.

Para comprobar en vivo los servicios nativos, con el sistema ya iniciado:

```powershell
$servicios = [ordered]@{
    Gateway = 8080
    Productos = 8081
    Ordenes = 8082
    Envios = 8083
}
foreach ($servicio in $servicios.GetEnumerator()) {
    $salud = Invoke-RestMethod -Uri "http://localhost:$($servicio.Value)/actuator/health"
    [pscustomobject]@{
        Servicio = $servicio.Key
        Puerto = $servicio.Value
        Estado = $salud.status
    }
}
```

Abran la dirección donde se esté sirviendo el frontend: 8088 si usan Nginx o 5173 si usan Vite. Ajusten `$base` en el anexo. En modo Vite no describan Nginx como parte del recorrido que se está ejecutando.

## Correspondencia con la guía del curso

| Requisito | Dónde se cumple |
| --- | --- |
| Caso práctico acotado que evidencie el estilo | Integrante 1: compra de un artículo, envío y rechazo de pago |
| Diagrama real con componentes, relaciones y tecnologías | Integrante 2 y diagramas del README |
| Aplicación funcional y tecnologías justificadas | Integrante 1 y ejecución de los integrantes 3 a 6 |
| Recorrido en vivo e interacción entre componentes | Arranque, carrito, orden, inventario, guía y cambio de estado |
| Justificación del estilo más allá del nombre | Integrantes 2, 4, 5 y cierre del 6: procesos, APIs y datos separados |

## Precisiones para responder preguntas del docente

- Hay **tres servicios de negocio** y una **cuarta aplicación Java que es el gateway**. El frontend y PostgreSQL completan el despliegue; no son otros servicios de negocio.
- El carrito y los identificadores consultados se guardan en el navegador. Las órdenes, reservas y envíos se guardan en las bases del servidor.
- El frontend también solicita la guía automáticamente después de una orden pagada. El botón de Mis pedidos permite generarla o recuperar la existente.
- Las operaciones entre servicios usan HTTP/JSON de forma síncrona. No se implementaron colas, eventos, descubrimiento dinámico de servicios ni una pasarela bancaria.
- Spring JDBC ejecuta SQL; el proyecto no utiliza un ORM JPA.
- Separar servicios no significa eliminar dependencias: una compra necesita al catálogo disponible. El sistema contempla reintentos con el mismo UUID, pero no una transacción distribuida ni conciliación automática.
- La recuperación ante una respuesta perdida se verificó en la revisión funcional local con un intermediario temporal. Ese intermediario no es un componente del producto ni hace falta para esta exposición.
- La configuración Docker existe, pero su ejecución debe comprobarse en el equipo que se usará para grabar. No conviertan una validación de archivos Compose en una afirmación de que los contenedores ya se ejecutaron.
