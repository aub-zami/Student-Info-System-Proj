import java.io.*;
import java.util.*;

// THE PARENT CLASS
class BaseEntity {
    protected String fileName;

    public BaseEntity(String fileName) {
        this.fileName = fileName;
    }
    //
    public String saveToCSV(String... data) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
            out.println(String.join(",", data));
            return "SUCCESS. ";
        } catch (IOException e) { return "ERROR. "; }
    }
    // para paupdate sa entire file after modifications like pag set Program Code to "NULL" for affected students
    public void saveAllToCSV(List<String[]> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) { 
            for (String[] row : data) {
                writer.println(String.join(",", row));
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
    // delete for Student kay di siya mag-cascade, so diretso delete lang. Ang Program and College kay naay validation sa child records, so di madelete if naa pay naka-link nga records.
    public String delete(String key) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.split(",")[0].equalsIgnoreCase(key)) lines.add(line);
            }
        } catch (IOException e) { return "ERROR. "; }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            for (String l : lines) out.println(l);
            return "SUCCESS. ";
        } catch (IOException e) { return "ERROR. "; }
    }

    // para ma-check if existing na ang key (ID for Student, Code for Program/College)
    public boolean exists(String key) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.split(",")[0].equalsIgnoreCase(key)) return true;
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return false;       
    }
    // para ma-update ang record sa file, either by replacing a specific column value or the entire line
    protected String modifyFile(String targetFile, int colIdx, String oldVal, String newVal, String fullLineReplacement) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length > colIdx && data[colIdx].equalsIgnoreCase(oldVal)) {
                if (fullLineReplacement != null) {
                    lines.add(fullLineReplacement);
                } else {
                    data[colIdx] = newVal.toUpperCase();
                    lines.add(String.join(",", data));
                }
            } else {
                lines.add(line);
            } 
        }
    } catch (IOException e) { return "ERROR. "; } 

    // para ibutang ang updated list sa file 
    try (PrintWriter out = new PrintWriter(new FileWriter(targetFile))) {
        for (String l : lines) out.println(l);
        return "SUCCESS. ";
    } catch (IOException e) { return "ERROR. "; }
}
    public String formatName(String name) {
        if (name == null || name.isEmpty()) {
        return name;
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    
    }
    // para ma-check if a key is being used in a specific column of another file (e.g., Program Code in student.csv)
    public boolean isKeyUsed(String key, int colIdx) {
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = br.readLine()) != null) {
        String[] data = line.split(",");
            if (data.length > colIdx && data[colIdx].equalsIgnoreCase(key)) {
            return true; 
            }
        }
    } 
    catch (IOException e) { }
    return false;
}
    public List<String[]> fetchData() {
    List<String[]> data = new ArrayList<>(); 
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = br.readLine()) != null) {
            //  icheck if line is not empty aron walay blank lines after new entry
            if (!line.trim().isEmpty()) { 
                data.add(line.split(","));
            }
        }
    } catch (IOException e) {
        e.printStackTrace(); 
    } 
    return data;
}   //pila ka records ang maapektuhan sa deletion sa Program or College
    public int countAffectedEntries(String targetKey, int colIndex) {
    int count = 0;
    List<String[]> data = fetchData(); // This calls the child's CSV reader
    for (String[] row : data) {
        if (row.length > colIndex && row[colIndex].equalsIgnoreCase(targetKey)) {
            count++;
        }
    } return count;
}   // para ma-nullify ang Program Code or College Code sa affected records after deletion
    public void nullifyAffectedEntries(String targetKey, int colIndex) {
    List<String[]> data = fetchData();
    boolean modified = false;

    for (String[] row : data) {
        // Check if the specific column matches the deleted ID/Code
        if (row.length > colIndex && row[colIndex].equalsIgnoreCase(targetKey)) {
            row[colIndex] = "NULL"; 
            modified = true;
        }
    }
    if (modified) {
        saveAllToCSV(data); // Write updated list back to the CSV file using method that accepts List<String[]>
    }
}
}
// THE CHILD CLASSES 
class Student extends BaseEntity {
    public Student() { super("student.csv"); }

    //CRUD ADD Student with validations
     
    public String add(String id, String first, String last, String pCode, String year, String gender) {
        // ID format XXXX-NNNN
        if (!id.matches("\\d{4}-\\d{4}")) return "Error: ID must follow XXXX-NNNN format.";
        
        // Names must only be letters
        if (!first.matches("[a-zA-Z ]+") || !last.matches("[a-zA-Z ]+")) 
            return "Error: Names must only contain letters.";

        // Year must be in number
        if (!year.matches("\\d+")) return "Error: Year must be in number.";

        Program program = new Program(); 
        if (!program.exists(pCode)) return "Error: Program " + pCode + " does not exist.";

        if (this.exists(id)) return "Error: Student ID " + id + " already exists.";

        String status = saveToCSV(id.toUpperCase(), formatName(first), formatName(last), pCode.toUpperCase(), year, formatName(gender));
        return status.contains("SUCCESS") ? "Student " + id + " added successfully." : "Save failed.";
    }
    //CRUD UPDATE Student with validations
    public String update(String oldId, String newId, String first, String last, String pCode, String year, String gender) {
        if (!newId.matches("\\d{4}-\\d{4}")) return "Error: ID must follow XXXX-NNNN format.";
        if (!year.matches("\\d+")) return "Error: Year must be in number.";

        Program program = new Program();
        if (!program.exists(pCode)) return "Error: Program " + pCode + " does not exist.";

        String newLine = newId.toUpperCase() + "," + formatName(first) + "," + formatName(last) + "," + 
                         pCode.toUpperCase() + "," + year + "," + formatName(gender);

        return modifyFile(this.fileName, 0, oldId, null, newLine);
    }

