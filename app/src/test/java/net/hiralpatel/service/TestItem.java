package net.hiralpatel.service;

public class TestItem {
    private String name;
    private String type;
    private String description;
    private TestFile[] files;
    private Structures structures;

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getType() { return type; }
    public void setType(String value) { this.type = value; }

    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }

    public TestFile[] getFiles() { return files; }
    public void setFiles(TestFile[] value) { this.files = value; }

    public Structures getStructures() { return structures; }
    public void setStructures(Structures value) { this.structures = value; }
}