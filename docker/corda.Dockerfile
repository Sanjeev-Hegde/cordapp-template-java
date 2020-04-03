FROM corda/corda-zulu-java1.8-4.3:latest

USER root
RUN mkdir -p /opt/corda/src && \
    mkdir -p /opt/corda/build && \
    chown -R corda /opt/corda && \
    chgrp -R corda /opt/corda && \
    chmod -R +x /opt/corda
 #  ./gradlew --stacktrace  --info --scan -Dhttp.proxyHost=webproxy.prd.lab-nxtit.priv -Dhttp.proxyPort=3128 -Dhttps.proxyHost=webproxy.prd.lab-nxtit.priv -Dhttps.proxyPort=3128  deployNodeUAT && \

COPY . /opt/corda/src
COPY ./docker/run-corda.sh /run-corda.sh

RUN  cd /opt/corda/src && \
    chmod +x gradlew && \
    ./gradlew  --stacktrace  --info --scan jar && \
#    cp /opt/corda/src/workflows/build/libs/* /opt/corda/build && \
#    cp /opt/corda/src/contracts/build/libs/* /opt/corda/build && \
    cp /opt/corda/src/build/libs/* /opt/corda/build && \
    chmod +x /run-corda.sh && \
    chown -R corda:corda /opt/corda && \
    chmod -R u+x /opt/corda && \
    chgrp -R 0 /opt/corda && \
    chmod -R g=u /opt/corda /etc/corda /etc/passwd


# Working directory for Corda
WORKDIR /opt/corda
ENV HOME=/opt/corda

USER corda
EXPOSE 10002
EXPOSE 10003
EXPOSE 10043
EXPOSE 10004

# Start it
CMD ["/run-corda.sh"]