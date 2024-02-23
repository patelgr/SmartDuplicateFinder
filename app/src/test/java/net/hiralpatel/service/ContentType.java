package net.hiralpatel.service;

import java.nio.file.Path;

public enum ContentType {
    APOLLO_MISSION_UNIQUE_CONTENT("apollo11_mission_details.txt", "The Apollo 11 mission of 1969 was the first manned mission to land on the Moon.", true, "missions/apollo"),
    VOYAGER_MISSION_UNIQUE_CONTENT("voyager1_details.txt", "Voyager 1, launched in 1977, is the farthest human-made object from Earth.", true, "missions/voyager"),
    PERSEVERANCE_ROVER_UNIQUE_CONTENT("perseverance_rover_info.txt", "Mars Rover Perseverance, launched in 2020, is searching for signs of ancient life on Mars.", true, "rovers/perseverance"),
    HUBBLE_TELESCOPE_UNIQUE_CONTENT("hubble_telescope_info.txt", "The Hubble Space Telescope, launched in 1990, has taken some of the most detailed images of distant galaxies.", true, "telescopes/hubble"),
    ISS_PROJECT_UNIQUE_CONTENT("iss_project_info.txt", "The International Space Station, operational since 2000, is a joint project among five participating space agencies.", true, "stations/iss"),
    JUPITER_FACT_UNIQUE_CONTENT("jupiter_facts.txt", "Jupiter is the largest planet in our solar system, with a diameter of 86,881 miles.", true, "facts/jupiter"),
    VENUS_FACT_UNIQUE_CONTENT("venus_facts.txt", "Venus is the hottest planet in our solar system with surface temperatures over 470°C.", true, "facts/venus"),
    MERCURY_ORBIT_UNIQUE_CONTENT("mercury_orbit_details.txt", "Mercury, the closest planet to the Sun, completes an orbit every 88 Earth days.", true, "orbits/mercury"),
    SOLAR_ECLIPSE_EVENT_UNIQUE_CONTENT("solar_eclipse_event_info.txt", "A total solar eclipse occurs when the Moon completely covers the Sun, as seen from Earth.", true, "events/solar_eclipse"),
    BIG_DIPPER_FACT_DUPLICATE_CONTENT("big_dipper_facts.txt", "The Big Dipper is not a constellation itself, but part of Ursa Major, the Great Bear.", false, "facts/big_dipper"),
    MILKY_WAY_FACT_DUPLICATE_CONTENT("milky_way_facts.txt", "The Milky Way, our galaxy, contains over 100 billion stars and is about 100,000 light-years across.", false, "facts/milky_way"),

    ISS_DUPLICATE_CONTENT("iss_details.txt", "Content about the International Space Station, intended for duplicate testing.", false, "stations/iss", "missions/apollo/iss_duplicate"),
    VENUS_DUPLICATE_CONTENT("venus_facts.txt", "Content about Venus, intended for duplicate testing.", false, "facts/venus", "orbits/mercury/venus_duplicate");

    private final String fileName;
    private final String text;
    private final boolean isUnique;
    private final String primaryDirectory;
    private final String duplicateDirectory; // Optional, only for duplicates

    ContentType(String fileName, String text, boolean isUnique, String primaryDirectory) {
        this(fileName, text, isUnique, primaryDirectory, null);
    }

    ContentType(String fileName, String text, boolean isUnique, String primaryDirectory, String duplicateDirectory) {
        this.fileName = fileName;
        this.text = text;
        this.isUnique = isUnique;
        this.primaryDirectory = primaryDirectory;
        this.duplicateDirectory = duplicateDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getText() {
        return text;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public String getPrimaryDirectory() {
        return primaryDirectory;
    }

    public String getDuplicateDirectory() {
        return duplicateDirectory;
    }

    public Path getFilePath(Path root) {
        return root.resolve(primaryDirectory).resolve(fileName);
    }

    public Path getDuplicateFilePath(Path root) {
        return root.resolve(duplicateDirectory).resolve(fileName);
    }
}
