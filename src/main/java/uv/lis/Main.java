package uv.lis;


import java.sql.Date;
import java.util.List;
import java.util.Scanner;
import uv.lis.logic.common.AutoevaluationCommon;
import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProfessorDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectRequestDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.StudentDAO;
import uv.lis.logic.dao.SubjectDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.Autoevaluation;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.MonthlyReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Professor;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.dto.Report;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.AuthenticateException;
import uv.lis.logic.exceptions.OperationException;


public class Main {
public static void main(String[] args) throws AuthenticateException, OperationException {
        Main app = new Main();
        Scanner scanner = new Scanner(System.in);
        User currentUser = app.login(scanner);

        if (currentUser != null) {
            String userType = currentUser.getUserType();
            System.out.println("\nAccediendo como: " + userType);

            if ("Profesor".equals(userType)) {
                app.showProfessorMenu(scanner);
            } else if ("Coordinador".equals(userType)) {
                app.showCoordinatorMenu(scanner);
            } else if ("Estudiante".equals(userType)) {
                app.showStudentMenu(scanner);
            } else if ("Administrador".equals(userType)){
                app.showAdministratorMenu(scanner);
            } else {
                System.out.println("No puede acceder");
            }
        } else {
            System.out.println("Intentalo de nuevo");
        }
    }

    private User login(Scanner scanner) throws AuthenticateException{
        UserDAO userDAO = new UserDAO();
        User loggedUser = null;

        System.out.println("=== SISTEMA DE GESTION DE PROYECTOS LIS ===");
        System.out.println("           Inicio de sesion\n");

        while (loggedUser == null) {
            System.out.print("Identificador: ");
            String identification = scanner.nextLine();

            System.out.print("Contraseña: ");
            String password = scanner.nextLine();

            loggedUser = userDAO.authenticate(identification, password);

            if (loggedUser == null) {
                System.out.println("Identificador/Contraseña incorrectos" + "\n");
            }
        }
        return loggedUser; 
    }

