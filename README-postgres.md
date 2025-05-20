# PostgreSQL Docker Setup for Secret Stash

This guide explains how to use Docker to run PostgreSQL locally for development.

## Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop/) installed on your system
- [Docker Compose](https://docs.docker.com/compose/install/) (usually comes with Docker Desktop)

## Getting Started

1. Start the PostgreSQL container:

```bash
docker-compose up -d
```

This command will:
- Pull the PostgreSQL 15 image if not already available
- Start a container named `secretstash-postgres`
- Create a volume for persistent data storage
- Map port 5432 to your localhost
- Set up the database with the credentials from docker-compose.yml

2. Verify the container is running:

```bash
docker ps
```

You should see your PostgreSQL container in the list of running containers.

## Connection Details

The PostgreSQL instance is configured with:

- **Host**: localhost
- **Port**: 5432
- **Database**: secretstash
- **Username**: secretstash
- **Password**: secretstash

These match the configuration in your `application.properties` file.

## Useful Commands

### View container logs:
```bash
docker logs secretstash-postgres
```

### Stop the container:
```bash
docker-compose down
```

### Stop container and remove data volume:
```bash
docker-compose down -v
```

### Connect to psql command line:
```bash
docker exec -it secretstash-postgres psql -U postgres -d secretstash
```

## Notes

- The data is persisted in a Docker volume named `postgres-data`
- The container is configured with health checks to ensure PostgreSQL is ready
- If port 5432 is already in use on your machine, modify the port mapping in the docker-compose.yml file
