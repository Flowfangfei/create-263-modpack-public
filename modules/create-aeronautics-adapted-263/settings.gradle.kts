pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral(); maven("https://maven.neoforged.net/releases") }
}
rootProject.name = "create-aeronautics-adapted-263"
include("sable", "simulated", "offroad", "aeronautics")
project(":sable").projectDir = file("../sable-adapted-263")
