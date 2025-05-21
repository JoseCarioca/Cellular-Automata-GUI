# Simulador de Autómatas Celulares Unidimensionales con GUI en Java

¡Bienvenido al Simulador de Autómatas Celulares ideados por Stepehn Wolfram! Esta aplicación permite explorar la fascinante dinámica de los autómatas celulares unidimensionales, configurando diversas reglas, condiciones iniciales y otros parámetros a través de una interfaz gráfica de usuario (GUI) construida con Java Swing.

## 📸 Capturas


[Captura con 3 estados (ejemplo propuesto)](img/Estados3.png)
[Captura con 7 estados](img/Estados7.png)

## ✨ Características Principales

*   **Configuración de Estados:** Define el número de estados posibles para cada célula (de 2 a 10, los colores se repiten para >10).
*   **Reglas de Transición Personalizables:** Ingresa la regla del autómata en base 10. La aplicación muestra su conversión a la base del número de estados (para bases <= 10).
*   **Condiciones Iniciales:**
    *   **Central:** Una única célula activa en el centro.
    *   **Aleatoria:** Generación inicial basada en una semilla y un generador pseudoaleatorio.
*   **Condiciones de Frontera:**
    *   **Nula:** Los bordes se consideran células con estado 0.
    *   **Cilíndrica (Periódica):** El borde derecho se conecta con el izquierdo.
*   **Control de Generaciones:** Especifica el número de generaciones a simular (máximo `MAX_GENERATIONS`, actualmente 500).
*   **Generadores Pseudoaleatorios (PRNG):** Elige entre varios algoritmos para la generación aleatoria:
    *   `java.util.Random`
    *   `26.3` (LCG específico)
    *   `Fishman-Moore` (LCG)
    *   `Randu` (LCG, ¡conocido por sus defectos!)
*   **Semilla Personalizable:** Introduce una semilla para la generación de números aleatorios, permitiendo replicar simulaciones.
*   **Visualización Dinámica:** El autómata se dibuja en un canvas, y este se ajusta al redimensionar la ventana (pulsando "Guardar" tras redimensionar).
*   **Controles de Simulación:**
    *   **Siguiente:** Avanza una generación.
    *   **Ejecutar:** Simula todas las generaciones configuradas.
    *   **Reiniciar:** Restablece la simulación a la generación 0 con la configuración actual.
*   **Menú de Ayuda:** Información básica sobre el uso de la aplicación.

## 🛠️ Tecnologías Utilizadas

*   **Java:** Lenguaje de programación principal.
*   **Java Swing:** Para la interfaz gráfica de usuario (GUI).
*   **Java AWT:** Para gráficos y manejo de eventos.

## 🚀 Cómo Ejecutar

### Requisitos Previos

*   **Java Runtime Environment (JRE) versión 21 o superior.** Puedes verificar tu versión de Java abriendo una terminal y escribiendo `java -version`. Si necesitas instalar o actualizar Java, puedes descargarlo desde [Oracle](https://www.oracle.com/java/technologies/downloads/) o [Adoptium (Temurin)](https://adoptium.net/).

### Opción 1: Usando el archivo JAR (Recomendado)
<!-- [**Releases**](https://github.com/JoseCarioca/TU_NOMBRE_DE_REPOSITORIO/releases) -->
1.  Descarga el archivo `ca1DSimGUI.jar` de la sección de (link en proceso) de este repositorio.
2.  Una vez descargado, ejecuta el JAR desde la línea de comandos:
    ```bash
    java -jar ca1DSimGUI.jar
    ```
    (En algunos sistemas, también puedes hacer doble clic en el archivo `.jar`).

### Opción 2: Compilando desde el Código Fuente

1.  Clona este repositorio o descarga el archivo `ca1DSimGUI.java`.
2.  Asegúrate de tener Java Development Kit (JDK) instalado.
3.  Abre una terminal o línea de comandos en el directorio donde guardaste el archivo.
4.  Compila el código:
    ```bash
    javac ca1DSimGUI.java
    ```
5.  Ejecuta la aplicación:
    ```bash
    java ca1DSimGUI
    ```

## 📖 Guía Rápida de Uso

1.  **Configura los Parámetros (Panel Derecho):**
    *   **Nº estados por célula:** Introduce un número (ej. 2, 3).
    *   **Función de transición:** Escribe la regla en base 10 (ej. 30, 110 para 2 estados). Observa la conversión.
    *   **Configuración:** Selecciona "Aleatoria" o "Central".
    *   **Condición Frontera:** Selecciona "Nula" o "Cilíndrica".
    *   **Nº Generaciones:** Define cuántas filas se dibujarán.
    *   **Generador:** Si elegiste configuración aleatoria, selecciona un PRNG.
    *   **Semilla:** Introduce una semilla para la configuración aleatoria.
2.  **Guarda los Parámetros:** Haz clic en el botón "**Guardar**". Esto aplica la configuración y prepara el canvas.
    *   *Nota:* Si redimensionas la ventana, pulsa "Guardar" de nuevo para que el canvas se ajuste al nuevo tamaño.
3.  **Inicia la Simulación (Panel Izquierdo - Botonera Superior):**
    *   **Siguiente:** Dibuja la siguiente generación.
    *   **Ejecutar:** Dibuja todas las generaciones hasta el límite configurado.
    *   **Reiniciar:** Limpia el canvas y reinicia el contador de generaciones para volver a empezar con la configuración actual.
4.  **Explora:** ¡Experimenta con diferentes reglas y parámetros para descubrir patrones emergentes!

## 📝 Comentarios del Desarrollador

Este proyecto fue desarrollado como una exploración práctica de los autómatas celulares y la creación de interfaces gráficas en Java. El generador `Randu` se incluye con fines educativos, dada su conocida historia de producir números pseudoaleatorios de baja calidad.

El cálculo del índice para la función de transición `fTransicion` a partir de la suma de los vecinos (`posiblesEstados - 1 - (suma_vecinos) % posiblesEstados`) es una implementación particular para este simulador.

## 👨‍💻 Autor

*   **Jose Carioca** - [Link al perfil](https://github.com/JoseCarioca)

## 📄 Licencia

Este proyecto es de código abierto. Siéntete libre de usarlo y modificarlo.
