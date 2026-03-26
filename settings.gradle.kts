pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "EsportsSpace"
include(":app")
include(":core-common")
include(":core-data")
include(":core-network")
include(":core-ui")
include(":feature-games")
include(":feature-news")
include(":feature-datacenter")
include(":feature-livestream")
include(":feature-performance")
include(":feature-agent")
