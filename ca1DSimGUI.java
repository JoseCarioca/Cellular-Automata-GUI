import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;
import static java.lang.Math.pow;

/**
 * 18/03/2025 se añade el redimensionado del canvas ajustando el tamaño
 * del autómata a la pantalla
 */

public class ca1DSimGUI extends JFrame {

    // --- Generador pseudoaleatorio ---
    public static class randomGenerator {
        private long seed;
        private Random javaRandom;
        private final String seleccionado;

        public randomGenerator(String s) {
            setSeed(System.nanoTime());
            seleccionado = s;
        }

        public randomGenerator(long s, String seleccionado) {
            setSeed(s);
            this.seleccionado = seleccionado;
        }

        public int generar(int n) {
            return switch (seleccionado) {
                case "util.Random" -> javarandom(n);
                case "26.3" -> generar263(n);
                case "Fishman-Moore" -> fishmanMoore(n);
                case "Randu" -> randu(n);
                default -> javarandom(n);
            };
        }

        public int generar263(int n) {
            if (n <= 0) n = 1;
            seed = (seed * 16807) % 2147483647;
            return (int) ((seed / 2147483647.0) * n);
        }

        public int fishmanMoore(int n) {
            if (n <= 0) n = 1;
            seed = (seed * 48271) % 2147483647;
            return (int) ((seed / 2147483647.0) * n);
        }

        public int randu(int n) {
            if (n <= 0) n = 1;
            seed = (65539 * seed) % 2147483647;
            return (int) ((seed / 2147483647.0) * n);
        }

        public int javarandom(int n) {
            if (n <= 0) n = 1;
            return javaRandom.nextInt(n);
        }

        public void setSeed(Long newSeed) {
            this.seed = Objects.requireNonNullElseGet(newSeed, System::nanoTime);
            this.javaRandom = new Random(seed);
        }

        public long getSeed() {
            return seed;
        }
    }

    // --- Canvas para dibujar el autómata ---
    private static class CanvasAutomata extends JPanel {
        // Gama de colores para la simulación
        public Color[] colors = {Color.WHITE, Color.BLACK, Color.GREEN, Color.BLUE,
                Color.RED, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK};

        private final BufferedImage canvas;
        private final Graphics2D canvasGraphics;
        private int currentLine; // Posición vertical donde se dibuja la siguiente generación
        private final int tamCelda;

        public CanvasAutomata(int width, int height, int tamCelda) {
            canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            canvasGraphics = canvas.createGraphics();
            // Rellenar el fondo de blanco
            canvasGraphics.setColor(Color.WHITE);
            canvasGraphics.fillRect(0, 0, width, height);
            currentLine = 0;
            this.tamCelda = tamCelda;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Se dibuja la imagen completa
            g.drawImage(canvas, 0, 0, null);
        }

        // Dibuja una generación (fila) en el canvas y actualiza la posición
        public void drawGen(int[] data) {
            for (int i = 0; i < data.length; i++) {
                // Se usa el valor de data para seleccionar un color usando el mapeo (invertido)
                canvasGraphics.setColor(colors[data[i] % colors.length]); //para Estados >10 se repiten colores
                canvasGraphics.fillRect(i * tamCelda, currentLine, tamCelda, tamCelda);
            }
            currentLine += tamCelda;
            repaint();
        }

    }

    // --- Parámetros del autómata ---
    private static int WIDTH = 400; // número de celdas
    private static final int MAX_GENERATIONS = 500;
    private static final int CELL_SIZE = 2;
    private int[] data, data2, temp;
    private int contador_generacion = 0;
    private randomGenerator _randomGenerator;

    // --- Componentes de la UI ---
    JPanel panelIzquierdo;
    private CanvasAutomata canvasAutomata;
    
    private final JButton guardarBoton;
    private String seleccionado;
    private final JTextField semillaTexto, estadoText, reglaText, nGensText;
    private final JLabel funcionLabel, conversionLabel;
    
