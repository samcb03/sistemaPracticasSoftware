package uv.lis;


import java.sql.Date;
import java.util.Scanner;

import uv.lis.logic.dao.AffiliatedOrganizationDAO;
import uv.lis.logic.dao.ProjectDAO;
import uv.lis.logic.dao.ProjectRequestDAO;
import uv.lis.logic.dao.ProjectSupervisorDAO;
import uv.lis.logic.dao.ReportDAO;
import uv.lis.logic.dao.UserDAO;
import uv.lis.logic.dto.AffiliatedOrganization;
import uv.lis.logic.dto.FinalReport;
import uv.lis.logic.dto.PartialReport;
import uv.lis.logic.dto.Project;
import uv.lis.logic.dto.ProjectSupervisor;
import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.User;



public class Main {
    public static void main(String[] args) {
        User currentUser = new User();
        UserDAO userDAO = new UserDAO();
        if(userDAO.getUserType(currentUser).equals("Profesor")) {
            new Main().showProfessorMenu();
        } else if (userDAO.getUserType(currentUser).equals("Coordinador")) {
            new Main().showCoordinatorMenu();
        } else if (userDAO.getUserType(currentUser).equals("Alumno")) {
            new Main().showStudentMenu();
        } else {
            System.out.println("Tipo de usuario no reconocido");
        }
    }

    private void showProfessorMenu() {
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println("1. Registrar notificacion");
            System.out.println("2. Evaluar Reporte");
            System.out.println("3. Salir");

            option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Función no disponible por el momento");
                    break;
                case 2:
                    System.out.println("Función no disponible por el momento");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (option != 3);

        scanner.close();   
    }   