    private void showProfessorMenu(Scanner scanner) {
        int option;
        do {
            System.out.println("1. Evaluar Reporte");
            System.out.println("2. Salir");

            option = scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:
                    ReportDAO reportDAO = new ReportDAO();
                    try {   
                        List<Report> reports = reportDAO.getReports(); 

                        if (reports.isEmpty()) {
                            System.out.println("No hay reportes disponibles.");
                        } else {
                            System.out.println("--- Lista de Reportes ---");
                            for (Report report : reports) {
                                System.out.println("ID: " + report.getId() + " | Descripción: "
                                     + report.getDescription());
                            }
                        }
                        System.out.println("\nEscriba el tipo de reporte a evaluar(Parcial/Final): ");
                        String type = scanner.nextLine();
                        if (type.equals("Final")) {
                            System.out.println("Escriba el ID del reporte final: ");
                            int finalId = scanner.nextInt();
                            FinalReport finalReport = reportDAO.getFinalReportById(finalId);
                            System.out.println("Calificacion (1-10)");
                            scanner.nextInt();
                            reportDAO.evaluationReport(finalReport);
                        } else if (type.equals("Parcial")) {
                            System.out.println("Escriba el ID del reporte parcial: ");
                            int partialId = scanner.nextInt();
                            PartialReport partialReport = reportDAO.getPartialReportById(partialId);
                            System.out.println("Calificacion (1-10)");
                            scanner.nextInt();
                            reportDAO.evaluationReport(partialReport);
                        }
                        scanner.nextInt();
                    } catch (OperationException e) {
                        e.getMessage();
                    }
                    break;
                case 2:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (option != 2);
    }   

    private void showAdministratorMenu(Scanner scanner) throws OperationException {
        UserDAO userDAO = new UserDAO();
        ProfessorDAO professorDAO = new ProfessorDAO();
        int option;
        do {
            System.out.println("1. Registrar profesor");
            System.out.println("2. Consultar profesor por numero de personal");
            System.out.println("3. Salir");

            option = scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:
                    Professor professor = new Professor();
                    System.out.println("Nombre: ");
                    String firstName = scanner.nextLine();
                    System.out.println("Apellido(s): ");
                    String lastName = scanner.nextLine();
                    System.out.println("Contraseña: ");
                    String password = scanner.nextLine();
                    System.out.println("Numero de personal: ");
                    String idProfessor = scanner.nextLine();
                    System.out.println("¿Es coordinador? (si/no): ");
                    String isCoordinatorInput = scanner.nextLine();
                    boolean isCoordinator = isCoordinatorInput.equalsIgnoreCase("si"); 

                    professor.setFirstName(firstName);
                    professor.setLastName(lastName);
                    professor.setPassword(password);
                    professor.setPersonnelNumber(idProfessor);
                    professor.setIsCoordinator(isCoordinator);
                    professor.setUserType(isCoordinator ? "Coordinador" : "Maestro");

                    int generatedId = userDAO.registerUser(professor); 
                    if (generatedId != -1) {
                        professor.setId(generatedId);                   
                        boolean registered = professorDAO.registerProfessor(professor);
                        if (registered) {
                            System.out.println("Profesor registrado con éxito");
                        } else {
                            System.out.println("Error al registrar profesor.");
                        }
                    } else {
                        System.out.println("Error al registrar usuario.");
                    }
                    break;
                case 2:
                    System.out.println("Escriba el numero de personal del profesor a consultar: ");
                    String searchNoPersonal = scanner.nextLine();
                    Professor foundProfessor = professorDAO.getProfessorByPersonalNumber(searchNoPersonal);
                    if (foundProfessor != null) {
                        System.out.println("Profesor encontrado");
                        System.out.println("Nombre: " + foundProfessor.getFirstName());
                        System.out.println("Apellido: " + foundProfessor.getLastName());
                        System.out.println("Numero de personal: " + foundProfessor.getPersonnelNumber());

                            System.out.println("\n1. Actualizar profesor");
                            System.out.println("2. Inactivar profesor");
                            System.out.println("3. Regresar al menú");

                            int professorOption = scanner.nextInt();
                            scanner.nextLine();
                            switch (professorOption) {
                                case 1:
                                    System.out.println("1. Modificar nombre");
                                    System.out.println("2. Modificar apellido");
                                    System.out.println("3. Modificar contraseña");
                                    System.out.println("4. Modificar si es coordinador");
                                    int modifyOption = scanner.nextInt();
                                    scanner.nextLine();
                                    switch (modifyOption) {
                                        case 1:
                                            System.out.println("Escriba el nuevo nombre: ");
                                            String newName = scanner.nextLine();
                                            foundProfessor.setFirstName(newName);
                                            professorDAO.modifyProfessor(foundProfessor);
                                            break;
                                        case 2:
                                            System.out.println("Escriba el nuevo apellido: ");
                                            String newLastName = scanner.nextLine();
                                            foundProfessor.setLastName(newLastName);
                                            professorDAO.modifyProfessor(foundProfessor);
                                            break;
                                        case 3:
                                            System.out.println("Escriba la nueva contraseña: ");
                                            String newPassword = scanner.nextLine();
                                            foundProfessor.setPassword(newPassword);
                                            professorDAO.modifyProfessor(foundProfessor);
                                            break;
                                        case 4:
                                            System.out.println("¿Es coordinador? (si/no): ");
                                            String isCoordInput = scanner.nextLine();
                                            boolean isCoord = isCoordInput.equalsIgnoreCase("si");
                                            foundProfessor.setIsCoordinator(isCoord);
                                            foundProfessor.setUserType(isCoord ? "Coordinador" : "Maestro");
                                            professorDAO.modifyProfessor(foundProfessor);
                                            break;
                                        default:
                                            System.out.println("Opción no válida");
                                    }
                                    break;
                                case 2:
                                    boolean inactivated = professorDAO.inactivateProfessor(foundProfessor);
                                    if (inactivated) {
                                        System.out.println("Profesor inactivado con exito");
                                    } else {
                                        System.out.println("Error al inactivar profesor");
                                    }
                                    break;  
                                case 3:
                                    System.out.println("Regresando al menú principal...");
                                    break;
                                default:
                                    System.out.println("Opción no válida");
                            }
                    } else {
                        System.out.println("Profesor no encontrado.");
                    }
                    break;
                case 3: 
                    System.out.println("Saliendo el programa");
                    break;
                default:
                    System.out.println("Opcion invalida");
                    break;
            }
        } while (option != 3);
    }

