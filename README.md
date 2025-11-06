# YouTrack Workflow Analysis Agent

An AI-powered agent that analyzes YouTrack workflows using OpenAI's GPT-4. The agent can retrieve and analyze issues, workflows, and workflow rules from your YouTrack instance.

## Prerequisites

- Java 24
- YouTrack instance with API access
- OpenAI API key

## Configuration

Create a `.env` file in the project root with the following variables (listed in `.env.example`):

```
YOUTRACK_BASE_URL=https://your-youtrack-instance.com
YOUTRACK_TOKEN=perm:your-token-here
OPENAI_API_KEY=sk-your-key-here
```

### Local Java Configuration

If you need to specify a custom Java installation, create or edit `~/.gradle/gradle.properties`:

`gradle.properties`
```
org.gradle.java.home=/path/to/your/jdk
```

## Building the Project

Navigate to the workflow-agent directory and build:

```bash
cd workflow-agent
./gradlew build
```

## Running the Application

Run the application using Gradle:

```bash
./gradlew run -q
```

## Running Tests

Execute the test suite:

```bash
./gradlew test
```

## Project Structure

```
workflow-agent/
├── src/main/kotlin/
│   ├── agent/          # Agent implementation
│   ├── api/            # YouTrack API client
│   ├── config/         # Configuration classes
│   ├── factory/        # Dependency injection
│   ├── http/           # HTTP service layer
│   ├── model/          # Data models
│   ├── service/        # Business logic
│   ├── tools/          # Agent tools
│   └── Main.kt         # Application entry point
└── build.gradle.kts    # Build configuration
```

## Continuous Integration

This project uses GitHub Actions for CI. The workflow runs tests on every push and pull request.



### Suggested reads [POTENTIAL IMPROVEMENTS](./IMPROVEMENTS.md) & [IMPLEMENTATION NOTES](./IMPLEMENTATION.md)