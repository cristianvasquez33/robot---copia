import javax.swing.*;
import java.awt.*;

public class VentanaReglas extends JFrame {
    private JTextArea area;

    public VentanaReglas() {
        setTitle("Reglas Difusas Activadas");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        area = new JTextArea();
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
        setVisible(true);
    }

    public void mostrarRegla(String regla) {
        area.append(regla + "\n");
    }

    public void limpiar() {
        area.setText("");
    }
}
