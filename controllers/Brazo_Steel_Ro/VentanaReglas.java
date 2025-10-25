import javax.swing.*;   // Importa componentes gráficos como JFrame, JTextArea, JScrollPane
import java.awt.*;      // Importa layouts y configuraciones visuales como BorderLayout

// Clase que extiende JFrame para crear una ventana gráfica
public class VentanaReglas extends JFrame {
      private static final long serialVersionUID = 1L;
    // area de texto donde se mostrarán las reglas activadas
    private JTextArea area;

    // Constructor: configura la ventana
    public VentanaReglas() {
        setTitle("Reglas Difusas Activadas"); // Título de la ventana
        setSize(500, 400);                    // Tamaño de la ventana en píxeles
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la ventana al salir

        area = new JTextArea();              // Crea el área de texto
        area.setEditable(false);            // No se puede escribir manualmente

        // Agrega el área de texto dentro de un scroll (por si hay muchas reglas)
        add(new JScrollPane(area), BorderLayout.CENTER);

        setVisible(true);                   // Muestra la ventana en pantalla
    }

    // Método para agregar una regla activada al área de texto
    public void mostrarRegla(String regla) {
        area.append(regla + "\n");          // Agrega la regla con salto de línea
    }

    // Método para limpiar el área de texto (por ejemplo, al iniciar un nuevo ciclo)
    public void limpiar() {
        area.setText("");                   // Borra todo el contenido del área
    }
}
