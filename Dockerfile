FROM ubuntu:22.04
RUN apt-get update && apt-get install -y x11-apps openjdk-17-jdk
ARG user=hakon
ARG home=/home/$user
RUN groupadd -g 1000 $user
RUN useradd -d $home -s /bin/bash -m $user -u 1000 -g 1000 \
        && echo $user:ubuntu | chpasswd \
        && adduser $user sudo
WORKDIR $home
USER $user
ENV HOME $home
WORKDIR $home/apps
COPY . $home/apps
ENTRYPOINT ["$home/apps/scripts/run.sh"]
