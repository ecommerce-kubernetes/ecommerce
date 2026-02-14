FROM jenkins/jenkins:lts
USER root
RUN apt-get update && \ 
    apt-get install -y ansible sshpass & \
    rm -rf /var/lib/apt/llsts/*