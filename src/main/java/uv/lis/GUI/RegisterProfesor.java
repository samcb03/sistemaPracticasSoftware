package uv.lis.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.exceptions.OperationException;

public class RegisterProfesor extends JFrame {

    private JTextField txtId;
    private JTextField txtNumeroPersonal;
    private JTextField txtNombre;
    private JTextField txtApellidos;
    private JCheckBox chkCoordinador;
    private JButton btnRegistrar;

    private ProfessorDAO professorDAO;

    public RegisterProfesor() {
        professorDAO = new ProfessorDAO();

        setTitle("Registro de Profesor");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        // Componentes
        add(new JLabel("ID Usuario:"));
        txtId = new JTextField();
        add(txtId);

        add(new JLabel("Número Personal:"));
        txtNumeroPersonal = new JTextField();
        add(txtNumeroPersonal);

        add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        add(txtNombre);

        add(new JLabel("Apellidos:"));
        txtApellidos = new JTextField();
        add(txtApellidos);

        add(new JLabel("¿Coordinador?"));
        chkCoordinador = new JCheckBox();
        add(chkCoordinador);

        btnRegistrar = new JButton("Registrar");
        add(btnRegistrar);

        // Acción del botón
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarProfesor();
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RegisterProfesor().setVisible(true);
        });
    }
}

