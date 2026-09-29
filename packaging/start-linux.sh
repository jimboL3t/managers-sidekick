#!/bin/sh
# Always store data next to the extracted application, regardless of caller cwd.
set -eu
SIDEKICK_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SIDEKICK_DIR"
SIDEKICK_JAVA=java
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    SIDEKICK_JAVA="$JAVA_HOME/bin/java"
fi
if ! "$SIDEKICK_JAVA" -version; then
    echo "Install a desktop Java runtime 21 or newer. See docs/DISTRIBUTION.md." >&2
    exit 1
fi
if [ ! -f managers-sidekick.jar ]; then
    echo "Missing managers-sidekick.jar. Extract the entire ZIP first." >&2
    exit 1
fi
if [ -n "${SIDEKICK_FONT:-}" ]; then
    exec "$SIDEKICK_JAVA" "-Dsidekick.font=$SIDEKICK_FONT" -jar managers-sidekick.jar
fi
exec "$SIDEKICK_JAVA" -jar managers-sidekick.jar
