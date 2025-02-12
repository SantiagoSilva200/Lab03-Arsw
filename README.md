# Lab03-Arsw

### 1 punto. 

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

El elevado consumo de CPU en este programa se debe principalmente a la implementación de la clase Consumer. Dicha clase ejecuta un bucle infinito (while (true)) que verifica constantemente 
la presencia de elementos en la cola para su procesamiento. Sin embargo, debido a la ausencia de un mecanismo de sincronización o espera, el hilo del consumidor permanece en ejecución continua, 
lo que provoca un uso excesivo de los recursos de la CPU. Esto se conoce como “busy-waiting” (ocurre cuando un hilo consume ciclos de CPU innecesariamente mientras espera la llegada de nuevos elementos a la cola).



El programa se ejecutó durante 2 minutos y 35 segundos, registrando un uso de CPU del 6.2%. Este porcentaje es relativamente bajo, lo que sugiere que el proceso no ha generado una carga 
significativa en el sistema durante este tiempo. Sin embargo, si la ejecución se prolongara o si el programa utilizara más hilos en estado de busy-waiting, el consumo de CPU podría aumentar considerablemente.


2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. 
Verifique con JVisualVM que el consumo de CPU se reduzca.


Para las dos clases (Producer y Consumer) se realizaron los siguientes cambios: 
        Se añadió sincronización (synchronized) para que el acceso a la cola (queue) está sincronizado.
        Se añadió wait() y notify(): El Consumer ahora usa queue.wait() para esperar cuando la cola está vacía y el Producer usa queue.notify() para avisar al Consumer cuando hay un nuevo elemento en la cola.

Esto se hizo con el fin de evitar el "busy-waiting":

        En la versión anterior, el Consumer estaba en un bucle infinito verificando constantemente si la cola tenía elementos (if (queue.size() > 0)). Esto consumía mucha CPU innecesariamente.
        Con wait() y notify(), el Consumer entra en estado de espera y no consume CPU mientras la cola está vacía.



Ahora el uso del CPU disminuyo radicalmente, esto significa que la parte del código que realizaba un bucle infinito con alto uso de CPU, se optimizo correctamente recudiendo su uso. 

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. 
Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.





Hicimos cambios en las clases de Producer, y StartProduction: 
    Verificamos si la cola está llena (while (queue.size() >= stockLimit)) antes de agregar un nuevo elemento.
    Usamos wait() para bloquear el productor si la cola está llena, evitando el consumo alto de CPU.
    Usamos notify() después de agregar un elemento para despertar al consumidor. Cambiamos Thread.sleep(1000) a Thread.sleep(100) para que el productor produzca más rápido.
    Agregamos long stockLimit = 5L; que representa el límite de stock que el productor no debe exceder al agregar elementos a la cola. 



### 3 punto:

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores 
siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, ¿cuál debería ser este valor?.


El valor total de los puntos de vida de todos los jugadores en el juego depende del número de inmortales (N). Si cada jugador tiene 100 puntos de vida al inicio, 
la suma total de salud será igual a N * 100. Este valor se mantendrá constante a menos que se realicen cambios en los puntos de vida o en la cantidad de jugadores.

3.  Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

No, como se menciono en el punto anterior, debido a que la sumatoria de vida de vida de todos los jugadores deberia ser N (numero de inmortales) en este caso 3, * 100, pero en este caso es 640. 


4.  
