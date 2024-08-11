# VecoLuc: Vector Search Engine

## Abstract

VecoLuc is a scalable vector search engine that leverages Apache Lucene and the JDK's incubator vector API for high-performance vector operations. Designed to efficiently index and search documents using vector-based queries, VecoLuc supports various optimization techniques, including the use of the `jdk.incubator.vector` module to enhance computation speed and accuracy. The application is built using Akka HTTP for handling RESTful APIs, making it easy to integrate with other services.

## Features

- **Vector Search:** Efficient vector-based document indexing and searching.
- **Optimized Vector Operations:** Utilizes the `jdk.incubator.vector` module for performance enhancements.
- **REST API:** Exposes endpoints for document indexing and vector-based search queries.
- **Scalable:** Designed for horizontal scaling with Akka.

## Getting Started

### Prerequisites

- **Java 17** or higher (with `jdk.incubator.vector` module support)
- **SBT (Scala Build Tool)**
- **Apache Lucene** (included as a dependency)

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/vectoluc.git
   cd vectoluc
   ```

2. **Build the Project**
Ensure that you have Java 17 or higher installed, and the environment is set up correctly to support the incubator vector module.
    ```bash
    sbt clean compile
    ```

### Building the Project

To build VecoLuc, execute the following command:
```bash
sbt compile
```
This will compile the project, including any necessary dependencies like Apache Lucene and Akka.

### Running the Application
To start the VecoLuc server, run:

```bash
sbt run
```
The server will start listening for incoming requests on http://0.0.0.0:9000.

### Usage
VecoLuc provides a simple RESTful API to index and search documents based on vector embeddings.

#### Indexing a Document
To index a single document, send a POST request to /index/doc with the document's metadata and vector embeddings.

#### Request:

```bash
curl -X POST http://localhost:9000/index/doc -H "Content-Type: application/json" -d '{
"metadata": {
"id": "1",
"title": "First Document"
},
"embeddings": [0.1, 0.2, 0.3, 0.4]
}'
```

#### Indexing Multiple Documents
To index multiple documents at once, send a POST request to /index/docs.

#### Request:

```bash
curl -X POST http://localhost:9000/index/docs -H "Content-Type: application/json" -d '[
{
"metadata": {
"id": "2",
"title": "Second Document"
},
"embeddings": [0.2, 0.3, 0.4, 0.5]
},
{
"metadata": {
"id": "3",
"title": "Third Document"
},
"embeddings": [0.3, 0.4, 0.5, 0.6]
}
]'
```

#### Searching for Documents
To search for documents using a vector query, send a GET request to /search with the query and topN parameters.

#### Request:

```bash
curl "http://localhost:9000/search?query=0.1,0.2,0.3,0.4&topN=5"
```
#### Response:

```json
{
"status": "success",
"latencyMillis": 10,
"data": {
"results": [
{
"fields": {
"id": "1",
"title": "First Document"
},
"score": 0.95
}
],
"count": 1
}
}
```

### Optimizing Vector Operations
VecoLuc takes advantage of the JDK's jdk.incubator.vector module to optimize vector computations. This module provides high-performance, hardware-accelerated vector operations, making VecoLuc suitable for large-scale vector searches. To ensure the optimizations are enabled, include the following JVM options when running the application:

```bash
sbt -J--add-modules=jdk.incubator.vector run
```

This will enable the vector API and ensure that VecoLuc utilizes hardware acceleration where available.

### Contributing

Contributions to VecoLuc are welcome! Please fork the repository and submit a pull request.

### License

VecoLuc is licensed under the Apache License, Version 2.0. See the `LICENSE` file for more details.

### Contact

For questions, issues, or suggestions, feel free to open an issue on GitHub or contact [project maintainer](https://blog.ivan.digital) directly .