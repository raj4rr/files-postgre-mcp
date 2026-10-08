# Spring AI File & PostgreSQL MCP Server

A production-grade **Model Context Protocol (MCP) Server** built with **Java 17+**, **Spring Boot 3.4**, **Spring AI**, **Spring Data JPA**, and **Maven**.

This MCP server allows AI hosts (such as Claude Desktop, Cursor, or Antigravity IDE) to interact with your local file system (reading, writing, browsing folders) and persist results directly into a **PostgreSQL** database (or H2 in-memory fallback).

---

## 🛠️ Features & Tools Exposed

### 📁 File Operations (`FileToolService`)
* **`list_directory`**: Lists files and subfolders with optional glob filtering (`*.txt`, `*.java`, etc.) and recursive search.
* **`read_file`**: Reads text file contents with optional maximum line limits.
* **`write_file`**: Writes or appends text to a file, automatically creating parent directories if needed.
* **`delete_file`**: Safely deletes a file from the file system.
* **`get_file_info`**: Retrieves detailed metadata (file size, line count, permissions, modified date).

### 🐘 Database Operations (`DatabaseToolService`)
* **`save_file_record`**: Saves file execution details, content summary, status, and JSON metadata into the PostgreSQL `file_records` table.
* **`get_file_records`**: Queries saved records by status (e.g., `SUCCESS`, `PROCESSED`) or file name keyword search.
* **`execute_sql`**: Executes custom SQL queries (`SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE`) against PostgreSQL.

---

## 🏗️ Project Architecture

```
files-postgre-mcp/
├── pom.xml
├── mvnw
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/example/mcp/
│   │   │   ├── FilesPostgreMcpApplication.java  # Main entry point
│   │   │   ├── config/
│   │   │   │   └── McpToolConfig.java           # MCP ToolCallbackProvider bean
│   │   │   ├── dto/
│   │   │   │   ├── FileInfoDto.java
│   │   │   │   ├── FileOperationResultDto.java
│   │   │   │   └── QueryResultDto.java
│   │   │   ├── entity/
│   │   │   │   └── FileRecordEntity.java        # JPA Entity for PostgreSQL
│   │   │   ├── repository/
│   │   │   │   └── FileRecordRepository.java    # Spring Data JPA Repository
│   │   │   └── service/
│   │   │       ├── FileToolService.java         # MCP File Tools (@Tool)
│   │   │       └── DatabaseToolService.java     # MCP DB Tools (@Tool)
│   │   └── resources/
│   │       ├── application.properties           # PostgreSQL config
│   │       ├── application-h2.properties        # In-memory H2 config
│   │       └── application-stdio.properties     # STDIO mode config for MCP hosts
│   └── test/
│       └── java/com/example/mcp/
│           └── FilesPostgreMcpApplicationTests.java
```

---

## 🚀 Getting Started

### Prerequisites
* **Java 17** or higher
* **PostgreSQL** (Optional for local testing; H2 in-memory mode is provided out of the box)

### 1. Build the Application
Use the included Maven wrapper (`./mvnw`):

```bash
./mvnw clean package
```

### 2. Configure Database

By default, the server connects to PostgreSQL at `jdbc:postgresql://localhost:5432/mcpdb`.

You can override credentials via environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:15432/mcpdb
export SPRING_DATASOURCE_USERNAME=postgresql
export SPRING_DATASOURCE_PASSWORD=postgresql

java -jar target/files-postgre-mcp-1.0.0-SNAPSHOT.jar
```

Or run with **H2 in-memory database** for instant zero-config testing:

```bash
java -jar target/files-postgre-mcp-1.0.0-SNAPSHOT.jar --spring.profiles.active=h2
```

---

## 🔌 Connecting to MCP Hosts (Claude Desktop / Cursor)

### Option A: STDIO Transport (Recommended for Local Clients)
Add to your `claude_desktop_config.json` (e.g., `~/Library/Application Support/Claude/claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "files-postgre-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/rajesh/Desktop/Java/files-postgre-mcp/target/files-postgre-mcp-1.0.0-SNAPSHOT.jar",
        "--spring.profiles.active=stdio,h2"
      ]
    }
  }
}
```

### Option B: HTTP / SSE Transport (Remote Web Server)
Run the app in WebMVC mode:

```bash
java -jar target/files-postgre-mcp-1.0.0-SNAPSHOT.jar
```

Endpoints exposed:
* SSE connection: `http://localhost:8080/mcp/sse`
* Message endpoint: `http://localhost:8080/mcp/message`

---

## 🧪 Testing

Run the automated integration suite:

```bash
./mvnw test
```

This verifies folder browsing, file reading/writing, metadata extraction, PostgreSQL database saving, and query execution.