    public String getCollegeForProgram(String pCode) {
        Program programLogic = new Program();
        List<String[]> allPrograms = programLogic.fetchData();
        for (String[] p : allPrograms) {
            if (p.length > 2 && p[0].equalsIgnoreCase(pCode)) {
                return p[2]; // Return College Code
            }
        }
        return "NULL"; 
    }
}

class Program extends BaseEntity {
    public Program() { super("program.csv"); }
    // Handles cascading updates to Students
    public String update(String oldCode, String newCode, String newName, String collegeCode) {
    College college = new College();
    if (!college.exists(collegeCode)) return "Error: College " + collegeCode + " does not exist.";
    if (!newName.matches("[a-zA-Z ]+")) return "Error: Program name must only contain letters.";

    String newLine = newCode.toUpperCase() + "," + newName.toUpperCase() + "," + collegeCode.toUpperCase();
    String status = modifyFile(this.fileName, 0, oldCode, null, newLine);

    if (status.contains("SUCCESS")) {
        modifyFile("student.csv", 3, oldCode, newCode, null); // Cascade
        return "Program " + oldCode + " updated successfully.";
    }
    return status;
}   //CRUD ADD Program with validation for existing College and duplicate Program Code
    public String add(String code, String name, String collegeCode) {
        // Validate that the referenced College exists
        College college = new College();
        
        if (!code.matches("[a-zA-Z]+")) return "Error: Program code must only contain letters.";
        if (!name.matches("[a-zA-Z ]+")) return "Error: Program name must only contain letters.";
        if (this.exists(code)) {
        return "Error: Program code " + code + " already exists.";
    }
        if (!college.exists(collegeCode)) {
        return "Error: College code " + collegeCode + " does not exist. Please add the college first.";
    }
        String status = saveToCSV(code.toUpperCase(), name.toUpperCase(), collegeCode.toUpperCase());
        return status.contains("SUCCESS") ? "Program " + code + " added successfully." :"Update failed, ensure the file is not open in another program.";

    }
    //CRUD DELETE PROGRAM with validation 
    public String delete(String code) {
    // Check if naay students nga naka-enroll ani nga program before delete
    Student studentLogic = new Student();
    if (studentLogic.isKeyUsed(code, 3)) { // Column 3 in student.csv is Program Code
        return "Error: Cannot delete. Students are still enrolled in " + code;
    }
   String status = super.delete(code); 
        return status.contains("SUCCESS") ? "Program " + code + " deleted successfully." :"Update failed, ensure the file is not open in another program.";
    }

    // HELPER: Fetches list for "Bachelor of Comp Sci - BSCS" dropdown
    public List<String> getProgListToChoose() {
        List<String> list = new ArrayList<>();
        List<String[]> data = fetchData();
        for (String[] row : data) {
            if (row.length >= 2) {
                list.add(row[1] + " - " + row[0]); // Format: Name - Code
            }
        }
        return list;
    }
}

class College extends BaseEntity {
    public College() { super("college.csv"); }

    //  check kung naay programs nga naka-link ani nga college, then cascade the changes to program.csv
    public String update(String oldCode, String newCode, String newName) {
    // Correctly format the line: CODE,NAME
    if (!newName.matches("[a-zA-Z ]+")) return "Error: College name must only contain letters.";
    String newLine = newCode.toUpperCase() + "," + newName.toUpperCase();
    
    // Pass the newLine as the fullLineReplacement (the last argument)
    String status = modifyFile(this.fileName, 0, oldCode, null, newLine);

    if (status.contains("SUCCESS")) {
        // Cascade to Program table
        modifyFile("program.csv", 2, oldCode, newCode, null); 
        return "College updated successfully.";
    }
    return "Update failed. Ensure the file is not open in another program.";
}
//CRUD ADD College with validation for duplicate College Code
    public String add(String code, String name) {
        if (!code.matches("[a-zA-Z]+")) return "Error: College code must only contain letters.";
        if (!name.matches("[a-zA-Z ]+")) return "Error: College name must only contain letters.";
        if (this.exists(code)) {
        return "Error: College code " + code + " already exists.";
    }
        String status = saveToCSV(code.toUpperCase(), name.toUpperCase());
        return status.contains("SUCCESS") ? "College " + code + " added successfully." :"Update failed, ensure the file is not open in another program.";

    }
//CRUD DELETE College with validation for existing linked Programs
    public String delete(String code) {
    // if naay programs nga naka-link ani nga college
        Program programLogic = new Program();
        if (programLogic.isKeyUsed(code, 2)) { // Column 2 College Code
        return "Error: Cannot delete. Programs are still offered by " + code;
    }       
    String status = super.delete(code);
        return status.contains("SUCCESS") ? "College " + code + " deleted successfully." :"Update failed, ensure the file is not open in another program.";
}}