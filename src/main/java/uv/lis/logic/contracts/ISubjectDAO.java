package uv.lis.logic.contracts;

import java.util.ArrayList;

import uv.lis.logic.dto.Student;
import uv.lis.logic.dto.Subject;
import uv.lis.logic.exceptions.OperationException;

/**
 * Defines the data access operations for subjects and their enrollments.
 */
public interface ISubjectDAO {

    /**
     * Registers a new subject in the system.
     *
     * @param subject the subject data to register
     * @return true if the subject was registered, false otherwise
     * @throws OperationException if the subject cannot be registered
     */
    boolean registerSubject(Subject subject) throws OperationException;

    /**
     * Retrieves the NRC and name of all the registered subjects.
     *
     * @return the list of subject NRC and names, empty if there are none
     * @throws OperationException if the subjects cannot be retrieved
     */
    ArrayList<Subject> getAllSubjectsNrcName() throws OperationException;

    /**
     * Retrieves the NRC of the subject a student is enrolled in.
     *
     * @param studentID the identifier of the student to query
     * @return the NRC of the subject the student is enrolled in
     * @throws OperationException if the NRC cannot be retrieved
     */
    String getSubjectNrcByStudentID(String studentID) throws OperationException;

    /**
     * Removes the subject assignment of a professor.
     *
     * @param personnelNumber the personnel number of the professor to unassign
     * @throws OperationException if the professor cannot be unassigned
     */
    void unassignProfessorFromSubject(String personnelNumber) throws OperationException;

    /**
     * Retrieves the subjects assigned to a professor.
     *
     * @param personnelNumber the personnel number of the professor to query
     * @return the list of subjects for the professor, empty if there are none
     * @throws OperationException if the subjects cannot be retrieved
     */
    ArrayList<Subject> getSubjectsByProfessor(String personnelNumber) throws OperationException;

    /**
     * Retrieves the students enrolled in a subject.
     *
     * @param nrc the NRC of the subject to query
     * @return the list of enrolled students, empty if there are none
     * @throws OperationException if the students cannot be retrieved
     */
    ArrayList<Student> getEnrolledStudentsBySubject(int nrc) throws OperationException;

    /**
     * Enrolls a student in a subject.
     *
     * @param studentId the identifier of the student to enroll
     * @param subjectNrc the NRC of the subject to enroll the student in
     * @return true if the student was enrolled, false otherwise
     * @throws OperationException if the student cannot be enrolled
     */
    boolean assignStudentToSubject(String studentId, int subjectNrc, int periodId) throws OperationException;

    /**
     * Checks whether a section is already taken in a given school period.
     *
     * @param periodId the identifier of the school period to check
     * @param section the section to check ('1' or '2')
     * @return true if the section is already assigned, false otherwise
     * @throws OperationException if the check cannot be performed
     */
    boolean isSectionTakenInPeriod(int periodId, String section) throws OperationException;
}