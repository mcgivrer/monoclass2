FROM ubuntu:22.04
RUN apt-get update && apt-get install -y x11-apps openjdk-18-jdk maven vim git
ARG user=hakon
ARG home=/home/$user
RUN groupadd -g 1000 $user
RUN useradd -d $home -s /bin/bash -m $user -u 1000 -g 1000
RUN echo $user:ubuntu | chpasswd
RUN adduser $user sudo
WORKDIR $home
USER $user
ENV HOME $home
WORKDIR $home/apps
COPY . $home/apps
ENTRYPOINT [ "./scripts/build.sh","a" ]
