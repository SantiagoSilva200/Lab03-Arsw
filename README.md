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


4.  Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos 
modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

Realizamos adiciones en el codigo, creando dos metodos en la clase Immortal: 

```yaml 
public static void pauseAll() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public static void resumeAll() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
```

Y en ControlFrame, simplemente hacemos los llamados a estos metodos de la clase con Immortal.resumeAll() y Immortal.pauseAll()

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

No, sigue sin cumplirse el invariante despues de las implementaciones anteiores. 

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. 
Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

Modificamos asi el metodo de pelea en la clase Immortal: 

```yaml 
public void fight(Immortal i2) {
        Immortal first = this;
        Immortal second = i2;

        if (System.identityHashCode(this)> System.identityHashCode(i2)){
            first=i2;
            second =this;
        }

        synchronized (first) {
            synchronized (second) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                }
            }
        }
    }
```
Esto con el fin de evitar las condiciones de carrera al modificar la salud de los inmortales, asegurando que un solo hilo pueda modificar la salud de los inmortales. 

Gracias a esto, ahora si se cumple el invariante:


7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

Tras hacer la implementacion anterior, usamos el comando "jps" e identificamos el numero que representaba el programa del codigo (ControlFrame), ejecutamos jstack, para ver los deadlock o problemas de la 
implementacion y tras realizar la lectura de lo que arrojaba el comando, no encontramos nada referenciado a deadlocks. Sin embargo, para asegurarnos, escribimos el comando "jstack -l 21864 | findstr -i deadlock"
y tampoco encontramos ningun error de deadlock. 


9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. 
Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

11. Para finalizar, implemente la opción STOP.

Cuando se presiona el boton STOP, ahora detiene la simulacion y permite iniciar de nuevo. 

```yaml 
btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                immortals.clear();
                btnStart.setEnabled(true);
            }
        });
```









Para 100,1000 y 10000 el programa sigue siendo consistente (aunque cuando probamos con 10000, el programa se demoro en empezar, sin emabargo, cuando se ejecutaban las peleas, fue consistente). Tambien el 
invariante seguia funcionando para todos los casos. 


