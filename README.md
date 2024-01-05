# Jira Timesheet

Clojure webapp to display timesheet for a Jira user.

## Run locally

1. Create a `config.edn`-file with the following content

    ```clojure
    {:host "https://<my-org>.atlassian.net"
     :api-key "<api-key>"
     :email "<email>"}
    ```

2. Start repl (also starts the server on port 3030)

    ```bash
    clj -M:dev
    ```
