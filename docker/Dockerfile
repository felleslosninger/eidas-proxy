FROM  maven:3.9.6-eclipse-temurin-17 as builder

WORKDIR /data

ARG GIT_PACKAGE_TOKEN

# Download our redis-lib
ARG REDIS_LIB_VERSION=1.0.2
RUN curl -H "Authorization: token ${GIT_PACKAGE_TOKEN}" -L -O \
  https://maven.pkg.github.com/felleslosninger/eidas-redis-lib/no/idporten/eidas/eidas-redis/${REDIS_LIB_VERSION}/eidas-redis-${REDIS_LIB_VERSION}.jar
RUN curl -H "Authorization: token ${GIT_PACKAGE_TOKEN}" -L -O \
  https://maven.pkg.github.com/felleslosninger/eidas-redis-lib/no/idporten/eidas/eidas-redis-node/${REDIS_LIB_VERSION}/eidas-redis-node-${REDIS_LIB_VERSION}.jar
RUN curl -H "Authorization: token ${GIT_PACKAGE_TOKEN}" -L -O \
  https://maven.pkg.github.com/felleslosninger/eidas-redis-lib/no/idporten/eidas/eidas-redis-specific-communication/${REDIS_LIB_VERSION}/eidas-redis-specific-communication-${REDIS_LIB_VERSION}.jar

# Logstash-logback-endcoder to enable JSON logging (needs jackson). Version must be competable with Logback version in eu eIDAS parent pom.xml.
ARG LOG_LIB_VERSION=8.0
RUN curl -L -O https://repo1.maven.org/maven2/net/logstash/logback/logstash-logback-encoder/${LOG_LIB_VERSION}/logstash-logback-encoder-${LOG_LIB_VERSION}.jar
ARG JACKSON_LIB_VERSION=2.15.2
RUN curl -L -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/${JACKSON_LIB_VERSION}/jackson-core-${JACKSON_LIB_VERSION}.jar
RUN curl -L -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/${JACKSON_LIB_VERSION}/jackson-databind-${JACKSON_LIB_VERSION}.jar
RUN curl -L -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/${JACKSON_LIB_VERSION}/jackson-annotations-${JACKSON_LIB_VERSION}.jar

# Download & build EU-eidas software
ARG EIDAS_NODE_VERSION=2.9.0
RUN git clone --depth 1 --branch eidasnode-${EIDAS_NODE_VERSION} https://ec.europa.eu/digital-building-blocks/code/scm/eid/eidasnode-pub.git

# Add our custom libs and config to EU-eidas software before build
RUN mkdir -p eidasnode-pub/EIDAS-Node-Proxy/src/main/webapp/WEB-INF/lib && cp /data/eidas-redis-*${REDIS_LIB_VERSION}.jar eidasnode-pub/EIDAS-Node-Proxy/src/main/webapp/WEB-INF/lib/
RUN cp /data/logstash-logback-encoder-*.jar /data/jackson-*.jar eidasnode-pub/EIDAS-Node-Proxy/src/main/webapp/WEB-INF/lib/
COPY docker/proxy/config/proxySpecificCommunicationCaches.xml eidasnode-pub/EIDAS-SpecificCommunicationDefinition/src/main/resources/
COPY docker/proxy/logback.xml eidasnode-pub/EIDAS-Node-Proxy/src/main/resources/logback.xml

#signature whiteliste use our list to package eu.eidas.auth.engine.configuration.dom
#COPY docker/proxy/KeyStoreSignatureConfigurator.java eidasnode-pub/EIDAS-Node-Proxy/src/main/java/eu/eidas/auth/engine/configuration/dom/KeyStoreSignatureConfigurator.java

#Add HSM config
COPY docker/luna/hsm.cfg eidasnode-pub/EIDAS-Node-Proxy/src/main/webapp/WEB-INF/

# Build eidas proxy service
RUN cd eidasnode-pub && mvn clean install --file EIDAS-Parent/pom.xml -P NodeOnly -P-specificCommunicationJcacheIgnite -DskipTests

#HSM
COPY docker/luna/Luna_min_client.tar /tmp
RUN mkdir -p /usr/local/luna
RUN tar xvf /tmp/Luna_min_client.tar --strip 1 -C /usr/local/luna

FROM tomcat:9.0.104-jre17-temurin-jammy

#Fjerner passord fra logger ved oppstart
RUN sed -i -e 's/FINE/WARNING/g' /usr/local/tomcat/conf/logging.properties
# Fjerner default applikasjoner fra tomcat
RUN rm -rf /usr/local/tomcat/webapps.dist

COPY docker/bouncycastle/bcprov-jdk18on-1.78.1.jar /usr/local/lib/bcprov-jdk18on-1.78.1.jar
COPY docker/java-security-providers/*java_bc.security /opt/java/openjdk/conf/security/
RUN chmod 776 /opt/java/openjdk/conf/security/java_bc.security

#HSM
COPY --from=builder /usr/local/luna /var/usrlocal/luna
RUN mkdir -p /var/usrlocal/luna/config/certs
ENV ChrystokiConfigurationPath=/var/usrlocal/luna/config
RUN cp /var/usrlocal/luna/jsp/64/libLunaAPI.so /opt/java/openjdk/lib/

#Tomcat changes
COPY docker/proxy/tomcat-setenv.sh ${CATALINA_HOME}/bin/setenv.sh
COPY docker/proxy/server.xml ${CATALINA_HOME}/conf/server.xml

# eIDAS config
RUN mkdir -p /etc/config && chmod 770 /etc/config
COPY docker/proxy/config /etc/config/eidas-proxy
COPY docker/proxy/profiles /etc/config/profiles
RUN chmod 776 /etc/config/eidas-proxy && mkdir /etc/config/eidas-proxy/keystore && chmod 776 /etc/config/eidas-proxy/keystore
RUN chmod 766 /etc/config/profiles/docker/SignModule_Service*.xml

# temp copy HSM testing files
COPY docker/luna/list.sh docker/luna/lunastore.txt ${CATALINA_HOME}/
RUN chmod 777 ${CATALINA_HOME}/list.sh && chmod 777 ${CATALINA_HOME}/lunastore.txt

# Scripts for copy config files
COPY docker/addEnvironmentSpesificConfigFiles.sh docker/updateKeyStoreConfig.sh ${CATALINA_HOME}/bin/
RUN chmod 755 ${CATALINA_HOME}/bin/addEnvironmentSpesificConfigFiles.sh && chmod 755 ${CATALINA_HOME}/bin/updateKeyStoreConfig.sh

# Add war files to webapps: /usr/local/tomcat/webapps
COPY --from=builder /data/eidasnode-pub/EIDAS-Node-Proxy/target/EidasNodeProxy.war ${CATALINA_HOME}/webapps/ROOT.war
RUN chmod -R 770 ${CATALINA_HOME}/webapps

RUN mkdir -p ${CATALINA_HOME}/conf/Catalina/localhost  && chmod 770 ${CATALINA_HOME}/conf/Catalina/localhost

# eIDAS audit log folder
RUN mkdir -p ${CATALINA_HOME}/eidas/logs && chmod 774 ${CATALINA_HOME}/eidas/logs

EXPOSE 8080

ENV CLASSPATH="/var/usrlocal/luna/jsp/LunaProvider.jar:$CLASSPATH"

CMD ["/bin/bash", "-c", "./bin/addEnvironmentSpesificConfigFiles.sh;./bin/updateKeyStoreConfig.sh;catalina.sh run"]
