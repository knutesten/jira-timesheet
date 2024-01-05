FROM clojure:tools-deps
COPY src /opt/app/src
COPY config.edn /opt/app/
COPY deps.edn /opt/app/
WORKDIR /opt/app
RUN clojure -P
ENTRYPOINT ["clojure", "-m", "jira-timesheet.core"]