    private void showCoordinatorMenu(Scanner scanner) throws OperationException {
        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();
        int optionCoordinator;
        do {
            System.out.println("1. Registrar alumno");
            System.out.println("2. Consultar alumno por matricula");
            System.out.println("3. Registrar organizacion vinculada");
            System.out.println("4. Consultar organizaciones vinculadas");
            System.out.println("5. Registrar responsable tecnico");
            System.out.println("6. Consultar responsables técnicos");
            System.out.println("7. Registrar proyecto");
            System.out.println("8. Consultar proyectos");
            System.out.println("9. Asignar alumno a proyecto");
            System.out.println("10. Registrar experiencia educativa");
            System.out.println("11. Asignar alumno a EE");
            System.out.println("12. Salir");

            optionCoordinator = scanner.nextInt();
            switch (optionCoordinator) {
                case 1:
                    scanner.nextLine();
                    Student student = new Student();
                    System.out.println("Nombre: ");
                    String firstName = scanner.nextLine();
                    System.out.println("Apellidos: ");
                    String lastName = scanner.nextLine();
                    System.out.println("Contraseña: ");
                    String password = scanner.nextLine();
                    System.out.println("Matricula: ");
                    String idStudent = scanner.nextLine();
                    System.out.println("Fecha de nacimiento (YYYY-MM-DD): ");
                    Date birthDate = Date.valueOf(scanner.nextLine());
                    System.out.println("Genero: ");
                    System.out.println("1. Masculino");
                    System.out.println("2. Femenino");
                    System.out.println("3. Otro");
                    int genderOption = scanner.nextInt();
                    String gender;
                    switch (genderOption) {
                        case 1:
                            gender = "Masculino";
                            break;
                        case 2:
                            gender = "Femenino";
                            break;
                        case 3:
                            gender = "Otro";
                            break;
                        default:
                            gender = "Desconocido";
                    }
                    student.setFirstName(firstName);
                    student.setLastName(lastName);
                    student.setPassword(password);
                    student.setIdStudent(idStudent);
                    student.setBirthDate(birthDate);
                    student.setGender(gender);
                    student.setUserType("Alumno");
                    student.setInactive(false);

                    student.setIdentification(idStudent); 
                    int generatedId = userDAO.registerUser(student); 
                    if (generatedId != -1) {
                        student.setId(generatedId);                   
                        studentDAO.registerStudent(student);
                    } else {
                        System.out.println("Error al registrar usuario.");
                    }
                    break;
                case 2:
                    scanner.nextLine();
                    System.out.println("Escriba la matricula del alumno a consultar: ");
                    String searchId = scanner.nextLine();
                    Student foundStudent = studentDAO.getStudentById(searchId);
                    if (foundStudent != null) {
                        System.out.println("Alumno encontrado:");
                        System.out.println("Nombre: " + foundStudent.getFirstName());
                        System.out.println("Apellido: " + foundStudent.getLastName());
                        System.out.println("Matricula: " + foundStudent.getIdStudent());
                        System.out.println("Fecha de nacimiento: " + foundStudent.getBirthDate());
                        System.out.println("Género: " + foundStudent.getGender());
                        System.out.println("Horas completadas: " + foundStudent.getCompletedHours());
                        System.out.println("\n1. Actualizar alumno");
                        System.out.println("2. Inactivar alumno");
                        System.out.println("3. Regresar al menú");
                        int studentOption = scanner.nextInt();
                        switch (studentOption) {
                            case 1:
                                int modifyOption;
                                do {
                                    System.out.println("\n¿Qué campo desea modificar?");
                                    System.out.println("1. Nombre");
                                    System.out.println("2. Apellidos");
                                    System.out.println("3. Género");
                                    System.out.println("4. Salir");

                                    modifyOption = scanner.nextInt();
                                    scanner.nextLine();

                                    switch (modifyOption) {
                                        case 1:
                                            System.out.println("Nuevo nombre:");
                                            foundStudent.setFirstName(scanner.nextLine());
                                            break;

                                        case 2:
                                            System.out.println("Nuevo apellido:");
                                            foundStudent.setLastName(scanner.nextLine());
                                            break;

                                        case 3:
                                            System.out.println("Nuevo género:");
                                            foundStudent.setGender(scanner.nextLine());
                                            break;

                                        case 4:
                                            System.out.println("Saliendo...");
                                            break;

                                        default:
                                            System.out.println("Opción no válida");
                                    }

                                    if (modifyOption != 4) {
                                        studentDAO.modifyStudent(foundStudent);
                                    }

                                } while (modifyOption != 4);
                                break;
                            case 2:
                                studentDAO.inactivateStudent(foundStudent);
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Opción no válida");
                        }
                    } else {
                        System.out.println("Alumno no encontrado.");
                    }
                    break;
                case 3:
                    AffiliatedOrganizationDAO organizationDAO = new AffiliatedOrganizationDAO();
                    AffiliatedOrganization organization = new AffiliatedOrganization();
                    scanner.nextLine();
                    System.out.println("Nombre: ");
                    String organizationName = scanner.nextLine();
                    System.out.println("Ciudad: ");
                    String city = scanner.nextLine();
                    System.out.println("Estado: ");
                    String state = scanner.nextLine();
                    System.out.println("Correo electrónico: ");
                    String email = scanner.nextLine();
                    System.out.println("Número telefónico: ");
                    String phoneNumber = scanner.nextLine();
                    System.out.println("Número de usuarios directos: ");
                    int directUsers = scanner.nextInt();
                    System.out.println("Número de usuarios indirectos: ");
                    int indirectUsers = scanner.nextInt();
                    
                    organization.setName(organizationName);
                    organization.setCity(city);
                    organization.setState(state);
                    organization.setEmail(email);
                    organization.setPhoneNumber(phoneNumber);
                    organization.setNumberOfDirectUsers(directUsers);
                    organization.setNumberOfIndirectUsers(indirectUsers);
                    

                    organizationDAO.registerOrganization(organization);
                    System.out.println("Organización registrada con éxito");
                    break;
                case 4:
                    AffiliatedOrganizationDAO organizationSearcherDAO = new AffiliatedOrganizationDAO();
                    System.out.println("Escriba el numero de identificacion de la organizacion a consultar: ");
                    int searchOrganizationId = scanner.nextInt();
                    AffiliatedOrganization foundOrganization = organizationSearcherDAO.getOrganizationById(searchOrganizationId);
                        if (foundOrganization != null) {
                            System.out.println("Organización encontrada:");
                            System.out.println("Nombre: " + foundOrganization.getName());
                            System.out.println("Ciudad: " + foundOrganization.getCity());
                            System.out.println("Estado: " + foundOrganization.getState());
                            System.out.println("Correo electrónico: " + foundOrganization.getEmail());
                            System.out.println("Número telefónico: " + foundOrganization.getPhoneNumber());
                            System.out.println("Número de usuarios directos: " + foundOrganization.getNumberOfDirectUsers());
                            System.out.println("Número de usuarios indirectos: " + foundOrganization.getNumberOfIndirectUsers());

                            System.out.println("\n1. Modificar Organizacion");
                            System.out.println("2. Inactivar organizacion");
                            System.out.println("3. Regresar al menú");

                    int modifyOption = scanner.nextInt();
                        switch (modifyOption) {
                            case 1:
                                System.out.println("1. Modificar nombre: ");
                                System.out.println("2. Modificar ciudad: ");
                                System.out.println("3. Modificar estado: ");
                                System.out.println("4. Modificar correo electrónico: ");
                                System.out.println("5. Modificar número telefónico: ");
                                System.out.println("6. Modificar número de usuarios directos: ");
                                System.out.println("7. Modificar número de usuarios indirectos: ");
                                System.out.println("8. Cancelar modificación");

                                int modifyOrganizationOption = scanner.nextInt();
                                scanner.nextLine();

                                switch (modifyOrganizationOption) {
                                    case 1:
                                        System.out.println("Escriba el nuevo nombre: ");
                                        String newName = scanner.nextLine();
                                        foundOrganization.setName(newName);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 2:
                                        System.out.println("Escriba la nueva ciudad: ");
                                        String newCity = scanner.nextLine();
                                        foundOrganization.setCity(newCity);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 3:
                                        System.out.println("Escriba el nuevo estado: ");
                                        String newState = scanner.nextLine();
                                        foundOrganization.setState(newState);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 4:
                                        System.out.println("Escriba el nuevo correo electrónico: ");
                                        String newEmail = scanner.nextLine();
                                        foundOrganization.setEmail(newEmail);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 5:
                                        System.out.println("Escriba el nuevo número telefónico: ");
                                        String newPhoneNumber = scanner.nextLine();
                                        foundOrganization.setPhoneNumber(newPhoneNumber);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 6:
                                        System.out.println("Escriba el nuevo número de usuarios directos: ");
                                        int newDirectUsers = scanner.nextInt();
                                        foundOrganization.setNumberOfDirectUsers(newDirectUsers);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 7:
                                        System.out.println("Escriba el nuevo número de usuarios indirectos: ");
                                        int newIndirectUsers = scanner.nextInt();
                                        foundOrganization.setNumberOfIndirectUsers(newIndirectUsers);
                                        organizationSearcherDAO.modifyOrganization(foundOrganization);
                                        break;
                                    case 8:
                                        System.out.println("Cancelando modificación...");
                                        break;
                                    default:
                                        System.out.println("Opción no válida");
                                    }
                                    break;
                            case 2:
                                boolean inactivated = organizationSearcherDAO.inactivateOrganization(foundOrganization);
                                    if (inactivated) {
                                        System.out.println("Organización inactivada con éxito");
                                    } else {
                                        System.out.println("Error al inactivar organización");
                                    }
                                    break;
                            case 3:
                                System.out.println("Regresando al menú principal...");
                                break;
                            default:
                                System.out.println("Opción no válida");
                                }
                            } else {
                                System.out.println("Organización no encontrada.");  
                            }
                            break;
                case 5:
                    scanner.nextLine();
                    ProjectSupervisorDAO supervisorDAO = new ProjectSupervisorDAO();
                    ProjectSupervisor supervisor = new ProjectSupervisor();
                    System.out.println("Nombre: ");
                    String firstNameSupervisor = scanner.nextLine();
                    System.out.println("Correo electrónico: ");
                    String emailSupervisor = scanner.nextLine();
                    System.out.println("Cargo: ");
                    String positionSupervisor = scanner.nextLine();

                    supervisor.setName(firstNameSupervisor);
                    supervisor.setEmail(emailSupervisor);
                    supervisor.setPosition(positionSupervisor);
                    supervisorDAO.registerProjectSupervisor(supervisor);
                    break;
                case 6:
                    ProjectSupervisorDAO supervisorSearcherDAO = new ProjectSupervisorDAO();
                    System.out.println("Escriba el numero de identificacion del supervisor a consultar: ");
                    int searchSupervisorId = scanner.nextInt();
                    ProjectSupervisor foundSupervisor = supervisorSearcherDAO.getProjectSupervisorById(searchSupervisorId);
                    if (foundSupervisor != null) {
                        System.out.println("Supervisor encontrado:");
                        System.out.println("Nombre: " + foundSupervisor.getName());
                        System.out.println("Correo electrónico: " + foundSupervisor.getEmail());
                        System.out.println("Cargo: " + foundSupervisor.getPosition());
                        System.out.println("\n1. Actualizar supervisor");
                        System.out.println("2. Inactivar supervisor");
                        System.out.println("3. Regresar al menú");

                        switch (scanner.nextInt()) {
                            case 1:
                                int modifyOption;
                                do {
                                    System.out.println("\n¿Qué campo desea modificar?");
                                    System.out.println("1. Nombre");
                                    System.out.println("2. Correo electrónico");
                                    System.out.println("3. Cargo");
                                    System.out.println("4. Salir");

                                    modifyOption = scanner.nextInt();
                                    scanner.nextLine();

                                    switch (modifyOption) {
                                        case 1:
                                            System.out.println("Nuevo nombre:");
                                            foundSupervisor.setName(scanner.nextLine());
                                            break;

                                        case 2:
                                            System.out.println("Nuevo correo electrónico:");
                                            foundSupervisor.setEmail(scanner.nextLine());
                                            break;

                                        case 3:
                                            System.out.println("Nuevo cargo:");
                                            foundSupervisor.setPosition(scanner.nextLine());
                                            break;
                                        case 4:
                                            System.out.println("Saliendo...");
                                            break;

                                        default:
                                            System.out.println("Opción no válida");
                                    }

                                    if (modifyOption != 4) {
                                        supervisorSearcherDAO.modifyProjectSupervisor(foundSupervisor);
                                    }

                                } while (modifyOption != 4);
                                break;
                            case 2:
                                supervisorSearcherDAO.inactivateProjectSupervisor(foundSupervisor);
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Opción no válida");
                        }
                    } else {
                        System.out.println("Supervisor no encontrado.");
                    }
                    break;
                case 7:
                    scanner.nextLine();

                    ProjectDAO projectDAO = new ProjectDAO();
                    Project project = new Project();
                    System.out.println("Nombre: ");
                    String projectName = scanner.nextLine();
                    System.out.println("Metodología: ");
                    String methodology = scanner.nextLine();
                    System.out.println("Capacidad de alumnos: ");

                    int capacity = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.println("Objetivo: ");
                    String objective = scanner.nextLine();
                    System.out.println("Descripción: ");
                    String description = scanner.nextLine();

                    project.setName(projectName);
                    project.setMethodology(methodology);
                    project.setCapacity(capacity);
                    project.setObjective(objective);
                    project.setDescription(description);
                    
                    projectDAO.registerProject(project);
                    System.out.println("Proyecto registrado con éxito");
                    break;
                case 8: 
                    ProjectDAO projectSearcherDAO = new ProjectDAO();
                    System.out.println("Escriba el numero de identificacion del proyecto a consultar: ");
                    int searchProjectId = scanner.nextInt();
                    Project foundProject = projectSearcherDAO.getProjectById(searchProjectId);
                    if (foundProject != null) {
                        System.out.println("Proyecto encontrado:");
                        System.out.println("Nombre: " + foundProject.getName());
                        System.out.println("Metodología: " + foundProject.getMethodology());
                        System.out.println("Capacidad de alumnos: " + foundProject.getCapacity());
                        System.out.println("Objetivo: " + foundProject.getObjective());
                        System.out.println("Descripción: " + foundProject.getDescription());

                        System.out.println("\n1. Modificar proyecto");
                        System.out.println("2. Inactivar proyecto");
                        System.out.println("3. Regresar al menú");

                        int projectSupervisorOption = scanner.nextInt();
                        scanner.nextLine();

                        switch (projectSupervisorOption) {
                            case 1:
                                System.out.println("1. Modificar nombre: ");
                                System.out.println("2. Modificar metodología: ");
                                System.out.println("3. Modificar capacidad de alumnos: ");
                                System.out.println("4. Modificar objetivo: ");
                                System.out.println("5. Modificar descripción: ");
                                System.out.println("6. Cancelar modificación");

                                int modifyProjectOption = scanner.nextInt();
                                scanner.nextLine();

                                switch (modifyProjectOption) {
                                    case 1:
                                        System.out.println("Escriba el nuevo nombre: ");
                                        String newName = scanner.nextLine();
                                        foundProject.setName(newName);
                                        projectSearcherDAO.modifyProject(foundProject);
                                        break;
                                    case 2:
                                        System.out.println("Escriba la nueva metodología: ");
                                        String newMethodology = scanner.nextLine();
                                        foundProject.setMethodology(newMethodology);
                                        projectSearcherDAO.modifyProject(foundProject);
                                        break;
                                    case 3:
                                        System.out.println("Escriba la nueva capacidad de alumnos: ");
                                        int newCapacity = scanner.nextInt();
                                        foundProject.setCapacity(newCapacity);
                                        projectSearcherDAO.modifyProject(foundProject);
                                        break;
                                    case 4:
                                        System.out.println("Escriba el nuevo objetivo: ");
                                        String newObjective = scanner.nextLine();
                                        foundProject.setObjective(newObjective);
                                        projectSearcherDAO.modifyProject(foundProject);
                                        break;
                                    case 5:
                                        System.out.println("Escriba la nueva descripción: ");
                                        String newDescription = scanner.nextLine();
                                        foundProject.setDescription(newDescription);
                                        projectSearcherDAO.modifyProject(foundProject);
                                        break;
                                    case 6:
                                        System.out.println("Cancelando modificación...");
                                        break;
                                    default:
                                        System.out.println("Opción no válida");
                                }
                                break;
                            case 2:
                                boolean inactivatedProject = projectSearcherDAO.inactivateProject(foundProject);
                                    if (inactivatedProject) {
                                        System.out.println("Proyecto inactivado con éxito");
                                    } else {
                                        System.out.println("Error al inactivar proyecto");
                                    }
                                break;
                            case 3:
                                System.out.println("Regresando al menú principal...");
                                break;
                            default:
                                System.out.println("Opcion no válida");
                        }
                    } else {
                        System.out.println("Proyecto no encontrado.");
                    }
                    break;
                case 9:
                    scanner.nextLine();
                    ProjectRequestDAO requestDAO = new ProjectRequestDAO();
                    System.out.println("Escriba la matricula del alumno: ");
                    String studentId = scanner.nextLine();
                    Student consultStudent = studentDAO.getStudentById(studentId);
                    if (consultStudent != null) {
                        System.out.println("Alumno encontrado:");
                        System.out.println("Nombre: " + consultStudent.getFirstName());
                        requestDAO.getRequestedProjectsByStudentId(studentId);
                        System.out.println("Escriba el numero de identificacion del proyecto a asignar: ");
                        int assignProjectId = scanner.nextInt();
                        requestDAO.assignStudentToProject(studentId, assignProjectId);
                    } else {
                        System.out.println("Alumno no encontrado.");
                    }
                    break;
                case 10:
                    SubjectDAO subjectDAO = new SubjectDAO();
                    System.out.println("NRC: ");
                    int nrc = scanner.nextInt();
                    System.out.println("Periodo escolar: ");
                    String schoolPeriod = scanner.nextLine();
                    Subject subject = new Subject(nrc, schoolPeriod);
                    subjectDAO.registerSubject(subject);
                    break;
                case 11:
                    SubjectDAO subjectDAOSearcher = new SubjectDAO();
                    try {
                        subjectDAOSearcher.getAllSubjects();
                    } catch (OperationException e) {
                        e.getMessage();
                    }
                    
                    break;
                case 12:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (optionCoordinator != 12);
    }

    private void showStudentMenu(Scanner scanner) throws OperationException {
        int option;
        do {
            System.out.println("1. Solicitar proyecto");
            System.out.println("2. Generar reporte");
            System.out.println("3. Generar autoevaluación");
            System.out.println("4. Salir");

            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    ProjectRequestDAO requestDAO = new ProjectRequestDAO();
                    requestDAO.getAvailableProjects();
                    System.out.println("Escriba el numero de identificacion del proyecto a solicitar: ");
                    int requestProjectId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Escriba su matricula: ");
                    String studentId = scanner.nextLine();
                    requestDAO.requestProject(studentId, requestProjectId);
                    break;
                case 2:
                    ReportDAO reportDAO = new ReportDAO();

                    System.out.println("Tipo de reporte a generar: ");
                    System.out.println("1. Reporte parcial");
                    System.out.println("2. Reporte mensual");
                    System.out.println("3. Reporte final");

                    int reportType = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("ID del reporte:");
                    int id = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Descripción:");
                    String description = scanner.nextLine();

                    System.out.println("Observaciones:");
                    String observations = scanner.nextLine();

                    System.out.println("Actividad:");
                    String activity = scanner.nextLine();

                    System.out.println("Matrícula del estudiante:");
                    String idStudentReport = scanner.nextLine();

                    if (reportType == 1) {
                        System.out.println("Tiempo planeado: ");
                        int plannedTime = scanner.nextInt();

                        System.out.println("Tiempo real: ");
                        int realTime = scanner.nextInt();


                        PartialReport partialReport = new PartialReport();
                        partialReport.setId(id);
                        partialReport.setDescription(description);
                        partialReport.setObservations(observations);
                        partialReport.setActivity(activity);
                        partialReport.setStudentId(idStudentReport);
                        partialReport.setPlannedTime(plannedTime);
                        partialReport.setRealTime(realTime);

                        if (reportDAO.registerPartialReport(partialReport)) {
                            System.out.println("Reporte parcial registrado correctamente");
                        } else {
                            System.out.println("Error al registrar reporte parcial");
                        }

                    } else if (reportType == 2) {
                        System.out.println("Mes:");
                        String month = scanner.nextLine();

                        System.out.println("Horas reportadas:");
                        int hours = scanner.nextInt();

                        MonthlyReport monthlyReport = new MonthlyReport();
                        monthlyReport.setId(id);
                        monthlyReport.setDescription(description);
                        monthlyReport.setObservations(observations);
                        monthlyReport.setActivity(activity);
                        monthlyReport.setStudentId(idStudentReport);
                        monthlyReport.setMonth(month);
                        monthlyReport.setReportedHours(hours);

                        if (reportDAO.registerMonthlyReport(monthlyReport)) {
                            System.out.println("Reporte mensual registrado correctamente");
                        } else {
                            System.out.println("Error al registrar reporte mensual");
                        }

                    } else if (reportType == 3) {
                        System.out.println("Porcentaje de avance:");
                        int porcentajeAvance = scanner.nextInt();
                        scanner.nextLine();

                        System.out.println("Resultado del entregable:");
                        String resultado = scanner.nextLine();

                        FinalReport finalReport = new FinalReport();
                        finalReport.setId(id);
                        finalReport.setDescription(description);
                        finalReport.setObservations(observations);
                        finalReport.setActivity(activity);
                        finalReport.setStudentId(idStudentReport);
                        finalReport.setAdvancePercentage(porcentajeAvance);
                        finalReport.setResult(resultado);

                        if (reportDAO.registerFinalReport(finalReport)) {
                            System.out.println("Reporte final registrado correctamente");
                        } else {
                            System.out.println("Error al registrar reporte final");
                        }

                    } else {
                        System.out.println("Opción no válida");
                    }

                    break; 
                case 3:
                    AutoevaluationCommon autoevaluationService = new AutoevaluationCommon();

                    System.out.println("Matrícula del estudiante:");
                    String studentIdAutoevaluation = scanner.nextLine();

                    int[] answers = new int[10];

                    System.out.println("Responde del 1 al 5:");

                    System.out.print("1. Participación productiva: ");
                    answers[0] = scanner.nextInt();

                    System.out.print("2. Conocimiento aplicado: ");
                    answers[1] = scanner.nextInt();

                    System.out.print("3. Confianza en actividades: ");
                    answers[2] = scanner.nextInt();

                    System.out.print("4. Interés en actividades: ");
                    answers[3] = scanner.nextInt();

                    System.out.print("5. Apoyo de la organización: ");
                    answers[4] = scanner.nextInt();

                    System.out.print("6. Conocimiento de reglas: ");
                    answers[5] = scanner.nextInt();

                    System.out.print("7. Supervisión recibida: ");
                    answers[6] = scanner.nextInt();

                    System.out.print("8. Seguimiento efectivo: ");
                    answers[7] = scanner.nextInt();

                    System.out.print("9. Relación con la carrera: ");
                    answers[8] = scanner.nextInt();

                    System.out.print("10. Importancia de la práctica: ");
                    answers[9] = scanner.nextInt();

                    Autoevaluation autoevaluation = new Autoevaluation(studentIdAutoevaluation, answers);

                    scanner.nextLine();
                    System.out.print("Fecha (YYYY-MM-DD): ");
                    autoevaluation.setEvaluationDate(scanner.nextLine());

                    System.out.println("Puntaje final: " + autoevaluation.getFinalScore());

                    if (autoevaluationService.registerAutoevaluation(autoevaluation)) {
                        System.out.println("Autoevaluación registrada");
                    } else {
                        System.out.println(" Error al registrar autoevaluación");
                    }

                    break;
                case 4:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (option != 4);
    }
}