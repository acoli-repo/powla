# Dockerfile

FROM ubuntu:20.04
MAINTAINER Christian Chiarcos <christian.chiarcos@gmail.com>

# orig:
# FROM python:3-alpine

#
# Pepper & Paula stuff
########################

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME
RUN apt install -y maven

# Python
RUN apt-get install -y python3 python3-pip
RUN pip3 install lxml
# for Swagger
RUN pip3 install pyrsistent==0.16.1

# dependencies
RUN apt-get install -y git unzip wget

# clone pepper-wrapper
RUN git clone https://github.com/acoli-repo/pepper-wrapper && \
    wget -nc https://korpling.german.hu-berlin.de/saltnpepper/pepper/download/stable/Pepper_2021.03.01.zip -O pepper-wrapper/Pepper.zip && \
    cd pepper-wrapper && \
    unzip Pepper.zip

# clone CoNLL-RDF
RUN git clone https://github.com/acoli-repo/conll-rdf && \
    cd conll-rdf &&\
    echo | ./run.sh CoNLLStreamExtractor '#' ID

# clone and configure powla
RUN git clone https://github.com/acoli-repo/powla
RUN ln -s /conll-rdf /powla/experimental/paula
RUN ln -s /pepper-wrapper /powla/experimental/salt
RUN ln -s /powla/experimental/salt/toRDF.sh /bin/toRDF

#
# swagger config
####################
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY requirements.txt /usr/src/app/

RUN pip3 install --no-cache-dir -r requirements.txt

RUN pip3 install connexion[swagger-ui]

COPY . /usr/src/app

EXPOSE 8080

ENTRYPOINT ["python3"]

CMD ["-m", "swagger_server"]
