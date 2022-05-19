## Using ELK to monitor throughJMX

_TODO_

https://medium.com/analytics-vidhya/installing-elk-stack-in-docker-828df335e421

see the [Docker-compose.yaml](./elk/Docker-compose.yaml "Open the docker compose file") file to install ELK through
Docker.

After installing the ELK through a docker-compose recipe, Kibana is reachable at http://localhost:5601

## Dockerize the Desktop java app

_TODO_

https://learnwell.medium.com/how-to-dockerize-a-java-gui-application-bce560abf62a

Prepare a Docker image (see [Dockerfile](../Dockerfile) "open the corresponding docker file")

and build tyhe image:

```shell
docker build --pull --rm -f "Dockerfile" -t monoclass2:latest "."
```

As soon as the docker image is built, you can execute it :

```shell
docker run --rm -it monoclass2:latest
```

