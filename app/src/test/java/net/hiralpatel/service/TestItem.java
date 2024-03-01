package net.hiralpatel.service;

public class TestItem {
    private String name;
    private String type;
    private String description;
    private TestFile[] files;
    private TestStructures testStructures;
    private String[] expectedResult; // Added field for expected results

    public String[] getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String[] expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public TestFile[] getFiles() {
        return files;
    }

    public void setFiles(TestFile[] value) {
        this.files = value;
    }

    public TestStructures getStructures() {
        return testStructures;
    }

    public void setStructures(TestStructures value) {
        this.testStructures = value;
    }
}