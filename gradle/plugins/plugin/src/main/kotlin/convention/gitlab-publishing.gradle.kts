/**
 * Usage:
 * ./gradlew publishAllPublicationsToGitLabRepository
 */
package convention

import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication

plugins {
    id("maven-publish")
}

project.findProperty("gitlab.version")?.let { version = it as String }

// Do NOT set project.group here — setting the same group on all modules causes Gradle to treat
// same-named submodules (e.g. :feature:home:domain and :feature:course-list:domain) as having
// identical Maven coordinates, which breaks internal dependency resolution.
// Instead, set groupId and a unique artifactId directly on each MavenPublication.
val csdkGroup: String? = project.findProperty("gitlab.group") as String?

// Use the full module path as artifact ID to avoid coordinate conflicts between
// same-named modules (e.g. feature-home-domain vs feature-course-list-domain).
val fullPathArtifactId = project.path.replace(":", "-").trimStart('-')
val projectName: String = project.name

val gitlabProjectId: String? = project.findProperty("gitlab.projectId") as String?
    ?: System.getenv("CI_PROJECT_ID")

val gitlabBaseUrl: String? = project.findProperty("gitlab.baseUrl") as String?
val ciJobToken: String? = System.getenv("CI_JOB_TOKEN")
val privateToken: String? = project.findProperty("gitlab.privateToken") as String?

// Only configure the GitLab repository when we actually have a credential to authenticate with —
// otherwise a local, token-less build would register a repo with a null credential value.
if (gitlabBaseUrl != null && gitlabProjectId != null && (ciJobToken != null || privateToken != null)) {
    publishing {
        repositories {
            maven {
                name = "GitLab"
                url = uri("$gitlabBaseUrl/api/v4/projects/$gitlabProjectId/packages/maven")
                credentials(HttpHeaderCredentials::class) {
                    name = if (ciJobToken != null) "Job-Token" else "Private-Token"
                    value = ciJobToken ?: privateToken
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }

        publications.withType<MavenPublication>().configureEach {
            // Preserve per-target suffixes from KMP (e.g. "-jvm", "-android", "-iosArm64")
            // by replacing only the project-name prefix in the default artifact ID.
            afterEvaluate {
                csdkGroup?.let { groupId = it }
                artifactId = artifactId.replaceFirst(projectName, fullPathArtifactId)
            }
        }
    }
}