    private int nEstados, posiblesEstados, nGens = 200; // parámetros extraídos de los JTextField
    private long nRegla;
    private final int nVecinos = 1;
    private int[] fTransicion; // función de transición en base k (representación de la regla)
    private long semilla;

    private final JRadioButton opAleatoria, opCentral, opNula, opCilindrica,
            aleatorioRandu, aleatorio263, aleatorioFishmanMoore, aleatorioJava;
    private boolean bCilindrica, bAleatoria; // opciones seleccionadas


    public ca1DSimGUI() {
        // Inicialización de la ventana
        super("Automata Celular Unidimensional");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null); // Centra la ventana
        setLayout(new BorderLayout());

        // --- Menú (para ayuda e instrucciones de uso ---
        JMenuBar menuBar = getBar();
        setJMenuBar(menuBar);

        // --- Panel izquierdo: contendrá la botonera y el canvas ---
        panelIzquierdo = new JPanel();
        panelIzquierdo.setPreferredSize(new Dimension(750, 600));
        panelIzquierdo.setBorder(BorderFactory.createTitledBorder("Simulaciones Gráficas"));
        panelIzquierdo.setLayout(new BorderLayout());

        // --- Panel derecho: controles y parámetros ---
        JPanel panelDerecho = new JPanel();
        panelDerecho.setPreferredSize(new Dimension(300, 600));
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Controles"));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS)); // Disposición vertical
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10)); // Márgenes

        // Panel de "Estados" (número de estados por célula: [2, 10])
        JPanel panelEstados = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstados.add(new JLabel("Nº estados por célula [2, 10]:"));
        estadoText = new JTextField("3", 5);
        // Se establece un tamaño mínimo para evitar que se corte
        estadoText.setMinimumSize(new Dimension(50, 25));
        panelEstados.add(estadoText);
        contenido.add(panelEstados);

        // Panel de "Función de transición"
        // Para nEstados = 2, se espera una regla en [0,16) (es decir, 0 a 15)
        JPanel panelFuncion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel funcionFixedLabel = new JLabel("Función de transición");
        funcionLabel = new JLabel("[0,3^7) (escribe en base 10):");
        panelFuncion.add(funcionFixedLabel);
        panelFuncion.add(funcionLabel);
        reglaText = new JTextField("777", 10);
        reglaText.setMinimumSize(new Dimension(50, 25));
        conversionLabel = new JLabel(" -> base 3: 100"); //" -> base " + nEstados + ": "+ resultado
        panelFuncion.add(reglaText);
        panelFuncion.add(conversionLabel);
        contenido.add(panelFuncion);

        // Actualiza la etiqueta de conversión según la regla escrita
        reglaText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLabelRegla();  }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLabelRegla(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLabelRegla(); }




        });

        // Actualiza la etiqueta de función según el número de estados
        estadoText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLabelEstado(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLabelEstado(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLabelEstado(); }

        });

        // Panel de configuración: opción "Aleatoria" o "Central"
        JPanel panelConfig = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelConfig.add(new JLabel("Configuración:"));
        opAleatoria = new JRadioButton("Aleatoria");
        opCentral = new JRadioButton("Central");
        ButtonGroup grupoConf = new ButtonGroup();
        grupoConf.add(opAleatoria);
        grupoConf.add(opCentral);
        panelConfig.add(opAleatoria);
        panelConfig.add(opCentral);
        opCentral.setSelected(true);
        contenido.add(panelConfig);

        // Panel de condición de frontera: "Nula" o "Cilindrica"
        JPanel panelFrontera = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFrontera.add(new JLabel("Condición Frontera:"));
        opNula = new JRadioButton("Nula");
        opCilindrica = new JRadioButton("Cilindrica");
        ButtonGroup grupoFrontera = new ButtonGroup();
        grupoFrontera.add(opNula);
        grupoFrontera.add(opCilindrica);
        panelFrontera.add(opNula);
        panelFrontera.add(opCilindrica);
        opCilindrica.setSelected(true);
        contenido.add(panelFrontera);

        // Panel de generaciones: se acepta un valor entre 1 y MAX_GENERATIONS )
        JPanel panelGeneraciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelGeneraciones.add(new JLabel("Nº Generaciones [1," + MAX_GENERATIONS + "]:"));
        nGensText = fixedTextField("200", 10);
        panelGeneraciones.add(nGensText);
        contenido.add(panelGeneraciones);

        //Listener to validate the input
        nGensText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }

            private void validateInput() {
                try {
                    int value = Integer.parseInt(nGensText.getText().trim());
                    guardarBoton.setEnabled(value >= 1 && value <= MAX_GENERATIONS);
                } catch (NumberFormatException ex) {
                    guardarBoton.setEnabled(false); // Disable if input is invalid
                }
            }
        });

        // Panel para selección de generador pseudoaleatorio
        JPanel panelAleatorio = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelGeneraciones.add(new JLabel("Generador:"));
        aleatorioRandu = new JRadioButton("Randu");
        aleatorio263 = new JRadioButton("26.3");
        aleatorioFishmanMoore = new JRadioButton("Fishman-Moore");
        aleatorioJava = new JRadioButton("util.Random");

        ButtonGroup grupoAleatorio = new ButtonGroup();
        grupoAleatorio.add(aleatorioRandu);
        grupoAleatorio.add(aleatorio263);
        grupoAleatorio.add(aleatorioFishmanMoore);
        grupoAleatorio.add(aleatorioJava);
        panelAleatorio.add(aleatorioRandu);
        panelAleatorio.add(aleatorio263);
        panelAleatorio.add(aleatorioFishmanMoore);
        panelAleatorio.add(aleatorioJava);
        aleatorioRandu.setSelected(true);
        contenido.add(panelAleatorio);

        // Panel de semilla
        JPanel panelSemilla = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSemilla.add(new JLabel("Semilla:"));
        semillaTexto = new JTextField("1", 5);
        semillaTexto.setMinimumSize(new Dimension(50, 25));
        panelSemilla.add(semillaTexto);
        contenido.add(panelSemilla);

        // Botón para guardar parámetros (se asigna un tamaño mínimo)
        guardarBoton = new JButton("Guardar");
        guardarBoton.setMinimumSize(new Dimension(80, 30));
        contenido.add(guardarBoton);
        panelDerecho.add(contenido, BorderLayout.CENTER);

        // --- Panel Izquierdo: Botonera y Canvas ---
        panelIzquierdo.setLayout(new BorderLayout());
        crearControlesAutomata();
        // Se crea un canvas con ancho basado en CELL_SIZE * WIDTH y alto en (nGens * CELL_SIZE)
        canvasAutomata = new CanvasAutomata(CELL_SIZE * WIDTH, nGens * CELL_SIZE, CELL_SIZE);
        panelIzquierdo.add(canvasAutomata, BorderLayout.CENTER);


        // Se agregan los paneles a la ventana principal
        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.EAST);

        // Listener para el botón "Guardar" (guarda parámetros, reinicia la simulación y el canvas)
        guardarBoton.addActionListener(e -> {
            guardarParametros();

            // Reinicializa el canvas para ajustarse a la nueva configuración
            panelIzquierdo.remove(canvasAutomata);
            int nuevasAltura = nGens * CELL_SIZE;
            int panelIzquierdoWidth = getWidth() - panelDerecho.getWidth() - 20; // Deja un pequeño margen
            WIDTH = (int)((panelIzquierdoWidth - 10)/ CELL_SIZE);
            panelIzquierdo.setPreferredSize(new Dimension(panelIzquierdoWidth, nuevasAltura));
            canvasAutomata = new CanvasAutomata(CELL_SIZE * WIDTH, nuevasAltura, CELL_SIZE);
            panelIzquierdo.add(canvasAutomata, BorderLayout.CENTER);
            panelIzquierdo.revalidate();
            panelIzquierdo.repaint();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int height = getHeight(); // Alto total de la ventana
                int panelIzquierdoWidth = getWidth() - panelDerecho.getWidth() - 20; // Deja un pequeño margen

                panelIzquierdo.setPreferredSize(new Dimension(panelIzquierdoWidth, height));
                panelIzquierdo.revalidate();
                panelIzquierdo.repaint();
            }
        });
        
    }

    // -- Funciones auxiliares genericas --

    //Crea la barra de Mneu y sus funcionalidades
    private JMenuBar getBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de");
        menuAyuda.add(itemAcercaDe);
        itemAcercaDe.addActionListener(e -> {
            // Se muestra un mensaje emergente temporal con los parámetros guardados
            String mensaje = """
                    Reglas básicas de uso:
                    OJO: si redimensionas la pantalla pulsa 'Guardar' para ajustar el canvas al nuevo tamaño.
                    Rango para Nº estados: >2 (para >10 los colores se repiten).
                    Función de transición (regla): el conversor a base 'Nº estados' no se mostrará para bases mayores a 10\s
                    Nº generaciones: hasta un maximo de 200.
                     \
                    Configuración aleatoria: se crea a partir de la semilla y el generador seleccionado.
                     \
                    Condición frontera (cilíndrica o nula).
                    \s""";
            mostrarMensajeTemporal(mensaje, 0, 500, 200);
        });

        menuBar.add(menuAyuda);
        return menuBar;
    }

    // Elimina ceros a la izquierda del array
    private String limpiarCeros(int[] array) {
        StringBuilder sb = new StringBuilder();
        boolean leadingZero = true;
        for (int num : array) {
            if (num != 0) {
                leadingZero = false;
            }
            if (!leadingZero) {
                sb.append(num);
            }
        }
        return !sb.isEmpty() ? sb.toString() : "0";
    }

    // Listeners de Textos para actualizar contenido

    //Logica de Listener para actualizar conversor de base por pantalla
    private void updateLabelRegla() {
        if (!Objects.equals(estadoText.getText(), "")) {
            nEstados = Integer.parseInt(estadoText.getText().trim());

            try {
                nRegla = Long.parseLong(reglaText.getText().trim());
                posiblesEstados = (nVecinos + 2) * nEstados - (nVecinos + 1);
                if (nRegla >= 0 && nRegla < pow(nEstados, posiblesEstados)) {
                    if (nEstados < 10) {
                        fTransicion = cambioBase(nRegla, nEstados, posiblesEstados);
                        String resultado = limpiarCeros(fTransicion);
                        conversionLabel.setText(" -> base " + nEstados + ": "+ resultado);
                    } else if (nEstados == 10){
                        conversionLabel.setText(" -> base " + nEstados + ": "+ nRegla);
                    } else {
                        conversionLabel.setText(" -> base " + nEstados + ": (no se muestra)\n");
                    }

                    guardarBoton.setEnabled(true);
                } else {
                    guardarBoton.setEnabled(false);
                    conversionLabel.setText(" -> base " + nEstados + ": _");
                }
            } catch (NumberFormatException ex) {
                conversionLabel.setText("_:");
            }
        }else conversionLabel.setText("_:");

    }

    //Logica de Listener para JTextField de Nº Estados
    private void updateLabelEstado() {
        String text = estadoText.getText();
        try {
            int numEstados = Integer.parseInt(text);
            if (numEstados > 1) {
                guardarBoton.setEnabled(true);
                funcionLabel.setText("[0, " + numEstados + "^" + (3*numEstados -2) + "):\n (escribe en base 10):");
            } else {
                guardarBoton.setEnabled(false);
                funcionLabel.setText("___:");
            }
        } catch (NumberFormatException ex) {
            funcionLabel.setText("___:");
        }

        updateLabelRegla();
    }


    // Convierte un número a una base dada, devolviendo un arreglo con longitud especificada (rellenando con ceros)
    private int[] cambioBase(long numero, int b, int longitud) {
        int[] nuevo = new int[longitud];
        for (int i = longitud - 1; i >= 0; i--) {
            nuevo[i] = (int) numero % b;
            numero = numero / b;
        }
        return nuevo;
    }

    // Inicializa el arreglo de la primera generación
    private void initRow() {
        data = new int[WIDTH];  // Inicializado en 0 por defecto
        data2 = new int[WIDTH];
        if (bAleatoria) {
            _randomGenerator.setSeed(semilla); //recomponer semilla para replicar generaciones
            for (int i = 0; i < WIDTH; i++) {
                data[i] = _randomGenerator.generar(nEstados);
            }
        } else {
            // Configuración central: se activa la célula central
            data[WIDTH / 2] = 1;
        }
    }

    // Crea la botonera (panel con controles de ejecución) y asigna sus listeners
    private void crearControlesAutomata() {
        JPanel botoneraPanel = new JPanel();
        botoneraPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Se crean los botones y se establece un tamaño mínimo para evitar que se corten
        JButton siguiente = new JButton("Siguiente");
        siguiente.setMinimumSize(new Dimension(80, 30));
        JButton ejecutar = new JButton("Ejecutar");
        ejecutar.setMinimumSize(new Dimension(80, 30));
        JButton reiniciar = new JButton("Reiniciar");
        reiniciar.setMinimumSize(new Dimension(80, 30));


        // Listener para el botón "Siguiente"
        siguiente.addActionListener(e -> siguienteAction());
        
        // Listener para "Ejecutar": ejecuta múltiples generaciones
        ejecutar.addActionListener(e -> {
            if (contador_generacion == 0) initRow();
            if (data == null || posiblesEstados < 1) { 
                JOptionPane.showMessageDialog(null, "Error: Guarda los datos primero.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                for (int i = contador_generacion; i < nGens; i++) {
                    siguienteAction();
                }
            }
        });

        // Listener para "Reiniciar"
        reiniciar.addActionListener(e -> reiniciarAction());

        botoneraPanel.add(siguiente);
        botoneraPanel.add(ejecutar);
        botoneraPanel.add(reiniciar);

        // Se agrega la botonera al panel izquierdo, en la parte superior (NORTH)
        panelIzquierdo.add(botoneraPanel, BorderLayout.NORTH);
    }

    // --- Métodos esqueléticos de los botones de la botonera ---
    private void siguienteAction() {
        if (contador_generacion == 0) initRow();
        if (data == null) { // ✅ Correct way to check for null in Java
            JOptionPane.showMessageDialog(null, "Error: Guarda los datos primero.", "Error", JOptionPane.ERROR_MESSAGE);
        } else{
            if (contador_generacion == 0) {
                canvasAutomata.drawGen(data);
                contador_generacion++;
            } else if (contador_generacion <= nGens) {
                // Calcula la siguiente generación y actualiza la visualización
                calculaHijosData();
                temp = data;
                data = data2;
                data2 = temp;
                canvasAutomata.drawGen(data);
            }
        }

    }

    private void reiniciarAction() {
        contador_generacion = 0;
        initRow();
        // Se reinicializa el canvas
        panelIzquierdo.remove(canvasAutomata);
        int nuevasAltura = Integer.parseInt(nGensText.getText()) * CELL_SIZE;
        canvasAutomata = new CanvasAutomata(CELL_SIZE * WIDTH, nuevasAltura, CELL_SIZE);
        panelIzquierdo.add(canvasAutomata, BorderLayout.CENTER);
        panelIzquierdo.revalidate();
        panelIzquierdo.repaint();
    }

    // Calcula los hijos de data[] según la suma de vecinos y aplica el módulo según posiblesEstados
    private void calculaHijosData() {
        int indice;
        // Para cada célula (excepto los extremos)
        for (int i = 1; i < data.length - 1; i++) {
            indice = posiblesEstados - 1 - (data[i - 1] + data[i] + data[i + 1]) % posiblesEstados;
            data2[i] = fTransicion[indice];
        }
        // Para las condiciones de frontera
        if (bCilindrica) {
            indice = posiblesEstados - 1 - (data[data.length - 1] + data[0] + data[1]) % posiblesEstados;
            data2[0] = fTransicion[indice];
            indice = posiblesEstados - 1 - (data[data.length - 2] + data[data.length - 1] + data[0]) % posiblesEstados;
            data2[data.length - 1] = fTransicion[indice];
        } else { // Frontera nula
            indice = posiblesEstados - 1 - (data[0] + data[1]) % posiblesEstados;
            data2[0] = fTransicion[indice];
            indice = posiblesEstados - 1 - (data[data.length - 2] + data[data.length - 1]) % posiblesEstados;
            data2[data.length - 1] = fTransicion[indice];
        }
    }

    // --- Método para guardar parámetros y configurar la simulación ---
    private void guardarParametros() {
        try {
            // Se leen y convierten los valores de los JTextField
            nEstados = Integer.parseInt(estadoText.getText().trim());
            nRegla = Long.parseLong(reglaText.getText().trim());
            nGens = Integer.parseInt(nGensText.getText().trim());
            contador_generacion = 0;
            posiblesEstados = (nVecinos + 2) * nEstados - (nVecinos + 1);
            // Se guarda la función de transición convirtiendo la regla a la base correspondiente
            fTransicion = cambioBase(nRegla, nEstados, posiblesEstados);
            //System.out.println(Arrays.toString(fTransicion));

            // Configuración: Aleatoria o Central
            if (opAleatoria.isSelected()) {
                bAleatoria = true;
            } else if (opCentral.isSelected()) {
                bAleatoria = false;
            }

            // Condición de frontera: Cilíndrica o Nula
            if (opCilindrica.isSelected()) {
                bCilindrica = true;
            } else if (opNula.isSelected()) {
                bCilindrica = false;
            }

            // Selección del generador pseudoaleatorio
            if (aleatorioRandu.isSelected()) {
                seleccionado = "Randu";
            } else if (aleatorio263.isSelected()) {
                seleccionado = "26.3";
            } else if (aleatorioFishmanMoore.isSelected()) {
                seleccionado = "Fishman-Moore";
            } else if (aleatorioJava.isSelected()) {
                seleccionado = "util.Random";
            }

            // Se lee la semilla y se inicializa el generador
            semilla = Long.parseLong(semillaTexto.getText().trim());
            _randomGenerator = new randomGenerator(semilla, seleccionado);

            // Se muestra un mensaje emergente temporal con los parámetros guardados
            String mensaje = "Parámetros guardados:\n" +
                    "Nº estados: " + nEstados + "\n" +
                    "Función de transición (regla): " + nRegla + "\n" +
                    "Nº generaciones: " + nGens + "\n" +
                    "Configuración aleatoria: " + bAleatoria + "\n" +
                    "Condición frontera (cilíndrica): " + bCilindrica + "\n" +
                    "Generador seleccionado: " + seleccionado + "\n" +
                    "Semilla: " + semilla;
            mostrarMensajeTemporal(mensaje, 2500, 300, 200);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en la conversión de números. Verifique los campos de entrada.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar un mensaje emergente temporal
    private void mostrarMensajeTemporal(String mensaje, int duracion, int w, int h) {
        String mensajeHTML = "<html>" + mensaje.replace("\n", "<br>") + "</html>";
        final JFrame ventana = new JFrame("Información");
        ventana.setSize(w, h);
        ventana.setLocationRelativeTo(this);
        JLabel etiqueta = new JLabel(mensajeHTML, SwingConstants.CENTER);
        ventana.add(etiqueta);
        ventana.setVisible(true);

        // Timer para cerrar la ventana automáticamente
        if (duracion > 0)
        {
            new Timer(duracion, e -> ventana.dispose()).start();
        }
    }

    // --- Método auxiliar para crear un JTextField de tamaño fijo ---
    private JTextField fixedTextField(String text, int cols) {
        JTextField textField = new JTextField(text, cols);
        Dimension dim = textField.getPreferredSize();
        // Se establece un tamaño máximo y mínimo para evitar que se corte
        textField.setMaximumSize(new Dimension(dim.width, dim.height));
        textField.setMinimumSize(new Dimension(50, 25));
        return textField;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ca1DSimGUI gui = new ca1DSimGUI();
            gui.setVisible(true);
        });
    }
}
