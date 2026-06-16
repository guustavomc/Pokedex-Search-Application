# Pokedex REST API

This project is a REST API built with Spring Boot to serve Pokémon data from a JSON file (`pokedex.json`). It allows users to query Pokémon by ID, name, or type. This README provides a step-by-step guide to build, containerize, and deploy the application using Maven, Docker, and Kubernetes (with Kind for local development). Instructions are provided for both Windows and macOS/Linux users.

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 17**: For building the Spring Boot application.
- **Maven**: For dependency management and building the JAR.
- **Docker**: For containerizing the application.
- **Kind**: For running a local Kubernetes cluster.
- **kubectl**: For interacting with Kubernetes.
- **curl** or **Postman**: For testing API endpoints.

### Installation (Windows)

For Windows, use **Chocolatey** (a package manager) or manual installation:

1. **Install Chocolatey** (optional, run in Admin PowerShell):
   ```powershell
   Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
   ```

2. **Install Tools with Chocolatey**:
   ```powershell
   choco install openjdk17
   choco install maven
   choco install docker-desktop
   choco install kind
   choco install kubernetes-cli
   choco install curl
   ```

3. **Manual Installation**:
    - **Java 17**: Download from [Adoptium](https://adoptium.net/), set `JAVA_HOME` environment variable.
    - **Maven**: Download from [Apache Maven](https://maven.apache.org/download.cgi), add `bin` to `PATH`.
    - **Docker**: Install [Docker Desktop](https://www.docker.com/products/docker-desktop/), enable WSL 2 backend.
    - **Kind**: Download from [Kind releases](https://github.com/kubernetes-sigs/kind/releases), place in `C:\bin`.
    - **kubectl**: Download from [Kubernetes releases](https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/), place in `C:\bin`.

4. **Set Up WSL 2** (recommended for Kind):
   ```powershell
   wsl --install
   wsl --update
   wsl --set-default-version 2
   ```

### Installation (macOS/Linux)

```bash
# Install Homebrew (macOS)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install tools
brew install openjdk@17
brew install maven
brew install docker
brew install kind
brew install kubectl
```

## Project Structure

```
Pokedex-Search-Application/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── Main.java
│   │   │   ├── ReadPokemonService.java
│   │   │   ├── PokedexController.java
│   │   │   ├── Pokemon.java
│   │   ├── resources/
│   │   │   ├── pokedex.json
├── pom.xml
├── Dockerfile
├── pokedex-deployment.yaml
├── pokedex-service.yaml
├── pokedex-ingress.yaml
├── README.md
```

- `pokedex.json`: Contains Pokémon data (sourced from Purukitto/pokemon-data.json).
- `Dockerfile`: Defines the Docker image build process.
- Kubernetes manifests (`pokedex-deployment.yaml`, `pokedex-service.yaml`): Define the Kubernetes resources.

## Step 1: Build the JAR

The application is built using Maven to create an executable JAR file.

1. **Build the JAR**:

   ```bash
   mvn clean package -DskipTests
   ```

    - This compiles the code, runs the build, and generates `target/Pokedex-Search-Application-1.0-SNAPSHOT.jar`.
    - The `-DskipTests` flag skips tests for faster builds (ensure tests pass if you have them).

2. **Run the Application Locally** (Optional):

   ```bash
   mvn spring-boot:run
   ```

    - This starts the Spring Boot application on `http://localhost:8080`.

3. **Test API Endpoints Locally**: Use `curl` or a browser to test the API:

   ```bash
   # Get all Pokémon
   curl http://localhost:8080/api/pokemon
   
   # Get Pokémon with ID 1
   curl http://localhost:8080/api/pokemon/id/1
   
   # Get Pokémon named "Squirtle"
   curl http://localhost:8080/api/pokemon/search/Squirtle
   
   # Get Pokémon of type "Grass"
   curl http://localhost:8080/api/pokemon/type/Grass
   ```

   **Windows Alternative** (PowerShell):
   ```powershell
   Invoke-WebRequest -Uri http://localhost:8080/api/pokemon
   ```

## Step 2: Deploy to Kubernetes with Kind

We use **Kind** to run a local Kubernetes cluster and deploy the API using Kubernetes manifests.

### 2.1 Set Up a Kind Cluster

1. **Create a Kind Cluster**:

   ```bash
   kind create cluster --name pokedex
   ```

    - This starts a local Kubernetes cluster named `pokedex`.

2. **Verify the Cluster**:

   ```bash
   kubectl cluster-info --context kind-pokedex
   ```

    - Ensures `kubectl` is connected to the Kind cluster.

### 2.2 Build and Load the Docker Image into Kind

Build the Docker image locally and load it into the Kind cluster:

```bash
docker build -t pokedex-api .
kind load docker-image pokedex-api:latest --name pokedex
```

- This makes the image available to the Kind cluster without needing a registry or Docker Hub account.
- The deployment uses `imagePullPolicy: Never`, so Kubernetes will only use the locally loaded image.

### 2.3 Deploy Kubernetes Resources

The application is deployed using a `Deployment` and exposed via a `Service`. Optionally, an `Ingress` can be used for HTTP access.

1. **Create the Deployment Manifest** (`pokedex-deployment.yaml`):

   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: pokedex-api
     namespace: default
     labels:
       app: pokedex-api
   spec:
     replicas: 2
     selector:
       matchLabels:
         app: pokedex-api
     template:
       metadata:
         labels:
           app: pokedex-api
       spec:
         containers:
         - name: pokedex-api
           image: pokedex-api:latest
           imagePullPolicy: Never
           ports:
           - containerPort: 8080
           resources:
             requests:
               memory: "256Mi"
               cpu: "250m"
             limits:
               memory: "512Mi"
               cpu: "500m"
           livenessProbe:
             httpGet:
               path: /api/pokemon
               port: 8080
             initialDelaySeconds: 15
             periodSeconds: 10
           readinessProbe:
             httpGet:
               path: /api/pokemon
               port: 8080
             initialDelaySeconds: 5
             periodSeconds: 5
   ```

    - `imagePullPolicy: Never` tells Kubernetes to use the locally loaded image (via `kind load docker-image`) instead of pulling from a registry.

2. **Create the Service Manifest** (`pokedex-service.yaml`):

   ```yaml
   apiVersion: v1
   kind: Service
   metadata:
     name: pokedex-api-service
     namespace: default
   spec:
     selector:
       app: pokedex-api
     ports:
       - protocol: TCP
         port: 80
         targetPort: 8080
         nodePort: 30080
     type: NodePort
   ```

3. **Apply the Manifests**:

   ```bash
   kubectl apply -f k8s/pokedex-deployment.yaml
   kubectl apply -f k8s/pokedex-service.yaml
   ```

4. **Verify the Deployment**:

   ```bash
   kubectl get deployments
   kubectl get pods
   kubectl get services
   ```

    - Ensure the `pokedex-api` Deployment has 2/2 pods ready.
    - Check that `pokedex-api-service` is running with `type: NodePort`.

### 2.4 Access the API

The `NodePort` Service exposes the API on a high port (e.g., `30080`).

1. **Get the Cluster IP**:

   ```bash
   kubectl get nodes -o wide
   ```

    - Note the `INTERNAL-IP` of the Kind control plane node (e.g., `172.18.0.2`).

2. **Test the API**:

   ```bash
   curl http://<INTERNAL-IP>:30080/api/pokemon
   curl http://<INTERNAL-IP>:30080/api/pokemon/id/1
   curl http://<INTERNAL-IP>:30080/api/pokemon/search/Squirtle
   curl http://<INTERNAL-IP>:30080/api/pokemon/type/Grass
   ```

   **Windows Alternative** (PowerShell):
   ```powershell
   Invoke-WebRequest -Uri http://<INTERNAL-IP>:30080/api/pokemon
   ```

    - Replace `<INTERNAL-IP>` with the node’s IP.



## Troubleshooting

- **JAR Build Fails**:
    - Ensure `pom.xml` includes dependencies (`spring-boot-starter-web`, `jackson-databind`).
    - Check for errors: `mvn clean package`.

- **Docker Build Fails**:
    - Verify `pokedex.json` is in `src/main/resources`.
    - Check `Dockerfile` syntax and JAR name (`Pokedex-Search-Application-1.0-SNAPSHOT.jar`).
    - Ensure Docker Desktop is running (Windows).

- **Docker Push to Docker Hub Fails**:
    - Verify Docker Hub credentials: `docker login`.
    - Ensure the repository exists or create it on Docker Hub.
    - Check network connectivity:
      ```bash
      docker pull alpine:latest
      ```
    - Confirm the image tag matches your Docker Hub username.

- **Pods Not Starting**:
    - Check logs: `kubectl logs -l app=pokedex-api`.
    - Ensure the image is accessible:
        - For Docker Hub, verify the image exists: `docker pull <your-dockerhub-username>/pokedex-api:latest`.
        - For local Kind, load the image: `kind load docker-image pokedex-api:latest --name pokedex`.
    - Check pod events: `kubectl describe pod <pod-name>`.

- **Service Not Accessible**:
    - Verify Service selector: `kubectl describe service pokedex-api-service`.
    - Check node IP and port: `kubectl get nodes -o wide`.

- **Ingress Not Working**:
- 
    - Ensure NGINX controller is running: `kubectl get pods -n ingress-nginx`.
    - Verify hosts file entry (`C:\Windows\System32\drivers\etc\hosts` on Windows).

## Clean Up

To remove the Kubernetes resources:

```bash
kubectl delete -f k8s/pokedex-deployment.yaml
kubectl delete -f k8s/pokedex-service.yaml
```

To delete the Kind cluster:

```bash
kind delete cluster --name pokedex
```
## Restarting After a Reboot

If you shut down your PC and want to restart the `pokedex-api` cluster, follow these steps:

1. **Start Docker Desktop**:

    - Ensure Docker Desktop is running (open it manually if needed).

    - Verify:

      ```powershell
      docker ps
      ```

2. **Verify the Kind Cluster**:

   ```bash
   kind get clusters
   kubectl cluster-info --context kind-pokedex
   ```

    - If the cluster is missing, recreate it:

      ```bash
      kind create cluster --name pokedex
      ```

        - For Ingress, use:

          ```bash
          kind create cluster --name pokedex --config kind-config.yaml
          ```

3. **Check Kubernetes Resources**:

   ```bash
   kubectl get deployments
   kubectl get pods
   kubectl get services
   ```

  - If resources are missing, reload the image and reapply manifests:

    ```bash
    kind load docker-image pokedex-api:latest --name pokedex
    kubectl apply -f k8s/pokedex-deployment.yaml
    kubectl apply -f k8s/pokedex-service.yaml
    ```

4. **Test the API**:

   ```bash
   kubectl port-forward service/pokedex-api-service 8080:80
   ```

    - Access: `http://localhost:8080/api/pokemon`.

    - Or use NodePort:

      ```bash
      kubectl get nodes -o wide
      curl http://<INTERNAL-IP>:30080/api/pokemon
      ```

      **Windows Alternative**:

      ```powershell
      Invoke-WebRequest -Uri http://<INTERNAL-IP>:30080/api/pokemon
      ```
## If You Modify the Code

If you change the application code (e.g., update `PokedexController.java` or `pokedex.json`):

1. **Rebuild the JAR**:

   ```bash
   mvn clean package -DskipTests
   ```

    - This rebuilds the JAR file at `target/Pokedex-Search-Application-1.0-SNAPSHOT.jar`.

2. **Rebuild the Docker Image and Load into Kind**:

   ```bash
   docker build -t pokedex-api .
   kind load docker-image pokedex-api:latest --name pokedex
   ```

    - This rebuilds the local image and loads it into the Kind cluster.

3. **Restart the Pods to Pick Up the New Image**:

   ```bash
   kubectl apply -f k8s/pokedex-deployment.yaml
   kubectl delete pod -l app=pokedex-api
   ```

    - `kubectl apply` ensures the Deployment uses the latest configuration.
    - `kubectl delete pod` forces Kubernetes to recreate the pods using the newly loaded image.
...
## Learning Resources

- **Spring Boot**: [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- **Docker**: [Docker Getting Started](https://docs.docker.com/get-started/)
- **Kubernetes**: [Kubernetes Basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/)
- **Kind**: [Kind Quick Start](https://kind.sigs.k8s.io/docs/user/quick-start/)
- **kubectl**: [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)