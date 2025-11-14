
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.*;


public class TimetableGeneratorApp extends JFrame {

    private JTabbedPane tabbedPane;

    // Data models
    private SubjectTableModel subjectModel = new SubjectTableModel();
    private TeacherTableModel teacherModel = new TeacherTableModel();
    private ClassroomTableModel classroomModel = new ClassroomTableModel();

    public TimetableGeneratorApp() {
        setTitle("Automatic Timetable Generator");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Teachers", new TeacherPanel(teacherModel));
        tabbedPane.addTab("Subjects", new SubjectPanel(subjectModel));
        tabbedPane.addTab("Classrooms", new ClassroomPanel(classroomModel));
        tabbedPane.addTab("Generate", new GeneratePanel(teacherModel, subjectModel, classroomModel));

        add(tabbedPane);

        // Ensure window opens in focus
        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TimetableGeneratorApp().setVisible(true);
        });
    }
}

// Teacher Panel
class TeacherPanel extends JPanel {

    private JTable teacherTable;
    private TeacherTableModel tableModel;
    private DefaultListModel<String> allSubjectsModel = new DefaultListModel<>();

    public TeacherPanel(TeacherTableModel model) {
        this.tableModel = model;
        initializeDefaultSubjects();
        setupUI();
    }

    private void initializeDefaultSubjects() {
        String[] defaultSubjects = {"Combined Maths", "Biology", "Chemistry", "Physics", "English", "Git"};
        for (String subject : defaultSubjects) {
            allSubjectsModel.addElement(subject);
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Teacher Table
        teacherTable = new JTable(tableModel);
        teacherTable.setRowHeight(30);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createAddTeacherButton());
        buttonPanel.add(createRemoveTeacherButton());
        buttonPanel.add(createManageSubjectsButton());
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(teacherTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createAddTeacherButton() {
        JButton button = new JButton("Add Teacher");
        button.addActionListener(e -> showAddTeacherDialog());
        return button;
    }

    private JButton createRemoveTeacherButton() {
        JButton button = new JButton("Remove Selected");
        button.addActionListener(e -> removeSelectedTeacher());
        return button;
    }

    private JButton createManageSubjectsButton() {
        JButton button = new JButton("Manage Subjects");
        button.addActionListener(e -> showSubjectManagementDialog());
        return button;
    }

    private void showSubjectManagementDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Subject Management");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 250);
        dialog.setModal(true);

        // Subject List
        JList<String> subjectList = new JList<>(allSubjectsModel);
        JScrollPane scrollPane = new JScrollPane(subjectList);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createAddSubjectButton(dialog));
        buttonPanel.add(createRemoveSubjectButton(dialog, subjectList));

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton createAddSubjectButton(JDialog parent) {
        JButton button = new JButton("Add Subject");
        button.addActionListener(e -> {
            String subjectName = JOptionPane.showInputDialog(parent, "Enter subject name:");
            if (subjectName != null && !subjectName.trim().isEmpty()) {
                if (!allSubjectsModel.contains(subjectName.trim())) {
                    allSubjectsModel.addElement(subjectName.trim());
                } else {
                    JOptionPane.showMessageDialog(parent, "Subject already exists!");
                }
            }
        });
        return button;
    }

    private JButton createRemoveSubjectButton(JDialog parent, JList<String> subjectList) {
        JButton button = new JButton("Remove Subject");
        button.addActionListener(e -> {
            int selectedIndex = subjectList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String subject = allSubjectsModel.getElementAt(selectedIndex);
                if (isSubjectAssigned(subject)) {
                    JOptionPane.showMessageDialog(parent,
                            "Cannot remove: Subject is assigned to one or more teachers");
                } else {
                    allSubjectsModel.remove(selectedIndex);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Please select a subject first");
            }
        });
        return button;
    }

    private boolean isSubjectAssigned(String subject) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String teacherSubjects = (String) tableModel.getValueAt(i, 1);
            if (teacherSubjects.contains(subject)) {
                return true;
            }
        }
        return false;
    }

    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Teacher");
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        dialog.setSize(400, 300);
        dialog.setModal(true);

        // Form components
        JTextField nameField = new JTextField();
        JTextField periodField = new JTextField("2");
        JList<String> subjectsList = new JList<>(allSubjectsModel);
        subjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Add components to dialog
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Max Period/Day:"));
        dialog.add(periodField);
        dialog.add(new JLabel("Subjects:"));
        dialog.add(new JScrollPane(subjectsList));

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveTeacher(
                nameField.getText().trim(),
                periodField.getText().trim(),
                subjectsList.getSelectedValuesList(),
                dialog
        ));

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void saveTeacher(String name, String periodStr, List<String> subjects, JDialog dialog) {
        try {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Teacher name cannot be empty");
            }
            if (subjects.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one subject");
            }

            int maxPeriod = Integer.parseInt(periodStr);
            tableModel.addTeacher(new Teacher(name, subjects, maxPeriod));
            dialog.dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Please enter valid periods (number)");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(dialog, e.getMessage());
        }
    }

    private void removeSelectedTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row >= 0) {
            tableModel.removeTeacher(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a teacher first");
        }
    }
}
// Subject Panel

