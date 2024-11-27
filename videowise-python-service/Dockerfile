# Use the official NVIDIA CUDA image as the base (with Ubuntu 20.04)
FROM nvidia/cuda:12.0.1-cudnn8-runtime-ubuntu20.04 as base

LABEL authors="giovanni.piccinini"

ENV DEBIAN_FRONTEND=noninteractive

# Install Python 3.10.15 and dependencies
RUN apt-get update && apt-get install -y \
    git \
    gcc \
    ffmpeg \
    curl \
    python3.10 \
    python3-pip \
    tzdata \
    wkhtmltopdf \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install --upgrade pip

# Set the working directory
WORKDIR /app

# Copy the requirements.txt file into the container
COPY requirements.txt /app/requirements.txt

# Install dependencies from the requirements.txt using pip
RUN pip install --no-cache-dir -r /app/requirements.txt

# Copy the rest of your Python project into the container
COPY . /app

# Expose the port if the service runs on a specific port
EXPOSE 8000

# Set the entry point for the Python application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
