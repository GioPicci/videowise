FROM postgres:14

ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=videowise_db

# Copy initialization SQL script
COPY V1.0.0_initial_schema.sql /docker-entrypoint-initdb.d/V1.0.0__initial_schema.sql

# Expose PostgreSQL default port
EXPOSE 5432