class SubjectPanel extends JPanel {

    private JTable subjectTable;
    private SubjectTableModel tableModel;

    public SubjectPanel(SubjectTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(30);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Subject");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> showAddSubjectDialog());
        removeButton.addActionListener(e -> removeSelectedSubject());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(subjectTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddSubjectDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Subject");
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField nameField = new JTextField();
        JTextField periodField = new JTextField("10");
        JCheckBox labCheckbox = new JCheckBox("Requires Lab");

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Weekly Period:"));
        dialog.add(periodField);
        dialog.add(new JLabel("Lab Requirement:"));
        dialog.add(labCheckbox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter subject name");
                    return;
                }

                int hours = Integer.parseInt(periodField.getText());
                boolean requiresLab = labCheckbox.isSelected();

                tableModel.addSubject(new Subject(name, hours, requiresLab));
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid weekly period");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void removeSelectedSubject() {
        int row = subjectTable.getSelectedRow();
        if (row != -1) {
            tableModel.removeSubject(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a subject to remove");
        }
    }
}

// Classroom Panel
class ClassroomPanel extends JPanel {

    private JTable classroomTable;
    private ClassroomTableModel tableModel;

    public ClassroomPanel(ClassroomTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        classroomTable = new JTable(tableModel);
        classroomTable.setRowHeight(30);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Classroom");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> showAddClassroomDialog());
        removeButton.addActionListener(e -> removeSelectedClassroom());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(classroomTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddClassroomDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Classroom");
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField roomIdField = new JTextField();
        JCheckBox labCheckbox = new JCheckBox("Is Lab");
        JTextField capacityField = new JTextField("30");

        dialog.add(new JLabel(" Class Room:"));
        dialog.add(roomIdField);
        dialog.add(new JLabel("Lab Room:"));
        dialog.add(labCheckbox);
        dialog.add(new JLabel("Capacity:"));
        dialog.add(capacityField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String roomId = roomIdField.getText().trim();
                if (roomId.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter class room");
                    return;
                }

                boolean isLab = labCheckbox.isSelected();
                int capacity = Integer.parseInt(capacityField.getText());

                tableModel.addClassroom(new Classroom(roomId, isLab, capacity));
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid capacity");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void removeSelectedClassroom() {
        int row = classroomTable.getSelectedRow();
        if (row != -1) {
            tableModel.removeClassroom(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a classroom to remove");
        }
    }
}

// Generate Panel
class GeneratePanel extends JPanel {

    private JTable timetableTable;
    private TimetableTableModel timetableModel = new TimetableTableModel(9); // 👈 FIXED
    private TeacherTableModel teacherModel;
    private SubjectTableModel subjectModel;
    private ClassroomTableModel classroomModel;

    public GeneratePanel(TeacherTableModel tModel, SubjectTableModel sModel, ClassroomTableModel cModel) {
        this.teacherModel = tModel;
        this.subjectModel = sModel;
        this.classroomModel = cModel;

        setLayout(new BorderLayout());

        timetableTable = new JTable(timetableModel);
        timetableTable.setRowHeight(60);

        JPanel buttonPanel = new JPanel();
        JButton generateButton = new JButton("Generate Timetable");
        generateButton.addActionListener(e -> generateTimetable());

        buttonPanel.add(generateButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(timetableTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generateTimetable() {
        List<Teacher> teachers = teacherModel.getTeachers();
        List<Subject> subjects = subjectModel.getSubjects();
        List<Classroom> classrooms = classroomModel.getClassrooms();

        if (teachers.isEmpty() || subjects.isEmpty() || classrooms.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one teacher, subject and classroom first");
            return;
        }

        TimetableGenerator generator = new TimetableGenerator(9); 
        Timetable timetable = generator.generateTimetable(teachers, subjects, classrooms);

        timetableModel.setTimetable(timetable);
    }
}


// Model Classes
class Teacher {

    private String name;
    private List<String> subjects;
    private int maxPeriodPerDay;

    public Teacher(String name, List<String> subjects, int maxperiodPerDay) {
        this.name = name;
        this.subjects = new ArrayList<>(subjects);
        this.maxPeriodPerDay = maxperiodPerDay;

    }

    public String getName() {
        return name;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public int getMaxPeriodPerDay() {
        return maxPeriodPerDay;
    }
}

class Subject {

    private String name;
    private int weeklyPeriod;
    private boolean requiresLab;

    public Subject(String name, int weeklyPeriod, boolean requiresLab) {
        this.name = name;
        this.weeklyPeriod = weeklyPeriod;
        this.requiresLab = requiresLab;
    }

    public String getName() {
        return name;
    }

    public int getWeeklyPeriod() {
        return weeklyPeriod;
    }

    public boolean requiresLab() {
        return requiresLab;
    }
}

class Classroom {

    private String classroom;
    private boolean isLab;
    private int capacity;

    public Classroom(String classroom, boolean isLab, int capacity) {
        this.classroom = classroom;
        this.isLab = isLab;
        this.capacity = capacity;
    }

    public String getClassRoom() {
        return classroom;
    }

    public boolean isLab() {
        return isLab;
    }

    public int getCapacity() {
        return capacity;
    }
}

enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
}

class Timeslot {

    private DayOfWeek day;
    private int period;

    public Timeslot(DayOfWeek day, int period) {
        this.day = day;
        this.period = period;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Timeslot timeslot = (Timeslot) o;
        return period == timeslot.period && day == timeslot.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, period);
    }
}

class TimetableEntry {

    private Teacher teacher;
    private Classroom classroom;
    private Timeslot timeslot;
    private Subject subject;

    public TimetableEntry(Teacher teacher, Classroom classroom, Timeslot timeslot, Subject subject) {
        this.teacher = teacher;
        this.classroom = classroom;
        this.timeslot = timeslot;
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Subject getSubject() {
        return subject;
    }
}

class Timetable {

    private Map<Timeslot, TimetableEntry> entries = new HashMap<>();

    public void addEntry(Teacher teacher, Classroom classroom, Timeslot slot, Subject subject) {
        entries.put(slot, new TimetableEntry(teacher, classroom, slot, subject));
    }

    public TimetableEntry getEntry(Timeslot slot) {
        return entries.get(slot);
    }

    public boolean isTeacherAvailable(Teacher teacher, Timeslot slot) {
        return entries.values().stream()
                .noneMatch(e -> e.getTeacher().equals(teacher) && e.getTimeslot().equals(slot));
    }

    public boolean isClassroomAvailable(Classroom classroom, Timeslot slot) {
        return entries.values().stream()
                .noneMatch(e -> e.getClassroom().equals(classroom) && e.getTimeslot().equals(slot));
    }
}



// Table Models
class TeacherTableModel extends AbstractTableModel {

    private List<Teacher> teachers = new ArrayList<>();
    private String[] columns = {"Name", "Subjects", "Max Period/Day"};

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        fireTableRowsInserted(teachers.size() - 1, teachers.size() - 1);
    }

    public void removeTeacher(int row) {
        teachers.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    @Override
    public int getRowCount() {
        return teachers.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Teacher teacher = teachers.get(row);
        switch (column) {
            case 0:
                return teacher.getName();
            case 1:
                return String.join(", ", teacher.getSubjects());
            case 2:
                return teacher.getMaxPeriodPerDay();
            default:
                return null;
        }
    }
}

class SubjectTableModel extends AbstractTableModel {

    private List<Subject> subjects = new ArrayList<>();
    private String[] columns = {"Name", "Weekly Period", "Requires Lab"};

    public void addSubject(Subject subject) {
        subjects.add(subject);
        fireTableRowsInserted(subjects.size() - 1, subjects.size() - 1);
    }

    public void removeSubject(int row) {
        subjects.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    @Override
    public int getRowCount() {
        return subjects.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Subject subject = subjects.get(row);
        switch (column) {
            case 0:
                return subject.getName();
            case 1:
                return subject.getWeeklyPeriod();
            case 2:
                return subject.requiresLab() ? "Yes" : "No";
            default:
                return null;
        }
    }
}

class ClassroomTableModel extends AbstractTableModel {

    private List<Classroom> classrooms = new ArrayList<>();
    private String[] columns = {"Class Room", "Is Lab", "Capacity"};

    public void addClassroom(Classroom classroom) {
        classrooms.add(classroom);
        fireTableRowsInserted(classrooms.size() - 1, classrooms.size() - 1);
    }

    public void removeClassroom(int row) {
        classrooms.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Classroom> getClassrooms() {
        return classrooms;
    }

    @Override
    public int getRowCount() {
        return classrooms.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Classroom classroom = classrooms.get(row);
        switch (column) {
            case 0:
                return classroom.getClassRoom();
            case 1:
                return classroom.isLab() ? "Yes" : "No";
            case 2:
                return classroom.getCapacity();
            default:
                return null;
        }
    }
}

class TimetableTableModel extends AbstractTableModel {

    private Timetable timetable;
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private int periodsPerDay;
    private String[] periods;


    public TimetableTableModel(int periodsPerDay) {
        this.periodsPerDay = periodsPerDay;
        this.periods = new String[]{
                "8.00 - 8.40",
                "8.40 - 9.20",
                "9.20 - 10.00",
                "10.00 - 10.40",
                "BREAK",
                "11.00 - 11.40",
                "11.40 - 12.20",
                "12.20 - 13.00",
                "13.00 - 13.40"
        };
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return periodsPerDay;
    }

    @Override
    public int getColumnCount() {
        return days.length + 1;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Time" : days[column - 1];
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) return periods[row];  // ✅ show actual time

        if (timetable == null) return "";
        Timeslot slot = new Timeslot(DayOfWeek.values()[column - 1], row + 1);
        TimetableEntry entry = timetable.getEntry(slot);
        return entry != null
                ? String.format("<html>%s<br>%s<br>%s</html>",
                entry.getSubject().getName(),
                entry.getTeacher().getName(),
                entry.getClassroom().getClassRoom())
                : "";
    }
}



// Timetable Generator Algorithm

class TimetableGenerator {

    private int periodsPerDay;
    private final int daysPerWeek = 5; // Monday-Friday
    private final int breakPeriod = 5; // optional lunch break

    private final Set<String> doublePeriodSubjects = Set.of("Combined Maths", "Biology", "Physics", "Chemistry");

    public TimetableGenerator(int periodsPerDay) {
        this.periodsPerDay = periodsPerDay;
    }

    public Timetable generateTimetable(List<Teacher> teachers, List<Subject> subjects, List<Classroom> classrooms) {
        Timetable timetable = new Timetable();
        Random random = new Random();

        // Step 1: Create possible 2-period blocks
        List<Timeslot[]> doublePeriodBlocks = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            for (int period : new int[]{1, 2, 3, 6, 7}) { // only morning-friendly start periods
                if (period == breakPeriod || period + 1 == breakPeriod) continue;
                Timeslot slot1 = new Timeslot(day, period);
                Timeslot slot2 = new Timeslot(day, period + 1);
                doublePeriodBlocks.add(new Timeslot[]{slot1, slot2});
            }
        }

        // Step 2: Schedule subjects based on weeklyPeriod
        for (Subject subject : subjects) {
            if (subject.getName().equals("English") || subject.getName().equals("Git")) continue; // skip special subjects
            
            int periodsNeeded = subject.getWeeklyPeriod();
            boolean isDoublePeriodSubject = doublePeriodSubjects.contains(subject.getName());

            List<Teacher> qualifiedTeachers = teachers.stream()
                    .filter(t -> t.getSubjects().contains(subject.getName()))
                    .collect(Collectors.toList());

            List<Classroom> suitableRooms = classrooms.stream()
                    .filter(r -> !subject.requiresLab() || r.isLab())
                    .collect(Collectors.toList());

            int periodsScheduled = 0;

            if (isDoublePeriodSubject) {
                // Schedule in 2-period blocks
                int blocksNeeded = (int) Math.ceil(periodsNeeded / 2.0);
                while (periodsScheduled < blocksNeeded) {
                    boolean scheduled = false;
                    Collections.shuffle(doublePeriodBlocks);
                    Collections.shuffle(qualifiedTeachers);
                    Collections.shuffle(suitableRooms);

                    outerLoop:
                    for (Timeslot[] block : doublePeriodBlocks) {
                        for (Teacher teacher : qualifiedTeachers) {
                            for (Classroom room : suitableRooms) {
                                if (timetable.getEntry(block[0]) == null
                                        && timetable.getEntry(block[1]) == null
                                        && timetable.isTeacherAvailable(teacher, block[0])
                                        && timetable.isTeacherAvailable(teacher, block[1])
                                        && timetable.isClassroomAvailable(room, block[0])
                                        && timetable.isClassroomAvailable(room, block[1])
                                        && !isSubjectAlreadyScheduledOnDay(timetable, subject, block[0].getDay())) {

                                    timetable.addEntry(teacher, room, block[0], subject);
                                    timetable.addEntry(teacher, room, block[1], subject);

                                    periodsScheduled++;
                                    scheduled = true;
                                    break outerLoop;
                                }
                            }
                        }
                    }

                    if (!scheduled) {
                        System.out.println("Could not schedule all periods for: " + subject.getName());
                        break;
                    }
                }

            } else {
                // Schedule in single periods
                outerLoop:
                for (DayOfWeek day : DayOfWeek.values()) {
                    for (int period = 1; period <= periodsPerDay; period++) {
                        if (period == breakPeriod) continue; // skip break
                        Timeslot slot = new Timeslot(day, period);

                        for (Teacher teacher : qualifiedTeachers) {
                            for (Classroom room : suitableRooms) {
                                if (timetable.getEntry(slot) == null
                                        && timetable.isTeacherAvailable(teacher, slot)
                                        && timetable.isClassroomAvailable(room, slot)
                                        && !isSubjectAlreadyScheduledOnDay(timetable, subject, day)) {

                                    timetable.addEntry(teacher, room, slot, subject);
                                    periodsScheduled++;
                                    if (periodsScheduled >= periodsNeeded) break outerLoop;
                                }
                            }
                        }
                    }
                }
            }
        }
// Step 3: Schedule English and Git in last 2 periods
        List<String> specialSubjects = List.of("English", "Git");

        for (String subName : specialSubjects) {
            Subject subject = subjects.stream()
                    .filter(s -> s.getName().equals(subName))
                    .findFirst()
                    .orElse(null);
            if (subject == null) continue;

            List<Teacher> qualifiedTeachers = teachers.stream()
                    .filter(t -> t.getSubjects().contains(subject.getName()))
                    .collect(Collectors.toList());

            List<Classroom> suitableRooms = classrooms.stream()
                    .filter(r -> !subject.requiresLab() || r.isLab())
                    .collect(Collectors.toList());

            List<DayOfWeek> days = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
            Collections.shuffle(days);

            for (DayOfWeek day : days) {
                int period = (subName.equals("English")) ? periodsPerDay - 1 : periodsPerDay; // English: 2nd last, Git: last
                Timeslot slot = new Timeslot(day, period);

                Collections.shuffle(qualifiedTeachers);
                Collections.shuffle(suitableRooms);

                for (Teacher teacher : qualifiedTeachers) {
                    for (Classroom room : suitableRooms) {
                        if (timetable.getEntry(slot) == null
                                && timetable.isTeacherAvailable(teacher, slot)
                                && timetable.isClassroomAvailable(room, slot)) {

                            timetable.addEntry(teacher, room, slot, subject);
                            break;
                        }
                    }
                }
            }
        }

        return timetable;
    }

    // Helper method to check if a subject is already scheduled on a given day
    private boolean isSubjectAlreadyScheduledOnDay(Timetable timetable, Subject subject, DayOfWeek day) {
        for (int period = 1; period <= periodsPerDay; period++) {
            Timeslot slot = new Timeslot(day, period);
            TimetableEntry entry = timetable.getEntry(slot);
            if (entry != null && entry.getSubject().getName().equals(subject.getName())) {
                return true;
            }
        }
        return false;
    }
}