    private void showCoordinatorMenu() {
        UserDAO userDAO = new UserDAO();
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println("1. Registrar alumno");
            System.out.println("2. Consultar alumnos");
            System.out.println("3. Registrar organizacion vinculada");
            System.out.println("4. Consultar organizaciones vinculadas");
            System.out.println("5. Registrar responsable tecnico");
            System.out.println("6. Consultar responsables técnicos");
            System.out.println("7. Registrar proyecto");
            System.out.println("8. Consultar proyectos");
            System.out.println("9. Asignar proyecto a alumno");
            System.out.println("10. Salir");

            option = scanner.nextInt();
            switch (option) {
                case 1:
                    Student student = new Student();
                    System.out.println("Nombre: ");
                    String firstName = scanner.nextLine();
                    System.out.println("Apellido: ");
                    String lastName = scanner.nextLine();
                    System.out.println("Contraseña: ");
                    String password = scanner.nextLine();
                    System.out.println("Matricula: ");
                    String idStudent = scanner.nextLine();
                    System.out.println("Fecha de nacimiento (YYYY-MM-DD): ");
                    Date dateOfBirth = Date.valueOf(scanner.nextLine());
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
                    System.out.println("¿Habla alguna lengua indígena? (true/false): ");
                    boolean indigenousLanguage = scanner.nextBoolean();
                    student.setFirstName(firstName);
                    student.setLastName(lastName);
                    student.setPassword(password);
                    student.setIdStudent(idStudent);
                    student.setDateOfBirth(dateOfBirth);
                    student.setGender(gender);
                    student.setIndigenousLanguage(indigenousLanguage);

                    userDAO.registerUser(student);
                    break;
                case 2:
                    System.out.println("Escriba la matricula del alumno a consultar: ");
                    String searchId = scanner.nextLine();
                    Student foundStudent = userDAO.getStudentById(searchId);
                    if (foundStudent != null) {
                        System.out.println("Alumno encontrado:");
                        System.out.println("Nombre: " + foundStudent.getFirstName());
                        System.out.println("Apellido: " + foundStudent.getLastName());
                        System.out.println("Matricula: " + foundStudent.getIdStudent());
                        System.out.println("Fecha de nacimiento: " + foundStudent.getDateOfBirth());
                        System.out.println("Género: " + foundStudent.getGender());
                        System.out.println("Habla alguna lengua indígena: " + foundStudent.isIndigenousLanguage());
                        System.out.println("Horas completadas: " + foundStudent.getCompletedHours());
                        System.out.println("\n1. Actualizar alumno");
                        System.out.println("2. Inactivar alumno");
                        System.out.println("3. Regresar al menú");
                        int studentOption = scanner.nextInt();
                        switch (studentOption) {
                            case 1:
                                userDAO.modifyStudent(foundStudent);
                                break;
                            case 2:
                                userDAO.inactivateStudent(foundStudent);
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

                    organizationDAO.registerAffiliatedOrganization(organization);
                    break;
                case 4:
                    AffiliatedOrganizationDAO organizationSearcherDAO = new AffiliatedOrganizationDAO();
                    System.out.println("Escriba el numero de identificacion de la organizacion a consultar: ");
                    int searchOrganizationId = scanner.nextInt();
                    AffiliatedOrganization foundOrganization 
                        = organizationSearcherDAO.getAffiliatedOrganizationById(searchOrganizationId);
                    if (foundOrganization != null) {
                        System.out.println("Organización encontrada:");
                        System.out.println("Nombre: " + foundOrganization.getName());
                        System.out.println("Ciudad: " + foundOrganization.getCity());
                        System.out.println("Estado: " + foundOrganization.getState());
                        System.out.println("Correo electrónico: " + foundOrganization.getEmail());
                        System.out.println("Número telefónico: " + foundOrganization.getPhoneNumber());
                        System.out.println("Número de usuarios directos: " + foundOrganization.getNumberOfDirectUsers());
                        System.out.println("Número de usuarios indirectos: " + foundOrganization.getNumberOfIndirectUsers());
                        System.out.println("\n1. Actualizar Organizacion");
                        System.out.println("2. Inactivar organizacion");
                        System.out.println("3. Regresar al menú");
                        int organizationOption = scanner.nextInt();
                        switch (organizationOption) {
                            case 1:
                                organizationSearcherDAO.modifyAffiliatedOrganization(foundOrganization);
                                break;
                            case 2:
                                organizationSearcherDAO.inactivateAffiliatedOrganization(foundOrganization);
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Opción no válida");
                        }
                    } else {
                        System.out.println("Organización no encontrada.");
                    } 
                    break;
                case 5:
                    ProjectSupervisorDAO supervisorDAO = new ProjectSupervisorDAO();
                    ProjectSupervisor supervisor = new ProjectSupervisor();
                    System.out.println("Nombre: ");
                    String firstNameSupervisor = scanner.nextLine();
                    System.out.println("Apellido(s): ");
                    String lastNameSupervisor = scanner.nextLine();
                    System.out.println("Correo electrónico: ");
                    String emailSupervisor = scanner.nextLine();

                    supervisor.setName(firstNameSupervisor);
                    supervisor.setLastName(lastNameSupervisor);
                    supervisor.setEmail(emailSupervisor);
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
                        System.out.println("Apellido(s): " + foundSupervisor.getLastName());
                        System.out.println("Correo electrónico: " + foundSupervisor.getEmail());
                        System.out.println("\n1. Actualizar supervisor");
                        System.out.println("2. Inactivar supervisor");
                        System.out.println("3. Regresar al menú");

                        switch (scanner.nextInt()) {
                            case 1:
                                supervisorSearcherDAO.modifyProjectSupervisor(foundSupervisor);
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
                case 7:
                    ProjectDAO projectDAO = new ProjectDAO();
                    Project project = new Project();
                    System.out.println("Nombre: ");
                    String name = scanner.nextLine();
                    System.out.println("Metodologia: ");
                    String methodology = scanner.nextLine();
                    System.out.println("Capacidad de alumnos: ");
                    int studentCapacity = scanner.nextInt();
                    System.out.println("Objetivo: ");
                    String objective = scanner.nextLine();
                    System.out.println("Descripción: ");
                    String description = scanner.nextLine();

                    project.setName(name);
                    project.setMethodology(methodology);
                    project.setCapacity(studentCapacity);
                    project.setObjective(objective);
                    project.setDescription(description);
                    projectDAO.registerProject(project);
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
                        System.out.println("\n1. Actualizar proyecto");
                        System.out.println("2. Inactivar proyecto");
                        System.out.println("3. Regresar al menú");

                        switch (scanner.nextInt()) {
                            case 1:
                                projectSearcherDAO.modifyProject(foundProject);
                                break;
                            case 2:
                                projectSearcherDAO.inactivateProject(foundProject);
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Opción no válida");
                        }
                    } else {
                        System.out.println("Proyecto no encontrado.");
                    }
                    break;
                case 9:
                    ProjectRequestDAO requestDAO = new ProjectRequestDAO();
                    System.out.println("Escriba la matricula del alumno: ");
                    String studentId = scanner.nextLine();
                    Student consultStudent = userDAO.getStudentById(studentId);
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
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (option != 10);
        scanner.close(); 
    }

    private void showStudentMenu() {
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println("1. Solicitar proyecto");
            System.out.println("2. Generar reporte");
            System.out.println("3. Generar autoevaluación");
            System.out.println("4. Salir");

            option = scanner.nextInt();
            switch (option) {
                case 1:
                    ProjectRequestDAO requestDAO = new ProjectRequestDAO();
                    requestDAO.getAvailableProjects();
                    System.out.println("Escriba el numero de identificacion del proyecto a solicitar: ");
                    int requestProjectId = scanner.nextInt();
                    System.out.println("Escriba su matricula: ");
                    String studentId = scanner.nextLine();
                    requestDAO.requestProject(studentId, requestProjectId);
                    break;
                case 2:
                    ReportDAO reportDAO = new ReportDAO();
                    System.out.println("Tipo de reporte a generar: ");
                    System.out.println("1. Reporte parcial");
                    System.out.println("2. Reporte final");
                    int reportType = scanner.nextInt();
                    if (reportType == 1) {
                        PartialReport partialReport = new PartialReport();
                        System.out.println("Observaciones: ");
                        String observation = scanner.nextLine();
                        System.out.println("Fecha límite (YYYY-MM-DD): ");
                        String dueDate = scanner.nextLine();
                        System.out.println("Es mensual? (true/false): ");
                        boolean monthly = scanner.nextBoolean();
                        partialReport.setObservation(observation);
                        partialReport.setDueDate(dueDate);
                        partialReport.setIsMonthly(monthly);

                        reportDAO.registerPartialReport(partialReport);
                    } else if (reportType == 2) {
                        FinalReport finalReport = new FinalReport();
                        System.out.println("Observaciones: ");
                        String observation = scanner.nextLine();
                        System.out.println("Fecha límite (YYYY-MM-DD): ");
                        String dueDate = scanner.nextLine();
                        System.out.println("Porcentaje de avance: ");
                        int porcentajeAvance = scanner.nextInt();
                        System.out.println("Resultado del entregable: ");
                        String resultadoEntregable = scanner.nextLine();

                        finalReport.setObservation(observation);
                        finalReport.setDueDate(dueDate);
                        finalReport.setAdvancePercentage(porcentajeAvance);
                        finalReport.setResult(resultadoEntregable);

                        reportDAO.registerFinalReport(finalReport);
                    } else {
                        System.out.println("Opción no válida");
                    }
                    break;
                case 3:
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (option != 3);

        scanner.close();
    }
}